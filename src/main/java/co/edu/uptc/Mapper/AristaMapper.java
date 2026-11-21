package co.edu.uptc.Mapper;

import co.edu.uptc.Model.Arista;
import co.edu.uptc.Model.Grafo;
import co.edu.uptc.Model.Nodo;
import co.edu.uptc.DTO.AristaDTO;

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
    public static List<AristaDTO> mapToDTOs(Grafo<String> grafo) {
        List<AristaDTO> listaDTOs = new ArrayList<>();
        Set<Arista<String>> aristasProcesadas = new HashSet<>(); // Evita duplicar aristas no dirigidas

        for (Nodo<String> nodo : grafo.obtenerTodosLosNodos()) {
            for (Arista<String> arista : grafo.obtenerAristas(nodo)) {
                
                // Si la arista ya fue procesada en el otro extremo, la saltamos
                if (!aristasProcesadas.contains(arista)) {
                    String origenId = arista.getNodoInicial().getContenido();
                    String destinoId = arista.getNodoFinal().getContenido();

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
    public static void reconstructAristasInGrafo(List<AristaDTO> aristasDTO, Grafo<String> grafo, Map<String, Nodo<String>> mapaNodosPorId) {

        for (AristaDTO dto : aristasDTO) {
            // Buscamos los nodos reales en memoria usando sus IDs en tiempo O(1)
            Nodo<String> origen = mapaNodosPorId.get(dto.getOrigenId());
            Nodo<String> destino = mapaNodosPorId.get(dto.getDestinoId());

            // Validamos que ambos nodos existan antes de conectar
            if (origen != null && destino != null) {
                Arista<String> arista = new Arista<>(origen, destino, dto.getDatosRelacion());
                // Inyectamos la arista reconstruida al grafo
                grafo.agregarArista(arista);
            }
        }
    }
}
