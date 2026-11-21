package co.edu.uptc.Persistence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import co.edu.uptc.Model.User;
import co.edu.uptc.Util.JacksonUtilities;

/**Clase PersistenciaUsuario: Se encarga de guardar y cargar los perfiles de usuario
 * 
 * @author: @FELPBEC
 * @since: 2026-19-11
 * @version: 1.0
 */
public class PersistenciaUsuario {
    private final JacksonUtilities jacksonUtilities = new JacksonUtilities();
    /**Método para guardar los usuarios en un archivo JSON o XML.
     * 
     * @param usuarios Map de usuarios que se va a guardar, indexado por correo electrónico.
     * @param rutaUsuarios la ruta donde se guardaran, determina si es JSON o XML por la extensión del archivo.
     * @throws IOException excepción en caso de que no se pueda escribir el archivo.
     */
    public void saveUsers(Map<String, User> usuarios, String rutaUsuarios) throws IOException {
        // 1. Extraemos la lista plana de usuarios desde el mapa
        List<User> listaUsuarios = new ArrayList<>(usuarios.values());
        // 2. Guardamos ambos archivos con Jackson
        jacksonUtilities.writeFiles(listaUsuarios, rutaUsuarios);
    }
    /**Método para cargar los usuarios desde un archivo JSON o XML.
     * 
     * @param rutaUsuarios la ruta del archivo desde el cual se cargarán los usuarios.
     * @return el mapa de usuarios
     * @throws IOException en caso de que no se encuentre el archivo o haya un error de lectura.
     */
    public Map<String, User> loadUsers(String rutaUsuarios) throws IOException {
        // 1. Leemos los usuarios desde el archivo (JSON o XML)
        List<User> listaUsuarios = jacksonUtilities.ReadFilesList(rutaUsuarios, User.class);
        // 2. Creamos un mapa de usuarios indexado por correo
        Map<String, User> usuariosMap = new HashMap<>();
        for (User user : listaUsuarios) {
            String correo = user.getEmail();
            usuariosMap.put(correo, user);
        }
        return usuariosMap;
    }
}
