package co.edu.uptc.View;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import co.edu.uptc.Enum.RelationType;
import co.edu.uptc.Enum.TypeUser;
import co.edu.uptc.Model.Arista;
import co.edu.uptc.Model.Grafo;
import co.edu.uptc.Model.Nodo;
import co.edu.uptc.Model.User;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Componente JavaFX reutilizable para representar gráficamente un grafo académico.
 */
public class GrafoView extends Pane {

    private static final double NODO_TAMANO_BASE = 44.0;
    private static final double SEPARACION_BORDE = 80.0;
    private static final double FACTOR_NODO_ACTUAL = 1.55;
    private static final double ANCHO_MINIMO_NODOS = 850.0;
    private static final double ALTO_MINIMO_NODOS = 600.0;
    private static final double ESPACIADO_HORIZONTAL_NODO = 190.0;
    private static final double ESPACIADO_VERTICAL_NODO = 160.0;

    private static final String SVG_ESTUDIANTE =
            "m 660.63474,26.682827 -21.16822,10.077421 9.77098,1.648995 "
            + "v 10.574031 h 0.10904 "
            + "c -0.0698,0.461256 -0.10904,0.930992 -0.10904,1.409216 0,3.7408 2.15496,7.89873 5.43791,10.37818 "
            + "v 3.08198 a 24.152512,19.27228 0 0 0 -5.21777,1.76062 24.152512,19.27228 0 0 0 -12.19512,16.74419 "
            + "h 24.15253 l 24.14943,0.32815 "
            + "A 24.152512,19.27228 0 0 0 673.72799,65.7776 24.152512,19.27228 0 0 0 668.63787,63.97047 "
            + "v -4.36356 c 2.58469,-2.49982 4.24263,-6.01019 4.24263,-9.21442 0,-0.480894 -0.0395,-0.953242 -0.11007,-1.416967 "
            + "v -11.56777 l 10.23503,-2.640665 z";

    private static final String SVG_PROFESOR =
            "M 749.80797 28.955338 "
            + "C 743.57898 29.051057 737.41259 32.131051 738.22935 38.409025 "
            + "L 738.22935 48.983056 L 738.33838 48.983056 "
            + "C 738.26858 49.444309 738.22935 49.914051 738.22935 50.392272 "
            + "C 738.22935 54.13306 740.38431 58.291006 743.66726 60.77045 "
            + "L 743.66726 63.852432 "
            + "C 741.84747 64.273949 740.09617 64.864885 738.44949 65.613048 "
            + "C 730.90999 69.041074 726.25459 75.433043 726.25437 82.357246 "
            + "L 750.4069 82.357246 L 774.55633 82.685391 "
            + "C 774.70402 75.761919 770.18548 69.307355 762.71984 65.777379 "
            + "C 761.11617 65.020352 759.4081 64.413942 757.62971 63.970255 "
            + "L 757.62971 59.606697 "
            + "C 760.2144 57.106883 761.87235 53.596493 761.87235 50.392272 "
            + "C 761.87235 49.911381 761.83285 49.439029 761.76228 48.975304 "
            + "L 761.76228 37.407535 "
            + "C 762.32858 31.748174 756.03694 28.859619 749.80797 28.955338 z "
            + "M 739.24531 41.313757 L 748.90415 41.313757 L 747.75693 48.294726 "
            + "L 741.06225 48.58153 L 739.24531 41.313757 z "
            + "M 750.87664 41.313757 L 760.53548 41.313757 L 758.71854 48.58153 "
            + "L 752.02385 48.294726 L 750.87664 41.313757 z "
            + "M 748.04374 65.317459 L 753.68577 65.317459 L 752.80675 69.670165 "
            + "L 755.33941 81.785704 L 746.76009 81.774852 L 749.32841 69.670165 "
            + "L 748.04374 65.317459 z";

    private static final String SVG_GRUPO_ACADEMICO =
            "m 617.80322,101.16394 "
            + "c 7.94844,3.38424 29.85767,2.90214 34.76223,14.33606 9.38635,-14.60981 17.93401,-11.94397 27.99187,-13.79508 "
            + "l 10.81967,40.57377 "
            + "c -16.62854,-4.92917 -26.0334,-6.00338 -35.70196,4.4546 -1.5027,1.62539 -3.41047,-0.39855 -5.51497,0.37966 "
            + "-8.98233,-6.55054 -18.23516,-7.9041 -27.21749,-4.56377 z";

