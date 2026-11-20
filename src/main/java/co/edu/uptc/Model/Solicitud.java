package co.edu.uptc.Model;

import co.edu.uptc.Enum.RelationType;
/**Clase Solicitud que representa una solicitud de un Usuario de establecer relación con otro Usuario
 * 
 * @FELPBEC
 * @since 2024-06-10
 * @version 1.0
 * 
 */
public class Solicitud {
    private String emailSolicitante;
    private String emailDestinatario;
    private RelationType tipoRelacion;
    private String comentario;
    /**Constructor vacío de la clase para persistencia */
    public Solicitud() {
    }
    /**Constructor de la clase que permite crear una solicitud con todos sus atributos
     * 
     * @param emailSolicitante
     * @param emailDestinatario
     * @param tipoRelacion
     * @param comentario
     */
    public Solicitud(String emailSolicitante, String emailDestinatario, RelationType tipoRelacion, String comentario) {
        this.emailSolicitante = emailSolicitante;
        this.emailDestinatario = emailDestinatario;
        this.tipoRelacion = tipoRelacion;
        this.comentario = comentario;
    }
    /**Método que permite obtener el correo del solicitante
     * 
     * @return correo del solicitante
     */
    public String getEmailSolicitante() {
        return emailSolicitante;
    }
    /**Método que permite establecer el correo del solicitante
     * 
     * @param emailSolicitante correo del solicitante
     */
    public void setEmailSolicitante(String emailSolicitante) {
        this.emailSolicitante = emailSolicitante;
    }
    /**Método que permite obtener el tipo de relación solicitada
     * 
     * @return tipo de relación solicitada
     */
    public RelationType getTipoRelacion() {
        return tipoRelacion;
    }
    /**Método que permite establecer el tipo de relación solicitada
     * 
     * @param tipoRelacion tipo de relación solicitada
     */
    public void setTipoRelacion(RelationType tipoRelacion) {
        this.tipoRelacion = tipoRelacion;
    }
    /**Método que permite obtener el comentario de la solicitud
     * 
     * @return comentario de la solicitud
     */
    public String getComentario() {
        return comentario;
    }
    /**Método que permite establecer el comentario de la solicitud
     * 
     * @param comentario comentario de la solicitud
     */
    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
    public String getEmailDestinatario() {
        return emailDestinatario;
    }
    public void setEmailDestinatario(String emailDestinatario) {
        this.emailDestinatario = emailDestinatario;
    }
    
}
