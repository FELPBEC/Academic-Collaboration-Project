package co.edu.uptc.ViewController;

import co.edu.uptc.Controller.ServicioReportesPDF;
import co.edu.uptc.Enum.InteresArea;
import co.edu.uptc.Enum.Program;
import co.edu.uptc.Enum.RelationType;
import co.edu.uptc.Enum.TipoOperacionSolicitud;
import co.edu.uptc.Exceptions.UsuarioNoEncontradoException;
import co.edu.uptc.Model.Arista;
import co.edu.uptc.Model.Nodo;
import co.edu.uptc.Model.User;
import co.edu.uptc.View.App;
import co.edu.uptc.View.ListaAristasView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ConexionesController implements Initializable {

    @FXML
    private ComboBox<Program> cbProgram;

    @FXML
    private ComboBox<InteresArea> cbInterestArea;

    @FXML
    private ComboBox<RelationType> cbRelationType;

    @FXML
    private StackPane contenedorLista;

    @FXML
    private Label lblSinConexiones;

    @FXML
    private Button btnReportarPdf;

    private ResourceBundle resources;
    private ListaAristasView listaAristasView;
    private ScrollPane scrollLista;

    // Estado de la última búsqueda ejecutada
    private List<Arista<String>> ultimasConexiones = new ArrayList<>();
    private Program ultimoProgram;
    private InteresArea ultimaArea;
    private RelationType ultimoTipoRelacion;
    private boolean busquedaRealizada = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;

        configurarComboEnumConTodos(cbProgram, Program.values());
        configurarComboEnumConTodos(cbInterestArea, InteresArea.values());
        configurarComboEnumConTodos(cbRelationType, RelationType.values());

        inicializarListaAristas();
        busquedaRealizada = false;
        ejecutarBusqueda();
    }

    private void inicializarListaAristas() {
        listaAristasView = new ListaAristasView();

        // Botón 1 de la tarjeta: eliminar / cancelar conexión
        listaAristasView.setOnCancelarConexion(this::confirmarYEliminarConexion);

        // Botón 2 de la tarjeta: solicitar cambio de tipo de relación
        listaAristasView.setOnModificarConexion(this::abrirFormularioModificacion);

        scrollLista = new ScrollPane(listaAristasView);
        scrollLista.setFitToWidth(true);
        scrollLista.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        contenedorLista.getChildren().add(0, scrollLista);
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

    @FXML
    private void onBuscar(ActionEvent event) {
        busquedaRealizada = true;
        ejecutarBusqueda();
    }

    private void ejecutarBusqueda() {
        Program program = cbProgram.getValue();
        InteresArea area = cbInterestArea.getValue();
        RelationType tipoRelacion = cbRelationType.getValue();

        List<Arista<String>> resultados = App.getServicioGeneral()
                .filtrarConexionesDirectas(program, area, tipoRelacion);

        boolean vacio = resultados == null || resultados.isEmpty();
        lblSinConexiones.setVisible(vacio);
        lblSinConexiones.setManaged(vacio);
        scrollLista.setVisible(!vacio);
        scrollLista.setManaged(!vacio);

        if (!vacio) {
            User actual = App.getServicioGeneral().getUsuarioActual();
            String emailActual = (actual != null) ? actual.getEmail() : "";
            listaAristasView.dibujarAristas(resultados, emailActual);
        }

        this.ultimasConexiones = !vacio ? new ArrayList<>(resultados) : new ArrayList<>();
        this.ultimoProgram = program;
        this.ultimaArea = area;
        this.ultimoTipoRelacion = tipoRelacion;

        boolean habilitarPdf = busquedaRealizada && !this.ultimasConexiones.isEmpty();
        if (btnReportarPdf != null) {
            btnReportarPdf.setVisible(habilitarPdf);
            btnReportarPdf.setManaged(habilitarPdf);
        }
    }

    @FXML
    private void onReportarPdf(ActionEvent event) {
        if (ultimasConexiones.isEmpty()) {
            return;
        }
        File archivoDestino = seleccionarDestinoPdf("reporte_conexiones.pdf");
        if (archivoDestino == null) {
            return;
        }
        try {
            Path rutaGenerada = App.getServicioGeneral().generarReporteConexionesFiltradas(
                    archivoDestino.getAbsolutePath(),
                    ultimasConexiones,
                    ultimoProgram,
                    ultimaArea,
                    ultimoTipoRelacion
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

    private void confirmarYEliminarConexion(Arista<String> arista) {
        String emailVecino = obtenerCorreoVecino(arista);
        if (emailVecino == null) {
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle(resources.getString("connections.delete.confirm.title"));
        confirmacion.setHeaderText(null);
        confirmacion.setContentText(resources.getString("connections.delete.confirm.message") + " " + emailVecino + "?");

        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == ButtonType.OK) {
            try {
                boolean eliminada = App.getServicioGeneral().eliminarRelacion(emailVecino);
                if (eliminada) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, resources.getString("connections.delete.success"));
                    ejecutarBusqueda();
                }
            } catch (UsuarioNoEncontradoException | IllegalArgumentException | IllegalStateException | IOException e) {
                mostrarAlerta(Alert.AlertType.ERROR, resources.getString("connections.delete.error"));
                e.printStackTrace();
            }
        }
    }

    private void abrirFormularioModificacion(Arista<String> arista) {
        String emailVecino = obtenerCorreoVecino(arista);
        if (emailVecino == null) {
            return;
        }

        RelationType tipoActual = (arista.getDatosRelacion() != null)
                ? arista.getDatosRelacion().getRelationType()
                : null;

        SolicitudFormController.prepararSolicitud(
                emailVecino,
                TipoOperacionSolicitud.MODIFICAR_RELACION,
                tipoActual
        );

        try {
            App.setRoot("SolicitudForm");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String obtenerCorreoVecino(Arista<String> arista) {
        User actual = App.getServicioGeneral().getUsuarioActual();
        if (actual == null || arista == null) {
            return null;
        }
        Nodo<String> nodoVecino = arista.getExtremoOpuesto(new Nodo<>(actual.getEmail()));
        return (nodoVecino != null) ? nodoVecino.getContenido() : null;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(resources.getString("connections.title"));
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarAlertaReporte(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(resources.getString("report.success.title"));
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}