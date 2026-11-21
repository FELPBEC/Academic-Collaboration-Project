package co.edu.uptc.Controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import co.edu.uptc.Enum.InteresArea;
import co.edu.uptc.Enum.Program;
import co.edu.uptc.Enum.RelationType;
import co.edu.uptc.Model.Arista;
import co.edu.uptc.Model.Grafo;
import co.edu.uptc.Model.Nodo;
import co.edu.uptc.Model.User;

/**
 * Servicio encargado de gestionar las consultas de colaboración académica (RF05)
 * y el filtrado multicriterio sobre la red de usuarios (RF09).
 */
public class ServicioColaboracion {
    private Grafo<String> grafo;
    private Map<String, User> usuarios;

    /**
     * Construye el servicio de colaboración a partir de las estructuras en memoria.
     *
     * @param grafo    Grafo no dirigido que representa las conexiones entre usuarios.
     * @param usuarios Mapa indexado por correo electrónico con los perfiles de los usuarios.
     */
    public ServicioColaboracion(Grafo<String> grafo, Map<String, User> usuarios) {
        this.grafo = grafo;
        this.usuarios = usuarios;
    }

    /**
     * Clase auxiliar que asocia el correo de un usuario con su distancia acumulada
     * para ordenar los turnos de exploración dentro de la cola de prioridad de Dijkstra.
     */
    static class NodoDistancia implements Comparable<NodoDistancia> {
        String correo;
        double distancia;

        /**
         * Crea un registro de nodo con su distancia acumulada.
         *
         * @param correo    Correo electrónico del usuario.
         * @param distancia Costo acumulado desde el nodo origen.
         */
        public NodoDistancia(String correo, double distancia) {
            this.correo = correo;
            this.distancia = distancia;
        }

        @Override
        public int compareTo(NodoDistancia nodo) {
            int comparacionDistancia = Double.compare(this.distancia, nodo.distancia);
            if (comparacionDistancia != 0) {
                return comparacionDistancia;
            }
            return this.correo.compareTo(nodo.correo);
        }

        public String getCorreo() {
            return correo;
        }

        public void setCorreo(String correo) {
            this.correo = correo;
        }

        public double getDistancia() {
            return distancia;
        }

        public void setDistancia(double distancia) {
            this.distancia = distancia;
        }
    }

    /**
     * Evalúa si una conexión y su usuario vecino cumplen con los criterios de filtrado activos.
     * Los parámetros en {@code null} se consideran filtros no aplicados.
     *
     * @param arista       Arista que conecta al usuario actual con el vecino.
     * @param vecino       Perfil del usuario vecino en el extremo opuesto de la arista.
     * @param program      Programa académico requerido (opcional, puede ser {@code null}).
     * @param area         Área de interés requerida (opcional, puede ser {@code null}).
     * @param tipoRelacion Tipo de relación académica requerida (opcional, puede ser {@code null}).
     * @return {@code true} si la conexión y el vecino cumplen todos los filtros activos; {@code false} en caso contrario.
     */
    public boolean cumpleFiltros(Arista<String> arista, User vecino, Program program, InteresArea area, RelationType tipoRelacion) {
        if (arista == null || arista.getDatosRelacion() == null || vecino == null) {
            return false;
        }
        boolean cumpleFiltros = true;
        if (program != null) {
            cumpleFiltros = vecino.getProgram() == program;
            if (!cumpleFiltros) {
                return false;
            }
        }
        if (tipoRelacion != null) {
            cumpleFiltros = (arista.getDatosRelacion().getRelationType() == tipoRelacion);
            if (!cumpleFiltros) {
                return false;
            }
        }
        if (area != null) {
            cumpleFiltros = (vecino.getInteresArea() == area);
            if (!cumpleFiltros) {
                return false;
            }
        }
        return cumpleFiltros;
    }

