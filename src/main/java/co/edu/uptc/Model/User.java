package co.edu.uptc.Model;
import java.util.Objects;

import co.edu.uptc.Enum.InteresArea;
import co.edu.uptc.Enum.TypeUser;
import co.edu.uptc.Interface.Identificable;
/**Clase Usuario que representa a los usuarios de la plataforma,
 *  incluyendo estudiantes, docentes, grupos de investigación y grupos académicos.
 * 
 * @author  @FELPBEC
 * @since  2024-06-10   
 * @version 1.0
 */
public class User  {
    private String name;
    private String email;
    private String password;
    private TypeUser typeUser;
    private String program;
    private InteresArea interesArea;
    /**Constructor vacío de la clase para persistencia */
    public User() {
    }
    /**Constructor de la clase que permite crear un usuario con todos sus atributos
     * 
     * @param name
     * @param password
     * @param email
     * @param typeUser
     * @param program
     * @param interesArea
     */
    public User( String name,String email, String password, TypeUser typeUser, String program, InteresArea interesArea) {
        this.name = name;
        this.email= email;
        this.password = password;
        this.typeUser = typeUser;
        this.program = program;
        this.interesArea = interesArea;
    }
    /**Método que permite obtener el nombre del usuario
     * 
     * @return nombre del usuario
     */
    public String getName() {
        return name;
    }
    /**Método que permite establecer el nombre del usuario
     * 
     * @param name nombre del usuario
     */
    public void setName(String name) {
        this.name = name;
    }
    /**Método que permite obtener el correo institucional
     * 
     * @return correo institucional
     */
    public String getEmail() {
        return email;
    }
    /**Método que permite establecer el correo institucional
     * 
     * @param name correo institucional
     */
    public void setEmail(String email) {
        this.email = email;
    }
    /**Método que permite obtener la contraseña del usuario
     * 
     * @return contraseña del usuario
     */
    public String getPassword() {
        return password;
    }
    /**Método que permite establecer la contraseña del usuario
     * 
     * @param password contraseña del usuario
     */
    public void setPassword(String password) {
        this.password = password;
    }
    /**Método que permite obtener el tipo de usuario
     * 
     * @return tipo de usuario
     */
    public TypeUser getTypeUser() {
        return typeUser;
    }
    /**Método que permite establecer el tipo de usuario
     * 
     * @param typeUser tipo de usuario
     */
    public void setTypeUser(TypeUser typeUser) {
        this.typeUser = typeUser;
    }
    /**Método que permite obtener el programa del usuario
     * 
     * @return programa del usuario
     */
    public String getProgram() {
        return program;
    }
    /**Método que permite establecer el programa del usuario
     * 
     * @param program programa del usuario
     */
    public void setProgram(String program) {
        this.program = program;
    }
    /**Método que permite obtener el área de interés del usuario
     * 
     * @return área de interés del usuario
     */
    public InteresArea getInteresArea() {
        return interesArea;
    }
    /**Método que permite establecer el área de interés del usuario
     * 
     * @param interesArea área de interés del usuario
     */
    public void setInteresArea(InteresArea interesArea) {
        this.interesArea = interesArea;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        // 2. Si el objeto a comparar es nulo o de otra clase, no son iguales
        if (obj == null || getClass() != obj.getClass()) return false;

        // 3. Convertimos (cast) el objeto al tipo Usuario
        User otroUsuario = (User) obj;

        // 4. Comparamos si sus correos únicos son iguales
        return Objects.equals(this.email, otroUsuario.email);
    }
    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
    
    
}
