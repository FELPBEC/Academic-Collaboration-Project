package co.edu.uptc.Interface;
/**Interfaz generica que representa un objeto que puede ser identificado por un id unico
 * Lo que nos permitirá guardar en persistencia solo los identificadores únicos de los objetos, en lugar de guardar toda la información del objeto.
 * 
 * @author @FELPBEC
 * @since 2024-06-10    
 * @version 1.0
 */
public interface Identificable {
    int getId();
    void setId(int id);
}
