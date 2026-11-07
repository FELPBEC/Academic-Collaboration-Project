package co.edu.uptc.Model;

/**
 * Representa una conexión de doble vía (no dirigida) entre dos nodos de tipo {@code T}.
 * Almacena el peso de la conexión y la información contextual de la relación.
 *
 * @author Julian Moreno
 * @version 1.0
 * @param <T> Tipo de dato almacenado en los nodos que conecta la arista.
 */
public class Arista<T> {

    /** Nodo situado en uno de los extremos de la arista. */
    private Nodo<T> nodoInicial;

    /** Nodo situado en el extremo opuesto de la arista. */
    private Nodo<T> nodoFinal;

    /** Información detallada de la relación entre los nodos conectados. */
    private Relation datosRelacion;

    /** Peso o costo numérico de la conexión entre los nodos. */
    private double peso;

    /**
     * Construye una nueva arista entre dos nodos con una relación y peso asociados.
     *
     * @param nodoInicial   Primer extremo del enlace.
     * @param nodoFinal     Segundo extremo del enlace.
     * @param datosRelacion Información contextual o tipo de relación.
     * @param peso          Costo o métrica asociada a la arista.
     */
    public Arista(Nodo<T> nodoInicial, Nodo<T> nodoFinal, Relation datosRelacion, double peso) {
        this.nodoInicial = nodoInicial;
        this.nodoFinal = nodoFinal;
        this.datosRelacion = datosRelacion;
        this.peso = peso;
    }

    /**
     * Dado uno de los extremos de la arista, retorna el nodo ubicado en el extremo opuesto.
     *
     * @param nodo Nodo de referencia para encontrar su contraparte.
     * @return El {@link Nodo} opuesto si el nodo suministrado pertenece a la arista; {@code null} si no pertenece.
     */
    public Nodo<T> getExtremoOpuesto(Nodo<T> nodo) {
        if (nodoInicial.equals(nodo)) return nodoFinal;
        if (nodoFinal.equals(nodo)) return nodoInicial;
        return null;
    }

    /**
     * Obtiene el nodo inicial de la arista.
     *
     * @return Instancia del {@link Nodo} inicial.
     */
    public Nodo<T> getNodoInicial() {
        return nodoInicial;
    }

    /**
     * Establece el nodo inicial de la arista.
     *
     * @param nodoInicial Nuevo {@link Nodo} a asignar como extremo inicial.
     */
    public void setNodoInicial(Nodo<T> nodoInicial) {
        this.nodoInicial = nodoInicial;
    }

    /**
     * Obtiene el nodo final de la arista.
     *
     * @return Instancia del {@link Nodo} final.
     */
    public Nodo<T> getNodoFinal() {
        return nodoFinal;
    }

    /**
     * Establece el nodo final de la arista.
     *
     * @param nodoFinal Nuevo {@link Nodo} a asignar como extremo final.
     */
    public void setNodoFinal(Nodo<T> nodoFinal) {
        this.nodoFinal = nodoFinal;
    }

    /**
     * Obtiene los datos detallados de la relación.
     *
     * @return Objeto {@link Relation} asociado a la arista.
     */
    public Relation getDatosRelacion() {
        return datosRelacion;
    }

    /**
     * Asigna la información detallada de la relación.
     *
     * @param datosRelacion Objeto {@link Relation} a vincular.
     */
    public void setDatosRelacion(Relation datosRelacion) {
        this.datosRelacion = datosRelacion;
    }

    /**
     * Obtiene el peso o costo numérico de la arista.
     *
     * @return Valor numérico de tipo {@code double}.
     */
    public double getPeso() {
        return peso;
    }

    /**
     * Asigna el peso o costo numérico de la arista.
     *
     * @param peso Nuevo valor numérico a asignar.
     */
    public void setPeso(double peso) {
        this.peso = peso;
    }
}