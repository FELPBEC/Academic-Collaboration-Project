package co.edu.uptc.Controller;

import java.util.Map;

import co.edu.uptc.Enum.InteresArea;
import co.edu.uptc.Enum.TypeUser;
import co.edu.uptc.Exceptions.DuplicateUserException;
import co.edu.uptc.Model.User;
import co.edu.uptc.Persistence.PersistenciaUsuario;
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
    
    /**Constructor de la clase ServicioUsuarios que inicializa la persistencia y carga los usuarios desde el archivo especificado.
     * 
     * @param rutaUsuarios la ruta desde la que se cargaran los usuarios
     */
    public ServicioUsuarios(String rutaUsuarios) {
        this.persistenciaUsuario = new PersistenciaUsuario();
        this.rutaUsuarios = rutaUsuarios;
        try {
            this.usuarios = persistenciaUsuario.loadUsers(rutaUsuarios);
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

    public boolean encontrarUsuario(String email) {
        return usuarios.containsKey(email);
    }

    
    
    


}
