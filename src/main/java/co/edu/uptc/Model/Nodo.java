package co.edu.uptc.Model;

import java.util.Objects;

/**
 * Representa un vértice genérico dentro de la estructura de grafo.
 * Encapsula la información del elemento contenido y delega las comparaciones
 * de igualdad y generación de hash a dicho contenido.
 *
 * @author Julian Moreno
 * @version 1.0
 * @param <T> Tipo de dato almacenado dentro del nodo.
 */
public class Nodo<T> {

    /** Contenido o entidad del dominio almacenada en el nodo. */
    private T contenido;

    /**
     * Construye un nuevo nodo con el contenido especificado.
     *
     * @param contenido Elemento de tipo {@code T} que contendrá el nodo.
     */
    public Nodo(T contenido) {
        this.contenido = contenido;
    }

    /**
     * Obtiene el contenido almacenado en el nodo.
     *
     * @return Objeto de tipo {@code T} encapsulado.
     */
    public T getContenido() {
        return contenido;
    }

    /**
     * Actualiza el contenido almacenado en el nodo.
     *
     * @param contenido Nuevo objeto de tipo {@code T} a asignar.
     */
    public void setContenido(T contenido) {
        this.contenido = contenido;
    }

    /**
     * Genera un código hash delegado al contenido del nodo.
     *
     * @return Entero que representa el hash del contenido.
     */
    @Override 
    public int hashCode() {
        return Objects.hashCode(contenido);
    }

    /**
     * Evalúa si dos nodos son iguales comparando el contenido encapsulado.
     *
     * @param object Objeto a comparar contra esta instancia.
     * @return {@code true} si ambos nodos contienen el mismo objeto (según su {@code equals}); {@code false} en caso contrario.
     */
    @Override 
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || !(object instanceof Nodo)) return false;
        Nodo<?> nodoObject = (Nodo<?>) object;
        return Objects.equals(this.contenido, nodoObject.contenido);
    }
}