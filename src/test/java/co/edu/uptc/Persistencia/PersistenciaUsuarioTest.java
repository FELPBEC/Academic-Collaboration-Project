package co.edu.uptc.Persistencia;

import co.edu.uptc.Enum.InteresArea;
import co.edu.uptc.Enum.Program;
import co.edu.uptc.Enum.TypeUser;
import co.edu.uptc.Model.User;
import co.edu.uptc.Persistence.PersistenciaUsuario;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PersistenciaUsuarioTest {

    private PersistenciaUsuario persistenciaUsuario;
    private Path archivoJson;
    private Path archivoXml;

    @BeforeEach
    void setUp() throws IOException {
        persistenciaUsuario = new PersistenciaUsuario();
        archivoJson = Path.of("test_usuarios.json");
        archivoXml = Path.of("test_usuarios.xml");

        Files.deleteIfExists(archivoJson);
        Files.deleteIfExists(archivoXml);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(archivoJson);
        Files.deleteIfExists(archivoXml);
    }

    private User crearUsuarioPrueba(String nombre, String correo, Program programa) {
        User user = new User();
        user.setName(nombre);
        user.setEmail(correo);
        user.setPassword("$2a$10$hashDePrueba1234567890");
        user.setProgram(programa);
        user.setTypeUser(TypeUser.values()[0]);
        user.setInteresArea(InteresArea.values()[0]);
        return user;
    }

    @Test
    void testSaveAndLoadUsersJson() throws IOException {
        Map<String, User> usuariosOriginales = new HashMap<>();
        User ana = crearUsuarioPrueba("Ana Pérez", "ana@uptc.edu.co", Program.IngenieriaDeSistemasYComputacion);
        User luis = crearUsuarioPrueba("Luis Gómez", "luis@uptc.edu.co", Program.IngenieriaElectronica);

        usuariosOriginales.put(ana.getEmail(), ana);
        usuariosOriginales.put(luis.getEmail(), luis);

        persistenciaUsuario.saveUsers(usuariosOriginales, archivoJson.toString());
        assertTrue(Files.exists(archivoJson), "El archivo JSON de usuarios debería haberse creado");

        Map<String, User> usuariosCargados = persistenciaUsuario.loadUsers(archivoJson.toString());

        assertEquals(2, usuariosCargados.size());
        assertTrue(usuariosCargados.containsKey("ana@uptc.edu.co"));
        assertTrue(usuariosCargados.containsKey("luis@uptc.edu.co"));
        assertEquals("Ana Pérez", usuariosCargados.get("ana@uptc.edu.co").getName());
        assertEquals(Program.IngenieriaElectronica, usuariosCargados.get("luis@uptc.edu.co").getProgram());
    }

    @Test
    void testSaveAndLoadUsersXml() throws IOException {
        Map<String, User> usuariosOriginales = new HashMap<>();
        User carla = crearUsuarioPrueba("Carla Ruiz", "carla@uptc.edu.co", Program.IngenieriaGeologica);
        usuariosOriginales.put(carla.getEmail(), carla);

        persistenciaUsuario.saveUsers(usuariosOriginales, archivoXml.toString());
        assertTrue(Files.exists(archivoXml), "El archivo XML de usuarios debería haberse creado");

        Map<String, User> usuariosCargados = persistenciaUsuario.loadUsers(archivoXml.toString());

        assertEquals(1, usuariosCargados.size());
        assertEquals("Carla Ruiz", usuariosCargados.get("carla@uptc.edu.co").getName());
    }

    @Test
    void testLoadUsersArchivoInexistenteLanzaExcepcion() {
        assertThrows(IOException.class, () -> persistenciaUsuario.loadUsers("archivo_que_no_existe.json"));
    }
}
