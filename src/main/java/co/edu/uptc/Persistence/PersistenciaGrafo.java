package co.edu.uptc.Persistence;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.edu.uptc.DTO.AristaDTO;
import co.edu.uptc.Mapper.AristaMapper;
import co.edu.uptc.Model.Grafo;
import co.edu.uptc.Model.Nodo;
import co.edu.uptc.Util.JacksonUtilities;
/**
 * Clase de PersistenciaGrafo encargada de guardar y cargar 
 * 
 */
public class PersistenciaGrafo {
    private final JacksonUtilities jacksonUtilities = new JacksonUtilities();
    /**Método para guardar el grafo en un archivo JSON o XML.
     * 
     * @param grafo 
     * @param rutaAristas
     * @throws IOException
     */
    public void saveGrafo( Grafo<String> grafo,String rutaAristas)throws IOException{
        List<AristaDTO> listaAristasDTO = AristaMapper.mapToDTOs(grafo);
         jacksonUtilities.writeFiles(listaAristasDTO, rutaAristas);
    }
    /**Método para cargar el grafo desde un archivo JSON o XML.
     * 
     * @param correosUsuarios lista de correos de usuarios 
     * @param rutaAristas ruta del archivo donde estan las aristas
     * @return 
     * @throws IOException excepción en caso de que no se encuentre el archivo o haya un error de lectura.
     */
    public Grafo<String> loadGrafo(List<String> correosUsuarios, String rutaAristas) throws IOException {
        Grafo<String> grafoNuevo = new Grafo<>();
        Map<String, Nodo<String>> mapaNodosPorCorreo = new HashMap<>();

        // 1. Con la lista de String de correos, creamos los nodos y los agregamos al grafo
        for (String correo : correosUsuarios) {

            Nodo<String> nodo = new Nodo<>(correo);
            grafoNuevo.agregarNodo(nodo);
            mapaNodosPorCorreo.put(correo, nodo);
        }

        // 2. Leemos los DTOs de las aristas desde el archivo
        List<AristaDTO> listaAristasDTO = jacksonUtilities.ReadFilesAristas(rutaAristas);

        // 3. Reconstruimos las conexiones usando el mapa de nodos indexado por correo
        AristaMapper.reconstructAristasInGrafo(listaAristasDTO, grafoNuevo, mapaNodosPorCorreo);

        return grafoNuevo;
    }
}
