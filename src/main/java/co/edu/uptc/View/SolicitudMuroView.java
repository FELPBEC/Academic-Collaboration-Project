package co.edu.uptc.View;

import co.edu.uptc.Enum.RelationType;
import co.edu.uptc.Enum.TipoOperacionSolicitud;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

public class SolicitudMuroView extends StackPane {

    private static final double TAMANO_FICHA = 110.0;
    private static final String RUTA_MURO = "/co/edu/uptc/images/";

    private final Map<String, Image> cacheImagenes = new HashMap<>();
    private final Pane capaMuro = new Pane();
    private final VBox contenidoFormulario = new VBox(12);

    private Label lblTitulo;
    private Label lblEmisor;
    private Label lblDestinatario;
    private ComboBox<RelationType> cbTipoRelacion;
    private TextArea txtComentario;
    private Label lblError;
    private Button btnAccionPrincipal;
    private Button btnCancelar;

    private ResourceBundle resources;
    private BiConsumer<RelationType, String> onConfirmar;
    private Runnable onCancelar;
    // Campos adicionales para modo visualización / revisión
    private Label lblValorRelacion;
    private Label lblValorComentario;
    private Button btnAceptar;
    private Button btnRechazar;
    private Button btnAtras;

    private Runnable onAceptar;
    private Runnable onRechazar;
    private Runnable onAtras;
    public SolicitudMuroView(ResourceBundle resources) {
        this.resources = resources;

        // Hace que el contenedor abrace exactamente el tamaño del formulario
        this.setMaxWidth(USE_PREF_SIZE);
        this.setMaxHeight(USE_PREF_SIZE);
        contenidoFormulario.setPrefWidth(660.0);
        contenidoFormulario.setMaxWidth(660.0);

        capaMuro.setManaged(false);

        inicializarComponentes();
        this.getChildren().addAll(capaMuro, contenidoFormulario);

        this.widthProperty().addListener((obs, oldV, newV) -> reconstruirMuro());
        this.heightProperty().addListener((obs, oldV, newV) -> reconstruirMuro());
    }

    private void inicializarComponentes() {
        contenidoFormulario.setAlignment(Pos.CENTER);
        contenidoFormulario.setPadding(new Insets(26, 48, 26, 48));

        lblTitulo = new Label();
        lblTitulo.getStyleClass().add("title");

        lblEmisor = new Label();
        lblEmisor.getStyleClass().add("general-text");

        lblDestinatario = new Label();
        lblDestinatario.getStyleClass().add("general-text");

        VBox boxCabecera = new VBox(2, lblEmisor, lblDestinatario);
        boxCabecera.setAlignment(Pos.CENTER_LEFT);

        // Selector de RelationType
        Label lblRelacion = new Label(resources.getString("request.relation.label"));
        lblRelacion.getStyleClass().add("general-text");

        cbTipoRelacion = new ComboBox<>();
        cbTipoRelacion.getStyleClass().add("combobox");
        cbTipoRelacion.setMaxWidth(Double.MAX_VALUE);
        cbTipoRelacion.setPromptText(resources.getString("request.relation.prompt"));
        cargarComboRelaciones();

        VBox boxRelacion = new VBox(4, lblRelacion, cbTipoRelacion);

        // Campo de comentario
        Label lblComentario = new Label(resources.getString("request.comment.label"));
        lblComentario.getStyleClass().add("general-text");

        txtComentario = new TextArea();
        txtComentario.getStyleClass().add("input");
        txtComentario.setPromptText(resources.getString("request.comment.prompt"));
        txtComentario.setPrefRowCount(3);
        txtComentario.setWrapText(true);

        VBox boxComentario = new VBox(4, lblComentario, txtComentario);

        // Label de error
        lblError = new Label();
        lblError.getStyleClass().add("label-errores");
        lblError.setVisible(false);
        lblError.setManaged(false);
        lblError.setWrapText(true);

        // Botones inferiores con estilo estrella
        btnAccionPrincipal = new Button();
        btnAccionPrincipal.getStyleClass().add("boton-estrella");
        btnAccionPrincipal.setDefaultButton(true);
        btnAccionPrincipal.setOnAction(e -> validarYConfirmar());

        btnCancelar = new Button(resources.getString("request.button.cancel"));
        btnCancelar.getStyleClass().add("boton-estrella");
        btnCancelar.setCancelButton(true);
        btnCancelar.setOnAction(e -> {
            if (onCancelar != null) {
                onCancelar.run();
            }
        });

        HBox boxBotones = new HBox(18, btnCancelar, btnAccionPrincipal);
        boxBotones.setAlignment(Pos.CENTER);
        boxBotones.setPadding(new Insets(6, 0, 0, 0));

        contenidoFormulario.getChildren().addAll(
                lblTitulo,
                boxCabecera,
                boxRelacion,
                boxComentario,
                lblError,
                boxBotones
        );
    }

