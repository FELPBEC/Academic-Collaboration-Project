package co.edu.uptc.View;

import co.edu.uptc.Enum.InteresArea;
import co.edu.uptc.Model.User;
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

public class ListaUsuariosView extends VBox {

    // Callbacks para avisar al controlador qué botón se presionó y sobre qué usuario
    private Consumer<User> onVerConexion;
    private Consumer<User> onEnviarSolicitud;

    public ListaUsuariosView() {
        this.setSpacing(10); // Separación vertical entre tarjetas
        this.setFillWidth(true); // Obliga a cada fila a ocupar todo el ancho disponible
        this.setMaxWidth(Double.MAX_VALUE);
    }
    private boolean mostrarBotones = true;

    public void setMostrarBotones(boolean mostrarBotones) {
        this.mostrarBotones = mostrarBotones;
    }
    /**
     * Limpia el contenedor y dibuja una tarjeta personalizada por cada usuario de la lista.
     */
    public void dibujarUsuarios(List<User> usuarios) {
        this.getChildren().clear();

        if (usuarios == null || usuarios.isEmpty()) {
            Label lblVacio = new Label("No se encontraron participantes.");
            lblVacio.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");
            this.getChildren().add(lblVacio);
            return;
        }

        for (User usuario : usuarios) {
            this.getChildren().add(crearTarjetaUsuario(usuario));
        }
    }

    private HBox crearTarjetaUsuario(User usuario) {
        HBox fila = new HBox();
        fila.setMaxWidth(Double.MAX_VALUE);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getStyleClass().addAll("tarjeta-usuario", obtenerClaseArea(usuario.getInteresArea()));

        // 1. Texto del usuario (zona central)
        Label lblCorreo = new Label(usuario.getEmail());
        lblCorreo.getStyleClass().add("tarjeta-correo");

        

        
        fila.getChildren().add(lblCorreo);

        if (mostrarBotones) {
            Region espaciador = new Region();
            HBox.setHgrow(espaciador, Priority.ALWAYS);
// 3. Botones de la derecha
            Button btnRed = new Button();
            btnRed.getStyleClass().addAll("boton-icono", "boton-tarjeta", "icono-red");
            btnRed.setTooltip(new Tooltip("Ver ruta de conexión"));
            btnRed.setOnAction(e -> {
                if (onVerConexion != null) {
                    onVerConexion.accept(usuario);
                }
            });

            Button btnSolicitud = new Button();
            btnSolicitud.getStyleClass().addAll("boton-icono", "boton-tarjeta", "icono-mensaje");
            btnSolicitud.setTooltip(new Tooltip("Enviar solicitud de colaboración"));
            btnSolicitud.setOnAction(e -> {
                if (onEnviarSolicitud != null) {
                    onEnviarSolicitud.accept(usuario);
                }
            });

            fila.getChildren().addAll(espaciador, btnRed, btnSolicitud);
        }

        return fila;
    }

    /**
     * Asigna la clase CSS correspondiente según el área de interés del usuario.
     */
    private String obtenerClaseArea(InteresArea area) {
        if (area == null) {
        return "area-humanidades";
    }
    switch (area) {
        case Cience:
            return "area-ciencia";
        case Technology:
            return "area-tecnologia";
        case Engineering:
            return "area-ingenieria";
        case Mathematics:
            return "area-matematicas";
        case SocialSciences:
            return "area-sociales";
        case Humanities:
        default:
            return "area-humanidades";
    }
                
        }

    // Setters para conectar las acciones desde cualquier controlador
    public void setOnVerConexion(Consumer<User> onVerConexion) {
        this.onVerConexion = onVerConexion;
    }

    public void setOnEnviarSolicitud(Consumer<User> onEnviarSolicitud) {
        this.onEnviarSolicitud = onEnviarSolicitud;
    }
}
