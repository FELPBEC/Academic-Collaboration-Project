package co.edu.uptc.ViewController;

import co.edu.uptc.Controller.ServicioReportesPDF;
import co.edu.uptc.Enum.InteresArea;
import co.edu.uptc.Enum.Program;
import co.edu.uptc.Enum.RelationType;
import co.edu.uptc.Model.User;
import co.edu.uptc.View.App;
import co.edu.uptc.View.ListaUsuariosView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class RutaConexionController implements Initializable {

    @FXML
    private Label lblDestino;

    @FXML
    private ComboBox<Program> cbProgram;

    @FXML
    private ComboBox<InteresArea> cbInterestArea;

    @FXML
    private ComboBox<RelationType> cbRelationType;

    @FXML
    private StackPane contenedorCadena;

    @FXML
    private Label lblSinRuta;

    @FXML
    private Label lblFiltroRuta;

    @FXML
    private Button btnReportarPdf;

    private ResourceBundle resources;
    private ListaUsuariosView listaCadenaView;
    private ScrollPane scrollCadena;
    private User usuarioDestino;

    // Estado de la última cadena de colaboración calculada
    private List<User> cadenaEncontrada = new ArrayList<>();
    private Program filtroProgram;
    private InteresArea filtroArea;
    private RelationType filtroRelacion;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;

        configurarComboConTodos(cbProgram, Program.values());
        configurarComboConTodos(cbInterestArea, InteresArea.values());
        configurarComboConTodos(cbRelationType, RelationType.values());

        listaCadenaView = new ListaUsuariosView();
        listaCadenaView.setMostrarBotones(false);

        scrollCadena = new ScrollPane(listaCadenaView);
        scrollCadena.setFitToWidth(true);
        scrollCadena.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        contenedorCadena.getChildren().add(0, scrollCadena);
    }

    /**
     * Recibe el usuario seleccionado desde ExplorarController y calcula la ruta inicial.
     */
    public void setUsuarioDestino(User usuarioDestino) {
        this.usuarioDestino = usuarioDestino;
        if (usuarioDestino != null) {
            lblDestino.setText(resources.getString("chain.subtitle") + " " + usuarioDestino.getEmail());
            calcularYDibujarRuta();
        }
    }

    @FXML
    private void onFiltrarRuta(ActionEvent event) {
        calcularYDibujarRuta();
    }

    private void calcularYDibujarRuta() {
        if (usuarioDestino == null) {
            return;
        }

        Program program = cbProgram.getValue();
        InteresArea area = cbInterestArea.getValue();
        RelationType tipoRelacion = cbRelationType.getValue();

        List<User> cadena = App.getServicioGeneral().buscarCadenaColaboracion(
                usuarioDestino.getEmail(),
                program,
                area,
                tipoRelacion
        );

        boolean sinRuta = cadena == null || cadena.isEmpty();
        lblSinRuta.setVisible(sinRuta);
        lblSinRuta.setManaged(sinRuta);
        boolean filtrosActivos = program != null || area != null || tipoRelacion != null;
        lblFiltroRuta.setVisible(filtrosActivos && !sinRuta);
        lblFiltroRuta.setManaged(filtrosActivos && !sinRuta);
        scrollCadena.setVisible(!sinRuta);
        scrollCadena.setManaged(!sinRuta);

        if (!sinRuta) {
            listaCadenaView.dibujarUsuarios(cadena);
        }

        this.cadenaEncontrada = !sinRuta ? new ArrayList<>(cadena) : new ArrayList<>();
        this.filtroProgram = program;
        this.filtroArea = area;
        this.filtroRelacion = tipoRelacion;

        if (btnReportarPdf != null) {
            btnReportarPdf.setVisible(!sinRuta);
            btnReportarPdf.setManaged(!sinRuta);
        }
    }

    @FXML
    private void onReportarPdf(ActionEvent event) {
        if (cadenaEncontrada.isEmpty() || usuarioDestino == null) {
            return;
        }
        File archivoDestino = seleccionarDestinoPdf("reporte_cadena_colaboracion.pdf");
        if (archivoDestino == null) {
            return;
        }
        try {
            Path rutaGenerada = App.getServicioGeneral().generarReporteCadenaColaboracion(
                    archivoDestino.getAbsolutePath(),
                    usuarioDestino,
                    cadenaEncontrada,
                    filtroProgram,
                    filtroArea,
                    filtroRelacion
            );
            mostrarAlertaReporte(Alert.AlertType.INFORMATION,
                    resources.getString("report.success.message") + "\n" + rutaGenerada.toAbsolutePath());
        } catch (Exception e) {
            mostrarAlertaReporte(Alert.AlertType.ERROR, resources.getString("report.error.message"));
            e.printStackTrace();
        }
    }

    private File seleccionarDestinoPdf(String nombreSugerido) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(resources.getString("report.chooser.title"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Documentos PDF (*.pdf)", "*.pdf")
        );
        fileChooser.setInitialFileName(nombreSugerido);

        try {
            Path rutaDefecto = ServicioReportesPDF.resolverRutaSalida(nombreSugerido);
            if (rutaDefecto != null && rutaDefecto.getParent() != null) {
                File carpetaInicial = rutaDefecto.getParent().toFile();
                if (carpetaInicial.exists() && carpetaInicial.isDirectory()) {
                    fileChooser.setInitialDirectory(carpetaInicial);
                }
            }
        } catch (IOException ignored) {
        }

        return fileChooser.showSaveDialog(btnReportarPdf.getScene().getWindow());
    }

    private void mostrarAlertaReporte(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(resources.getString("report.success.title"));
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private <T extends Enum<T>> void configurarComboConTodos(ComboBox<T> combo, T[] valores) {
        combo.getItems().clear();
        combo.getItems().add(null);
        combo.getItems().addAll(valores);

        combo.setConverter(new StringConverter<T>() {
            @Override
            public String toString(T item) {
                if (item == null) {
                    return resources.getString("explore.filter.all");
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

        combo.setValue(null);
    }

    @FXML
    private void onCerrar(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}