package co.edu.uptc.Controller;

import java.util.List;

import co.edu.uptc.Enum.RelationType;
import co.edu.uptc.Enum.TipoOperacionSolicitud;
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

    /**
     * Crea una solicitud para modificar el tipo de una relación existente.
     *
     * @param emailSolicitante usuario que solicita la modificación
     * @param emailDestinatario usuario que debe aprobar la modificación
     * @param tipoAnterior tipo actual de la relación
     * @param tipoNuevo tipo solicitado
     * @param comentario comentario adicional
     * @return solicitud de modificación pendiente
     * @throws DuplicateUserException si el solicitante intenta modificar consigo mismo
     * @throws IllegalArgumentException si el tipo actual o nuevo no es válido o no cambia
     */
    public Solicitud solicitarModificacionRelacion(
            String emailSolicitante,
            String emailDestinatario,
            RelationType tipoAnterior,
            RelationType tipoNuevo,
            String comentario) throws DuplicateUserException {
        if (emailSolicitante == null || emailDestinatario == null
                || emailSolicitante.equals(emailDestinatario)) {
            throw new DuplicateUserException("No se puede modificar una relación consigo mismo");
        }
        if (tipoAnterior == null || tipoNuevo == null) {
            throw new IllegalArgumentException("Los tipos anterior y nuevo son obligatorios");
        }
        if (tipoAnterior == tipoNuevo) {
            throw new IllegalArgumentException("El tipo nuevo debe ser diferente al actual");
        }

        Solicitud solicitud = new Solicitud(
                emailSolicitante,
                emailDestinatario,
                tipoAnterior,
                tipoNuevo,
                comentario);
        solicitud.setTipoOperacion(TipoOperacionSolicitud.MODIFICAR_RELACION);
        return solicitud;
    }
    //ACEPTAR SOLICITUD
    /**
     * Elimina una solicitud pendiente de la lista y devuelve sus datos para que
     * {@link ServicioGeneral} aplique la creación o modificación correspondiente.
     *
     * @param solicitud solicitud aceptada
     * @return solicitud aceptada
     */
    public Solicitud aceptarSolicitud(Solicitud solicitud) {
        solicitudes.remove(solicitud);
        return solicitud;
    }
    //RECHAZAR SOLICITUD
    /**
     * Elimina una solicitud rechazada sin modificar el grafo ni la relación actual.
     *
     * @param solicitud solicitud que será rechazada
     */
    public void rechazarSolicitud(Solicitud solicitud){
        solicitudes.remove(solicitud);
    }
}
