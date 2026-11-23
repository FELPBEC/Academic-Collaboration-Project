package co.edu.uptc.View;

import co.edu.uptc.Enum.RelationType;
import co.edu.uptc.Model.Solicitud;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

public class ListaSolicitudesView extends VBox {

    private Consumer<Solicitud> onVerSolicitud;

    public ListaSolicitudesView() {
        this.setSpacing(10);
        this.setFillWidth(true);
        this.setMaxWidth(Double.MAX_VALUE);
    }

    /**
     * Dibuja la bandeja de solicitudes recibidas por el usuario actual.
     */
    public void dibujarSolicitudes(List<Solicitud> solicitudes) {
        this.getChildren().clear();

        if (solicitudes == null || solicitudes.isEmpty()) {
            Label lblVacio = new Label("No tienes solicitudes pendientes.");
            lblVacio.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");
            this.getChildren().add(lblVacio);
            return;
        }

        for (Solicitud solicitud : solicitudes) {
            this.getChildren().add(crearTarjetaSolicitud(solicitud));
        }
    }

    private HBox crearTarjetaSolicitud(Solicitud solicitud) {
        HBox fila = new HBox();
        fila.setMaxWidth(Double.MAX_VALUE);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getStyleClass().addAll("tarjeta-usuario", obtenerClaseRelacion(solicitud.getTipoRelacion()));

        // Correo de quien envió la solicitud
        Label lblSolicitante = new Label(solicitud.getEmailSolicitante());
        lblSolicitante.getStyleClass().add("tarjeta-correo");

        // Al haber solo un botón, este espaciador ocupa automáticamente el lugar del segundo botón
        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);

        // Único botón de acción: abrir detalle completo de la solicitud
        Button btnVerDetalle = new Button();
        btnVerDetalle.getStyleClass().addAll("boton-icono", "boton-tarjeta", "icono-ver-solicitud");
        btnVerDetalle.setTooltip(new Tooltip("Revisar solicitud"));
        btnVerDetalle.setOnAction(e -> {
            if (onVerSolicitud != null) {
                onVerSolicitud.accept(solicitud);
            }
        });

        fila.getChildren().addAll(lblSolicitante, espaciador, btnVerDetalle);
        return fila;
    }

    private String obtenerClaseRelacion(RelationType tipo) {
        if (tipo == null) {
            return "relacion-participacion";
        }
        switch (tipo) {
            case InvestigationCollaboration:
                return "relacion-investigacion";
            case Mentoring:
                return "relacion-mentoria";
            case JointParticipation:
            default:
                return "relacion-participacion";
        }
    }

    public void setOnVerSolicitud(Consumer<Solicitud> onVerSolicitud) {
        this.onVerSolicitud = onVerSolicitud;
    }
}
