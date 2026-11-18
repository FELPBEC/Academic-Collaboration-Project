package co.edu.uptc.Mapper;

import co.edu.uptc.Model.Arista;
import co.edu.uptc.Model.Grafo;
import co.edu.uptc.Model.Nodo;
import co.edu.uptc.DTO.AristaDTO;
import co.edu.uptc.Interface.Identificable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AristaMapper {

    /**
     * CONVERSIÓN 1: Del Grafo (Memoria) hacia la Lista DTO (Archivo)
     * Extrae las conexiones usando únicamente los IDs de los elementos identificables.
     */
    public <T extends Identificable> List<AristaDTO> mapToDTOs(Grafo<T> grafo) {
        List<AristaDTO> listaDTOs = new ArrayList<>();
        Set<Arista<T>> aristasProcesadas = new HashSet<>(); // Evita duplicar aristas no dirigidas

        for (Nodo<T> nodo : grafo.obtenerTodosLosNodos()) {
            for (Arista<T> arista : grafo.obtenerAristas(nodo)) {
                
                // Si la arista ya fue procesada en el otro extremo, la saltamos
                if (!aristasProcesadas.contains(arista)) {
                    int origenId = arista.getNodoInicial().getContenido().getId();
                    int destinoId = arista.getNodoFinal().getContenido().getId();

                    listaDTOs.add(new AristaDTO(origenId, destinoId, arista.getDatosRelacion()));

                    aristasProcesadas.add(arista);
                }
            }
        }
        return listaDTOs;
    }

    /**
     * CONVERSIÓN 2: De la Lista DTO (Archivo) hacia el Grafo (Memoria)
     * Reconstruye los objetos Arista reales buscando los nodos en un mapa de índice rápido.
     */
    public <T extends Identificable> void reconstructAristasInGrafo(List<AristaDTO> aristasDTO, Grafo<T> grafo, Map<Integer, Nodo<T>> mapaNodosPorId) {

        for (AristaDTO dto : aristasDTO) {
            // Buscamos los nodos reales en memoria usando sus IDs en tiempo O(1)
            Nodo<T> origen = mapaNodosPorId.get(dto.getOrigenId());
            Nodo<T> destino = mapaNodosPorId.get(dto.getDestinoId());

            // Validamos que ambos nodos existan antes de conectar
            if (origen != null && destino != null) {
                Arista<T> arista = new Arista<>(origen, destino, dto.getDatosRelacion());
                // Inyectamos la arista reconstruida al grafo
                grafo.agregarArista(arista);
            }
        }
    }
}
