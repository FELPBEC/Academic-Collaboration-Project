package co.edu.uptc.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import co.edu.uptc.Enum.InteresArea;
import co.edu.uptc.Enum.Program;
import co.edu.uptc.Enum.RelationType;
import co.edu.uptc.Enum.TypeUser;
import co.edu.uptc.Exceptions.DuplicateUserException;
import co.edu.uptc.Model.Arista;
import co.edu.uptc.Model.Nodo;
import co.edu.uptc.Model.Solicitud;
import co.edu.uptc.Model.User;

/**
 * Pruebas de integración para creación, modificación y eliminación de relaciones.
 *
 * @author @jm1407db
 * @version 1.0
 */
class ServicioGeneralTest {

    @TempDir
    Path directorioTemporal;

    private Path archivoUsuarios;
    private Path archivoAristas;
    private ServicioGeneral servicio;

    @BeforeEach
    void setUp() {
        archivoUsuarios = directorioTemporal.resolve("usuarios.json");
        archivoAristas = directorioTemporal.resolve("aristas.json");
        servicio = new ServicioGeneral(
                archivoUsuarios.toString(),
                archivoAristas.toString());
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(archivoUsuarios);
        Files.deleteIfExists(archivoAristas);
    }

    private User crearUsuario(
            String correo,
            String nombre,
            Program programa,
            InteresArea area) {
        return new User(
                nombre,
                correo,
                "ClaveSegura2026!",
                TypeUser.Estudents,
                programa,
                area);
    }

    private void registrarAnaYLuis() throws Exception {
        servicio.registrarUsuario(crearUsuario(
                "ana@uptc.edu.co",
                "Ana Pérez",
                Program.IngenieriaDeSistemasYComputacion,
                InteresArea.Technology));
        servicio.registrarUsuario(crearUsuario(
                "luis@uptc.edu.co",
                "Luis Gómez",
                Program.IngenieriaElectronica,
                InteresArea.Engineering));
    }

    @Test
    void testCrearRelacionMedianteSolicitudAceptada() throws Exception {
        registrarAnaYLuis();

        assertTrue(servicio.iniciarSesion("ana@uptc.edu.co", "ClaveSegura2026!"));
        servicio.enviarSolicitud(
                "luis@uptc.edu.co",
                RelationType.Mentoring,
                "Solicitud de mentoría");

        servicio.cerrarSesion();
        assertTrue(servicio.iniciarSesion("luis@uptc.edu.co", "ClaveSegura2026!"));
        List<Solicitud> solicitudes = servicio.getUsuarioActual().getSolicitudesRecibidas();
        assertEquals(1, solicitudes.size());

        servicio.aceptarSolicitud(solicitudes.get(0));

        Arista<String> arista = servicio.getGrafo().encontrarArista(
                new Nodo<>("ana@uptc.edu.co"),
                new Nodo<>("luis@uptc.edu.co"));
        assertNotNull(arista);
        assertEquals(RelationType.Mentoring,
                arista.getDatosRelacion().getRelationType());
        assertTrue(Files.exists(archivoAristas));
    }

    @Test
    void testModificarRelacionMedianteSolicitudAceptada() throws Exception {
        registrarAnaYLuis();

        assertTrue(servicio.iniciarSesion("ana@uptc.edu.co", "ClaveSegura2026!"));
        servicio.enviarSolicitud(
                "luis@uptc.edu.co",
                RelationType.Mentoring,
                "Solicitud inicial");

        servicio.cerrarSesion();
        assertTrue(servicio.iniciarSesion("luis@uptc.edu.co", "ClaveSegura2026!"));
        Solicitud creacion = servicio.getUsuarioActual().getSolicitudesRecibidas().get(0);
        servicio.aceptarSolicitud(creacion);

        servicio.cerrarSesion();
        assertTrue(servicio.iniciarSesion("ana@uptc.edu.co", "ClaveSegura2026!"));
        servicio.solicitarModificacionRelacion(
                "luis@uptc.edu.co",
                RelationType.InvestigationCollaboration,
                "Cambio a colaboración investigativa");

        servicio.cerrarSesion();
        assertTrue(servicio.iniciarSesion("luis@uptc.edu.co", "ClaveSegura2026!"));
        Solicitud modificacion =
                servicio.getUsuarioActual().getSolicitudesRecibidas().get(0);
        servicio.aceptarSolicitud(modificacion);

        Arista<String> arista = servicio.getGrafo().encontrarArista(
                new Nodo<>("ana@uptc.edu.co"),
                new Nodo<>("luis@uptc.edu.co"));
        assertNotNull(arista);
        assertEquals(RelationType.InvestigationCollaboration,
                arista.getDatosRelacion().getRelationType());
    }

    @Test
    void testEliminarRelacionDirectamente() throws Exception {
        registrarAnaYLuis();

        assertTrue(servicio.iniciarSesion("ana@uptc.edu.co", "ClaveSegura2026!"));
        servicio.enviarSolicitud(
                "luis@uptc.edu.co",
                RelationType.JointParticipation,
                "Relación inicial");

        servicio.cerrarSesion();
        assertTrue(servicio.iniciarSesion("luis@uptc.edu.co", "ClaveSegura2026!"));
        Solicitud solicitud =
                servicio.getUsuarioActual().getSolicitudesRecibidas().get(0);
        servicio.aceptarSolicitud(solicitud);

        assertTrue(servicio.eliminarRelacion("ana@uptc.edu.co"));
        assertTrue(servicio.getGrafo().encontrarArista(
                new Nodo<>("ana@uptc.edu.co"),
                new Nodo<>("luis@uptc.edu.co")) == null);
    }

    @Test
    void testNoPermitirSolicitudDuplicada() throws Exception {
        registrarAnaYLuis();

        assertTrue(servicio.iniciarSesion("ana@uptc.edu.co", "ClaveSegura2026!"));
        servicio.enviarSolicitud(
                "luis@uptc.edu.co",
                RelationType.Mentoring,
                "Primera solicitud");

        assertThrows(DuplicateUserException.class,
                () -> servicio.enviarSolicitud(
                        "luis@uptc.edu.co",
                        RelationType.Mentoring,
                        "Solicitud duplicada"));
    }

    @Test
    void testEliminarCuentaPurgaSolicitudesRelacionadas() throws Exception {
        registrarAnaYLuis();

        assertTrue(servicio.iniciarSesion("ana@uptc.edu.co", "ClaveSegura2026!"));
        servicio.enviarSolicitud(
                "luis@uptc.edu.co",
                RelationType.Mentoring,
                "Solicitud pendiente");

        servicio.eliminarCuentaActual();

        assertFalse(servicio.getServicioUsuarios().encontrarUsuario("ana@uptc.edu.co"));
        User luis = servicio.getServicioUsuarios().obtenerUsuario("luis@uptc.edu.co");
        assertTrue(luis.getSolicitudesRecibidas().isEmpty());
    }
}
