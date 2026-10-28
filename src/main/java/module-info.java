module co.edu.uptc {
    requires javafx.controls;
    requires javafx.fxml;

    opens co.edu.uptc.View to javafx.fxml;
    exports co.edu.uptc.View;
    opens co.edu.uptc.ViewController to javafx.fxml;
    exports co.edu.uptc.ViewController;
}
