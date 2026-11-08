package co.edu.uptc.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class GrafoTest {

    // =======================================================
    // PRUEBAS DE AGREGAR NODO
    // =======================================================

    @Test 
    void agregarNodo_NodoNoExiste_DeberiaRetornarTrue() {
        Grafo<String> grafo = new Grafo<>();
        Nodo<String> nodo = new Nodo<>("A");
        assertTrue(grafo.agregarNodo(nodo));
        assertEquals(1, grafo.obtenerTodosLosNodos().size());
    }

    @Test 
    void agregarNodo_MismoNodoExiste_DeberiaRetornarFalse() {
        Grafo<String> grafo = new Grafo<>();
        Nodo<String> nodo = new Nodo<>("A");
        grafo.agregarNodo(nodo);
        assertFalse(grafo.agregarNodo(nodo));
    }

    @Test 
    void agregarNodo_NodoDiferenteMismoContenidoExiste_DeberiaRetornarFalse() {
        Grafo<String> grafo = new Grafo<>();
        Nodo<String> nodo1 = new Nodo<>("A");
        Nodo<String> nodo2 = new Nodo<>("A");
        grafo.agregarNodo(nodo1);
        assertFalse(grafo.agregarNodo(nodo2));
    }

    @Test 
    void agregarNodo_NodoContenidoNullNoExiste_DeberiaRetornarTrue() {
        Grafo<String> grafo = new Grafo<>();
        Nodo<String> nodo = new Nodo<>(null);
        assertTrue(grafo.agregarNodo(nodo));
    }

    // =======================================================
    // PRUEBAS DE AGREGAR ARISTA
    // =======================================================

    @Test 
    void agregarArista_AristaNoExiste_DeberiaRetornarTrue() {
        Grafo<String> grafo = new Grafo<>();
        Nodo<String> nodo1 = new Nodo<>("A");
        Nodo<String> nodo2 = new Nodo<>("B");
        Arista<String> arista = new Arista<>(nodo1, nodo2, new Relation(), 1.0);
        
        grafo.agregarNodo(nodo1);
        grafo.agregarNodo(nodo2);

        assertTrue(grafo.agregarArista(arista));
        assertEquals(1, grafo.obtenerAristas(nodo1).size());
        assertEquals(1, grafo.obtenerAristas(nodo2).size());
    }

    @Test 
    void agregarArista_NodoNoEstaEnGrafo_DeberiaRetornarFalse() {
        Grafo<String> grafo = new Grafo<>();
        Nodo<String> nodo1 = new Nodo<>("A");
        Nodo<String> nodo2 = new Nodo<>("B");
        Arista<String> arista = new Arista<>(nodo1, nodo2, new Relation(), 1.0);
        
        grafo.agregarNodo(nodo1);
        // nodo2 NO se agrega al grafo

        assertFalse(grafo.agregarArista(arista));
    }

    @Test 
    void agregarArista_AristaDuplicada_DeberiaRetornarFalse() {
        Grafo<String> grafo = new Grafo<>();
        Nodo<String> nodo1 = new Nodo<>("A");
        Nodo<String> nodo2 = new Nodo<>("B");
        Arista<String> arista = new Arista<>(nodo1, nodo2, new Relation(), 1.0);
        
        grafo.agregarNodo(nodo1);
        grafo.agregarNodo(nodo2);
        grafo.agregarArista(arista);

        assertFalse(grafo.agregarArista(arista));
    }

    // =======================================================
    // PRUEBAS DE OBTENER VECINOS Y ARISTAS
    // =======================================================

    @Test 
    void obtenerVecinos_NodoTieneVecinos_DeberiaRetornarLaMismaLista() {
        Grafo<String> grafo = new Grafo<>();
        Nodo<String> nodo1 = new Nodo<>("A");
        Nodo<String> nodo2 = new Nodo<>("B");
        Arista<String> arista = new Arista<>(nodo1, nodo2, new Relation(), 1.0);
        
        grafo.agregarNodo(nodo1);
        grafo.agregarNodo(nodo2);
        grafo.agregarArista(arista);

        List<Nodo<String>> esperado = new ArrayList<>();
        esperado.add(nodo2);

        assertEquals(esperado, grafo.obtenerVecinos(nodo1));
    }

    @Test 
    void obtenerVecinos_NodoNoExisteEnElMap_DeberiaRetornarListaVacia() {
        Grafo<String> grafo = new Grafo<>();
        Nodo<String> nodo1 = new Nodo<>("A");
        assertEquals(new ArrayList<>(), grafo.obtenerVecinos(nodo1));
    }

    @Test 
    void obtenerAristas_NodoExiste_DeberiaRetornarListaDeAristas() {
        Grafo<String> grafo = new Grafo<>();
        Nodo<String> nodo1 = new Nodo<>("A");
        Nodo<String> nodo2 = new Nodo<>("B");
        Arista<String> arista = new Arista<>(nodo1, nodo2, new Relation(), 1.0);
        
        grafo.agregarNodo(nodo1);
        grafo.agregarNodo(nodo2);
        grafo.agregarArista(arista);

        assertEquals(1, grafo.obtenerAristas(nodo1).size());
    }

    @Test 
    void obtenerAristas_NodoNoExiste_DeberiaRetornarListaVacia() {
        Grafo<String> grafo = new Grafo<>();
        Nodo<String> nodo = new Nodo<>("A");
        assertTrue(grafo.obtenerAristas(nodo).isEmpty());
    }

    // =======================================================
    // PRUEBAS DE OBTENER TODOS LOS NODOS
    // =======================================================

    @Test
    void obtenerTodosLosNodos_GrafoConNodos_DeberiaRetornarTodosLosNodos() {
        Grafo<String> grafo = new Grafo<>();
        Nodo<String> nodo1 = new Nodo<>("A");
        Nodo<String> nodo2 = new Nodo<>("B");

        grafo.agregarNodo(nodo1);
        grafo.agregarNodo(nodo2);

        List<Nodo<String>> nodos = grafo.obtenerTodosLosNodos();
        assertEquals(2, nodos.size());
        assertTrue(nodos.contains(nodo1));
        assertTrue(nodos.contains(nodo2));
    }

    @Test
    void obtenerTodosLosNodos_GrafoVacio_DeberiaRetornarListaVacia() {
        Grafo<String> grafo = new Grafo<>();
        assertTrue(grafo.obtenerTodosLosNodos().isEmpty());
    }

    // =======================================================
    // PRUEBAS DE ENCONTRAR ARISTA
    // =======================================================

    @Test
    void encontrarArista_AristaExiste_DeberiaRetornarArista() {
        Grafo<String> grafo = new Grafo<>();
        Nodo<String> nodo1 = new Nodo<>("A");
        Nodo<String> nodo2 = new Nodo<>("B");
        Arista<String> arista = new Arista<>(nodo1, nodo2, new Relation(), 1.0);

        grafo.agregarNodo(nodo1);
        grafo.agregarNodo(nodo2);
        grafo.agregarArista(arista);

        Arista<String> encontrada = grafo.encontrarArista(nodo1, nodo2);
        assertNotNull(encontrada);
        assertEquals(arista, encontrada);
    }

    @Test
    void encontrarArista_AristaNoExiste_DeberiaRetornarNull() {
        Grafo<String> grafo = new Grafo<>();
        Nodo<String> nodo1 = new Nodo<>("A");
        Nodo<String> nodo2 = new Nodo<>("B");

        grafo.agregarNodo(nodo1);
        grafo.agregarNodo(nodo2);

        assertNull(grafo.encontrarArista(nodo1, nodo2));
    }

    // =======================================================
    // PRUEBAS DE ELIMINAR ARISTA
    // =======================================================

    @Test
    void eliminarArista_AristaExiste_DeberiaEliminarEnAmbosExtremosYRetornarTrue() {
        Grafo<String> grafo = new Grafo<>();
        Nodo<String> nodo1 = new Nodo<>("A");
        Nodo<String> nodo2 = new Nodo<>("B");
        Arista<String> arista = new Arista<>(nodo1, nodo2, new Relation(), 1.0);

        grafo.agregarNodo(nodo1);
        grafo.agregarNodo(nodo2);
        grafo.agregarArista(arista);

        assertTrue(grafo.eliminarArista(nodo1, nodo2));
        assertTrue(grafo.obtenerVecinos(nodo1).isEmpty());
        assertTrue(grafo.obtenerVecinos(nodo2).isEmpty());
    }

    @Test
    void eliminarArista_AristaNoExiste_DeberiaRetornarFalse() {
        Grafo<String> grafo = new Grafo<>();
        Nodo<String> nodo1 = new Nodo<>("A");
        Nodo<String> nodo2 = new Nodo<>("B");

        grafo.agregarNodo(nodo1);
        grafo.agregarNodo(nodo2);

        assertFalse(grafo.eliminarArista(nodo1, nodo2));
    }

    // =======================================================
    // PRUEBAS DE ELIMINAR NODO
    // =======================================================

    @Test
    void eliminarNodo_NodoExisteConVecinos_DeberiaEliminarNodoYDesacoplarVecinos() {
        Grafo<String> grafo = new Grafo<>();
        Nodo<String> nodo1 = new Nodo<>("A");
        Nodo<String> nodo2 = new Nodo<>("B");
        Nodo<String> nodo3 = new Nodo<>("C");

        grafo.agregarNodo(nodo1);
        grafo.agregarNodo(nodo2);
        grafo.agregarNodo(nodo3);

        grafo.agregarArista(new Arista<>(nodo1, nodo2, new Relation(), 1.0));
        grafo.agregarArista(new Arista<>(nodo1, nodo3, new Relation(), 1.0));

        // Borramos nodo1 (que tiene a nodo2 y nodo3 como vecinos)
        assertTrue(grafo.eliminarNodo(nodo1));

        // El nodo1 no debe estar en la lista total de nodos
        assertFalse(grafo.obtenerTodosLosNodos().contains(nodo1));

        // Ni nodo2 ni nodo3 deben seguir teniendo a nodo1 como vecino
        assertFalse(grafo.obtenerVecinos(nodo2).contains(nodo1));
        assertFalse(grafo.obtenerVecinos(nodo3).contains(nodo1));
    }

    @Test
    void eliminarNodo_NodoExisteSinVecinos_DeberiaRetornarTrue() {
        Grafo<String> grafo = new Grafo<>();
        Nodo<String> nodo1 = new Nodo<>("A");
        grafo.agregarNodo(nodo1);

        assertTrue(grafo.eliminarNodo(nodo1));
        assertTrue(grafo.obtenerTodosLosNodos().isEmpty());
    }

    @Test
    void eliminarNodo_NodoNoExiste_DeberiaRetornarFalse() {
        Grafo<String> grafo = new Grafo<>();
        Nodo<String> nodo1 = new Nodo<>("A");

        assertFalse(grafo.eliminarNodo(nodo1));
    }
}