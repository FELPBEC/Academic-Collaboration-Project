package co.edu.uptc.ViewController;

import co.edu.uptc.Controller.ServicioReportesPDF;
import co.edu.uptc.Enum.InteresArea;
import co.edu.uptc.Enum.Program;
import co.edu.uptc.Enum.TipoOperacionSolicitud;
import co.edu.uptc.Enum.TypeUser;
import co.edu.uptc.Model.User;
import co.edu.uptc.View.App;
import co.edu.uptc.View.ListaUsuariosView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ExplorarController implements Initializable {

    private enum FiltroGrupoTipo {
        TODOS,
        PERSONAS,
        GRUPOS
    }

    @FXML
    private ComboBox<Program> cbProgram;

    @FXML
    private ComboBox<InteresArea> cbInterestArea;

    @FXML
    private ComboBox<FiltroGrupoTipo> cbTipoParticipante;

    @FXML
    private ToggleButton tglRelevantes;

    @FXML
    private StackPane contenedorLista;

    @FXML
    private Label lblSinResultados;

    @FXML
    private Button btnReportarPdf;

    private ScrollPane scrollLista;
    private ResourceBundle resources;
    private ListaUsuariosView listaUsuariosView;

    // Estado de la última búsqueda ejecutada
    private List<User> ultimosResultados = new ArrayList<>();
    private Program ultimoProgram;
    private InteresArea ultimaArea;
    private int ultimaPuntuacionMinima;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;

        configurarComboEnumConTodos(cbProgram, Program.values());
        configurarComboEnumConTodos(cbInterestArea, InteresArea.values());
        configurarComboTipoParticipante();

        inicializarListaUsuarios();

        // Carga inicial sin mostrar todavía el botón de reporte PDF
        ejecutarBusqueda(false);
    }

    private void inicializarListaUsuarios() {
        listaUsuariosView = new ListaUsuariosView();

        // Abre la ventana emergente con la cadena de conexión (Dijkstra)
        listaUsuariosView.setOnVerConexion(this::abrirVentanaRutaConexion);

        // Redirige al formulario de solicitud de nueva relación
        listaUsuariosView.setOnEnviarSolicitud(usuarioSeleccionado -> {
            SolicitudFormController.prepararSolicitud(
                    usuarioSeleccionado.getEmail(),
                    TipoOperacionSolicitud.CREAR_RELACION,
                    null
            );
            try {
                App.setRoot("SolicitudForm");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        scrollLista = new ScrollPane(listaUsuariosView);
        scrollLista.setFitToWidth(true);
        scrollLista.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        contenedorLista.getChildren().add(0, scrollLista);
    }

    private void abrirVentanaRutaConexion(User usuarioDestino) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/co/edu/uptc/fxml/RutaConexion.fxml"),
                    resources
            );
            Parent root = loader.load();

            RutaConexionController controller = loader.getController();
            controller.setUsuarioDestino(usuarioDestino);

            Stage modalStage = new Stage();
            modalStage.setTitle(resources.getString("chain.title"));
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(contenedorLista.getScene().getWindow());
            modalStage.setScene(new Scene(root));
            modalStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private <T extends Enum<T>> void configurarComboEnumConTodos(ComboBox<T> combo, T[] valores) {
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

    private void configurarComboTipoParticipante() {
        cbTipoParticipante.getItems().setAll(FiltroGrupoTipo.values());
        cbTipoParticipante.setConverter(new StringConverter<FiltroGrupoTipo>() {
            @Override
            public String toString(FiltroGrupoTipo filtro) {
                if (filtro == null || filtro == FiltroGrupoTipo.TODOS) {
                    return resources.getString("explore.filter.all");
                }
                if (filtro == FiltroGrupoTipo.PERSONAS) {
                    return resources.getString("explore.filter.type.people");
                }
                return resources.getString("explore.filter.type.groups");
            }

            @Override
            public FiltroGrupoTipo fromString(String string) {
                return null;
            }
        });
        cbTipoParticipante.setValue(FiltroGrupoTipo.TODOS);
    }

    @FXML
    private void onBuscar(ActionEvent event) {
        ejecutarBusqueda(true);
    }

    private void ejecutarBusqueda(boolean mostrarBotonPdf) {
        boolean soloRelevantes = tglRelevantes.isSelected();
        Program program = cbProgram.getValue();
        InteresArea area = cbInterestArea.getValue();
        TypeUser[] tipos = obtenerTiposSeleccionados();

        List<User> resultados = App.getServicioGeneral()
                .explorarUsuarios(soloRelevantes, program, area, tipos);

        boolean vacio = resultados == null || resultados.isEmpty();

        lblSinResultados.setVisible(vacio);
        lblSinResultados.setManaged(vacio);
        scrollLista.setVisible(!vacio);
        scrollLista.setManaged(!vacio);

        if (!vacio) {
            listaUsuariosView.dibujarUsuarios(resultados);
        }

        this.ultimosResultados = !vacio ? new ArrayList<>(resultados) : new ArrayList<>();
        this.ultimoProgram = program;
        this.ultimaArea = area;
        this.ultimaPuntuacionMinima = soloRelevantes ? 1 : 0;

        boolean habilitarPdf = mostrarBotonPdf && !this.ultimosResultados.isEmpty();
        if (btnReportarPdf != null) {
            btnReportarPdf.setVisible(habilitarPdf);
            btnReportarPdf.setManaged(habilitarPdf);
        }
    }

    private TypeUser[] obtenerTiposSeleccionados() {
        FiltroGrupoTipo seleccion = cbTipoParticipante.getValue();
        if (seleccion == FiltroGrupoTipo.PERSONAS) {
            return new TypeUser[]{TypeUser.Estudents, TypeUser.Teachers};
        }
        if (seleccion == FiltroGrupoTipo.GRUPOS) {
            return new TypeUser[]{TypeUser.InvestigationGroup, TypeUser.AcademicGroup};
        }
        return new TypeUser[0];
    }

    @FXML
    private void onReportarPdf(ActionEvent event) {
        if (ultimosResultados.isEmpty()) {
            return;
        }
        File archivoDestino = seleccionarDestinoPdf("reporte_exploracion.pdf");
        if (archivoDestino == null) {
            return;
        }
        try {
            Path rutaGenerada = App.getServicioGeneral().generarReporteBusquedaUsuarios(
                    archivoDestino.getAbsolutePath(),
                    ultimosResultados,
                    ultimoProgram,
                    ultimaArea,
                    ultimaPuntuacionMinima
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
}