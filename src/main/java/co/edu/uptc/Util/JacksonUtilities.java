package co.edu.uptc.Util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import co.edu.uptc.DTO.AristaDTO;

public class JacksonUtilities {
    // =========================================================================
    // MÉTODOS AUXILIARES USANDO JACKSON (JSON / XML)
    // =========================================================================

    public void writeFiles(Object objeto, String ruta) throws IOException {
        ObjectMapper mapper = getMapper(ruta);
        // Habilita indentación bonita (pretty print) para que el archivo sea legible por humanos
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(new File(ruta), objeto);
    }

    public <T> List<T> ReadFilesList(String ruta, Class<T> claseTipo) throws IOException {
        ObjectMapper mapper = getMapper(ruta);
        // Usamos TypeReference para leer colecciones genéricas correctamente en Jackson
        return mapper.readValue(new File(ruta), mapper.getTypeFactory().constructCollectionType(List.class, claseTipo));
    }

    public List<AristaDTO> ReadFilesAristas(String ruta) throws IOException {
        ObjectMapper mapper = getMapper(ruta);
        return mapper.readValue(new File(ruta), new TypeReference<List<AristaDTO>>() {});
    }

    /**
     * Selector inteligente: Si la ruta termina en .xml usa XmlMapper, de lo contrario usa ObjectMapper estándar (JSON).
     */
    private ObjectMapper getMapper(String ruta) {
        if (ruta != null && ruta.toLowerCase().endsWith(".xml")) {
            return new XmlMapper();
        }
        return new ObjectMapper(); // Por defecto JSON
    }
}
