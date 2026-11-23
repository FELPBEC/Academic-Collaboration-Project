package co.edu.uptc.ViewController;
import co.edu.uptc.Exceptions.DuplicateUserException;
import co.edu.uptc.Exceptions.InvalidPasswordFormat;
import co.edu.uptc.Enum.InteresArea; // Si tu clase se llama InterestArea, actualiza este import
import co.edu.uptc.Enum.Program;
import co.edu.uptc.Enum.TypeUser;
import co.edu.uptc.Model.User;
import co.edu.uptc.View.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import co.edu.uptc.Exceptions.DuplicateUserException;
import co.edu.uptc.Exceptions.InvalidPasswordFormat;
import co.edu.uptc.Exceptions.UsuarioNoEncontradoException;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class RegistroController implements Initializable {

    @FXML
    private ComboBox<Locale> cbIdioma;

    @FXML
    private TextField txtUsername;

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private ComboBox<TypeUser> cbTypeUser;

    @FXML
    private ComboBox<Program> cbProgram;

    @FXML
    private ComboBox<InteresArea> cbInterestArea;

    @FXML
    private Label lblMensajeError;

    private ResourceBundle resources;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;


        // Carga dinámica consultando directamente cada Enum
        configurarComboEnum(cbTypeUser, TypeUser.values());
        configurarComboEnum(cbProgram, Program.values());
        configurarComboEnum(cbInterestArea, InteresArea.values());
    }

    /**
     * Llena cualquier ComboBox consultando las constantes de un Enum y traduce su texto
     * si existe la clave "enum.<NOMBRE>" en el ResourceBundle.
     */
    private <T extends Enum<T>> void configurarComboEnum(ComboBox<T> combo, T[] valores) {
        combo.getItems().setAll(valores);
        combo.setConverter(new StringConverter<T>() {
            @Override
            public String toString(T item) {
                if (item == null) {
                    return "";
                }
                String clave = "enum." + item.name();
                if (resources.containsKey(clave)) {
                    return resources.getString(clave);
                }
                // Separa palabras en CamelCase si aún no tiene clave en el .properties
                return item.name().replaceAll("([a-z])([A-Z])", "$1 $2");
            }

            @Override
            public T fromString(String string) {
                return null;
            }
        });
    }

    

  @FXML
    private void onRegistrar(ActionEvent event) {
        ocultarError();

        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();
        TypeUser typeUser = cbTypeUser.getValue();
        Program program = cbProgram.getValue();
        InteresArea interesArea = cbInterestArea.getValue();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()
                || typeUser == null || program == null || interesArea == null) {
            mostrarError(resources.getString("register.error.empty.fields"));
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            mostrarError(resources.getString("register.error.invalid.email"));
            return;
        }

        try {
            User nuevoUsuario = new User(username, email, password, typeUser, program, interesArea);
            
            // 1. Registra y guarda en ../data/
            App.getServicioGeneral().registrarUsuario(nuevoUsuario);
            
            // 2. Inicia sesión automáticamente con el usuario recién creado
            App.getServicioGeneral().iniciarSesion(email, password);

            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle(resources.getString("register.success.title"));
            alerta.setHeaderText(null);
            alerta.setContentText(resources.getString("register.success.message"));
            alerta.showAndWait();

            // 3. Entra directo a la pantalla principal
            App.setRoot("Explorar");

        } catch (DuplicateUserException e) {
            mostrarError(resources.getString("register.error.duplicate.email"));
        } catch (InvalidPasswordFormat e) {
            mostrarError(resources.getString("register.error.invalid.password"));
        } catch (UsuarioNoEncontradoException | IOException e) {
            mostrarError(resources.getString("register.error.io"));
            e.printStackTrace();
        }
    }

    @FXML
    private void onVolverLogin(ActionEvent event) {
        try {
            App.setRoot("Login");
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
