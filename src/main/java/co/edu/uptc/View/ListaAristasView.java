package co.edu.uptc.View;

import co.edu.uptc.Enum.RelationType;
import co.edu.uptc.Model.Arista;
import co.edu.uptc.Model.Nodo;
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

public class ListaAristasView extends VBox {

    private Consumer<Arista<String>> onCancelarConexion;
    private Consumer<Arista<String>> onModificarConexion;

    public ListaAristasView() {
        this.setSpacing(10);
        this.setFillWidth(true);
        this.setMaxWidth(Double.MAX_VALUE);
    }

    /**
     * Dibuja cada conexión directa mostrando el correo del colaborador opuesto
     * al usuario que tiene la sesión iniciada.
     */
    public void dibujarAristas(List<Arista<String>> aristas, String emailUsuarioActual) {
        this.getChildren().clear();

        if (aristas == null || aristas.isEmpty()) {
            Label lblVacio = new Label("Aún no tienes conexiones registradas.");
            lblVacio.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");
            this.getChildren().add(lblVacio);
            return;
        }

        Nodo<String> nodoActual = new Nodo<>(emailUsuarioActual);

        for (Arista<String> arista : aristas) {
            this.getChildren().add(crearTarjetaArista(arista, nodoActual));
        }
    }

    private HBox crearTarjetaArista(Arista<String> arista, Nodo<String> nodoActual) {
        HBox fila = new HBox();
        fila.setMaxWidth(Double.MAX_VALUE);
        fila.setAlignment(Pos.CENTER_LEFT);

        RelationType tipo = (arista.getDatosRelacion() != null)
                ? arista.getDatosRelacion().getRelationType()
                : null;
        fila.getStyleClass().addAll("tarjeta-usuario", obtenerClaseRelacion(tipo));

        // Extrae el email del colaborador (extremo opuesto al usuario actual)
        Nodo<String> nodoColaborador = arista.getExtremoOpuesto(nodoActual);
        String emailMostrar = (nodoColaborador != null)
                ? nodoColaborador.getContenido()
                : arista.getNodoFinal().getContenido();

        Label lblCorreo = new Label(emailMostrar);
        lblCorreo.getStyleClass().add("tarjeta-correo");

        // Zona central estirable
        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);

        // Botón 1: Cancelar / eliminar conexión (icono de persona con X)
        Button btnCancelar = new Button();
        btnCancelar.getStyleClass().addAll("boton-icono", "boton-tarjeta", "icono-cancelar");
        btnCancelar.setTooltip(new Tooltip("Eliminar conexión"));
        btnCancelar.setOnAction(e -> {
            if (onCancelarConexion != null) {
                onCancelarConexion.accept(arista);
            }
        });

        // Botón 2: Modificar tipo de relación (icono de engranaje)
        Button btnModificar = new Button();
        btnModificar.getStyleClass().addAll("boton-icono", "boton-tarjeta", "icono-modificar");
        btnModificar.setTooltip(new Tooltip("Cambiar tipo de relación"));
        btnModificar.setOnAction(e -> {
            if (onModificarConexion != null) {
                onModificarConexion.accept(arista);
            }
        });

        fila.getChildren().addAll(lblCorreo, espaciador, btnCancelar, btnModificar);
        return fila;
    }

    private String obtenerClaseRelacion(RelationType tipo) {
        if (tipo == null) {
            return "relacion-participacion";
        }
        switch (tipo) {
            case InvestigationCollaboration:
                return "relacion-investigacion"; // Lupa verde
            case Mentoring:
                return "relacion-mentoria";      // Pizarra coral/roja
            case JointParticipation:
            default:
                return "relacion-participacion"; // Rompecabezas aguamarina
        }
    }

    public void setOnCancelarConexion(Consumer<Arista<String>> onCancelarConexion) {
        this.onCancelarConexion = onCancelarConexion;
    }

    public void setOnModificarConexion(Consumer<Arista<String>> onModificarConexion) {
        this.onModificarConexion = onModificarConexion;
    }
}
