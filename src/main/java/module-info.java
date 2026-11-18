module co.edu.uptc {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.xml;
    
    opens co.edu.uptc.View to javafx.fxml;
    exports co.edu.uptc.View;
    opens co.edu.uptc.ViewController to javafx.fxml;
    exports co.edu.uptc.ViewController;
}
