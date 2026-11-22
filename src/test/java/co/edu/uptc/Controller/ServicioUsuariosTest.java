package co.edu.uptc.Controller;

import co.edu.uptc.Enum.InteresArea;
import co.edu.uptc.Enum.Program;
import co.edu.uptc.Enum.TypeUser;
import co.edu.uptc.Exceptions.DuplicateUserException;
import co.edu.uptc.Exceptions.InvalidPasswordFormat;
import co.edu.uptc.Exceptions.UsuarioNoEncontradoException;
import co.edu.uptc.Model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ServicioUsuariosTest {

    private Path archivoUsuariosTest;
    private ServicioUsuarios servicioUsuarios;

    @BeforeEach
    void setUp() throws IOException {
        archivoUsuariosTest = Path.of("test_servicio_usuarios.json");
        Files.deleteIfExists(archivoUsuariosTest);
        servicioUsuarios = new ServicioUsuarios(archivoUsuariosTest.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(archivoUsuariosTest);
    }

    private User construirUsuario(String nombre, String email, String password, Program programa) {
        User user = new User();
        user.setName(nombre);
        user.setEmail(email);
        user.setPassword(password);
        user.setProgram(programa);
        user.setTypeUser(TypeUser.values()[0]);
        user.setInteresArea(InteresArea.values()[0]);
        return user;
    }

    @Test
    void testCrearUsuarioExitosoYPersistencia() throws Exception {
        User ana = construirUsuario("Ana Pérez", "ana@uptc.edu.co", "ClaveSegura2026!", Program.IngenieriaDeSistemasYComputacion);

        servicioUsuarios.crearUsuario(ana);

        assertTrue(servicioUsuarios.encontrarUsuario("ana@uptc.edu.co"));
        assertEquals(1, servicioUsuarios.obtenerTodosLosEmails().size());

        // Verifica que la contraseña se haya encriptado con BCrypt y no quede en texto plano
        User guardada = servicioUsuarios.obtenerUsuario("ana@uptc.edu.co");
        assertNotEquals("ClaveSegura2026!", guardada.getPassword());
        assertTrue(guardada.getPassword().startsWith("$2"));

        // Verifica que una nueva instancia cargue los datos persistidos desde disco
        ServicioUsuarios nuevoServicio = new ServicioUsuarios(archivoUsuariosTest.toString());
        assertTrue(nuevoServicio.encontrarUsuario("ana@uptc.edu.co"));
        assertEquals("Ana Pérez", nuevoServicio.obtenerUsuario("ana@uptc.edu.co").getName());
    }

    @Test
    void testCrearUsuarioDuplicadoLanzaExcepcion() throws Exception {
        User ana1 = construirUsuario("Ana Pérez", "ana@uptc.edu.co", "ClaveSegura2026!", Program.IngenieriaDeSistemasYComputacion);
        User ana2 = construirUsuario("Ana Duplicada", "ana@uptc.edu.co", "OtraClave2026!", Program.IngenieriaIndustrial);

        servicioUsuarios.crearUsuario(ana1);

        assertThrows(DuplicateUserException.class, () -> servicioUsuarios.crearUsuario(ana2));
    }

    @Test
    void testCrearUsuarioPasswordInvalidaLanzaExcepcion() {
        User usuarioDebil = construirUsuario("Luis Gómez", "luis@uptc.edu.co", "123", Program.IngenieriaElectronica);

        assertThrows(InvalidPasswordFormat.class, () -> servicioUsuarios.crearUsuario(usuarioDebil));
        assertFalse(servicioUsuarios.encontrarUsuario("luis@uptc.edu.co"));
    }

    @Test
    void testIniciarSesion() throws Exception {
        User ana = construirUsuario("Ana Pérez", "ana@uptc.edu.co", "ClaveSegura2026!", Program.IngenieriaDeSistemasYComputacion);
        servicioUsuarios.crearUsuario(ana);

        assertTrue(servicioUsuarios.iniciarSesion("ana@uptc.edu.co", "ClaveSegura2026!"));
        assertFalse(servicioUsuarios.iniciarSesion("ana@uptc.edu.co", "PasswordIncorrecta1!"));
        assertThrows(UsuarioNoEncontradoException.class,
                () -> servicioUsuarios.iniciarSesion("noexiste@uptc.edu.co", "ClaveSegura2026!"));
    }

    @Test
    void testActualizarUsuarioSinCambiarPassword() throws Exception {
        User ana = construirUsuario("Ana Pérez", "ana@uptc.edu.co", "ClaveSegura2026!", Program.IngenieriaDeSistemasYComputacion);
        servicioUsuarios.crearUsuario(ana);

        String hashOriginal = servicioUsuarios.obtenerUsuario("ana@uptc.edu.co").getPassword();

        // Simulamos edición de datos pasando el hash existente y un intento de cambiar el email
        User datosEditados = construirUsuario("Ana María Pérez", "otro@uptc.edu.co", hashOriginal, Program.IngenieriaElectronica);
        servicioUsuarios.actualizarUsuario("ana@uptc.edu.co", datosEditados);

        User actualizada = servicioUsuarios.obtenerUsuario("ana@uptc.edu.co");
        assertEquals("Ana María Pérez", actualizada.getName());
        assertEquals(Program.IngenieriaElectronica, actualizada.getProgram());
        assertEquals("ana@uptc.edu.co", actualizada.getEmail(), "El correo llave no debe sobrescribirse");
        assertTrue(servicioUsuarios.iniciarSesion("ana@uptc.edu.co", "ClaveSegura2026!"),
                "No debe ocurrir doble encriptación al mantener la misma contraseña");
    }

    @Test
    void testActualizarUsuarioConNuevaPassword() throws Exception {
        User ana = construirUsuario("Ana Pérez", "ana@uptc.edu.co", "ClaveSegura2026!", Program.IngenieriaDeSistemasYComputacion);
        servicioUsuarios.crearUsuario(ana);

        // Intento con contraseña inválida debe fallar
        User conClaveInvalida = construirUsuario("Ana Pérez", "ana@uptc.edu.co", "abc", Program.IngenieriaDeSistemasYComputacion);
        assertThrows(InvalidPasswordFormat.class,
                () -> servicioUsuarios.actualizarUsuario("ana@uptc.edu.co", conClaveInvalida));

        // Actualización con contraseña nueva válida
        User conClaveNueva = construirUsuario("Ana Pérez", "ana@uptc.edu.co", "NuevaClave2026#", Program.IngenieriaDeSistemasYComputacion);
        servicioUsuarios.actualizarUsuario("ana@uptc.edu.co", conClaveNueva);

        assertFalse(servicioUsuarios.iniciarSesion("ana@uptc.edu.co", "ClaveSegura2026!"));
        assertTrue(servicioUsuarios.iniciarSesion("ana@uptc.edu.co", "NuevaClave2026#"));
    }

    @Test
    void testEliminarUsuario() throws Exception {
        User ana = construirUsuario("Ana Pérez", "ana@uptc.edu.co", "ClaveSegura2026!", Program.IngenieriaDeSistemasYComputacion);
        servicioUsuarios.crearUsuario(ana);

        String eliminado = servicioUsuarios.eliminarUsuario("ana@uptc.edu.co");

        assertEquals("ana@uptc.edu.co", eliminado);
        assertFalse(servicioUsuarios.encontrarUsuario("ana@uptc.edu.co"));
        assertThrows(UsuarioNoEncontradoException.class, () -> servicioUsuarios.obtenerUsuario("ana@uptc.edu.co"));
        assertThrows(UsuarioNoEncontradoException.class, () -> servicioUsuarios.eliminarUsuario("ana@uptc.edu.co"));
    }

    @Test
    void testFiltrarUsuariosPorProgramaYArea() {
        User ana = construirUsuario(
                "Ana Pérez",
                "ana@uptc.edu.co",
                "ClaveSegura2026!",
                Program.IngenieriaDeSistemasYComputacion);
        ana.setInteresArea(InteresArea.Technology);

        User luis = construirUsuario(
                "Luis Gómez",
                "luis@uptc.edu.co",
                "ClaveSegura2026!",
                Program.IngenieriaElectronica);
        luis.setInteresArea(InteresArea.Technology);

        User carlos = construirUsuario(
                "Carlos Ruiz",
                "carlos@uptc.edu.co",
                "ClaveSegura2026!",
                Program.IngenieriaDeSistemasYComputacion);
        carlos.setInteresArea(InteresArea.Mathematics);

        Map<String, User> usuarios = new HashMap<>();
        usuarios.put(ana.getEmail(), ana);
        usuarios.put(luis.getEmail(), luis);
        usuarios.put(carlos.getEmail(), carlos);
        servicioUsuarios.setUsuarios(usuarios);

        List<User> resultado = servicioUsuarios.filtrarUsuarios(
                Program.IngenieriaDeSistemasYComputacion,
                InteresArea.Technology);

        assertEquals(1, resultado.size());
        assertEquals(ana, resultado.get(0));
        assertTrue(servicioUsuarios.filtrarUsuarios(
                Program.IngenieriaDeSistemasYComputacion,
                null).contains(carlos));
        assertTrue(servicioUsuarios.filtrarUsuarios(
                null,
                InteresArea.Technology).contains(luis));
    }

    @Test
    void testFiltrarUsuariosPorTipoParticipante() {
        User estudiante = construirUsuario(
                "Estudiante",
                "estudiante@uptc.edu.co",
                "ClaveSegura2026!",
                Program.IngenieriaDeSistemasYComputacion);
        estudiante.setTypeUser(TypeUser.Estudents);

        User docente = construirUsuario(
                "Docente",
                "docente@uptc.edu.co",
                "ClaveSegura2026!",
                Program.IngenieriaDeSistemasYComputacion);
        docente.setTypeUser(TypeUser.Teachers);

        User semillero = construirUsuario(
                "Semillero",
                "semillero@uptc.edu.co",
                "ClaveSegura2026!",
                Program.IngenieriaDeSistemasYComputacion);
        semillero.setTypeUser(TypeUser.InvestigationGroup);

        Map<String, User> usuarios = new HashMap<>();
        usuarios.put(estudiante.getEmail(), estudiante);
        usuarios.put(docente.getEmail(), docente);
        usuarios.put(semillero.getEmail(), semillero);
        servicioUsuarios.setUsuarios(usuarios);

        assertEquals(2, servicioUsuarios.filtrarUsuariosPorTipo(
                TypeUser.Estudents,
                TypeUser.Teachers).size());
        assertTrue(servicioUsuarios.filtrarUsuariosPorTipo(
                TypeUser.InvestigationGroup,
                TypeUser.AcademicGroup).contains(semillero));
    }
}
