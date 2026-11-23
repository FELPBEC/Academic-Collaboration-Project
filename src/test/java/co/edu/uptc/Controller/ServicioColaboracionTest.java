package co.edu.uptc.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.uptc.Enum.InteresArea;
import co.edu.uptc.Enum.Program;
import co.edu.uptc.Enum.RelationType;
import co.edu.uptc.Enum.TypeUser;
import co.edu.uptc.Exceptions.UsuarioNoEncontradoException;
import co.edu.uptc.Model.Arista;
import co.edu.uptc.Model.Grafo;
import co.edu.uptc.Model.Nodo;
import co.edu.uptc.Model.Relation;
import co.edu.uptc.Model.User;

class ServicioColaboracionTest {

    private Grafo<String> grafo;
    private Map<String, User> usuarios;
    private ServicioColaboracion servicio;

    private User ana;
    private User luis;
    private User carlos;
    private User marta;

    @BeforeEach
    void setUp() {
        grafo = new Grafo<>();
        usuarios = new HashMap<>();

        ana = crearUsuario("ana@uptc.edu.co", "Ana", Program.IngenieriaDeSistemasYComputacion, InteresArea.Technology);
        luis = crearUsuario("luis@uptc.edu.co", "Luis", Program.IngenieriaDeSistemasYComputacion, InteresArea.Technology);
        carlos = crearUsuario("carlos@uptc.edu.co", "Carlos", Program.IngenieriaIndustrial, null);
        marta = crearUsuario("marta@uptc.edu.co", "Marta", Program.ContaduriaPublica, InteresArea.Cience);

        registrarUsuario(ana);
        registrarUsuario(luis);
        registrarUsuario(carlos);
        registrarUsuario(marta);

        conectar("ana@uptc.edu.co", "luis@uptc.edu.co", RelationType.InvestigationCollaboration);
        conectar("luis@uptc.edu.co", "marta@uptc.edu.co", RelationType.Mentoring);
        conectar("ana@uptc.edu.co", "carlos@uptc.edu.co", RelationType.JointParticipation);
        conectar("carlos@uptc.edu.co", "marta@uptc.edu.co", RelationType.Mentoring);

        servicio = new ServicioColaboracion(grafo, usuarios);
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

    private void registrarUsuario(User user) {
        usuarios.put(user.getEmail(), user);
        grafo.agregarNodo(new Nodo<>(user.getEmail()));
    }

    private void conectar(String origen, String destino, RelationType tipo) {
        Relation relacion = new Relation();
        relacion.setRelationType(tipo);
        grafo.agregarArista(new Arista<>(new Nodo<>(origen), new Nodo<>(destino), relacion));
    }

    @Test
    void testFiltrarConexionesDirectasSinFiltros() {
        List<Arista<String>> resultado = servicio.filtrarConexionesDirectas("ana@uptc.edu.co", null, null, null);
        assertEquals(2, resultado.size());
    }

    @Test
    void testFiltrarConexionesDirectasConFiltros() {
        List<Arista<String>> resultado = servicio.filtrarConexionesDirectas(
                "ana@uptc.edu.co",
                Program.IngenieriaDeSistemasYComputacion,
                InteresArea.Technology,
                RelationType.InvestigationCollaboration);

        assertEquals(1, resultado.size());
        String vecino = resultado.get(0).getExtremoOpuesto(new Nodo<>("ana@uptc.edu.co")).getContenido();
        assertEquals("luis@uptc.edu.co", vecino);
    }

    @Test
    void testFiltrarConexionesDirectasUsuarioInexistente() {
        List<Arista<String>> resultado = servicio.filtrarConexionesDirectas("noexiste@uptc.edu.co", null, null, null);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void testFiltrarConexionesDirectasPorCadaTipoDeRelacion() {
        long colaboracion = servicio.filtrarConexionesDirectas(
                "ana@uptc.edu.co", null, null, RelationType.InvestigationCollaboration).size();
        long mentorias = servicio.filtrarConexionesDirectas(
                "ana@uptc.edu.co", null, null, RelationType.Mentoring).size();
        long participacion = servicio.filtrarConexionesDirectas(
                "ana@uptc.edu.co", null, null, RelationType.JointParticipation).size();

        assertEquals(1, colaboracion);
        assertEquals(0, mentorias);
        assertEquals(1, participacion);
    }

    @Test
    void testFiltrarUsuariosPorRelacionesRelevantesOrdenadosPorPuntaje() {
        List<User> resultado = servicio.filtrarUsuariosPorRelacionesRelevantes(
                "ana@uptc.edu.co");

        assertEquals(3, resultado.size());
        assertFalse(resultado.contains(ana));
        assertEquals("luis@uptc.edu.co", resultado.get(0).getEmail());
        assertEquals("marta@uptc.edu.co", resultado.get(1).getEmail());
        assertEquals("carlos@uptc.edu.co", resultado.get(2).getEmail());
    }

    @Test
    void testFiltrarUsuariosPorRelacionesRelevantesConExclusion() {
        List<User> resultado = servicio.filtrarUsuariosPorRelacionesRelevantes(
                "ana@uptc.edu.co",
                3);

        assertFalse(resultado.contains(ana));
        assertTrue(resultado.contains(luis));
        assertTrue(resultado.contains(marta));
        assertTrue(resultado.contains(carlos));
    }

    @Test
    void testIdentificarParticipantesPuentePorPrograma() {
        List<User> resultado = servicio.identificarParticipantesPuentePorPrograma(
                "ana@uptc.edu.co");

        assertFalse(resultado.contains(ana));
        assertTrue(resultado.contains(carlos));
        assertTrue(resultado.contains(marta));
        assertEquals(carlos, resultado.get(0));
    }

    @Test
    void testPuenteNoIncluyeGrupos() {
        User semillero = crearUsuario(
                "semillero@uptc.edu.co",
                "Semillero IA",
                Program.IngenieriaDeSistemasYComputacion,
                InteresArea.Technology);
        semillero.setTypeUser(co.edu.uptc.Enum.TypeUser.InvestigationGroup);
        registrarUsuario(semillero);
        conectar("ana@uptc.edu.co", "semillero@uptc.edu.co",
                RelationType.Mentoring);
        conectar("semillero@uptc.edu.co", "marta@uptc.edu.co",
                RelationType.Mentoring);

        List<User> resultado = servicio.identificarParticipantesPuentePorPrograma(
                "ana@uptc.edu.co");

        assertFalse(resultado.contains(semillero));
    }

    @Test
    void testBuscarCadenaColaboracionRutaMasCortaSinFiltros() {
        List<User> cadena = servicio.buscarCadenaColaboracion("ana@uptc.edu.co", "marta@uptc.edu.co", null, null, null);

        assertEquals(3, cadena.size());
        assertEquals("ana@uptc.edu.co", cadena.get(0).getEmail());
        assertTrue(
                cadena.get(1).getEmail().equals("carlos@uptc.edu.co")
                        || cadena.get(1).getEmail().equals("luis@uptc.edu.co"));
        assertEquals("marta@uptc.edu.co", cadena.get(2).getEmail());
    }

    @Test
    void testBuscarCadenaColaboracionConFiltrosDesviaRuta() {
        List<User> cadena = servicio.buscarCadenaColaboracion(
                "ana@uptc.edu.co",
                "marta@uptc.edu.co",
                Program.IngenieriaDeSistemasYComputacion,
                InteresArea.Technology,
                null);

        assertEquals(2, cadena.size());
        assertEquals("ana@uptc.edu.co", cadena.get(0).getEmail());
        assertEquals("luis@uptc.edu.co", cadena.get(1).getEmail());
    }

    @Test
    void testBuscarCadenaColaboracionSinRutaValidaPorTipoRelacion() {
        List<User> cadena = servicio.buscarCadenaColaboracion(
                "ana@uptc.edu.co",
                "marta@uptc.edu.co",
                null,
                null,
                RelationType.InvestigationCollaboration);

        assertEquals(2, cadena.size());
        assertEquals("ana@uptc.edu.co", cadena.get(0).getEmail());
        assertTrue(cadena.get(1).getEmail().equals("carlos@uptc.edu.co")
                || cadena.get(1).getEmail().equals("luis@uptc.edu.co"));
    }

    @Test
    void testBuscarCadenaColaboracionRutaDirectaPorTipoRelacion() {
        List<User> cadena = servicio.buscarCadenaColaboracion(
                "ana@uptc.edu.co",
                "luis@uptc.edu.co",
                null,
                null,
                RelationType.InvestigationCollaboration);

        assertEquals(2, cadena.size());
        assertEquals("ana@uptc.edu.co", cadena.get(0).getEmail());
        assertEquals("luis@uptc.edu.co", cadena.get(1).getEmail());
    }

    @Test
    void testBuscarCadenaColaboracionTipoRelacionIncompatible() {
        List<User> cadena = servicio.buscarCadenaColaboracion(
                "ana@uptc.edu.co",
                "luis@uptc.edu.co",
                null,
                null,
                RelationType.Mentoring);

        assertTrue(cadena.isEmpty());
    }

    @Test
    void testBuscarCadenaColaboracionDirectaPorParticipacionConjunta() {
        List<User> cadena = servicio.buscarCadenaColaboracion(
                "ana@uptc.edu.co",
                "carlos@uptc.edu.co",
                null,
                null,
                RelationType.JointParticipation);

        assertEquals(2, cadena.size());
        assertEquals("ana@uptc.edu.co", cadena.get(0).getEmail());
        assertEquals("carlos@uptc.edu.co", cadena.get(1).getEmail());
    }

    @Test
    void testBuscarCadenaColaboracionFiltraDestinoPorProgramaYArea() {
        List<User> cadenaValida = servicio.buscarCadenaColaboracion(
                "ana@uptc.edu.co",
                "luis@uptc.edu.co",
                Program.IngenieriaDeSistemasYComputacion,
                InteresArea.Technology,
                RelationType.InvestigationCollaboration);

        assertEquals(2, cadenaValida.size());

        List<User> cadenaProgramaIncorrecto = servicio.buscarCadenaColaboracion(
                "ana@uptc.edu.co",
                "luis@uptc.edu.co",
                Program.IngenieriaIndustrial,
                null,
                null);

        assertTrue(cadenaProgramaIncorrecto.isEmpty());

        List<User> cadenaAreaIncorrecta = servicio.buscarCadenaColaboracion(
                "ana@uptc.edu.co",
                "luis@uptc.edu.co",
                null,
                InteresArea.Cience,
                null);

        assertTrue(cadenaAreaIncorrecta.isEmpty());
    }

    @Test
    void testBuscarCadenaColaboracionDestinoDebeCumplirProgramaYArea() {
        List<User> cadenaPorPrograma = servicio.buscarCadenaColaboracion(
                "ana@uptc.edu.co",
                "marta@uptc.edu.co",
                Program.IngenieriaDeSistemasYComputacion,
                null,
                null);

        assertEquals(2, cadenaPorPrograma.size());
        assertEquals("ana@uptc.edu.co", cadenaPorPrograma.get(0).getEmail());
        assertEquals("luis@uptc.edu.co", cadenaPorPrograma.get(1).getEmail());

        List<User> cadenaPorAreaIncorrecta = servicio.buscarCadenaColaboracion(
                "ana@uptc.edu.co",
                "marta@uptc.edu.co",
                null,
                InteresArea.Technology,
                null);

        assertEquals(2, cadenaPorAreaIncorrecta.size());
        assertEquals("ana@uptc.edu.co", cadenaPorAreaIncorrecta.get(0).getEmail());
        assertEquals("luis@uptc.edu.co", cadenaPorAreaIncorrecta.get(1).getEmail());
    }

    @Test
    void testBuscarCadenaColaboracionAlternativaSinFiltros() {
        List<User> cadenaAlternativa = servicio.buscarCadenaColaboracion(
                "ana@uptc.edu.co",
                "marta@uptc.edu.co",
                null,
                null,
                null);

        assertEquals(3, cadenaAlternativa.size());
        assertEquals("ana@uptc.edu.co", cadenaAlternativa.get(0).getEmail());
        assertEquals("marta@uptc.edu.co", cadenaAlternativa.get(2).getEmail());
    }

    @Test
    void testBuscarCadenaColaboracionOrigenODestinoInexistente() {
        List<User> cadena = servicio.buscarCadenaColaboracion("ana@uptc.edu.co", "fantasma@uptc.edu.co", null, null, null);
        assertTrue(cadena.isEmpty());
    }

    @Test
    void testBuscarCadenaColaboracionMismoOrigenYDestino() {
        List<User> cadena = servicio.buscarCadenaColaboracion("ana@uptc.edu.co", "ana@uptc.edu.co", null, null, null);

        assertEquals(1, cadena.size());
        assertEquals("ana@uptc.edu.co", cadena.get(0).getEmail());
    }
    // ==================== PRUEBAS RF04: RELACIONES DIRECTAS ====================

    @Test
    void testObtenerRelacionesDirectas_RF04() throws UsuarioNoEncontradoException {
        // Según tu setUp: Ana conecta directo con Luis y Carlos; Marta está a 2 saltos
        List<User> directosAna = servicio.obtenerRelacionesDirectas("ana@uptc.edu.co");

        assertEquals(2, directosAna.size());
        assertTrue(directosAna.contains(luis));
        assertTrue(directosAna.contains(carlos));
        assertFalse(directosAna.contains(marta), "Marta es conexión indirecta, no debe aparecer");
        assertFalse(directosAna.contains(ana), "El mismo usuario no debe incluirse");
    }

    @Test
    void testObtenerRelacionesDirectasUsuarioAislado_RF04() throws UsuarioNoEncontradoException {
        User pedro = crearUsuario("pedro@uptc.edu.co", "Pedro",Program.IngenieriaDeSistemasYComputacion , InteresArea.Cience);
        registrarUsuario(pedro);

        List<User> directosPedro = servicio.obtenerRelacionesDirectas("pedro@uptc.edu.co");

        assertTrue(directosPedro.isEmpty());
    }

    @Test
    void testObtenerRelacionesDirectasUsuarioInexistente_RF04() {
        assertThrows(UsuarioNoEncontradoException.class,
                () -> servicio.obtenerRelacionesDirectas("noexiste@uptc.edu.co"));
    }

    // ==================== PRUEBAS RF06: NIVEL DE INTERACCIÓN ====================

    @Test
    void testObtenerColaboradoresPorNivelInteraccion_RF06() throws UsuarioNoEncontradoException {
        // Agregamos a Marta como conexión directa intermedia (Mentoring = proximidad 2)
        // Así Ana queda con los 3 niveles: Luis (3), Marta (2) y Carlos (1)
        conectar("ana@uptc.edu.co", "marta@uptc.edu.co", RelationType.Mentoring);

        List<User> ordenados = servicio.obtenerColaboradoresPorNivelInteraccion("ana@uptc.edu.co");

        assertEquals(3, ordenados.size());
        assertEquals(luis, ordenados.get(0), "InvestigationCollaboration (3) debe ir primero");
        assertEquals(marta, ordenados.get(1), "Mentoring (2) debe ir segundo");
        assertEquals(carlos, ordenados.get(2), "JointParticipation (1) debe ir último");
    }
    @Test
    void testObtenerColaboradoresPorNivelInteraccionUsuarioInexistente() {
        assertThrows(UsuarioNoEncontradoException.class,
                () -> servicio.obtenerColaboradoresPorNivelInteraccion("noexiste@uptc.edu.co"));
    }
}