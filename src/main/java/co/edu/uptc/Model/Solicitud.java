package co.edu.uptc.Model;

import co.edu.uptc.Enum.RelationType;
import co.edu.uptc.Enum.TipoOperacionSolicitud;
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
    private TipoOperacionSolicitud tipoOperacion;
    private RelationType tipoRelacionAnterior;
    private RelationType tipoRelacionNueva;
    /**Constructor vacío de la clase para persistencia */
    public Solicitud() {
        this.tipoOperacion = TipoOperacionSolicitud.CREAR_RELACION;
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
        this.tipoOperacion = TipoOperacionSolicitud.CREAR_RELACION;
        this.tipoRelacionAnterior = null;
        this.tipoRelacionNueva = null;
    }

    /**
     * Crea una solicitud de modificación de una relación existente.
     *
     * @param emailSolicitante usuario que solicita el cambio
     * @param emailDestinatario usuario que debe aprobar el cambio
     * @param tipoAnterior relación actual
     * @param tipoNuevo relación solicitada
     * @param comentario comentario adicional
     */
    public Solicitud(String emailSolicitante, String emailDestinatario,
            RelationType tipoAnterior, RelationType tipoNuevo, String comentario) {
        this.emailSolicitante = emailSolicitante;
        this.emailDestinatario = emailDestinatario;
        this.tipoRelacion = tipoNuevo;
        this.comentario = comentario;
        this.tipoOperacion = TipoOperacionSolicitud.MODIFICAR_RELACION;
        this.tipoRelacionAnterior = tipoAnterior;
        this.tipoRelacionNueva = tipoNuevo;
    }

    /**
     * Obtiene la operación pendiente representada por la solicitud.
     *
     * @return operación de creación o modificación
     */
    public TipoOperacionSolicitud getTipoOperacion() {
        return tipoOperacion;
    }

    /**
     * Establece la operación pendiente representada por la solicitud.
     *
     * @param tipoOperacion operación de la solicitud
     */
    public void setTipoOperacion(TipoOperacionSolicitud tipoOperacion) {
        this.tipoOperacion = tipoOperacion;
    }

    /**
     * Obtiene el tipo previo en una solicitud de modificación.
     *
     * @return tipo de relación anterior o {@code null}
     */
    public RelationType getTipoRelacionAnterior() {
        return tipoRelacionAnterior;
    }

    /**
     * Establece el tipo previo en una solicitud de modificación.
     *
     * @param tipoRelacionAnterior tipo anterior
     */
    public void setTipoRelacionAnterior(RelationType tipoRelacionAnterior) {
        this.tipoRelacionAnterior = tipoRelacionAnterior;
    }

    /**
     * Obtiene el tipo nuevo solicitado en una modificación.
     *
     * @return tipo nuevo
     */
    public RelationType getTipoRelacionNueva() {
        return tipoRelacionNueva;
    }

    /**
     * Establece el tipo nuevo solicitado en una modificación.
     *
     * @param tipoRelacionNueva tipo nuevo
     */
    public void setTipoRelacionNueva(RelationType tipoRelacionNueva) {
        this.tipoRelacionNueva = tipoRelacionNueva;
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
