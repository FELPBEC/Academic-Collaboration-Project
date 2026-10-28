package co.edu.uptc.ViewController;

import java.io.IOException;

import co.edu.uptc.View.App;
import javafx.fxml.FXML;

public class PrimaryController {

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
}
