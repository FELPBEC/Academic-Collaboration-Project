package co.edu.uptc.ViewController;

import co.edu.uptc.Enum.TypeUser;
import co.edu.uptc.Model.Grafo;
import co.edu.uptc.Model.User;
import co.edu.uptc.View.App;
import co.edu.uptc.View.GrafoView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Shape;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class GrafoController implements Initializable {

    @FXML
    private ScrollPane scrollGrafo;

    @FXML
    private StackPane contenedorLienzo;

    @FXML
    private HBox boxLeyendaNodos;

    private ResourceBundle resources;
    private GrafoView grafoView;
    private double escalaActual = 1.0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;

        grafoView = new GrafoView();
        contenedorLienzo.getChildren().add(grafoView);

        User actual = App.getServicioGeneral().getUsuarioActual();
        if (actual != null) {
            grafoView.setUsuarioActual(actual.getEmail());
        }

        Grafo<String> grafo = App.getServicioGeneral().getGrafo();
        Map<String, User> usuarios = App.getServicioGeneral().getUsuariosMap();
        grafoView.dibujarGrafo(grafo, usuarios);

        construirLeyenda();
    }

    private void construirLeyenda() {
        boxLeyendaNodos.getChildren().clear();

        for (TypeUser tipo : TypeUser.values()) {
            Shape icono = grafoView.crearForma(tipo, false);
            icono.setFill(GrafoView.getNodeColor(tipo));
            icono.setScaleX(icono.getScaleX() * 0.55);
            icono.setScaleY(icono.getScaleY() * 0.55);

            Group contenedorIcono = new Group(icono);
            String clave = "enum." + tipo.name();
            String textoTipo = resources.containsKey(clave)
                    ? resources.getString(clave)
                    : tipo.name().replaceAll("([a-z])([A-Z])", "$1 $2");

            Label lblTipo = new Label(textoTipo);
            lblTipo.getStyleClass().add("general-text");

            HBox item = new HBox(6.0, contenedorIcono, lblTipo);
            item.setAlignment(Pos.CENTER);
            boxLeyendaNodos.getChildren().add(item);
        }
    }

    @FXML
    private void onZoomIn(ActionEvent event) {
        aplicarZoom(Math.min(2.0, escalaActual + 0.15));
    }

    @FXML
    private void onZoomOut(ActionEvent event) {
        aplicarZoom(Math.max(0.5, escalaActual - 0.15));
    }

    @FXML
    private void onResetZoom(ActionEvent event) {
        aplicarZoom(1.0);
        scrollGrafo.setHvalue(0.5);
        scrollGrafo.setVvalue(0.5);
    }

    private void aplicarZoom(double nuevaEscala) {
        escalaActual = nuevaEscala;
        grafoView.setScaleX(escalaActual);
        grafoView.setScaleY(escalaActual);
    }
}