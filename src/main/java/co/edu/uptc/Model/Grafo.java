package co.edu.uptc.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Estructura de datos de Grafo No Dirigido implementada mediante lista de adyacencia.
 * Permite gestionar vértices genéricos y aristas ponderadas con datos de relación.
 *
 * @author Julian Moreno
 * @version 1.0
 * @param <T> Tipo de dato almacenado en los nodos del grafo.
 */
public class Grafo<T> {

    /** Mapa de adyacencias que relaciona cada nodo con su lista de aristas incidentes. */
    private Map<Nodo<T>, List<Arista<T>>> adyacencias;

    /**
     * Construye un grafo vacío e inicializa la estructura de adyacencias.
     */
    public Grafo() {
        this.adyacencias = new HashMap<>();
    }

    /**
     * Agrega un nuevo nodo al grafo si no existe previamente.
     *
     * @param nodo Instancia de {@link Nodo} a incorporar.
     * @return {@code true} si el nodo fue agregado exitosamente; {@code false} si ya existía en el grafo.
     */
    public boolean agregarNodo(Nodo<T> nodo) {
        if (!adyacencias.containsKey(nodo)) {
            this.adyacencias.put(nodo, new ArrayList<>());
            return true;
        }
        return false;
    }

    /**
     * Agrega una arista no dirigida entre dos nodos existentes en el grafo.
     * La arista se enlaza en las listas de adyacencia de ambos extremos.
     *
     * @param arista Instancia de {@link Arista} que conecta los dos nodos.
     * @return {@code true} si la arista fue agregada con éxito; {@code false} si alguno de los nodos no existe o la conexión ya está presente.
     */
    public boolean agregarArista(Arista<T> arista) {
        if (adyacencias.containsKey(arista.getNodoInicial()) && 
            adyacencias.containsKey(arista.getNodoFinal()) && 
            !existeArista(arista.getNodoInicial(), arista.getNodoFinal())) {
            
            adyacencias.get(arista.getNodoFinal()).add(arista);
            adyacencias.get(arista.getNodoInicial()).add(arista);
            return true;
        }
        return false;
    }

    /**
     * Obtiene la lista de nodos vecinos adyacentes a un nodo dado.
     *
     * @param nodo Nodo del cual se desean consultar los vecinos.
     * @return Lista de instancias de {@link Nodo} conectadas directamente; lista vacía si el nodo no existe o carece de vecinos.
     */
    public List<Nodo<T>> obtenerVecinos(Nodo<T> nodo) {
        List<Nodo<T>> vecinos = new ArrayList<>();
        if (adyacencias.containsKey(nodo)) {
            for (Arista<T> arista : adyacencias.get(nodo)) {
                vecinos.add(arista.getExtremoOpuesto(nodo));
            }
        }
        return vecinos;
    }

    /**
     * Obtiene la lista de aristas incidentes asociadas a un nodo específico.
     *
     * @param nodo Nodo del cual se consultarán sus aristas.
     * @return Lista de objetos {@link Arista} conectados al nodo; lista vacía si el nodo no existe.
     */
    public List<Arista<T>> obtenerAristas(Nodo<T> nodo) {
        if (adyacencias.containsKey(nodo)) {
            return adyacencias.get(nodo);
        }
        return new ArrayList<>();
    }

    /**
     * Retorna una colección con todos los nodos registrados actualmente en el grafo.
     *
     * @return Lista que contiene la totalidad de los vértices ({@link Nodo}).
     */
    public List<Nodo<T>> obtenerTodosLosNodos() {
        return new ArrayList<>(adyacencias.keySet());
    }

    /**
     * Busca y retorna la arista que conecta directamente a dos nodos dados.
     *
     * @param origen  Nodo extremo de partida.
     * @param destino Nodo extremo opuesto.
     * @return La instancia de {@link Arista} que los enlaza; {@code null} si no existe tal conexión.
     */
    public Arista<T> encontrarArista(Nodo<T> origen, Nodo<T> destino) {
        if (existeArista(origen, destino)) {
            for (Arista<T> arista : adyacencias.get(origen)) {
                if (arista.getExtremoOpuesto(origen).equals(destino)) {
                    return arista;
                }
            }
        }
        return null;
    }

    /**
     * Elimina un nodo del grafo y remueve todas las aristas asociadas en sus vecinos.
     *
     * @param nodo Nodo a remover.
     * @return {@code true} si el nodo existía y fue eliminado junto con sus enlaces; {@code false} en caso contrario.
     */
    public boolean eliminarNodo(Nodo<T> nodo) {
        if (adyacencias.containsKey(nodo)) {
            List<Nodo<T>> listaVecinos = obtenerVecinos(nodo);
            for (Nodo<T> vecino : listaVecinos) {
                eliminarArista(nodo, vecino);
            }
            adyacencias.remove(nodo);
            return true;
        }
        return false;
    }

    /**
     * Elimina la arista entre dos nodos, removiéndola de ambas listas de adyacencia.
     *
     * @param origen  Primer nodo extremo de la conexión.
     * @param destino Segundo nodo extremo de la conexión.
     * @return {@code true} si la arista fue localizada y eliminada; {@code false} si no existía la arista o alguno de los nodos.
     */
    public boolean eliminarArista(Nodo<T> origen, Nodo<T> destino) {
        Arista<T> arista = encontrarArista(origen, destino);
        if (arista != null) {
            adyacencias.get(origen).remove(arista);
            adyacencias.get(destino).remove(arista);
            return true;
        }
        return false;
    }

    /**
     * Método auxiliar privado para verificar si existe una arista enlazando dos nodos.
     *
     * @param origen  Nodo de origen a consultar.
     * @param destino Nodo de destino a verificar.
     * @return {@code true} si existe una arista conectando ambos nodos; {@code false} de lo contrario.
     */
    private boolean existeArista(Nodo<T> origen, Nodo<T> destino) {
        if (!adyacencias.containsKey(origen) || !adyacencias.containsKey(destino)) {
            return false;
        }
        for (Arista<T> arista : adyacencias.get(origen)) {
            if (arista.getExtremoOpuesto(origen).equals(destino)) {
                return true;
            }
        }
        return false;
    }
}