    /**
     * Obtiene las conexiones directas de un usuario que satisfacen los filtros especificados (RF09).
     *
     * @param correoUsuario Correo electrónico del usuario cuyas conexiones se desean consultar.
     * @param program       Programa académico para filtrar vecinos (opcional, puede ser {@code null}).
     * @param area          Área de interés para filtrar vecinos (opcional, puede ser {@code null}).
     * @param tipoRelacion  Tipo de relación para filtrar las aristas (opcional, puede ser {@code null}).
     * @return Lista de aristas directas que cumplen con los criterios establecidos.
     */
    public List<Arista<String>> filtrarConexionesDirectas(String correoUsuario, Program program, InteresArea area, RelationType tipoRelacion) {
        Nodo<String> nodo = new Nodo<>(correoUsuario);
        List<Arista<String>> listaAristas = grafo.obtenerAristas(nodo);
        List<Arista<String>> listaAristasExitosas = new ArrayList<>();
        if (listaAristas == null) {
            return listaAristasExitosas;
        }
        for (Arista<String> arista : listaAristas) {
            String correoVecino = arista.getExtremoOpuesto(nodo).getContenido();
            if (cumpleFiltros(arista, usuarios.get(correoVecino), program, area, tipoRelacion)) {
                listaAristasExitosas.add(arista);
            }
        }
        return listaAristasExitosas;
    }

    /**
     * Calcula la cadena de colaboración más corta (menor peso acumulado) entre dos usuarios
     * mediante el algoritmo de Dijkstra, aplicando restricciones de filtrado en cada salto (RF05 y RF09).
     *
     * @param correoOrigen  Correo electrónico del usuario de partida.
     * @param correoDestino Correo electrónico del usuario objetivo.
     * @param program       Programa académico exigido a los usuarios intermediarios (opcional, puede ser {@code null}).
     * @param area          Área de interés exigida a los usuarios intermediarios (opcional, puede ser {@code null}).
     * @param tipoRelacion  Tipo de relación exigida en cada conexión de la cadena (opcional, puede ser {@code null}).
     * @return Lista ordenada de objetos {@link User} desde el origen hasta el destino, o una lista vacía si no existe ruta válida.
     */
    public List<User> buscarCadenaColaboracion(String correoOrigen, String correoDestino, Program program, InteresArea area, RelationType tipoRelacion) {
        if (correoOrigen == null || correoDestino == null || !usuarios.containsKey(correoOrigen) || !usuarios.containsKey(correoDestino)) {
            return new ArrayList<>();
        }
        Map<String, Double> listaDistancias = new HashMap<>();
        Map<String, String> listaPredecesores = new HashMap<>();
        PriorityQueue<NodoDistancia> listaEspera = new PriorityQueue<>();
        Set<String> listaVisitados = new HashSet<>();

        for (String correo : usuarios.keySet()) {
            listaDistancias.put(correo, Double.POSITIVE_INFINITY);
        }
        listaDistancias.put(correoOrigen, 0.0);
        listaEspera.add(new NodoDistancia(correoOrigen, 0.0));

        while (!listaEspera.isEmpty()) {
            NodoDistancia nodoActualD = listaEspera.poll();
            String correoActual = nodoActualD.getCorreo();

            if (correoActual.equals(correoDestino)) {
                break;
            }
            if (!listaVisitados.contains(correoActual)) {
                listaVisitados.add(correoActual);
                Nodo<String> nodoActual = new Nodo<>(correoActual);
                List<Arista<String>> vecinos = grafo.obtenerAristas(nodoActual);
                if (vecinos == null) {
                    continue;
                }
                for (Arista<String> arista : vecinos) {
                    String correoVecino = arista.getExtremoOpuesto(nodoActual).getContenido();
                    User perfilVecino = usuarios.get(correoVecino);

                    if (correoVecino.equals(correoDestino)) {
                        if (!cumpleFiltros(arista, perfilVecino, null, null, tipoRelacion)) {
                            continue;
                        }
                    } else if (!cumpleFiltros(arista, perfilVecino, program, area, tipoRelacion)) {
                        continue;
                    }

                    double nuevaDistancia = listaDistancias.get(correoActual) + arista.getPeso();
                    if (nuevaDistancia < listaDistancias.get(correoVecino)) {
                        listaDistancias.put(correoVecino, nuevaDistancia);
                        listaPredecesores.put(correoVecino, correoActual);
                        listaEspera.add(new NodoDistancia(correoVecino, nuevaDistancia));
                    }
                }
            }
        }

        if (!correoOrigen.equals(correoDestino) && !listaPredecesores.containsKey(correoDestino)) {
            return new ArrayList<>();
        } else {
            List<User> cadena = new ArrayList<>();
            String paso = correoDestino;
            while (paso != null) {
                cadena.add(usuarios.get(paso));
                paso = listaPredecesores.get(paso);
            }
            Collections.reverse(cadena);
            return cadena;
        }
    }
}