package co.edu.uptc.Controller;


import co.edu.uptc.Enum.RelationType;
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

import java.util.ArrayList;
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

    // Estado de la sesión actual, guarda el Usuario registrado actualmente
    private User usuarioActual;
    // Definimos al inicio si la app va a guardar en Json o XML
    public ServicioGeneral(boolean usarXml) {
        this(usarXml ? "usuarios.xml" : "usuarios.json",usarXml ? "aristas.xml" : "aristas.json");
    }
    public ServicioGeneral(String rutaUsuarios, String rutaAristas) {
        this.rutaUsuarios = rutaUsuarios;
        this.rutaAristas = rutaAristas;
        this.persistenciaGrafo = new PersistenciaGrafo();

        // 1. Cargar usuarios desde persistencia
        this.servicioUsuarios = new ServicioUsuarios(rutaUsuarios);

        // 2. Reconstruir el grafo usando los correos cargados y el archivo de aristas
        try {
            this.grafo = persistenciaGrafo.loadGrafo(servicioUsuarios.obtenerTodosLosEmails(), rutaAristas);
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
    }

    // ==================== SINCRONIZACIÓN USUARIOS + GRAFO ====================
    /**Se env
     * 
     * @param nuevoUsuario
     * @throws DuplicateUserException
     * @throws InvalidPasswordFormat
     */
    public void registrarUsuario(User nuevoUsuario) throws DuplicateUserException, InvalidPasswordFormat {
        servicioUsuarios.crearUsuario(nuevoUsuario); 
        grafo.agregarNodo(new Nodo<>(nuevoUsuario.getEmail()));
        guardarGrafo();
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

    public void eliminarCuentaActual() throws UsuarioNoEncontradoException {
        if (usuarioActual == null) return;
        String emailEliminado = servicioUsuarios.eliminarUsuario(usuarioActual.getEmail());
        grafo.eliminarNodo(new Nodo<>(emailEliminado)); // Asegúrate de que tu Grafo elimine también sus aristas
        guardarGrafo();
        cerrarSesion();
    }

    // ==================== SINCRONIZACIÓN SOLICITUDES + GRAFO ====================

    public void enviarSolicitud(String emailDestinatario, RelationType tipo, String comentario)
            throws UsuarioNoEncontradoException {
        if (usuarioActual == null) return;

        User destinatario = servicioUsuarios.obtenerUsuario(emailDestinatario);
        Solicitud nueva = servicioSolicitudes.enviarSolicitud(usuarioActual.getEmail(), emailDestinatario, tipo, comentario);
        destinatario.guardarSolicitud(nueva);
        servicioUsuarios.guardarUsuarios(); // Persiste la lista actualizada del destinatario
    }

    public void aceptarSolicitud(Solicitud solicitud) {
        Solicitud aceptada = servicioSolicitudes.aceptarSolicitud(solicitud);
        servicioUsuarios.guardarUsuarios(); // Guarda que la solicitud ya fue removida

        // Crea la arista en el grafo y persiste el grafo
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

    public void rechazarSolicitud(Solicitud solicitud) {
        servicioSolicitudes.rechazarSolicitud(solicitud);
        servicioUsuarios.guardarUsuarios();
    }

    // ==================== MÉTODOS AUXILIARES Y GETTERS ====================

    public void guardarGrafo() {
        try {
            persistenciaGrafo.saveGrafo(grafo, rutaAristas);
        } catch (Exception e) {
            System.err.println("Error al guardar el grafo: " + e.getMessage());
        }
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

    public ServicioSolicitudes getServicioSolicitudes() {
        return servicioSolicitudes;
    }
}

