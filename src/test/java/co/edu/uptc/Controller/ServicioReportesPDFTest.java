package co.edu.uptc.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import co.edu.uptc.Enum.InteresArea;
import co.edu.uptc.Enum.Program;
import co.edu.uptc.Enum.RelationType;
import co.edu.uptc.Enum.TypeUser;
import co.edu.uptc.Model.Arista;
import co.edu.uptc.Model.Grafo;
import co.edu.uptc.Model.Nodo;
import co.edu.uptc.Model.Relation;
import co.edu.uptc.Model.User;

class ServicioReportesPDFTest {

    @TempDir
    Path tempDir;

    private Map<String, User> usuarios;
    private ServicioReportesPDF servicioPDF;
    private User ana;
    private User luis;
    private User marta;

    @BeforeEach
    void setUp() {
        usuarios = new HashMap<>();

        ana = crearUsuario("ana@uptc.edu.co", "Ana Pérez", Program.IngenieriaDeSistemasYComputacion, InteresArea.Technology);
        luis = crearUsuario("luis@uptc.edu.co", "Luis Gómez", Program.IngenieriaDeSistemasYComputacion, InteresArea.Engineering);
        marta = crearUsuario("marta@uptc.edu.co", "Marta Díaz", Program.ContaduriaPublica, InteresArea.Cience);

        usuarios.put(ana.getEmail(), ana);
        usuarios.put(luis.getEmail(), luis);
        usuarios.put(marta.getEmail(), marta);

        servicioPDF = new ServicioReportesPDF(usuarios);
    }

    private User crearUsuario(String email, String nombre, Program programa, InteresArea area) {
        User user = new User();
        user.setEmail(email);
        user.setName(nombre);
        user.setProgram(programa);
        user.setInteresArea(area);
        user.setTypeUser(TypeUser.Estudents);
        return user;
    }

