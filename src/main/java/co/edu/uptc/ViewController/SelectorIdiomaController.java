package co.edu.uptc.ViewController;

import co.edu.uptc.View.App;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class SelectorIdiomaController implements Initializable {

    @FXML
    private ComboBox<Locale> cbIdioma;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbIdioma.getItems().setAll(App.getIdiomasDisponibles());

        cbIdioma.setConverter(new StringConverter<Locale>() {
            @Override
            public String toString(Locale item) {
                if (item == null) return "";
                String nombre = item.getDisplayLanguage(item);
                return nombre.substring(0, 1).toUpperCase(item) + nombre.substring(1);
            }

            @Override
            public Locale fromString(String string) {
                return null;
            }
        });

        Locale actual = App.getLocale();
        for (Locale disponible : App.getIdiomasDisponibles()) {
            if (disponible.getLanguage().equalsIgnoreCase(actual.getLanguage())) {
                cbIdioma.setValue(disponible);
                break;
            }
        }

        cbIdioma.setOnAction(event -> {
            Locale seleccionado = cbIdioma.getValue();
            if (seleccionado != null) {
                try {
                    App.cambiarIdioma(seleccionado);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}