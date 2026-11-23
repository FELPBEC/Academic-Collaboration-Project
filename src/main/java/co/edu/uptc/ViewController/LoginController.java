package co.edu.uptc.ViewController;

import co.edu.uptc.Exceptions.UsuarioNoEncontradoException;
import co.edu.uptc.View.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import java.util.Locale;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblMensajeError;

    @FXML
    private Button btnIniciarSesion;

    @FXML
    private StackPane panelDerecho;
    @FXML
    private ComboBox<Locale> cbIdioma;
    @FXML
    private ImageView imgBanner;

    private ResourceBundle resources;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        if (imgBanner != null && panelDerecho != null) {
            imgBanner.fitWidthProperty().bind(
                    panelDerecho.widthProperty().multiply(0.82)
            );
            imgBanner.fitHeightProperty().bind(
                    panelDerecho.heightProperty().multiply(0.82)
            );

            if (imgBanner.getImage() == null) {
                URL imagen = LoginController.class.getResource("/co/edu/uptc/images/LogoElMuro.png");
                if (imagen != null) {
                    imgBanner.setImage(new Image(imagen.toExternalForm()));
                }
            }
        }
    }

    @FXML
    private void onIniciarSesion(ActionEvent event) {
        ocultarError();

        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            mostrarError(resources.getString("login.error.empty.fields"));
            return;
        }

        try {
            boolean valido = App.getServicioGeneral().iniciarSesion(email, password);
            if (valido) {
                App.setRoot("Explorar");
            } else {
                mostrarError(resources.getString("login.error.credentials"));
            }
        } catch (UsuarioNoEncontradoException e) {
            mostrarError(resources.getString("login.error.user.not.found"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void onIrARegistro(ActionEvent event) {
        try {
            App.setRoot("Registro");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarError(String mensaje) {
        lblMensajeError.setText(mensaje);
        lblMensajeError.setVisible(true);
        lblMensajeError.setManaged(true);
    }

    private void ocultarError() {
        lblMensajeError.setVisible(false);
        lblMensajeError.setManaged(false);
    }
}