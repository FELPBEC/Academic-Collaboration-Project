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
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Componente JavaFX reutilizable para representar gráficamente un grafo académico.
 * <p>
 * Convierte los nodos y aristas del modelo en formas, líneas, etiquetas y tooltips
 * sin modificar la estructura original del grafo. Los tipos de usuario determinan
 * la forma de los nodos y los tipos de relación determinan el color de las aristas.
 * </p>
 *
 * @author Julian Moreno
 * @version 1.1
 */
public class GrafoView extends Pane {
    private static final double NODO_CIRCULO_RADIO = 24.0;
    private static final double NODO_RECTANGULO_ANCHO = 52.0;
    private static final double NODO_RECTANGULO_ALTO = 42.0;
    private static final double SEPARACION_BORDE = 70.0;
    private static final double FACTOR_NODO_ACTUAL = 1.15;
    private static final double ANCHO_MINIMO_NODOS = 800.0;
    private static final double ALTO_MINIMO_NODOS = 600.0;
    private static final double ESPACIADO_HORIZONTAL_NODO = 180.0;
    private static final double ESPACIADO_VERTICAL_NODO = 150.0;

    private Grafo<String> grafoActual;
    private Map<String, User> usuariosActuales;
    private String identificadorUsuarioActual;

    /**
     * Crea un componente listo para mostrar un grafo academico.
     */
    public GrafoView() {
        setPrefSize(800.0, 600.0);
        setStyle("-fx-background-color: #fafafa;");
        widthProperty().addListener((observable, anterior, actual) -> redibujar());
        heightProperty().addListener((observable, anterior, actual) -> redibujar());
    }

    /**
     * Dibuja el grafo con los perfiles asociados a sus identificadores.
     *
     * @param grafo grafo no dirigido cuyos nodos contienen correos electronicos.
     * @param usuarios mapa indexado por correo electronico.
     */
    public void dibujarGrafo(Grafo<String> grafo, Map<String, User> usuarios) {
        this.grafoActual = grafo;
        this.usuariosActuales = usuarios != null ? usuarios : new HashMap<>();
        ajustarTamañoContenido();
        redibujar();
    }