    /**
     * Configura y dibuja la solicitud con los datos del emisor, destinatario y tipo de operación.
     */
    public void dibujarSolicitud(String usuarioActual, String destinatario, TipoOperacionSolicitud tipoOperacion) {
        ocultarError();

        lblEmisor.setText(resources.getString("request.from.label") + " " + usuarioActual);
        lblDestinatario.setText(resources.getString("request.to.label") + " " + destinatario);

        if (tipoOperacion == TipoOperacionSolicitud.MODIFICAR_RELACION) {
            lblTitulo.setText(resources.getString("request.title.modify"));
            btnAccionPrincipal.setText(resources.getString("request.button.modify"));
        } else {
            lblTitulo.setText(resources.getString("request.title.create"));
            btnAccionPrincipal.setText(resources.getString("request.button.send"));
        }

        reconstruirMuro();
    }

    /**
     * Construye el mosaico de 12 piezas del muro:
     * - Franja vertical: "sup" (fila 0), "med" (filas intermedias) o "inf" (última fila).
     * - Fila par arranca en tipo 1; fila impar arranca en tipo 2.
     * - Alterna 1 <-> 2 en cada columna y en la última columna invierte horizontalmente
     *   la pieza lateral opuesta a la ficha anterior.
     */
    private void reconstruirMuro() {
        capaMuro.getChildren().clear();

        double ancho = getWidth() > 0 ? getWidth() : getPrefWidth();
        double alto = getHeight() > 0 ? getHeight() : getMinHeight();

        int columnas = Math.max(2, (int) Math.ceil(ancho / TAMANO_FICHA));
        int filas = Math.max(2, (int) Math.ceil(alto / TAMANO_FICHA));

        double anchoFicha = ancho / columnas;
        double altoFicha = alto / filas;

        for (int fila = 0; fila < filas; fila++) {
            String franja = obtenerFranjaVertical(fila, filas);

            // Fila 0 arranca en 1; fila 1 arranca en 2; fila 2 arranca en 1, etc.
            int tipoActual = (fila % 2 == 0) ? 1 : 2;

            double y = Math.round(fila * altoFicha);
            double altoExacto = Math.round((fila + 1) * altoFicha) - y;

            for (int col = 0; col < columnas; col++) {
                boolean esPrimeraCol = (col == 0);
                boolean esUltimaCol = (col == columnas - 1);

                if (!esPrimeraCol) {
                    // Alterna respecto a la pieza inmediatamente anterior (1 -> 2, 2 -> 1)
                    tipoActual = (tipoActual == 1) ? 2 : 1;
                }

                boolean esBordeLateral = esPrimeraCol || esUltimaCol;
                String nombreArchivo = resolverNombrePieza(franja, esBordeLateral, tipoActual);
                Image img = obtenerImagen(nombreArchivo);

                if (img != null) {
                    double x = Math.round(col * anchoFicha);
                    double anchoExacto = Math.round((col + 1) * anchoFicha) - x;

                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(anchoExacto);
                    iv.setFitHeight(altoExacto);
                    iv.setPreserveRatio(false);
                    iv.setLayoutX(x);
                    iv.setLayoutY(y);

                    // La última columna usa la pieza lateral opuesta invertida en espejo
                    if (esUltimaCol) {
                        iv.setScaleX(-1);
                    }

                    capaMuro.getChildren().add(iv);
                }
            }
        }
    }

    private String obtenerFranjaVertical(int fila, int totalFilas) {
        if (fila == 0) {
            return "sup";
        }
        if (fila == totalFilas - 1) {
            return "inf";
        }
        return "med";
    }

    private String resolverNombrePieza(String franja, boolean esBordeLateral, int tipo) {
        String posicion = esBordeLateral ? "esq" : "cen";
        return "muro_" + franja + "_" + posicion + "_" + tipo + ".png";
    }

    private Image obtenerImagen(String nombreArchivo) {
        if (cacheImagenes.containsKey(nombreArchivo)) {
            return cacheImagenes.get(nombreArchivo);
        }
        String rutaCompleta = RUTA_MURO + nombreArchivo;
        InputStream stream = getClass().getResourceAsStream(rutaCompleta);
        if (stream == null) {
            System.err.println("No se encontró la pieza del muro: " + rutaCompleta);
            return null;
        }
        Image img = new Image(stream);
        cacheImagenes.put(nombreArchivo, img);
        return img;
    }
    private void cargarComboRelaciones() {
        cbTipoRelacion.getItems().setAll(RelationType.values());
        cbTipoRelacion.setConverter(new StringConverter<RelationType>() {
            @Override
            public String toString(RelationType item) {
                if (item == null) return "";
                String clave = "enum." + item.name();
                if (resources.containsKey(clave)) {
                    return resources.getString(clave);
                }
                return item.name().replaceAll("([a-z])([A-Z])", "$1 $2");
            }

            @Override
            public RelationType fromString(String string) {
                return null;
            }
        });
    }

    private void validarYConfirmar() {
        ocultarError();
        RelationType tipoRelacion = cbTipoRelacion.getValue();
        String comentario = txtComentario.getText() != null ? txtComentario.getText().trim() : "";

        if (tipoRelacion == null || comentario.isEmpty()) {
            mostrarError(resources.getString("request.error.empty"));
            return;
        }

        if (onConfirmar != null) {
            onConfirmar.accept(tipoRelacion, comentario);
        }
    }

