package co.edu.uptc.Controller;

import java.util.List;

import co.edu.uptc.Enum.RelationType;
import co.edu.uptc.Exceptions.DuplicateUserException;
import co.edu.uptc.Model.Solicitud;
/**Clase para manejar las solicitudes de colaboración entre usuarios, incluyendo la creación, aceptación y rechazo de solicitudes.
 * 
 * @author @FELPBEC
 * @since 2026-19-11 
 *  @version 1.0
 */
public class ServicioSolicitudes {
    private List<Solicitud> solicitudes;
    /**Constructor de la clase ServicioSolicitudes que inicializa la lista de solicitudes.
     * 
     * @param solicitudes
     */
    public ServicioSolicitudes(List<Solicitud> solicitudes) {
        this.solicitudes = solicitudes;
    }
    /**Método que permite obtener la lista de solicitudes.
     * 
     * @return lista de solicitudes
     */
    public List<Solicitud> getSolicitudes() {
        return solicitudes;
    }
    /**Método que permite establecer la lista de solicitudes.
     * 
     * @param solicitudes
     */
    public void setSolicitudes(List<Solicitud> solicitudes) {
        this.solicitudes = solicitudes;
    }
    //CREAR SOLICITUD
    /**Método para crear una solicitud de colaboración entre usuarios.
     * Esta solicitud se añade automaticamente a la lista de solicitudes del destinatario
     * @param emailSolicitante correo del usuario que solicita la colaboración
     * @param emailDestinatario correo del usuario al que se le solicita la colaboración
     * @param tipoRelacion  tipo de relación solicitada
     * @param comentario    comentario adicional de la solicitud
     * @return
     */
    public Solicitud enviarSolicitud(String emailSolicitante, String emailDestinatario, RelationType tipoRelacion, String comentario)throws DuplicateUserException {
        if(emailSolicitante.equals(emailDestinatario))throw new DuplicateUserException("No se puede enviar una solicitud a si mismo");
        Solicitud solicitud = new Solicitud(emailSolicitante, emailDestinatario, tipoRelacion, comentario);
        return solicitud;
    }
    //ACEPTAR SOLICITUD
    /**Método de aceptar solicitud que la elimina de la lista de solicitudes del usuario
     * y envía de vuelta la solicitud (que contiene los atributos para hacer una Arista) 
     * para poder hacer la conexión en el grafo
     * 
     * @param solicitud la solicitud aceptada
     * @return
     */
    public Solicitud aceptarSolicitud(Solicitud solicitud) {
        solicitudes.remove(solicitud);
        return solicitud;
    }
    //RECHAZAR SOLICITUD
    public void rechazarSolicitud(Solicitud solicitud){
        solicitudes.remove(solicitud);
    }
}