    /**
     * Ajusta el tamaño preferido del lienzo según la cantidad de nodos.
     * <p>
     * Esta operación permite colocar el componente dentro de un
     * {@link javafx.scene.control.ScrollPane}: para redes pequeñas conserva un
     * área compacta y para redes grandes amplía el lienzo para mantener separadas
     * las formas y facilitar el desplazamiento. La clase no instala filtros de
     * teclado ni mouse y no modifica el comportamiento de desplazamiento externo.
     * </p>
     */
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
        double ancho = Math.max(ANCHO_MINIMO_NODOS,
                columnas * ESPACIADO_HORIZONTAL_NODO);
        double alto = Math.max(ALTO_MINIMO_NODOS,
                filas * ESPACIADO_VERTICAL_NODO);
        setPrefSize(ancho, alto);
    }

    /**
     * Establece el usuario actual cuyo nodo debe resaltarse en el grafo.
     * <p>
     * El identificador corresponde al correo electrónico almacenado como contenido
     * del nodo. Puede usarse {@code null} para quitar el resaltado.
     * </p>
     *
     * @param identificadorUsuario identificador del nodo que representa al usuario actual
     */
    public void setUsuarioActual(String identificadorUsuario) {
        this.identificadorUsuarioActual = identificadorUsuario;
        redibujar();
    }

    /**
     * Obtiene el identificador del usuario actualmente resaltado.
     *
     * @return identificador del usuario actual o {@code null} si no hay uno seleccionado
     */
    public String getUsuarioActual() {
        return identificadorUsuarioActual;
    }

    /**
     * Elimina todas las figuras visuales del componente.
     */
    public void limpiar() {
        grafoActual = null;
        usuariosActuales = null;
        identificadorUsuarioActual = null;
        getChildren().clear();
        setPrefSize(ANCHO_MINIMO_NODOS, ALTO_MINIMO_NODOS);
    }

    /**
     * Reconstruye la visualizacion con el ultimo grafo recibido.
     */
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

    /**
     * Calcula posiciones circulares para todos los nodos del lienzo.
     *
     * @param nodos nodos que serán representados
     * @return mapa de identificadores a coordenadas visuales
     */
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

    /**
     * Crea la forma visual, etiqueta y tooltip de un nodo.
     *
     * @param nodo nodo del modelo que será dibujado
     * @param punto coordenada calculada para el nodo
     */
    private void dibujarNodo(Nodo<String> nodo, Punto punto) {
        if (punto == null) {
            return;
        }

        User usuario = usuariosActuales.get(nodo.getContenido());
        TypeUser tipoUsuario = usuario != null ? usuario.getTypeUser() : null;
        boolean usuarioActual = nodo.getContenido() != null
                && nodo.getContenido().equals(identificadorUsuarioActual);
        Shape forma = crearForma(tipoUsuario);
        if (usuarioActual) {
            forma.setScaleX(FACTOR_NODO_ACTUAL);
            forma.setScaleY(FACTOR_NODO_ACTUAL);
            forma.setStroke(Color.CRIMSON);
            forma.setStrokeWidth(3.0);
        } else {
            forma.setStroke(Color.DARKSLATEGRAY);
            forma.setStrokeWidth(1.5);
        }
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
                ? "-fx-font-size: 12px; -fx-font-weight: bold;"
                : "-fx-font-size: 11px;");
        etiqueta.setTranslateX(punto.x - etiqueta.getLayoutBounds().getWidth() / 2.0);
        etiqueta.setTranslateY(punto.y + obtenerRadioForma(tipoUsuario) + 18.0);

        Group grupo = new Group(forma, etiqueta);
        Tooltip tooltip = crearTooltipUsuario(usuario, nodo.getContenido());
        if (usuarioActual) {
            tooltip.setText("USUARIO ACTUAL\n" + tooltip.getText());
        }
        Tooltip.install(grupo, tooltip);
        getChildren().add(grupo);
    }

    /**
     * Crea la línea, etiqueta y tooltip de una arista.
     *
     * @param arista arista del modelo que será dibujada
     * @param posiciones coordenadas disponibles para sus dos extremos
     */
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

    /**
     * Selecciona la forma JavaFX correspondiente al tipo de usuario.
     *
     * @param tipoUsuario tipo registrado en el perfil
     * @return forma visual del nodo
     */
    private Shape crearForma(TypeUser tipoUsuario) {
        if (tipoUsuario == TypeUser.Teachers) {
            Rectangle rectangulo = new Rectangle(
                    -NODO_RECTANGULO_ANCHO / 2.0,
                    -NODO_RECTANGULO_ALTO / 2.0,
                    NODO_RECTANGULO_ANCHO,
                    NODO_RECTANGULO_ALTO);
            rectangulo.setArcWidth(8.0);
            rectangulo.setArcHeight(8.0);
            return rectangulo;
        }
        if (tipoUsuario == TypeUser.InvestigationGroup) {
            Polygon hexagono = new Polygon(
                    0, -26,
                    23, -13,
                    23, 13,
                    0, 26,
                    -23, 13,
                    -23, -13);
            return hexagono;
        }
        if (tipoUsuario == TypeUser.AcademicGroup) {
            Polygon rombo = new Polygon(
                    0, -28,
                    28, 0,
                    0, 28,
                    -28, 0);
            return rombo;
        }
        return new Circle(0, 0, NODO_CIRCULO_RADIO);
    }

    /**
     * Obtiene una referencia vertical de altura para ubicar la etiqueta del nodo.
     *
     * @param tipoUsuario tipo que determina las dimensiones de la forma
     * @return distancia aproximada desde el centro hasta el borde inferior
     */
    private double obtenerRadioForma(TypeUser tipoUsuario) {
        if (tipoUsuario == TypeUser.Teachers) {
            return NODO_RECTANGULO_ALTO / 2.0;
        }
        return NODO_CIRCULO_RADIO;
    }

    /**
     * Construye el tooltip público de un nodo sin datos sensibles.
     *
     * @param usuario perfil asociado al nodo
     * @param identificador identificador usado si el perfil no existe
     * @return tooltip con nombre, tipo, programa y área de interés
     */
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

    /**
     * Obtiene el texto breve mostrado directamente debajo de un nodo.
     *
     * @param usuario perfil asociado al nodo
     * @param identificador identificador de respaldo
     * @return nombre visible o descripción breve del nodo
     */
    private String textoUsuario(User usuario, String identificador) {
        if (usuario != null && usuario.getName() != null && !usuario.getName().trim().isEmpty()) {
            return usuario.getName();
        }
        if (usuario != null && usuario.getTypeUser() != null) {
            return usuario.getTypeUser().name();
        }
        return identificador != null ? identificador : "Nodo";
    }

    /**
     * Obtiene el texto utilizado al ordenar nodos determinísticamente.
     *
     * @param nodo nodo a representar
     * @return texto comparable del nodo
     */
    private String textoNodo(Nodo<String> nodo) {
        if (nodo == null || nodo.getContenido() == null) {
            return "";
        }
        User usuario = usuariosActuales != null ? usuariosActuales.get(nodo.getContenido()) : null;
        return textoUsuario(usuario, nodo.getContenido());
    }

    /**
     * Obtiene el color visual asociado al tipo de nodo.
     *
     * @param tipoUsuario tipo registrado para el participante
     * @return color asociado a la forma del nodo
     */
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

    /**
     * Obtiene el color visual asociado al tipo de relacion.
     *
     * @param tipoRelacion tipo de relación que conecta los participantes
     * @return color asociado a la línea y etiqueta de la relación
     */
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

    /**
     * Convierte un color JavaFX a su representación hexadecimal CSS.
     *
     * @param color color JavaFX
     * @return código hexadecimal RGB
     */
    private String toCssColor(Color color) {
        return String.format(java.util.Locale.US,
                "#%02X%02X%02X",
                (int) Math.round(color.getRed() * 255),
                (int) Math.round(color.getGreen() * 255),
                (int) Math.round(color.getBlue() * 255));
    }

    /**
     * Coordenada bidimensional utilizada internamente para posicionar la visualización.
     */
    private static class Punto {
        private final double x;
        private final double y;

        private Punto(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