    public void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    public void ocultarError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }

    public void setDatosIniciales(RelationType relacionActual, String comentarioActual) {
        if (relacionActual != null) {
            cbTipoRelacion.setValue(relacionActual);
        }
        if (comentarioActual != null) {
            txtComentario.setText(comentarioActual);
        }
    }
    /**
     * Dibuja la tarjeta de muro en modo lectura para revisar una solicitud recibida
     * y decidir si aceptarla, rechazarla o volver atrás.
     */
    public void dibujarDetalleSolicitud(String emisor,
                                        String destinatario,
                                        TipoOperacionSolicitud tipoOperacion,
                                        RelationType tipoRelacion,
                                        RelationType tipoAnterior,
                                        String comentario) {
        contenidoFormulario.getChildren().clear();
        ocultarError();

        boolean esModificacion = (tipoOperacion == TipoOperacionSolicitud.MODIFICAR_RELACION);
        lblTitulo.setText(resources.getString(
                esModificacion ? "request.detail.title.modify" : "request.detail.title.create"
        ));

        lblEmisor.setText(resources.getString("request.from.label") + " " + emisor);
        lblDestinatario.setText(resources.getString("request.to.label") + " " + destinatario);
        VBox boxCabecera = new VBox(2, lblEmisor, lblDestinatario);
        boxCabecera.setAlignment(Pos.CENTER_LEFT);

        // Tipo de relación solicitada (solo lectura)
        Label lblRelacionTitulo = new Label(resources.getString("request.relation.label"));
        lblRelacionTitulo.getStyleClass().add("general-text");

        String textoRelacion = traducirEnum(tipoRelacion);
        if (esModificacion && tipoAnterior != null) {
            textoRelacion = traducirEnum(tipoAnterior) + "  →  " + textoRelacion;
        }

        lblValorRelacion = new Label(textoRelacion);
        lblValorRelacion.getStyleClass().add("input");
        lblValorRelacion.setMaxWidth(Double.MAX_VALUE);
        lblValorRelacion.setWrapText(true);
        VBox boxRelacion = new VBox(4, lblRelacionTitulo, lblValorRelacion);

        // Comentario recibido (solo lectura)
        Label lblComentarioTitulo = new Label(resources.getString("request.comment.label"));
        lblComentarioTitulo.getStyleClass().add("general-text");

        lblValorComentario = new Label(
                (comentario != null && !comentario.isBlank()) ? comentario : "-"
        );
        lblValorComentario.getStyleClass().add("input");
        lblValorComentario.setMaxWidth(Double.MAX_VALUE);
        lblValorComentario.setMinHeight(72.0);
        lblValorComentario.setAlignment(Pos.TOP_LEFT);
        lblValorComentario.setPadding(new Insets(8, 12, 8, 12));
        lblValorComentario.setWrapText(true);
        VBox boxComentario = new VBox(4, lblComentarioTitulo, lblValorComentario);

        // Botones de decisión con estilo estrella
        btnAtras = new Button(resources.getString("request.button.back"));
        btnAtras.getStyleClass().add("boton-estrella");
        btnAtras.setCancelButton(true);
        btnAtras.setOnAction(e -> {
            if (onAtras != null) onAtras.run();
        });

        btnRechazar = new Button(resources.getString("request.button.reject"));
        btnRechazar.getStyleClass().add("boton-estrella");
        btnRechazar.setOnAction(e -> {
            if (onRechazar != null) onRechazar.run();
        });

        btnAceptar = new Button(resources.getString("request.button.accept"));
        btnAceptar.getStyleClass().add("boton-estrella");
        btnAceptar.setDefaultButton(true);
        btnAceptar.setOnAction(e -> {
            if (onAceptar != null) onAceptar.run();
        });

        HBox boxBotones = new HBox(14, btnAtras, btnRechazar, btnAceptar);
        boxBotones.setAlignment(Pos.CENTER);
        boxBotones.setPadding(new Insets(6, 0, 0, 0));

        contenidoFormulario.getChildren().addAll(
                lblTitulo,
                boxCabecera,
                boxRelacion,
                boxComentario,
                lblError,
                boxBotones
        );

        reconstruirMuro();
    }

    private String traducirEnum(Enum<?> valor) {
        if (valor == null) return "";
        String clave = "enum." + valor.name();
        if (resources.containsKey(clave)) {
            return resources.getString(clave);
        }
        return valor.name().replaceAll("([a-z])([A-Z])", "$1 $2");
    }

    public void setOnAceptar(Runnable onAceptar) {
        this.onAceptar = onAceptar;
    }

    public void setOnRechazar(Runnable onRechazar) {
        this.onRechazar = onRechazar;
    }

    public void setOnAtras(Runnable onAtras) {
        this.onAtras = onAtras;
    }
    public void setOnConfirmar(BiConsumer<RelationType, String> onConfirmar) {
        this.onConfirmar = onConfirmar;
    }

    public void setOnCancelar(Runnable onCancelar) {
        this.onCancelar = onCancelar;
    }
}