    private static final String SVG_GRUPO_INVESTIGACION =
            "m 739.93054,93.319884 a 23.938524,23.938524 0 0 0 -23.93859,23.938076 "
            + "23.938524,23.938524 0 0 0 23.93859,23.93859 23.938524,23.938524 0 0 0 14.67455,-5.09685 "
            + "l -0.0548,0.092 c -0.6909,1.15927 -0.29913,2.62333 0.8785,3.28248 l 1.88981,1.0573 "
            + "c 1.17764,0.65916 2.682,0.2565 3.37292,-0.90279 l 1.21233,-2.03398 27.45776,14.22084 "
            + "c 1.08507,0.56196 2.41098,0.14095 2.97294,-0.94413 l 4.16616,-8.04447 "
            + "c 0.56195,-1.08508 0.14094,-2.41098 -0.94413,-2.97295 l -26.74462,-13.85083 0.85886,-1.44074 "
            + "c 0.69092,-1.15929 0.29914,-2.62333 -0.8785,-3.28249 l -1.88929,-1.0573 "
            + "c -1.17764,-0.65915 -2.682,-0.257 -3.37292,0.90227 l -0.008,0.0129 "
            + "a 23.938524,23.938524 0 0 0 0.34623,-3.87986 23.938524,23.938524 0 0 0 -23.93807,-23.938076 z "
            + "m -0.39843,6.515364 a 16.550301,16.550301 0 0 1 16.55041,16.550412 "
            + "16.550301,16.550301 0 0 1 -16.55041,16.55041 16.550301,16.550301 0 0 1 -16.55041,-16.55041 "
            + "16.550301,16.550301 0 0 1 16.55041,-16.550412 z";

    private Grafo<String> grafoActual;
    private Map<String, User> usuariosActuales;
    private String identificadorUsuarioActual;

    public GrafoView() {
        setPrefSize(ANCHO_MINIMO_NODOS, ALTO_MINIMO_NODOS);
        setStyle("-fx-background-color: #fafafa;");
        widthProperty().addListener((observable, anterior, actual) -> redibujar());
        heightProperty().addListener((observable, anterior, actual) -> redibujar());
    }

    public void dibujarGrafo(Grafo<String> grafo, Map<String, User> usuarios) {
        this.grafoActual = grafo;
        this.usuariosActuales = usuarios != null ? usuarios : new HashMap<>();
        ajustarTamañoContenido();
        redibujar();
    }

    public void ajustarTamañoContenido() {
        if (grafoActual == null) {
            return;
        }

        int cantidadNodos = grafoActual.obtenerTodosLosNodos().size();
        if (cantidadNodos <= 6) {
            setPrefSize(ANCHO_MINIMO_NODOS, ALTO_MINIMO_NODOS);
            return;
        }

        int columnas = (int) Math.ceil(Math.sqrt(cantidadNodos));
        int filas = (int) Math.ceil((double) cantidadNodos / columnas);
        double ancho = Math.max(ANCHO_MINIMO_NODOS, columnas * ESPACIADO_HORIZONTAL_NODO);
        double alto = Math.max(ALTO_MINIMO_NODOS, filas * ESPACIADO_VERTICAL_NODO);
        setPrefSize(ancho, alto);
    }

    public void setUsuarioActual(String identificadorUsuario) {
        this.identificadorUsuarioActual = identificadorUsuario;
        redibujar();
    }

    public String getUsuarioActual() {
        return identificadorUsuarioActual;
    }

    public void limpiar() {
        grafoActual = null;
        usuariosActuales = null;
        identificadorUsuarioActual = null;
        getChildren().clear();
        setPrefSize(ANCHO_MINIMO_NODOS, ALTO_MINIMO_NODOS);
    }

    public void redibujar() {
        getChildren().clear();
        if (grafoActual == null) {
            return;
        }

        List<Nodo<String>> nodos = grafoActual.obtenerTodosLosNodos();
        nodos.sort((nodo1, nodo2) -> textoNodo(nodo1).compareToIgnoreCase(textoNodo(nodo2)));

        Map<String, Punto> posiciones = calcularPosiciones(nodos);

        Set<Arista<String>> aristasDibujadas = new HashSet<>();
        for (Nodo<String> nodo : nodos) {
            for (Arista<String> arista : grafoActual.obtenerAristas(nodo)) {
                if (aristasDibujadas.add(arista)) {
                    dibujarArista(arista, posiciones);
                }
            }
        }

        for (Nodo<String> nodo : nodos) {
            dibujarNodo(nodo, posiciones.get(nodo.getContenido()));
        }
    }

