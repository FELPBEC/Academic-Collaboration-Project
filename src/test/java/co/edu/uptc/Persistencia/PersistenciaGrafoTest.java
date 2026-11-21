package co.edu.uptc.Persistencia;

import co.edu.uptc.Model.Arista;
import co.edu.uptc.Model.Grafo;
import co.edu.uptc.Model.Nodo;
import co.edu.uptc.Model.Relation;
import co.edu.uptc.Enum.RelationType;
import co.edu.uptc.Persistence.PersistenciaGrafo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PersistenciaGrafoTest {

    private PersistenciaGrafo persistenciaGrafo;
    private Path archivoAristasJson;
    private Path archivoAristasXml;

    @BeforeEach
    void setUp() throws IOException {
        persistenciaGrafo = new PersistenciaGrafo();
        archivoAristasJson = Path.of("test_aristas.json");
        archivoAristasXml = Path.of("test_aristas.xml");

        Files.deleteIfExists(archivoAristasJson);
        Files.deleteIfExists(archivoAristasXml);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(archivoAristasJson);
        Files.deleteIfExists(archivoAristasXml);
    }

    private Relation crearRelacionPrueba() {
        Relation relation = new Relation();
        relation.setRelationType(RelationType.values()[0]);
        return relation;
    }

    @Test
    void testSaveAndLoadGrafoJson() throws IOException {
        // 1. Armamos el grafo original con 3 nodos y 2 conexiones
        Grafo<String> grafoOriginal = new Grafo<>();
        Nodo<String> nodoAna = new Nodo<>("ana@uptc.edu.co");
        Nodo<String> nodoLuis = new Nodo<>("luis@uptc.edu.co");
        Nodo<String> nodoCarla = new Nodo<>("carla@uptc.edu.co");

        grafoOriginal.agregarNodo(nodoAna);
        grafoOriginal.agregarNodo(nodoLuis);
        grafoOriginal.agregarNodo(nodoCarla);

        grafoOriginal.agregarArista(new Arista<>(nodoAna, nodoLuis, crearRelacionPrueba()));
        grafoOriginal.agregarArista(new Arista<>(nodoLuis, nodoCarla, crearRelacionPrueba()));

        // 2. Guardamos las aristas en JSON
        persistenciaGrafo.saveGrafo(grafoOriginal, archivoAristasJson.toString());
        assertTrue(Files.exists(archivoAristasJson), "El archivo JSON de aristas debería haberse creado");

        // 3. Reconstruimos el grafo desde disco
        List<String> usuariosRegistrados = new ArrayList<>();
        usuariosRegistrados.add("ana@uptc.edu.co");
        usuariosRegistrados.add("luis@uptc.edu.co");
        usuariosRegistrados.add("carla@uptc.edu.co");

        Grafo<String> grafoCargado = persistenciaGrafo.loadGrafo(usuariosRegistrados, archivoAristasJson.toString());

        // 4. Verificamos nodos y conexiones reconstruidas sin duplicados
        assertEquals(3, grafoCargado.obtenerTodosLosNodos().size());
        assertEquals(1, grafoCargado.obtenerAristas(new Nodo<>("ana@uptc.edu.co")).size());
        assertEquals(2, grafoCargado.obtenerAristas(new Nodo<>("luis@uptc.edu.co")).size());
        assertEquals(1, grafoCargado.obtenerAristas(new Nodo<>("carla@uptc.edu.co")).size());
    }

    @Test
    void testSaveAndLoadGrafoXml() throws IOException {
        Grafo<String> grafoOriginal = new Grafo<>();
        Nodo<String> nodoAna = new Nodo<>("ana@uptc.edu.co");
        Nodo<String> nodoLuis = new Nodo<>("luis@uptc.edu.co");

        grafoOriginal.agregarNodo(nodoAna);
        grafoOriginal.agregarNodo(nodoLuis);
        grafoOriginal.agregarArista(new Arista<>(nodoAna, nodoLuis, crearRelacionPrueba()));

        persistenciaGrafo.saveGrafo(grafoOriginal, archivoAristasXml.toString());
        assertTrue(Files.exists(archivoAristasXml), "El archivo XML de aristas debería haberse creado");

        List<String> usuarios = new ArrayList<>();
        usuarios.add("ana@uptc.edu.co");
        usuarios.add("luis@uptc.edu.co");

        Grafo<String> grafoCargado = persistenciaGrafo.loadGrafo(usuarios, archivoAristasXml.toString());

        assertEquals(2, grafoCargado.obtenerTodosLosNodos().size());
        assertEquals(1, grafoCargado.obtenerAristas(new Nodo<>("ana@uptc.edu.co")).size());
    }
}
