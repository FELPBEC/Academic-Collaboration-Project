package co.edu.uptc.Persistence;

import co.edu.uptc.Model.Grafo;
import co.edu.uptc.Model.Nodo;
import co.edu.uptc.Util.JacksonUtilities;
import co.edu.uptc.DTO.AristaDTO;
import co.edu.uptc.Interface.Identificable;
import co.edu.uptc.Mapper.AristaMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**Clase PeristenceGestor: Se encarga de cargar y guardar los grafos en archivos JSON o XML
 * separa las aristas de los nodos respectivamente
 * 
 * PersistenceGestor
 */
public class PersistenceGestor {
AristaMapper aristaMapper= new AristaMapper();
JacksonUtilities jacksonUtilities= new JacksonUtilities();
    /**
     * Guarda el grafo completo (nodos y aristas) en archivos independientes (JSON o XML).
     */
    public <T extends Identificable> void saveGrafo(Grafo<T> grafo, String rutaNodos, String rutaAristas) throws IOException {

        // 1. Extraemos los datos puros usando el mapeador que ya hicimos
        List<T> listaNodos = new ArrayList<>();
        for (Nodo<T> nodo : grafo.obtenerTodosLosNodos()) {
            listaNodos.add(nodo.getContenido());
        }
        List<AristaDTO> listaAristasDTO=aristaMapper.mapToDTOs(grafo);

        // 2. Guardamos usando Jackson (Detecta automáticamente si es JSON o XML por la extensión)
        jacksonUtilities.writeFiles(listaNodos, rutaNodos);
        jacksonUtilities.writeFiles(listaAristasDTO, rutaAristas);
    }

    /**
     * Carga y reconstruye el grafo desde los archivos de persistencia.
     */
    public <T extends Identificable> Grafo<T> loadGrafo(String rutaNodos, String rutaAristas, Class<T> claseTipo) throws IOException {

        Grafo<T> grafoNuevo = new Grafo<>();
        Map<Integer, Nodo<T>> mapaNodosPorId = new HashMap<>();

        // 1. Leemos los nodos desde el archivo (JSON o XML)
        List<T> listaNodos = jacksonUtilities.ReadFilesList(rutaNodos, claseTipo);
        for (T elemento : listaNodos) {
            Nodo<T> nodo = new Nodo<>(elemento);
            grafoNuevo.agregarNodo(nodo);
            mapaNodosPorId.put(elemento.getId(), nodo);
        }

        // 2. Leemos los DTOs de las aristas desde el archivo
        List<AristaDTO> listaAristasDTO = jacksonUtilities.ReadFilesAristas(rutaAristas);

        // 3. Reconstruimos las conexiones en el grafo usando el AristaMapper
        aristaMapper.reconstructAristasInGrafo(listaAristasDTO, grafoNuevo, mapaNodosPorId);

        return grafoNuevo;
    }
}