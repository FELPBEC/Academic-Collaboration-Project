package co.edu.uptc.DTO;

import co.edu.uptc.Model.Relation;
/**Clase DTO que representa una arista entre dos nodos, util para persistencia
 * 
 * @author @FELPBEC
 * @since 2026-06-10
 * @version 1.0
 */
public class AristaDTO {
    private String origenId;
    private String destinoId;
    private Relation datosRelacion;
    /**Constructor vacío de la clase para persistencia
     * 
     */
    public AristaDTO() {
    }
    /**Constructor de la clase que permite crear una arista con los ids de los nodos y la relación entre ellos
     * 
     * @param origenId
     * @param destinoId
     * @param datosRelacion
     */
    public AristaDTO(String origenId, String destinoId,  Relation datosRelacion) {
        this.origenId = origenId;
        this.destinoId = destinoId;
        this.datosRelacion = datosRelacion;
    }

    
    /**Método que permite obtener el id del nodo origen de la arista
     * 
     * @return
     */
    public String getOrigenId() {
        return origenId;
    }
    /**Método que permite establecer el id del nodo origen de la arista
     * 
     * @param origenId
     */
    public void setOrigenId(String origenId) {
        this.origenId = origenId;
    }
    /**Método que permite obtener el id del nodo destino de la arista
     * 
     * @return
     */
    public String getDestinoId() {
        return destinoId;
    }
    /**Método que permite establecer el id del nodo destino de la arista
     * 
     * @param destinoId
     */
    public void setDestinoId(String destinoId) {
        this.destinoId = destinoId;
    }
    /**Método que permite obtener la relación entre los nodos de la arista
     * 
     * @return
     */
    public Relation getDatosRelacion() {
        return datosRelacion;
    }
    /**Método que permite establecer la relación entre los nodos de la arista
     * 
     * @param datosRelacion
     */
    public void setDatosRelacion(Relation datosRelacion) {
        this.datosRelacion = datosRelacion;
    }
    
    // Getters y Setters
}
