package co.edu.uptc.ViewController;

import java.io.IOException;

import co.edu.uptc.View.App;
import javafx.fxml.FXML;

public class SecondaryController {

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }
}