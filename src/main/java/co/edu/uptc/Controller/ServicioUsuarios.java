package co.edu.uptc.Controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import co.edu.uptc.Enum.InteresArea;
import co.edu.uptc.Enum.Program;
import co.edu.uptc.Enum.TypeUser;
import co.edu.uptc.Exceptions.DuplicateUserException;
import co.edu.uptc.Exceptions.InvalidPasswordFormat;
import co.edu.uptc.Exceptions.UsuarioNoEncontradoException;
import co.edu.uptc.Model.User;
import co.edu.uptc.Persistence.PersistenciaUsuario;
import co.edu.uptc.Util.PasswordSecurityService;
/**Clase que se encarga del CRUD de usuarios, manejo de cuentas etc
 * 
 * @author @FELPBEC
 * @since 2026-19-11
 * @version 1.0
 */
public class ServicioUsuarios {
    private Map<String, User> usuarios;
    private PersistenciaUsuario persistenciaUsuario;
    private String rutaUsuarios;
    private final PasswordSecurityService securityService = new PasswordSecurityService();
    /**Constructor de la clase ServicioUsuarios que inicializa la persistencia y carga los usuarios desde el archivo especificado.
     * 
     * @param rutaUsuarios la ruta desde la que se cargaran los usuarios
     */
    public ServicioUsuarios(String rutaUsuarios) {
        this.persistenciaUsuario = new PersistenciaUsuario();
        this.rutaUsuarios = rutaUsuarios;
        cargarUsuarios();
    }
    /**Método que permite guardar los usuarios en el archivo especificado.
     * 
     */
    public void guardarUsuarios() throws IOException {
        persistenciaUsuario.saveUsers(usuarios, rutaUsuarios);
    }
    public void cargarUsuarios() {
        try {
            this.usuarios = persistenciaUsuario.loadUsers(rutaUsuarios);
        } catch (FileNotFoundException e) {
            this.usuarios = new java.util.HashMap<>();
        } catch (Exception e) {
            System.err.println("Error al cargar los usuarios: " + e.getMessage());
            this.usuarios = new java.util.HashMap<>();
        }
    }
    /**Método que permite obtener el mapa de usuarios.
     * 
     * @return el Map de usuarios indexado por correo electrónico.
     */
    public Map<String, User> getUsuarios() {
        return usuarios;
    }
    /**Método para establecer el Map de Usuarios
     * 
     * @param usuarios map de Usuarios que se va a establecer
     */
    public void setUsuarios(Map<String, User> usuarios) {
        this.usuarios = usuarios;
    }
    /**Método que permite obtener la ruta del archivo de usuarios.
     * 
     * @return
     */
    public String getRutaUsuarios() {
        return rutaUsuarios;
    }
    /**Método que permite establecer la ruta del archivo de usuarios.
     * 
     * @param rutaUsuarios
     */
    public void setRutaUsuarios(String rutaUsuarios) {
        this.rutaUsuarios = rutaUsuarios;
    }
    /**Método que permite determinar si un usuario se encontro o no, mediante su correo Electronico
     * 
     * @param email correo electronico del usuario que se desea buscar
     * @return boleando según si el usuario se encontro o no
     */
    public boolean encontrarUsuario(String email) {
        return usuarios.containsKey(email);
    }
    /**Método que retorna todos los correos electronicos de los usuarios registrados en el sistema.
     * útil para reconstruir el grafo de relaciones al iniciar la aplicación.
     * @return
     */
    public List<String> obtenerTodosLosEmails() {
        return new ArrayList<>(usuarios.keySet());
    }

    /**
     * Filtra los usuarios por programa académico y área de interés.
     * <p>
     * Un filtro con valor {@code null} no se aplica. La búsqueda conserva
     * únicamente perfiles que cumplen todos los filtros activos.
     * </p>
     *
     * @param program programa académico requerido o {@code null} para no filtrar
     * @param area área de interés requerida o {@code null} para no filtrar
     * @return lista de usuarios que cumplen ambos filtros
     */
    public List<User> filtrarUsuarios(Program program, InteresArea area) {
        List<User> resultado = new ArrayList<>();

        for (User usuario : usuarios.values()) {
            if (usuario == null) {
                continue;
            }
            if (program != null && usuario.getProgram() != program) {
                continue;
            }
            if (area != null && usuario.getInteresArea() != area) {
                continue;
            }
            resultado.add(usuario);
        }

        resultado.sort((usuarioUno, usuarioDos) -> {
            String correoUno = usuarioUno.getEmail() != null
                    ? usuarioUno.getEmail()
                    : "";
            String correoDos = usuarioDos.getEmail() != null
                    ? usuarioDos.getEmail()
                    : "";
            return correoUno.compareToIgnoreCase(correoDos);
        });

        return resultado;
    }

