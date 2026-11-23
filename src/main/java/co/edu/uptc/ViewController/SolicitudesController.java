package co.edu.uptc.ViewController;

import co.edu.uptc.Enum.RelationType;
import co.edu.uptc.Enum.TipoOperacionSolicitud;
import co.edu.uptc.Model.Solicitud;
import co.edu.uptc.Model.User;
import co.edu.uptc.View.App;
import co.edu.uptc.View.ListaSolicitudesView;
import co.edu.uptc.View.SolicitudMuroView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SolicitudesController implements Initializable {

    @FXML
    private BorderPane vistaLista;

    @FXML
    private StackPane contenedorLista;

    @FXML
    private Label lblSinSolicitudes;

    @FXML
    private StackPane vistaDetalle;

    private ResourceBundle resources;
    private ListaSolicitudesView listaSolicitudesView;
    private ScrollPane scrollSolicitudes;
    private SolicitudMuroView muroDetalleView;
    private Solicitud solicitudSeleccionada;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;

        inicializarVistaLista();
        inicializarVistaDetalle();
        cargarSolicitudes();
    }

    private void inicializarVistaLista() {
        listaSolicitudesView = new ListaSolicitudesView();

        // Conecta el botón de detalle de ListaSolicitudesView con el muro
        listaSolicitudesView.setOnVerSolicitud(this::abrirDetalleSolicitud);

        scrollSolicitudes = new ScrollPane(listaSolicitudesView);
        scrollSolicitudes.setFitToWidth(true);
        scrollSolicitudes.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        contenedorLista.getChildren().add(0, scrollSolicitudes);
    }

    private void inicializarVistaDetalle() {
        muroDetalleView = new SolicitudMuroView(resources);

        muroDetalleView.setOnAtras(this::volverALaLista);
        muroDetalleView.setOnAceptar(this::aceptarSolicitudActual);
        muroDetalleView.setOnRechazar(this::rechazarSolicitudActual);

        vistaDetalle.getChildren().add(muroDetalleView);
    }

    private void cargarSolicitudes() {
        User actual = App.getServicioGeneral().getUsuarioActual();
        List<Solicitud> pendientes = (actual != null) ? actual.getSolicitudesRecibidas() : null;

        boolean vacio = (pendientes == null || pendientes.isEmpty());
        lblSinSolicitudes.setVisible(vacio);
        lblSinSolicitudes.setManaged(vacio);
        scrollSolicitudes.setVisible(!vacio);
        scrollSolicitudes.setManaged(!vacio);

        if (!vacio) {
            listaSolicitudesView.dibujarSolicitudes(pendientes);
        }
    }

    private void abrirDetalleSolicitud(Solicitud solicitud) {
        if (solicitud == null) {
            return;
        }
        this.solicitudSeleccionada = solicitud;

        User actual = App.getServicioGeneral().getUsuarioActual();
        String correoDestino = (solicitud.getEmailDestinatario() != null)
                ? solicitud.getEmailDestinatario()
                : (actual != null ? actual.getEmail() : "");

        TipoOperacionSolicitud operacion = (solicitud.getTipoOperacion() != null)
                ? solicitud.getTipoOperacion()
                : TipoOperacionSolicitud.CREAR_RELACION;

        RelationType tipoMostrar = (operacion == TipoOperacionSolicitud.MODIFICAR_RELACION
                && solicitud.getTipoRelacionNueva() != null)
                ? solicitud.getTipoRelacionNueva()
                : solicitud.getTipoRelacion();

        muroDetalleView.dibujarDetalleSolicitud(
                solicitud.getEmailSolicitante(),
                correoDestino,
                operacion,
                tipoMostrar,
                solicitud.getTipoRelacionAnterior(),
                solicitud.getComentario()
        );

        vistaLista.setVisible(false);
        vistaLista.setManaged(false);
        vistaDetalle.setVisible(true);
        vistaDetalle.setManaged(true);
    }

    private void volverALaLista() {
        solicitudSeleccionada = null;
        vistaDetalle.setVisible(false);
        vistaDetalle.setManaged(false);
        vistaLista.setVisible(true);
        vistaLista.setManaged(true);
    }

    private void aceptarSolicitudActual() {
        if (solicitudSeleccionada == null) {
            return;
        }
        try {
            App.getServicioGeneral().aceptarSolicitud(solicitudSeleccionada);

            mostrarAlerta(resources.getString("requests.accept.success"));
            volverALaLista();
            cargarSolicitudes();
        } catch (IllegalStateException e) {
            muroDetalleView.mostrarError(resources.getString("requests.error.relation.missing"));
        } catch (IOException e) {
            muroDetalleView.mostrarError(resources.getString("requests.error.io"));
            e.printStackTrace();
        }
    }

    private void rechazarSolicitudActual() {
        if (solicitudSeleccionada == null) {
            return;
        }
        try {
            App.getServicioGeneral().rechazarSolicitud(solicitudSeleccionada);

            mostrarAlerta(resources.getString("requests.reject.success"));
            volverALaLista();
            cargarSolicitudes();
        } catch (IOException e) {
            muroDetalleView.mostrarError(resources.getString("requests.error.io"));
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(resources.getString("requests.title"));
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}