    private Map<String, Punto> calcularPosiciones(List<Nodo<String>> nodos) {
        Map<String, Punto> posiciones = new HashMap<>();
        if (nodos.isEmpty()) {
            return posiciones;
        }

        double ancho = getWidth() > 0 ? getWidth() : getPrefWidth();
        double alto = getHeight() > 0 ? getHeight() : getPrefHeight();
        if (nodos.size() == 1) {
            posiciones.put(nodos.get(0).getContenido(), new Punto(ancho / 2.0, alto / 2.0));
            return posiciones;
        }

        double centroX = ancho / 2.0;
        double centroY = alto / 2.0;
        double radioX = Math.max(SEPARACION_BORDE, ancho / 2.0 - SEPARACION_BORDE);
        double radioY = Math.max(SEPARACION_BORDE, alto / 2.0 - SEPARACION_BORDE);

        for (int i = 0; i < nodos.size(); i++) {
            double angulo = 2.0 * Math.PI * i / nodos.size() - Math.PI / 2.0;
            double x = centroX + radioX * Math.cos(angulo);
            double y = centroY + radioY * Math.sin(angulo);
            posiciones.put(nodos.get(i).getContenido(), new Punto(x, y));
        }
        return posiciones;
    }

    private void dibujarNodo(Nodo<String> nodo, Punto punto) {
        if (punto == null) {
            return;
        }

        User usuario = usuariosActuales.get(nodo.getContenido());
        TypeUser tipoUsuario = usuario != null ? usuario.getTypeUser() : null;
        boolean usuarioActual = nodo.getContenido() != null
                && nodo.getContenido().equals(identificadorUsuarioActual);

        Shape forma = crearForma(tipoUsuario, usuarioActual);
        forma.setFill(getNodeColor(tipoUsuario));
        forma.setTranslateX(punto.x);
        forma.setTranslateY(punto.y);

        String titulo = textoUsuario(usuario, nodo.getContenido());
        if (usuarioActual) {
            titulo = "★ " + titulo;
        }

        Text etiqueta = new Text(titulo);
        etiqueta.setFill(usuarioActual ? Color.DARKRED : Color.BLACK);
        etiqueta.setStyle(usuarioActual
                ? "-fx-font-size: 13px; -fx-font-weight: bold;"
                : "-fx-font-size: 11px;");
        etiqueta.setTranslateX(punto.x - etiqueta.getLayoutBounds().getWidth() / 2.0);
        etiqueta.setTranslateY(punto.y + obtenerRadioForma(usuarioActual) + 16.0);

        Group grupo = new Group(forma, etiqueta);
        Tooltip tooltip = crearTooltipUsuario(usuario, nodo.getContenido());
        if (usuarioActual) {
            tooltip.setText("USUARIO ACTUAL\n" + tooltip.getText());
        }
        Tooltip.install(grupo, tooltip);
        getChildren().add(grupo);
    }

    private void dibujarArista(Arista<String> arista, Map<String, Punto> posiciones) {
        Punto origen = posiciones.get(arista.getNodoInicial().getContenido());
        Punto destino = posiciones.get(arista.getNodoFinal().getContenido());
        if (origen == null || destino == null) {
            return;
        }

        RelationType tipoRelacion = arista.getDatosRelacion() != null
                ? arista.getDatosRelacion().getRelationType()
                : null;
        Color color = getEdgeColor(tipoRelacion);

        Line linea = new Line(origen.x, origen.y, destino.x, destino.y);
        linea.setStroke(color);
        linea.setStrokeWidth(3.0);

        String textoRelacion = tipoRelacion != null ? tipoRelacion.name() : "Sin tipo";
        Label etiqueta = new Label(textoRelacion);
        etiqueta.setAlignment(Pos.CENTER);
        etiqueta.setStyle("-fx-background-color: white; -fx-border-color: "
                + toCssColor(color) + "; -fx-padding: 2px 5px;");
        etiqueta.setLayoutX((origen.x + destino.x) / 2.0 - 45.0);
        etiqueta.setLayoutY((origen.y + destino.y) / 2.0 - 12.0);

        Tooltip tooltip = new Tooltip("Relacion: " + textoRelacion
                + "\nPeso: " + String.format(java.util.Locale.US, "%.2f", arista.getPeso()));
        tooltip.setShowDelay(Duration.millis(300));
        tooltip.setHideDelay(Duration.millis(200));
        Tooltip.install(linea, tooltip);
        Tooltip.install(etiqueta, tooltip);

        getChildren().addAll(linea, etiqueta);
    }

