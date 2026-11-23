package co.edu.uptc.ViewController;

import co.edu.uptc.Enum.RelationType;
import co.edu.uptc.Enum.TipoOperacionSolicitud;
import co.edu.uptc.Model.User;
import co.edu.uptc.View.App;
import co.edu.uptc.View.SolicitudMuroView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import co.edu.uptc.Exceptions.DuplicateUserException;
import co.edu.uptc.Exceptions.UsuarioNoEncontradoException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SolicitudFormController implements Initializable {

    private static String correoDestinatarioPendiente;
    private static TipoOperacionSolicitud operacionPendiente = TipoOperacionSolicitud.CREAR_RELACION;
    private static RelationType relacionInicialPendiente;

    @FXML
    private StackPane contenedorCentral;

    private ResourceBundle resources;
    private SolicitudMuroView muroView;

    /**
     * Prepara los datos antes de navegar a "SolicitudForm" con App.setRoot("SolicitudForm").
     */
    public static void prepararSolicitud(String correoDestinatario,
                                         TipoOperacionSolicitud operacion,
                                         RelationType relacionInicial) {
        correoDestinatarioPendiente = correoDestinatario;
        operacionPendiente = (operacion != null) ? operacion : TipoOperacionSolicitud.CREAR_RELACION;
        relacionInicialPendiente = relacionInicial;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;

        User actual = App.getServicioGeneral().getUsuarioActual();
        String correoActual = (actual != null) ? actual.getEmail() : "";

        muroView = new SolicitudMuroView(resources);
        muroView.dibujarSolicitud(correoActual, correoDestinatarioPendiente, operacionPendiente);
        muroView.setDatosIniciales(relacionInicialPendiente, null);

        muroView.setOnConfirmar((tipoRelacion, comentario) -> {
            procesarSolicitud(correoActual, correoDestinatarioPendiente, tipoRelacion, comentario);
        });

        // Botón Cancelar regresa a la ventana anterior según el tipo de operación
        muroView.setOnCancelar(this::volverAVentanaAnterior);

        contenedorCentral.getChildren().add(muroView);
    }

    private void procesarSolicitud(String correoOrigen,
                                   String correoDestino,
                                   RelationType tipoRelacion,
                                   String comentario) {
        try {
            if (operacionPendiente == TipoOperacionSolicitud.MODIFICAR_RELACION) {
                App.getServicioGeneral().solicitarModificacionRelacion(
                        correoDestino, tipoRelacion, comentario);
            } else {
                App.getServicioGeneral().enviarSolicitud(
                        correoDestino, tipoRelacion, comentario);
            }

            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle(resources.getString("request.success.title"));
            alerta.setHeaderText(null);
            alerta.setContentText(resources.getString("request.success.message"));
            alerta.showAndWait();

            volverAVentanaAnterior();

        } catch (UsuarioNoEncontradoException e) {
            muroView.mostrarError(resources.getString("request.error.user.not.found"));
        } catch (DuplicateUserException e) {
            muroView.mostrarError(resources.getString("request.error.duplicate"));
        } catch (IllegalArgumentException e) {
            muroView.mostrarError(traducirErrorValidacion(e.getMessage()));
        } catch (IllegalStateException e) {
            muroView.mostrarError(resources.getString("request.error.no.session"));
        } catch (IOException e) {
            muroView.mostrarError(resources.getString("request.error.io"));
            e.printStackTrace();
        }
    }
    private String traducirErrorValidacion(String mensajeOriginal) {
        if (mensajeOriginal == null) {
            return resources.getString("request.error.send");
        }
        String texto = mensajeOriginal.toLowerCase();
        if (texto.contains("ya existe una relación")) {
            return resources.getString("request.error.relation.exists");
        }
        if (texto.contains("no existe una relación")) {
            return resources.getString("request.error.relation.not.exists");
        }
        if (texto.contains("mismo") || texto.contains("cambia")) {
            return resources.getString("request.error.same.type");
        }
        return mensajeOriginal;
    }

    private void volverAVentanaAnterior() {
        try {
            if (operacionPendiente == TipoOperacionSolicitud.MODIFICAR_RELACION) {
                App.setRoot("Conexiones");
            } else {
                App.setRoot("Explorar");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
