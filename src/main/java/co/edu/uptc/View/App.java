package co.edu.uptc.View;

import co.edu.uptc.Controller.ServicioGeneral;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class App extends Application {

    private static Locale locale = Locale.forLanguageTag("es");
    private static Scene scene;
    private static ServicioGeneral servicioGeneral;
    private static String vistaActual = "Login";

    private static final List<Locale> IDIOMAS_DISPONIBLES = Arrays.asList(
            Locale.forLanguageTag("es"),
            Locale.forLanguageTag("en")
    );

    private boolean preguntarFormatoPersistencia() {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Formato de Almacenamiento");
        alerta.setHeaderText("Selecciona el archivo de persistencia");
        alerta.setContentText("¿En qué formato deseas cargar y guardar los datos en esta sesión?");

        ButtonType btnJson = new ButtonType("JSON");
        ButtonType btnXml = new ButtonType("XML");
        alerta.getButtonTypes().setAll(btnJson, btnXml);

        Optional<ButtonType> opcion = alerta.showAndWait();
        return opcion.isPresent() && opcion.get() == btnXml;
    }

    @Override
    public void start(Stage stage) throws IOException {
        cargarFuentes();
        boolean usarXml = preguntarFormatoPersistencia();
        servicioGeneral = new ServicioGeneral(usarXml);

        vistaActual = "Login";
        scene = new Scene(loadFXML("Login"), 1050, 650);
        stage.setTitle("Red de Colaboración Académica");
        stage.setScene(scene);
        stage.show();
    }

    private void cargarFuentes() {
        String[] rutas = {
            "/co/edu/uptc/fonts/DonGraffiti.otf",
            "/co/edu/uptc/fonts/PermanentMarker-Regular.ttf",
            "/co/edu/uptc/fonts/SedgwickAveDisplay-Regular.ttf",
            "/co/edu/uptc/fonts/adrip1.ttf"
        };

        for (String ruta : rutas) {
            try (InputStream fuente = App.class.getResourceAsStream(ruta)) {
                if (fuente == null) {
                    throw new IOException("No se encontro el recurso de fuente: " + ruta);
                }

                Font cargada = Font.loadFont(fuente, 10);
                if (cargada == null) {
                    throw new IOException("JavaFX no pudo leer la fuente: " + ruta);
                }
            } catch (IOException e) {
                System.err.println("Error al cargar fuente: " + e.getMessage());
            }
        }
    }

    public static void setRoot(String fxml) throws IOException {
        vistaActual = fxml;
        scene.setRoot(loadFXML(fxml));
    }

    public static void cambiarIdioma(Locale nuevoLocale) throws IOException {
        if (nuevoLocale == null || nuevoLocale.getLanguage().equalsIgnoreCase(locale.getLanguage())) {
            return;
        }
        locale = nuevoLocale;
        scene.setRoot(loadFXML(vistaActual));
    }

    public static Locale getLocale() {
        return locale;
    }

    public static Locale getLocaleActual() {
        return locale;
    }

    public static List<Locale> getIdiomasDisponibles() {
        return IDIOMAS_DISPONIBLES;
    }

    public static ResourceBundle getMessages() {
        return ResourceBundle.getBundle("co.edu.uptc.i18n.messages", locale);
    }

    public static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                App.class.getResource("/co/edu/uptc/fxml/" + fxml + ".fxml"),
                getMessages()
        );
        return fxmlLoader.load();
    }

    public static ServicioGeneral getServicioGeneral() {
        return servicioGeneral;
    }

    public static void main(String[] args) {
        launch();
    }
}