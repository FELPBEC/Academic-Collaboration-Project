package co.edu.uptc.ViewController;

import co.edu.uptc.View.App;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MenuLateralController implements Initializable {

    private static final double ANCHO_MENU = 260.0;

    @FXML
    private HBox contenedorDesplegable;

    @FXML
    private VBox panelMenu;

    private boolean abierto = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Arranca escondido al borde izquierdo; solo sobresale la pestaña
        contenedorDesplegable.setTranslateX(-ANCHO_MENU);
        abierto = false;
    }

    @FXML
    private void onToggleMenu(ActionEvent event) {
        TranslateTransition animacion = new TranslateTransition(Duration.millis(220), contenedorDesplegable);
        if (abierto) {
            animacion.setToX(-ANCHO_MENU);
        } else {
            animacion.setToX(0);
        }
        abierto = !abierto;
        animacion.play();
    }

    @FXML
    private void onIrPerfil(ActionEvent event) {
        navegarA("Perfil");
    }

    @FXML
    private void onIrExplorar(ActionEvent event) {
        navegarA("Explorar");
    }

    @FXML
    private void onIrConexiones(ActionEvent event) {
        navegarA("Conexiones");
    }

    @FXML
    private void onIrSolicitudes(ActionEvent event) {
        navegarA("Solicitudes");
    }

    @FXML
    private void onIrGrafo(ActionEvent event) {
        navegarA("Grafo");
    }

    @FXML
    private void onCerrarSesion(ActionEvent event) {
        // Si tienes método cerrarSesion() en ServicioGeneral, llámalo aquí:
        // App.getServicioGeneral().cerrarSesion();
        navegarA("Login");
    }

    private void navegarA(String nombreFxml) {
        try {
            App.setRoot(nombreFxml);
        } catch (IOException | IllegalStateException e) {
            System.out.println("Vista aún no creada: " + nombreFxml + ".fxml");
        }
    }
}
