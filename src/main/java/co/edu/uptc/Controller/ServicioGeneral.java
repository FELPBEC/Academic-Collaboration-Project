package co.edu.uptc.Controller;

import java.nio.file.Path;
import co.edu.uptc.Enum.InteresArea;
import co.edu.uptc.Enum.Program;
import co.edu.uptc.Enum.RelationType;
import co.edu.uptc.Enum.TipoOperacionSolicitud;
import co.edu.uptc.Enum.TypeUser;
import co.edu.uptc.Exceptions.DuplicateUserException;
import co.edu.uptc.Exceptions.InvalidPasswordFormat;
import co.edu.uptc.Exceptions.UsuarioNoEncontradoException;
import co.edu.uptc.Model.Arista;
import co.edu.uptc.Model.Grafo;
import co.edu.uptc.Model.Nodo;
import co.edu.uptc.Model.Relation;
import co.edu.uptc.Model.Solicitud;
import co.edu.uptc.Model.User;
import co.edu.uptc.Persistence.PersistenciaGrafo;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**Clase encargada de hacer de puente entre los distintos servicios y las vistas
 * funcionando de observador de las vistas y enviando de regreso la información que le proporcionan los distintos servicios
 * @author @FELPBEC
 * @since 2026-21-11
 * @version 1.0
 */
public class ServicioGeneral {

    // Persistencia y rutas
    private final PersistenciaGrafo persistenciaGrafo;
    private final String rutaUsuarios;
    private final String rutaAristas;

    // Estructura principal compartida
    private Grafo<String> grafo;

    // Servicios especializados
    private final ServicioUsuarios servicioUsuarios;
    private final ServicioColaboracion servicioColaboracion;
    private ServicioSolicitudes servicioSolicitudes;
    private final ServicioReportesPDF servicioReportesPDF;

