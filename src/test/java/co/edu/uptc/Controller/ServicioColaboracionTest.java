package co.edu.uptc.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.uptc.Enum.InteresArea;
import co.edu.uptc.Enum.RelationType;
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

        ana = crearUsuario("ana@uptc.edu.co", "Ana", "Ingeniería de Sistemas", InteresArea.Technology);
        luis = crearUsuario("luis@uptc.edu.co", "Luis", "Ingeniería de Sistemas", InteresArea.Technology);
        carlos = crearUsuario("carlos@uptc.edu.co", "Carlos", "Derecho", null);
        marta = crearUsuario("marta@uptc.edu.co", "Marta", "Medicina", InteresArea.Cience);

        registrarUsuario(ana);
        registrarUsuario(luis);
        registrarUsuario(carlos);
        registrarUsuario(marta);

        conectar("ana@uptc.edu.co", "luis@uptc.edu.co", RelationType.InvestigationCollaboration, 0.33);
        conectar("luis@uptc.edu.co", "marta@uptc.edu.co", RelationType.Mentoring, 0.50);
        conectar("ana@uptc.edu.co", "carlos@uptc.edu.co", RelationType.JointParticipation, 0.20);
        conectar("carlos@uptc.edu.co", "marta@uptc.edu.co", RelationType.Mentoring, 0.20);

        servicio = new ServicioColaboracion(grafo, usuarios);
    }

    private User crearUsuario(String email, String nombre, String programa, InteresArea area) {
        User user = new User();
        user.setEmail(email);
        user.setName(nombre);
        user.setProgram(programa);
        user.setInteresArea(area);
        return user;
    }

    private void registrarUsuario(User user) {
        usuarios.put(user.getEmail(), user);
        grafo.agregarNodo(new Nodo<>(user.getEmail()));
    }

    private void conectar(String origen, String destino, RelationType tipo, double peso) {
        Relation relacion = new Relation();
        relacion.setRelationType(tipo);
        grafo.agregarArista(new Arista<>(new Nodo<>(origen), new Nodo<>(destino), relacion, peso));
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
                "ingeniería de sistemas",
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
    void testBuscarCadenaColaboracionRutaMasCortaSinFiltros() {
        List<User> cadena = servicio.buscarCadenaColaboracion("ana@uptc.edu.co", "marta@uptc.edu.co", null, null, null);

        assertEquals(3, cadena.size());
        assertEquals("ana@uptc.edu.co", cadena.get(0).getEmail());
        assertEquals("carlos@uptc.edu.co", cadena.get(1).getEmail());
        assertEquals("marta@uptc.edu.co", cadena.get(2).getEmail());
    }

    @Test
    void testBuscarCadenaColaboracionConFiltrosDesviaRuta() {
        List<User> cadena = servicio.buscarCadenaColaboracion(
                "ana@uptc.edu.co",
                "marta@uptc.edu.co",
                "Ingeniería de Sistemas",
                InteresArea.Technology,
                null);

        assertEquals(3, cadena.size());
        assertEquals("ana@uptc.edu.co", cadena.get(0).getEmail());
        assertEquals("luis@uptc.edu.co", cadena.get(1).getEmail());
        assertEquals("marta@uptc.edu.co", cadena.get(2).getEmail());
    }

    @Test
    void testBuscarCadenaColaboracionSinRutaValidaPorTipoRelacion() {
        List<User> cadena = servicio.buscarCadenaColaboracion(
                "ana@uptc.edu.co",
                "marta@uptc.edu.co",
                null,
                null,
                RelationType.InvestigationCollaboration);

        assertTrue(cadena.isEmpty());
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
}