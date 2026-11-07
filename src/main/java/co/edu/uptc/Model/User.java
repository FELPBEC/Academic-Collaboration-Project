package co.edu.uptc.Model;
import co.edu.uptc.Enum.InteresArea;
import co.edu.uptc.Enum.TypeUser;
/**Clase Usuario que representa a los usuarios de la plataforma,
 *  incluyendo estudiantes, docentes, grupos de investigación y grupos académicos.
 * 
 * @author  @FELPBEC
 * @since  2024-06-10   
 * @version 1.0
 */
public class User {
    private String id;
    private String name;
    private String password;
    private TypeUser typeUser;
    private String program;
    private InteresArea interesArea;
    /**Constructor vacío de la clase para persistencia */
    public User() {
    }
    /**Constructor de la clase que permite crear un usuario con todos sus atributos
     * 
     * @param id
     * @param name
     * @param password
     * @param typeUser
     * @param program
     * @param interesArea
     */
    public User(String id, String name, String password, TypeUser typeUser, String program, InteresArea interesArea) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.typeUser = typeUser;
        this.program = program;
        this.interesArea = interesArea;
    }
    /**Método que permite obtener el id del usuario
     * 
     * @return id del usuario
     */
    public String getId() {
        return id;
    }
    /**Método que permite establecer el id del usuario
     * 
     * @param id id del usuario
     */
    public void setId(String id) {
        this.id = id;
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
        return super.equals(obj);
    }
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    
}
