package co.edu.uptc.ViewController;

import co.edu.uptc.Enum.InteresArea;
import co.edu.uptc.Enum.Program;
import co.edu.uptc.Enum.TypeUser;
import co.edu.uptc.Exceptions.InvalidPasswordFormat;
import co.edu.uptc.Exceptions.UsuarioNoEncontradoException;
import co.edu.uptc.Model.User;
import co.edu.uptc.View.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class PerfilController implements Initializable {

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtName;

    @FXML
    private ComboBox<TypeUser> cbTypeUser;

    @FXML
    private ComboBox<Program> cbProgram;

    @FXML
    private ComboBox<InteresArea> cbInterestArea;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblError;

    private ResourceBundle resources;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;

        configurarComboEnum(cbTypeUser, TypeUser.values());
        configurarComboEnum(cbProgram, Program.values());
        configurarComboEnum(cbInterestArea, InteresArea.values());

        cargarDatosUsuarioActual();
    }

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
                return item.name().replaceAll("([a-z])([A-Z])", "$1 $2");
            }

            @Override
            public T fromString(String string) {
                return null;
            }
        });
    }

    private void cargarDatosUsuarioActual() {
        User actual = App.getServicioGeneral().getUsuarioActual();
        if (actual == null) {
            return;
        }

        txtEmail.setText(actual.getEmail());
        txtName.setText(actual.getName());
        cbTypeUser.setValue(actual.getTypeUser());
        cbProgram.setValue(actual.getProgram());
        cbInterestArea.setValue(actual.getInteresArea());
        txtPassword.clear();
    }

    @FXML
    private void onGuardarCambios(ActionEvent event) {
        ocultarError();

        User actual = App.getServicioGeneral().getUsuarioActual();
        if (actual == null) {
            mostrarError(resources.getString("profile.error.no.session"));
            return;
        }

        String nombre = (txtName.getText() != null) ? txtName.getText().trim() : "";
        TypeUser tipo = cbTypeUser.getValue();
        Program programa = cbProgram.getValue();
        InteresArea area = cbInterestArea.getValue();
        String nuevaPassword = (txtPassword.getText() != null) ? txtPassword.getText().trim() : "";

        if (nombre.isEmpty() || tipo == null || programa == null || area == null) {
            mostrarError(resources.getString("profile.error.empty"));
            return;
        }

        User datosActualizados = new User();
        datosActualizados.setEmail(actual.getEmail());
        datosActualizados.setName(nombre);
        datosActualizados.setTypeUser(tipo);
        datosActualizados.setProgram(programa);
        datosActualizados.setInteresArea(area);
        datosActualizados.setPassword(nuevaPassword);

        try {
            App.getServicioGeneral().actualizarUsuarioActual(datosActualizados);
            txtPassword.clear();

            mostrarAlerta(Alert.AlertType.INFORMATION, resources.getString("profile.save.success"));
        } catch (InvalidPasswordFormat e) {
            mostrarError(resources.getString("profile.error.password.format"));
        } catch (UsuarioNoEncontradoException e) {
            mostrarError(resources.getString("profile.error.not.found"));
        } catch (IllegalStateException e) {
            mostrarError(resources.getString("profile.error.no.session"));
        } catch (IOException e) {
            mostrarError(resources.getString("profile.error.io"));
            e.printStackTrace();
        }
    }

    @FXML
    private void onEliminarCuenta(ActionEvent event) {
        ocultarError();

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle(resources.getString("profile.delete.confirm.title"));
        confirmacion.setHeaderText(resources.getString("profile.delete.confirm.header"));
        confirmacion.setContentText(resources.getString("profile.delete.confirm.message"));

        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == ButtonType.OK) {
            try {
                App.getServicioGeneral().eliminarCuentaActual();
                mostrarAlerta(Alert.AlertType.INFORMATION, resources.getString("profile.delete.success"));
                App.setRoot("Login");
            } catch (UsuarioNoEncontradoException e) {
                mostrarError(resources.getString("profile.error.not.found"));
            } catch (IOException e) {
                mostrarError(resources.getString("profile.error.io"));
                e.printStackTrace();
            }
        }
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void ocultarError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(resources.getString("profile.title"));
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