    // Estado de la sesión actual, guarda el Usuario registrado actualmente
    private User usuarioActual;
    // Definimos al inicio si la app va a guardar en Json o XML
    public ServicioGeneral(boolean usarXml) {
        this(
        usarXml ? "../data/usuarios.xml" : "../data/usuarios.json",
        usarXml ? "../data/aristas.xml" : "../data/aristas.json"
    );
    }
    public ServicioGeneral(String rutaUsuarios, String rutaAristas) {
        this.rutaUsuarios = rutaUsuarios;
        this.rutaAristas = rutaAristas;
        File carpeta = new File(rutaUsuarios).getParentFile();
         if (carpeta != null && !carpeta.exists()) {
        carpeta.mkdirs();
        }
        this.persistenciaGrafo = new PersistenciaGrafo();

        // 1. Cargar usuarios desde persistencia
        this.servicioUsuarios = new ServicioUsuarios(rutaUsuarios);

        // 2. Reconstruir el grafo usando los correos cargados y el archivo de aristas
        try {
            this.grafo = persistenciaGrafo.loadGrafo(servicioUsuarios.obtenerTodosLosEmails(), rutaAristas);
        } catch (java.io.FileNotFoundException e) {
            this.grafo = new Grafo<>();
            for (String email : servicioUsuarios.obtenerTodosLosEmails()) {
                this.grafo.agregarNodo(new Nodo<>(email));
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar el grafo, iniciando uno nuevo: " + e.getMessage());
            this.grafo = new Grafo<>();
            for (String email : servicioUsuarios.obtenerTodosLosEmails()) {
                this.grafo.agregarNodo(new Nodo<>(email));
            }
        }

        // 3. Conectar ServicioColaboracion a las mismas instancias en memoria
        this.servicioColaboracion = new ServicioColaboracion(this.grafo, servicioUsuarios.getUsuarios());
        this.servicioSolicitudes = new ServicioSolicitudes(new ArrayList<>());
        this.servicioReportesPDF = new ServicioReportesPDF(
                servicioUsuarios.getUsuarios(),
                this.grafo);
    }

    // ==================== SINCRONIZACIÓN USUARIOS + GRAFO ====================
    /**Se env
     * 
     * @param nuevoUsuario
     * @throws DuplicateUserException
     * @throws InvalidPasswordFormat
     */
    public void registrarUsuario(User nuevoUsuario) throws DuplicateUserException, InvalidPasswordFormat, IOException {
        servicioUsuarios.crearUsuario(nuevoUsuario); 
        grafo.agregarNodo(new Nodo<>(nuevoUsuario.getEmail()));
        guardarGrafo();
    }
    public void actualizarUsuarioActual(User datosActualizados)
            throws UsuarioNoEncontradoException, InvalidPasswordFormat, IOException {
        if (usuarioActual == null) {
            throw new IllegalStateException("Debe iniciar sesión para actualizar el perfil");
        }
        servicioUsuarios.actualizarUsuario(usuarioActual.getEmail(), datosActualizados);
        this.usuarioActual = servicioUsuarios.obtenerUsuario(usuarioActual.getEmail());
    }
    public boolean iniciarSesion(String email, String password) throws UsuarioNoEncontradoException {
        boolean valido = servicioUsuarios.iniciarSesion(email, password);
        if (valido) {
            this.usuarioActual = servicioUsuarios.obtenerUsuario(email);
            this.servicioSolicitudes = new ServicioSolicitudes(usuarioActual.getSolicitudesRecibidas());
        }
        return valido;
    }

    public void cerrarSesion() {
        this.usuarioActual = null;
        this.servicioSolicitudes = new ServicioSolicitudes(new ArrayList<>());
    }

    public void eliminarCuentaActual() throws UsuarioNoEncontradoException, IOException {
        if (usuarioActual == null) return;
        String emailEliminado = usuarioActual.getEmail();
        eliminarSolicitudesRelacionadas(emailEliminado);
        servicioUsuarios.eliminarUsuario(emailEliminado);
        grafo.eliminarNodo(new Nodo<>(emailEliminado));
        guardarGrafo();
        cerrarSesion();
    }

    // ==================== SINCRONIZACIÓN SOLICITUDES + GRAFO ====================

    /**
     * Envía una solicitud de creación de relación al participante indicado.
     *
     * @param emailDestinatario participante que recibirá la solicitud
     * @param tipo tipo de relación solicitada
     * @param comentario comentario adicional
     * @throws UsuarioNoEncontradoException si el destinatario no existe
     * @throws DuplicateUserException si se intenta enviar una solicitud al usuario actual
     * @throws IllegalStateException si no existe una sesión iniciada
     * @throws IllegalArgumentException si el tipo de relación no está definido
     */
    public void enviarSolicitud(String emailDestinatario, RelationType tipo, String comentario)
            throws UsuarioNoEncontradoException, DuplicateUserException, IOException {
        if (usuarioActual == null) {
            throw new IllegalStateException("Debe iniciar sesión para enviar una solicitud");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de relación es obligatorio");
        }

        User destinatario = servicioUsuarios.obtenerUsuario(emailDestinatario);
        Nodo<String> origen = new Nodo<>(usuarioActual.getEmail());
        Nodo<String> destino = new Nodo<>(destinatario.getEmail());
        if (grafo.encontrarArista(origen, destino) != null) {
            throw new IllegalArgumentException("Ya existe una relación entre ambos participantes");
        }
        if (solicitudPendienteExiste(usuarioActual.getEmail(),
                destinatario.getEmail(),
                TipoOperacionSolicitud.CREAR_RELACION,
                null)) {
            throw new DuplicateUserException("Ya existe una solicitud de relación pendiente");
        }
        Solicitud nueva = servicioSolicitudes.enviarSolicitud(usuarioActual.getEmail(), emailDestinatario, tipo, comentario);
        destinatario.guardarSolicitud(nueva);
        servicioUsuarios.guardarUsuarios(); // Persiste la lista actualizada del destinatario
    }

    /**
     * Envía una solicitud para modificar el tipo de una relación existente.
     * La modificación no se aplica hasta que el otro participante acepta.
     *
     * @param emailDestinatario participante que debe aprobar la modificación
     * @param tipoNuevo nuevo tipo de relación solicitado
     * @param comentario comentario adicional
     * @throws UsuarioNoEncontradoException si alguno de los participantes no existe
     * @throws DuplicateUserException si se intenta modificar consigo mismo
     * @throws IllegalArgumentException si no existe la relación o no cambia el tipo
     */
    public void solicitarModificacionRelacion(
            String emailDestinatario,
            RelationType tipoNuevo,
            String comentario)
            throws UsuarioNoEncontradoException, DuplicateUserException, IOException {
        if (usuarioActual == null) {
            throw new IllegalStateException("Debe iniciar sesión para solicitar una modificación");
        }
        if (tipoNuevo == null) {
            throw new IllegalArgumentException("El nuevo tipo de relación es obligatorio");
        }

        User destinatario = servicioUsuarios.obtenerUsuario(emailDestinatario);
        Nodo<String> origen = new Nodo<>(usuarioActual.getEmail());
        Nodo<String> destino = new Nodo<>(destinatario.getEmail());
        Arista<String> arista = grafo.encontrarArista(origen, destino);
        if (arista == null || arista.getDatosRelacion() == null) {
            throw new IllegalArgumentException("No existe una relación entre ambos participantes");
        }

        RelationType tipoAnterior = arista.getDatosRelacion().getRelationType();
        if (solicitudPendienteExiste(usuarioActual.getEmail(),
                destinatario.getEmail(),
                TipoOperacionSolicitud.MODIFICAR_RELACION,
                tipoNuevo)) {
            throw new DuplicateUserException("Ya existe una solicitud de modificación pendiente");
        }
        Solicitud solicitud = servicioSolicitudes.solicitarModificacionRelacion(
                usuarioActual.getEmail(),
                destinatario.getEmail(),
                tipoAnterior,
                tipoNuevo,
                comentario);
        destinatario.guardarSolicitud(solicitud);
        servicioUsuarios.guardarUsuarios();
    }

    /**
     * Elimina directamente una relación existente entre el usuario actual y otro participante.
     * No requiere solicitud ni aprobación porque la relación es no dirigida.
     *
     * @param emailOtroParticipante participante con quien se eliminará la relación
     * @return {@code true} si la relación existía y fue eliminada
     * @throws UsuarioNoEncontradoException si alguno de los participantes no existe
     * @throws IllegalStateException si no existe una sesión iniciada
     */
    public boolean eliminarRelacion(String emailOtroParticipante)
            throws UsuarioNoEncontradoException, IOException {
        if (usuarioActual == null) {
            throw new IllegalStateException("Debe iniciar sesión para eliminar una relación");
        }
        if (emailOtroParticipante == null
                || emailOtroParticipante.isBlank()
                || emailOtroParticipante.equals(usuarioActual.getEmail())) {
            throw new IllegalArgumentException("Debe indicar otro participante válido");
        }

        servicioUsuarios.obtenerUsuario(emailOtroParticipante);
        boolean eliminada = grafo.eliminarArista(
                new Nodo<>(usuarioActual.getEmail()),
                new Nodo<>(emailOtroParticipante));
        if (eliminada) {
            guardarGrafo();
        }
        return eliminada;
    }

    /**
     * Acepta una solicitud pendiente y aplica su operación al grafo.
     * Una solicitud de creación agrega una arista; una de modificación reemplaza
     * el tipo de una arista existente.
     *
     * @param solicitud solicitud aceptada por el usuario actual
     * @throws IllegalStateException si la relación a modificar dejó de existir
     */
    public void aceptarSolicitud(Solicitud solicitud) throws IOException {
        Solicitud aceptada = servicioSolicitudes.aceptarSolicitud(solicitud);
        servicioUsuarios.guardarUsuarios(); // Guarda que la solicitud ya fue removida

        TipoOperacionSolicitud operacion = aceptada.getTipoOperacion();
        if (operacion == null) {
            operacion = TipoOperacionSolicitud.CREAR_RELACION;
        }

        if (operacion == TipoOperacionSolicitud.MODIFICAR_RELACION) {
            Nodo<String> origen = new Nodo<>(aceptada.getEmailSolicitante());
            Nodo<String> destino = new Nodo<>(aceptada.getEmailDestinatario());
            Arista<String> arista = grafo.encontrarArista(origen, destino);
            if (arista == null) {
                throw new IllegalStateException("La relación a modificar ya no existe");
            }
            Relation relacion = arista.getDatosRelacion();
            if (relacion == null) {
                relacion = new Relation();
                arista.setDatosRelacion(relacion);
            }
            RelationType tipoNuevo = aceptada.getTipoRelacionNueva();
            if (tipoNuevo == null) {
                tipoNuevo = aceptada.getTipoRelacion();
            }
            relacion.setRelationType(tipoNuevo);
            guardarGrafo();
        } else {
            Relation relacion = new Relation();
            relacion.setRelationType(aceptada.getTipoRelacion());
            Arista<String> nuevaArista = new Arista<>(
                    new Nodo<>(aceptada.getEmailSolicitante()),
                    new Nodo<>(aceptada.getEmailDestinatario()),
                    relacion
            );

            if (grafo.agregarArista(nuevaArista)) {
                guardarGrafo();
            }
        }
    }

    /**
     * Rechaza una solicitud pendiente sin modificar el grafo.
     *
     * @param solicitud solicitud que será rechazada
     */
    public void rechazarSolicitud(Solicitud solicitud) throws IOException {
        servicioSolicitudes.rechazarSolicitud(solicitud);
        servicioUsuarios.guardarUsuarios();
    }

    /**
     * Comprueba si ya existe una solicitud pendiente entre dos participantes.
     *
     * @param emailUno primer participante
     * @param emailDos segundo participante
     * @param operacion operación requerida
     * @param tipoNuevo tipo nuevo esperado en modificaciones; puede ser {@code null}
     * @return {@code true} si existe una solicitud equivalente o inversa pendiente
     */
    private boolean solicitudPendienteExiste(
            String emailUno,
            String emailDos,
            TipoOperacionSolicitud operacion,
            RelationType tipoNuevo) {
        for (User usuario : servicioUsuarios.getUsuarios().values()) {
            for (Solicitud solicitud : usuario.getSolicitudesRecibidas()) {
                TipoOperacionSolicitud operacionSolicitud = solicitud.getTipoOperacion();
                if (operacionSolicitud == null) {
                    operacionSolicitud = TipoOperacionSolicitud.CREAR_RELACION;
                }
                if (operacionSolicitud != operacion) {
                    continue;
                }

                boolean directa = emailUno.equals(solicitud.getEmailSolicitante())
                        && emailDos.equals(solicitud.getEmailDestinatario());
                boolean inversa = emailDos.equals(solicitud.getEmailSolicitante())
                        && emailUno.equals(solicitud.getEmailDestinatario());
                if (!directa && !inversa) {
                    continue;
                }

                if (operacion == TipoOperacionSolicitud.MODIFICAR_RELACION) {
                    RelationType nuevoSolicitado = solicitud.getTipoRelacionNueva();
                    if (nuevoSolicitado == null) {
                        nuevoSolicitado = solicitud.getTipoRelacion();
                    }
                    if (tipoNuevo == null || tipoNuevo == nuevoSolicitado) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Elimina solicitudes enviadas o recibidas por un participante que será eliminado.
     *
     * @param emailEliminado correo del participante eliminado
     * @throws IOException si no se puede persistir la limpieza
     */
    private void eliminarSolicitudesRelacionadas(String emailEliminado) throws IOException {
        for (User usuario : servicioUsuarios.getUsuarios().values()) {
            List<Solicitud> solicitudes = usuario.getSolicitudesRecibidas();
            solicitudes.removeIf(solicitud ->
                    emailEliminado.equals(solicitud.getEmailSolicitante())
                            || emailEliminado.equals(solicitud.getEmailDestinatario()));
        }
        servicioUsuarios.guardarUsuarios();
    }
    // ===================== CONSULTAS Y FILTRADO ========================
    /**
     * Ejecuta los tres filtros de exploración en cascada:
     * 1. Si {@code soloRelevantes} es {@code true}, parte de los participantes con relaciones relevantes
     *    ordenados por puntuación; si es {@code false}, parte de todos los usuarios del sistema.
     * 2. Filtra por programa académico y área de interés.
     * 3. Filtra por tipo(s) de participante y excluye al usuario actual.
     *
     * @param soloRelevantes indica si se activa el filtro base de {@link ServicioColaboracion}
     * @param program        programa académico o {@code null} para no filtrar
     * @param area           área de interés o {@code null} para no filtrar
     * @param tipos          tipos de usuario admitidos (vacío o {@code null} para incluir todos)
     * @return lista final de usuarios filtrados
     */
    public List<User> explorarUsuarios(boolean soloRelevantes, Program program, InteresArea area, TypeUser... tipos) {
        List<User> resultado = null;

        // 1. Base: usuarios relevantes (si está activo) o null para usar el Map completo
        if (soloRelevantes && usuarioActual != null) {
            resultado = servicioColaboracion.filtrarUsuariosPorRelacionesRelevantes(usuarioActual.getEmail());
        }

        // 2. Filtro por programa y área de interés
        resultado = servicioUsuarios.filtrarUsuarios(resultado, program, area);

        // 3. Filtro por tipo(s) de participante
        resultado = servicioUsuarios.filtrarUsuariosPorTipo(resultado, tipos);

        // 4. Excluye al usuario actual cuando el filtro base fue el mapa general
        if (usuarioActual != null && usuarioActual.getEmail() != null) {
            String correoActual = usuarioActual.getEmail();
            resultado.removeIf(u -> u != null && correoActual.equalsIgnoreCase(u.getEmail()));
        }

        return resultado;
    }
    /**Método que busca la cadena de colaboración entre un Usuario y otro
     * 
     * @param correoDestino el usuario al que se busca llegar
     * @param program programa academico filtro
     * @param area area de interes filtro
     * @param tipoRelacion tipo de relacion filtro
     * @return
     */
    public List<User> buscarCadenaColaboracion(String correoDestino, Program program, InteresArea area, RelationType tipoRelacion) {
        if (usuarioActual == null || usuarioActual.getEmail() == null) {
            return new ArrayList<>();
        }
        return servicioColaboracion.buscarCadenaColaboracion(
                usuarioActual.getEmail(), correoDestino, program, area, tipoRelacion);
    }

    /**Método para obtener filtradas las conexiones directas de un usuario 
     * 
     * @param program el programa filtro
     * @param area el area filtro
     * @param tipoRelacion el tipo de relación filtro
     * @return
     */
    public List<Arista<String>> filtrarConexionesDirectas(Program program, InteresArea area, RelationType tipoRelacion) {
        if (usuarioActual == null || usuarioActual.getEmail() == null) {
            return new ArrayList<>();
        }
        return servicioColaboracion.filtrarConexionesDirectas(
                usuarioActual.getEmail(), program, area, tipoRelacion);
    }
    // ==================== MÉTODOS DE REPORTES EN PDF =====================
    public Path generarReporteBusquedaUsuarios(
            String rutaONombreArchivo,
            List<User> participantes,
            Program program,
            InteresArea area,
            int puntuacionMinima) throws Exception {
        if (usuarioActual == null) {
            throw new IllegalStateException("Debe iniciar sesión para generar reportes");
        }
        return servicioReportesPDF.generarReporteBusquedaUsuarios(
                rutaONombreArchivo, usuarioActual, participantes, program, area, puntuacionMinima);
    }

    public Path generarReporteConexionesFiltradas(
            String rutaONombreArchivo,
            List<Arista<String>> conexiones,
            Program program,
            InteresArea area,
            RelationType tipoRelacion) throws Exception {
        if (usuarioActual == null) {
            throw new IllegalStateException("Debe iniciar sesión para generar reportes");
        }
        return servicioReportesPDF.generarReporteConexionesFiltradas(
                rutaONombreArchivo, usuarioActual, conexiones, program, area, tipoRelacion);
    }

    public Path generarReporteCadenaColaboracion(
            String rutaONombreArchivo,
            User destino,
            List<User> cadena,
            Program program,
            InteresArea area,
            RelationType tipoRelacion) throws Exception {
        if (usuarioActual == null) {
            throw new IllegalStateException("Debe iniciar sesión para generar reportes");
        }
        return servicioReportesPDF.generarReporteCadenaColaboracion(
                rutaONombreArchivo, usuarioActual, destino, cadena, program, area, tipoRelacion);
    }
    // ==================== MÉTODOS AUXILIARES Y GETTERS ====================

    public void guardarGrafo() throws IOException {
        persistenciaGrafo.saveGrafo(grafo, rutaAristas);
    }

    public User getUsuarioActual() {
        return usuarioActual;
    }

    public ServicioUsuarios getServicioUsuarios() {
        return servicioUsuarios;
    }

    public ServicioColaboracion getServicioColaboracion() {
        return servicioColaboracion;
    }

    /**
     * Obtiene el grafo compartido administrado por el servicio general.
     *
     * @return grafo de participantes y relaciones
     */
    public Grafo<String> getGrafo() {
        return grafo;
    }

    public Map<String, User> getUsuariosMap() {
        return servicioUsuarios.getUsuarios();
    }

    public ServicioSolicitudes getServicioSolicitudes() {
        return servicioSolicitudes;
    }
}

