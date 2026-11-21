package co.edu.uptc.View;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.Optional;

import co.edu.uptc.Controller.ServicioGeneral;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {
    private static Stage primaryStage;
    private static Locale locale = Locale.forLanguageTag("es");
    private static String currentView = "role-selection";
    private static Scene scene;
    
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
        boolean usarXml = preguntarFormatoPersistencia();

        // 2. Inicializar ServicioGeneral apuntando únicamente a ese formato
        ServicioGeneral servicioGeneral = new ServicioGeneral(usarXml);
        scene = new Scene(loadFXML("primary"), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    public static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                App.class.getResource("/co/edu/uptc/fxml/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}