    private String leerTextoPdf(Path rutaPdf) throws Exception {
        byte[] bytes = Files.readAllBytes(rutaPdf);
        PdfReader reader = new PdfReader(bytes);
        try {
            StringBuilder texto = new StringBuilder();
            for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                texto.append(PdfTextExtractor.getTextFromPage(reader, i));
            }
            return texto.toString();
        } finally {
            reader.close();
        }
    }

    @Test
    void testResolverRutaSalidaExternaPorDefecto() throws Exception {
        Path ruta = ServicioReportesPDF.resolverRutaSalida("mi_reporte_prueba");
        Path esperado = Paths.get(System.getProperty("user.home"), "Reportes_RedAcademica", "mi_reporte_prueba.pdf");

        assertEquals(esperado, ruta);
        assertTrue(Files.exists(ruta.getParent()));
    }

    @Test
    void testGenerarReporteConexionesFiltradasConResultados() throws Exception {
        Relation rel = new Relation(RelationType.InvestigationCollaboration);
        Arista<String> arista = new Arista<>(new Nodo<>(ana.getEmail()), new Nodo<>(luis.getEmail()), rel);
        List<Arista<String>> conexiones = Arrays.asList(arista);

        Path destino = tempDir.resolve("reporte_conexiones.pdf");
        Path generado = servicioPDF.generarReporteConexionesFiltradas(
                destino.toString(),
                ana,
                conexiones,
                Program.IngenieriaDeSistemasYComputacion,
                InteresArea.Engineering,
                RelationType.InvestigationCollaboration);

        assertTrue(Files.exists(generado));
        assertTrue(Files.size(generado) > 0);

        String contenido = leerTextoPdf(generado);
        assertTrue(contenido.contains("REPORTE DE CONEXIONES DIRECTAS FILTRADAS"));
        assertTrue(contenido.contains("Ana Pérez"));
        assertTrue(contenido.contains("Luis Gómez"));
        assertTrue(contenido.contains("InvestigationCollaboration"));
        assertTrue(contenido.contains("1 conexiones encontradas"));
    }

    @Test
    void testGenerarReporteConexionesFiltradasSinResultados() throws Exception {
        Path destino = tempDir.resolve("reporte_vacio.pdf");
        Path generado = servicioPDF.generarReporteConexionesFiltradas(
                destino.toString(),
                ana,
                new ArrayList<>(),
                null,
                null,
                null);

        assertTrue(Files.exists(generado));
        String contenido = leerTextoPdf(generado);
        assertTrue(contenido.contains("Todos"));
        assertTrue(contenido.contains("No se encontraron conexiones directas"));
    }

    @Test
    void testGenerarReporteCadenaColaboracionConRuta() throws Exception {
        List<User> cadena = Arrays.asList(ana, luis, marta);
        Path destino = tempDir.resolve("reporte_cadena.pdf");

        Path generado = servicioPDF.generarReporteCadenaColaboracion(
                destino.toString(),
                ana,
                marta,
                cadena,
                Program.IngenieriaDeSistemasYComputacion,
                null,
                RelationType.Mentoring);

        assertTrue(Files.exists(generado));
        assertTrue(Files.size(generado) > 0);

        String contenido = leerTextoPdf(generado);
        assertTrue(contenido.contains("REPORTE DE CADENA DE COLABORACIÓN ÓPTIMA"));
        assertTrue(contenido.contains("Marta Díaz"));
        assertTrue(contenido.contains("3 integrantes (2 saltos de conexión)"));
    }

    @Test
    void testGenerarReporteConexionesSolicitanteNuloLanzaExcepcion() {
        Path destino = tempDir.resolve("reporte_error.pdf");
        assertThrows(IllegalArgumentException.class, () -> servicioPDF.generarReporteConexionesFiltradas(
                destino.toString(),
                null,
                new ArrayList<>(),
                null,
                null,
                null));
        assertFalse(Files.exists(destino));
    }

    @Test
    void testGenerarReporteBusquedaUsuarios() throws Exception {
        Grafo<String> grafo = new Grafo<>();
        Nodo<String> nodoAna = new Nodo<>(ana.getEmail());
        Nodo<String> nodoLuis = new Nodo<>(luis.getEmail());
        Nodo<String> nodoMarta = new Nodo<>(marta.getEmail());
        grafo.agregarNodo(nodoAna);
        grafo.agregarNodo(nodoLuis);
        grafo.agregarNodo(nodoMarta);
        grafo.agregarArista(new Arista<>(
                nodoAna,
                nodoLuis,
                new Relation(RelationType.InvestigationCollaboration)));
        grafo.agregarArista(new Arista<>(
                nodoLuis,
                nodoMarta,
                new Relation(RelationType.Mentoring)));

        ServicioReportesPDF servicioConGrafo =
                new ServicioReportesPDF(usuarios, grafo);
        Path destino = tempDir.resolve("reporte_busqueda_usuarios.pdf");

        Path generado = servicioConGrafo.generarReporteBusquedaUsuarios(
                destino.toString(),
                ana,
                Arrays.asList(luis, marta),
                Program.IngenieriaDeSistemasYComputacion,
                null,
                2);

        assertTrue(Files.exists(generado));
        assertTrue(Files.size(generado) > 0);

        String contenido = leerTextoPdf(generado);
        assertTrue(contenido.contains("REPORTE DE BÚSQUEDA DE USUARIOS"));
        assertTrue(contenido.contains("Luis Gómez"));
        assertTrue(contenido.contains("Marta Díaz"));
        assertTrue(contenido.contains("Puntuación mínima"));
        assertTrue(contenido.contains("2 usuarios encontrados"));
    }

    @Test
    void testGenerarReporteBusquedaPuentesSoloPersonas() throws Exception {
        Grafo<String> grafo = new Grafo<>();
        Nodo<String> nodoAna = new Nodo<>(ana.getEmail());
        Nodo<String> nodoLuis = new Nodo<>(luis.getEmail());
        grafo.agregarNodo(nodoAna);
        grafo.agregarNodo(nodoLuis);
        grafo.agregarArista(new Arista<>(
                nodoAna,
                nodoLuis,
                new Relation(RelationType.InvestigationCollaboration)));

        ServicioReportesPDF servicioConGrafo =
                new ServicioReportesPDF(usuarios, grafo);
        Path destino = tempDir.resolve("reporte_puentes.pdf");

        Path generado = servicioConGrafo.generarReporteBusquedaUsuarios(
                destino.toString(),
                ana,
                Arrays.asList(luis),
                null,
                null,
                0,
                "Puentes por programa",
                "Personas");

        assertTrue(Files.exists(generado));
        String contenido = leerTextoPdf(generado);
        assertTrue(contenido.contains("Puentes por programa"));
        assertTrue(contenido.contains("Personas"));
        assertTrue(contenido.contains("Luis Gómez"));
        assertTrue(contenido.contains("1 usuarios encontrados"));
    }
}