    /**
     * Filtra usuarios por tipo de participante.
     * Permite separar búsquedas de personas y búsquedas de grupos académicos.
     *
     * @param tipos tipos admitidos; si está vacío no se aplican restricciones
     * @return usuarios que pertenecen a alguno de los tipos indicados
     */
    public List<User> filtrarUsuariosPorTipo(TypeUser... tipos) {
        List<User> resultado = new ArrayList<>();
        if (tipos == null || tipos.length == 0) {
            resultado.addAll(usuarios.values());
        } else {
            for (User usuario : usuarios.values()) {
                if (usuario == null) {
                    continue;
                }
                for (TypeUser tipo : tipos) {
                    if (usuario.getTypeUser() == tipo) {
                        resultado.add(usuario);
                        break;
                    }
                }
            }
        }

        resultado.sort((usuarioUno, usuarioDos) -> {
            String correoUno = usuarioUno.getEmail() != null
                    ? usuarioUno.getEmail()
                    : "";
            String correoDos = usuarioDos.getEmail() != null
                    ? usuarioDos.getEmail()
                    : "";
            return correoUno.compareToIgnoreCase(correoDos);
        });
        return resultado;
    }
    //CREAR 
    /**Método para crear una nueva cuenta de usuario
     * Valida si el usuario ya existe a traves del Email
     * Valida si la contraseña cumple con el formato requerido
     * Encripta la contraseña antes de guardarla
     * @param usuario usuario que se desea crear
     * @throws DuplicateUserException excepción lanzada si el usuario ya existe
     * @throws InvalidPasswordFormat excepción lanzada si la contraseña no cumple con el formato requerido
     */
    public void crearUsuario(User usuario)throws DuplicateUserException, InvalidPasswordFormat, IOException {
        if(encontrarUsuario(usuario.getEmail()))throw new DuplicateUserException("El usuario con email " + usuario.getEmail() + " ya existe.");
        if (!securityService.isValidFormat(usuario.getPassword())) throw new InvalidPasswordFormat("La contraseña no cumple con el formato requerido.");
        usuario.setPassword(securityService.encrypt(usuario.getPassword()));
        usuarios.put(usuario.getEmail(), usuario);
        guardarUsuarios();
    }
    //LEER
    /**Método que envía el Usuario según el correo electrónico,
     *  si no se encuentra lanza una excepción UsuarioNoEncontradoException
     * 
     * @param email correo electrónico del usuario que se desea obtener
     * @return el usuario correspondiente al correo electrónico proporcionado
     * @throws UsuarioNoEncontradoException excepción lanzada si el usuario no se encuentra
     */
     public User obtenerUsuario(String email) throws UsuarioNoEncontradoException {
        if(!encontrarUsuario(email)){
            throw new UsuarioNoEncontradoException("El usuario con email " + email + " no existe.");
        }else{
            return usuarios.get(email);
        } 
        
     }
    //ACTUALIZAR
    /**Método que permite actualizar la información de un usuario existente.
     * 
     * @param email correo electrónico del usuario que se desea actualizar
     * @param usuarioActualizado objeto User con la información actualizada
     * @throws UsuarioNoEncontradoException excepción lanzada si el usuario no se encuentra
     */
    public void actualizarUsuario(String email, User usuarioActualizado) throws UsuarioNoEncontradoException, InvalidPasswordFormat, IOException {
    if (!encontrarUsuario(email)) {
        throw new UsuarioNoEncontradoException("El usuario con email " + email + " no existe.");
    }

    User usuarioActual = usuarios.get(email);
    String nuevaPassword = usuarioActualizado.getPassword();

    // Se valida que el usuario cambio de contraseña para evitar reencriptar la misma contraseña y que se cumpla el formato de la nueva contraseña
    if (nuevaPassword == null || nuevaPassword.isBlank() || nuevaPassword.equals(usuarioActual.getPassword())) {
        usuarioActualizado.setPassword(usuarioActual.getPassword());
    } else {
        if (!securityService.isValidFormat(nuevaPassword)) {
            throw new InvalidPasswordFormat("La nueva contraseña no cumple con el formato requerido.");
        }
        usuarioActualizado.setPassword(securityService.encrypt(nuevaPassword));
    }

    usuarioActual.setName(usuarioActualizado.getName());
    usuarioActual.setEmail(email);
    usuarioActual.setTypeUser(usuarioActualizado.getTypeUser());
    usuarioActual.setProgram(usuarioActualizado.getProgram());
    usuarioActual.setInteresArea(usuarioActualizado.getInteresArea());
    usuarioActual.setPassword(usuarioActualizado.getPassword());
    usuarios.put(email, usuarioActual);
    guardarUsuarios();
    }
    //ELIMINAR
    /**Método que permite eliminar un usuario existente del sistema.
     * y envía el correo electronico del usuario para poder eliminar sus aristas en el grafo de relaciones
     * @param email correo electrónico del usuario que se desea eliminar
     * @return  el correo electrónico del usuario eliminado
     * @throws UsuarioNoEncontradoException excepción lanzada si el usuario no se encuentra
     */
    public String eliminarUsuario(String email) throws UsuarioNoEncontradoException, IOException {
        if(!encontrarUsuario(email)){
            throw new UsuarioNoEncontradoException("El usuario con email " + email + " no existe.");
        }else{
            usuarios.remove(email);
            guardarUsuarios();
            return email;
        }
    }
    //INICIAR SESIÓN
    /**Método que permite iniciar sesión en el sistema.
     * Valida si el usuario existe y si la contraseña es correcta
     * @param email correo electrónico del usuario que desea iniciar sesión
     * @param password contraseña proporcionada para iniciar sesión
     * @return true si el inicio de sesión es exitoso, false de lo contrario
     */ 
    public boolean iniciarSesion(String email, String password) throws UsuarioNoEncontradoException {
        if(!encontrarUsuario(email)) throw new UsuarioNoEncontradoException("El usuario con email " + email + " no existe.");
        User usuario = usuarios.get(email);
        return securityService.verify(password, usuario.getPassword()); 
    }
    
    
}