    public Shape crearForma(TypeUser tipoUsuario, boolean esUsuarioActual) {
        String pathData = obtenerSvgPathPorTipo(tipoUsuario);
        double tamanoObjetivo = esUsuarioActual
                ? NODO_TAMANO_BASE * FACTOR_NODO_ACTUAL
                : NODO_TAMANO_BASE;

        if (pathData == null || pathData.isBlank()) {
            Circle respaldo = new Circle(0, 0, tamanoObjetivo / 2.0);
            respaldo.setStroke(esUsuarioActual ? Color.CRIMSON : Color.DARKSLATEGRAY);
            respaldo.setStrokeWidth(esUsuarioActual ? 3.0 : 1.5);
            return respaldo;
        }

        SVGPath svg = new SVGPath();
        svg.setFillRule(FillRule.EVEN_ODD);
        svg.setContent(pathData);

        Bounds bounds = svg.getBoundsInLocal();
        double maxLado = Math.max(bounds.getWidth(), bounds.getHeight());
        double escala = (maxLado > 0) ? (tamanoObjetivo / maxLado) : 1.0;

        double centroX = bounds.getMinX() + bounds.getWidth() / 2.0;
        double centroY = bounds.getMinY() + bounds.getHeight() / 2.0;
        svg.setLayoutX(-centroX);
        svg.setLayoutY(-centroY);

        svg.setScaleX(escala);
        svg.setScaleY(escala);

        if (esUsuarioActual) {
            svg.setStroke(Color.CRIMSON);
            svg.setStrokeWidth(2.8 / escala);
            svg.setEffect(new DropShadow(12.0, Color.web("#6A1B9A")));
        } else {
            svg.setStroke(Color.DARKSLATEGRAY);
            svg.setStrokeWidth(1.5 / escala);
        }

        return svg;
    }

    private String obtenerSvgPathPorTipo(TypeUser tipoUsuario) {
        if (tipoUsuario == TypeUser.Teachers) {
            return SVG_PROFESOR;
        }
        if (tipoUsuario == TypeUser.InvestigationGroup) {
            return SVG_GRUPO_INVESTIGACION;
        }
        if (tipoUsuario == TypeUser.AcademicGroup) {
            return SVG_GRUPO_ACADEMICO;
        }
        return SVG_ESTUDIANTE;
    }

    private double obtenerRadioForma(boolean esUsuarioActual) {
        double tamano = esUsuarioActual
                ? NODO_TAMANO_BASE * FACTOR_NODO_ACTUAL
                : NODO_TAMANO_BASE;
        return tamano / 2.0;
    }

    private Tooltip crearTooltipUsuario(User usuario, String identificador) {
        String nombre = usuario != null && usuario.getName() != null
                ? usuario.getName()
                : identificador;
        String tipo = usuario != null && usuario.getTypeUser() != null
                ? usuario.getTypeUser().name()
                : "No especificado";
        String programa = usuario != null && usuario.getProgram() != null
                ? usuario.getProgram().getDisplayName()
                : "No especificado";
        String area = usuario != null && usuario.getInteresArea() != null
                ? usuario.getInteresArea().name()
                : "No especificada";

        Tooltip tooltip = new Tooltip("Nombre: " + nombre
                + "\nTipo: " + tipo
                + "\nPrograma: " + programa
                + "\nArea de interes: " + area);
        tooltip.setShowDelay(Duration.millis(300));
        tooltip.setHideDelay(Duration.millis(200));
        return tooltip;
    }

    private String textoUsuario(User usuario, String identificador) {
        if (usuario != null && usuario.getName() != null && !usuario.getName().trim().isEmpty()) {
            return usuario.getName();
        }
        if (usuario != null && usuario.getTypeUser() != null) {
            return usuario.getTypeUser().name();
        }
        return identificador != null ? identificador : "Nodo";
    }

    private String textoNodo(Nodo<String> nodo) {
        if (nodo == null || nodo.getContenido() == null) {
            return "";
        }
        User usuario = usuariosActuales != null ? usuariosActuales.get(nodo.getContenido()) : null;
        return textoUsuario(usuario, nodo.getContenido());
    }

    public static Color getNodeColor(TypeUser tipoUsuario) {
        if (tipoUsuario == TypeUser.Teachers) {
            return Color.CORNFLOWERBLUE;
        }
        if (tipoUsuario == TypeUser.InvestigationGroup) {
            return Color.MEDIUMPURPLE;
        }
        if (tipoUsuario == TypeUser.AcademicGroup) {
            return Color.GOLDENROD;
        }
        return Color.LIGHTSEAGREEN;
    }

    public static Color getEdgeColor(RelationType tipoRelacion) {
        if (tipoRelacion == RelationType.InvestigationCollaboration) {
            return Color.DODGERBLUE;
        }
        if (tipoRelacion == RelationType.Mentoring) {
            return Color.ORANGE;
        }
        if (tipoRelacion == RelationType.JointParticipation) {
            return Color.MEDIUMSEAGREEN;
        }
        return Color.DARKGRAY;
    }

    private String toCssColor(Color color) {
        return String.format(java.util.Locale.US,
                "#%02X%02X%02X",
                (int) Math.round(color.getRed() * 255),
                (int) Math.round(color.getGreen() * 255),
                (int) Math.round(color.getBlue() * 255));
    }

    private static class Punto {
        private final double x;
        private final double y;

        private Punto(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}