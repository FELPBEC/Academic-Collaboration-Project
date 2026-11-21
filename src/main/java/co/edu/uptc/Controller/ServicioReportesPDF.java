package co.edu.uptc.Controller;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import co.edu.uptc.Enum.InteresArea;
import co.edu.uptc.Enum.Program;
import co.edu.uptc.Enum.RelationType;
import co.edu.uptc.Model.Arista;
import co.edu.uptc.Model.Nodo;
import co.edu.uptc.Model.Relation;
import co.edu.uptc.Model.User;

/**
 * Servicio encargado de generar informes institucionales en formato PDF (RF11)
 * sobre las consultas de conexiones filtradas y cadenas de colaboración académica,
 * almacenando los documentos fuera del paquete ejecutable del proyecto.
 */
public class ServicioReportesPDF {

    private static final String NOMBRE_CARPETA_EXTERNA = "Reportes_RedAcademica";

    private static final BaseColor COLOR_PRIMARIO = new BaseColor(24, 58, 90);
    private static final BaseColor COLOR_FILA_ALTERNA = new BaseColor(242, 245, 249);

    private static final Font FUENTE_TITULO = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, COLOR_PRIMARIO);
    private static final Font FUENTE_SECCION = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, COLOR_PRIMARIO);
    private static final Font FUENTE_ENCABEZADO = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE);
    private static final Font FUENTE_TEXTO = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.BLACK);
    private static final Font FUENTE_NEGRITA = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.BLACK);

    private Map<String, User> usuarios;

    /**
     * Bloque de contenido que se escribe dentro de un documento PDF ya abierto.
     */
    @FunctionalInterface
    private interface ContenidoPdf {
        void escribir(Document documento) throws Exception;
    }

    /**
     * Construye el servicio de generación de reportes PDF.
     *
     * @param usuarios Mapa indexado por correo electrónico con los perfiles de los usuarios registrados.
     */
    public ServicioReportesPDF(Map<String, User> usuarios) {
        this.usuarios = usuarios;
    }

    /**
     * Obtiene y crea (si no existe) el directorio externo por defecto en la carpeta personal
     * del usuario del sistema operativo para evitar conflictos dentro del ejecutable.
     *
     * @return Ruta absoluta del directorio externo donde se almacenan los reportes.
     * @throws IOException Si ocurre un error de permisos o entrada/salida al crear el directorio.
     */
    public static Path obtenerDirectorioReportesExterno() throws IOException {
        Path directorioExterno = Paths.get(System.getProperty("user.home"), NOMBRE_CARPETA_EXTERNA);
        Files.createDirectories(directorioExterno);
        return directorioExterno;
    }

    /**
     * Resuelve la ruta final del archivo PDF. Si se proporciona únicamente un nombre de archivo,
     * lo ubica dentro del directorio externo por defecto; si se proporciona una ruta completa,
     * garantiza que sus directorios padre existan.
     *
     * @param rutaONombreArchivo Nombre del archivo o ruta completa de destino.
     * @return Ruta normalizada con extensión {@code .pdf} lista para escritura.
     * @throws IOException Si no es posible crear los directorios de destino.
     */
    public static Path resolverRutaSalida(String rutaONombreArchivo) throws IOException {
        if (rutaONombreArchivo == null || rutaONombreArchivo.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre o ruta del archivo PDF no puede estar vacío.");
        }
        String nombreNormalizado = rutaONombreArchivo.trim();
        if (!nombreNormalizado.toLowerCase().endsWith(".pdf")) {
            nombreNormalizado += ".pdf";
        }

        Path ruta = Paths.get(nombreNormalizado);
        if (ruta.getParent() == null) {
            return obtenerDirectorioReportesExterno().resolve(ruta.getFileName());
        }
        Files.createDirectories(ruta.getParent());
        return ruta;
    }

    /**
     * Crea el archivo PDF, ejecuta el contenido recibido y cierra todo de forma segura.
     * <p>
     * El flujo de salida lo cierra únicamente el bloque try-with-resources (iText no lo cierra
     * por su cuenta), de modo que no hay dobles cierres. Si ocurre un error se conserva la
     * excepción original y se elimina el archivo parcial.
     *
     * @param rutaONombreArchivo Nombre del archivo o ruta completa de destino.
     * @param contenido          Bloque que agrega los elementos al documento.
     * @return Ruta final del archivo PDF generado.
     * @throws Exception Si falla la creación del archivo o la construcción del PDF.
     */
    private Path escribirPdf(String rutaONombreArchivo, ContenidoPdf contenido) throws Exception {
        Path rutaFinal = resolverRutaSalida(rutaONombreArchivo);
        Document documento = new Document(PageSize.A4, 36, 36, 40, 40);

        try (OutputStream salida = new BufferedOutputStream(Files.newOutputStream(rutaFinal))) {
            PdfWriter escritor = PdfWriter.getInstance(documento, salida);
            escritor.setCloseStream(false);

            documento.open();
            contenido.escribir(documento);
            documento.close();
        } catch (Exception e) {
            try {
                if (documento.isOpen()) {
                    documento.close();
                }
            } catch (Exception ignorada) {
                e.addSuppressed(ignorada);
            }
            Files.deleteIfExists(rutaFinal);
            throw e;
        }

        return rutaFinal;
    }

    /**
     * Genera un informe PDF con los resultados del filtrado multicriterio de conexiones directas (RF09 y RF11),
     * exponiendo únicamente información pública de los colaboradores.
     *
     * @param rutaONombreArchivo Nombre del archivo (se guardará fuera del proyecto) o ruta completa de destino.
     * @param solicitante        Usuario que solicita el reporte.
     * @param conexiones         Lista de aristas directas obtenidas tras aplicar los filtros.
     * @param program            Filtro de programa académico aplicado (puede ser {@code null}).
     * @param area               Filtro de área de interés aplicado (puede ser {@code null}).
     * @param tipoRelacion       Filtro de tipo de relación aplicado (puede ser {@code null}).
     * @return Ruta final del archivo PDF generado en disco.
     * @throws Exception Si el solicitante es inválido o ocurre un error al construir o escribir el PDF.
     */
    public Path generarReporteConexionesFiltradas(
            String rutaONombreArchivo,
            User solicitante,
            List<Arista<String>> conexiones,
            Program program,
            InteresArea area,
            RelationType tipoRelacion) throws Exception {

        if (solicitante == null || solicitante.getEmail() == null) {
            throw new IllegalArgumentException("El usuario solicitante es obligatorio para generar el reporte.");
        }

        return escribirPdf(rutaONombreArchivo, documento -> {
            agregarEncabezadoDocumento(documento, "REPORTE DE CONEXIONES DIRECTAS FILTRADAS");
            agregarSeccionSolicitante(documento, solicitante);
            agregarSeccionFiltros(documento, program, area, tipoRelacion);

            Paragraph tituloResultados = new Paragraph("3. Resultados de Colaboradores Encontrados", FUENTE_SECCION);
            tituloResultados.setSpacingAfter(8f);
            documento.add(tituloResultados);

            if (conexiones == null || conexiones.isEmpty()) {
                documento.add(new Paragraph("No se encontraron conexiones directas que cumplan con los filtros aplicados.", FUENTE_TEXTO));
                return;
            }

            PdfPTable tabla = new PdfPTable(6);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[] { 2.2f, 2.5f, 2.2f, 1.8f, 2.2f, 1.1f });

            tabla.addCell(crearCeldaEncabezado("Nombre"));
            tabla.addCell(crearCeldaEncabezado("Correo"));
            tabla.addCell(crearCeldaEncabezado("Programa"));
            tabla.addCell(crearCeldaEncabezado("Área de Interés"));
            tabla.addCell(crearCeldaEncabezado("Tipo de Relación"));
            tabla.addCell(crearCeldaEncabezado("Peso"));

            Nodo<String> nodoSolicitante = new Nodo<>(solicitante.getEmail());
            int investigaciones = 0;
            int mentorias = 0;
            int participaciones = 0;

            for (int i = 0; i < conexiones.size(); i++) {
                Arista<String> arista = conexiones.get(i);
                BaseColor fondo = (i % 2 == 1) ? COLOR_FILA_ALTERNA : BaseColor.WHITE;

                String correoVecino = arista.getExtremoOpuesto(nodoSolicitante).getContenido();
                User vecino = usuarios != null ? usuarios.get(correoVecino) : null;
                Relation relacion = arista.getDatosRelacion();

                String tipoTexto = "N/A";
                if (relacion != null && relacion.getRelationType() != null) {
                    tipoTexto = relacion.getRelationType().name();
                    if (relacion.getRelationType() == RelationType.InvestigationCollaboration) {
                        investigaciones++;
                    } else if (relacion.getRelationType() == RelationType.Mentoring) {
                        mentorias++;
                    } else if (relacion.getRelationType() == RelationType.JointParticipation) {
                        participaciones++;
                    }
                }

                tabla.addCell(crearCeldaDato(vecino != null ? valorSeguro(vecino.getName(), "Sin nombre") : "Desconocido", fondo));
                tabla.addCell(crearCeldaDato(correoVecino, fondo));
                tabla.addCell(crearCeldaDato(vecino != null ? nombrePrograma(vecino.getProgram()) : "N/A", fondo));
                tabla.addCell(crearCeldaDato(vecino != null ? valorSeguro(vecino.getInteresArea(), "No registra") : "N/A", fondo));
                tabla.addCell(crearCeldaDato(tipoTexto, fondo));
                tabla.addCell(crearCeldaDato(String.format("%.2f", arista.getPeso()), fondo));
            }

            documento.add(tabla);

            Paragraph resumen = new Paragraph(
                    String.format("\nResumen: %d conexiones encontradas (Investigación: %d | Mentoría: %d | Participación Conjunta: %d).",
                            conexiones.size(), investigaciones, mentorias, participaciones),
                    FUENTE_NEGRITA);
            resumen.setSpacingBefore(8f);
            documento.add(resumen);
        });
    }

    /**
     * Genera un informe PDF con la cadena óptima de colaboración hallada entre un usuario origen
     * y un usuario destino mediante Dijkstra y filtros multicriterio (RF05, RF09 y RF11).
     *
     * @param rutaONombreArchivo Nombre del archivo (se guardará fuera del proyecto) o ruta completa de destino.
     * @param solicitante        Usuario que inicia la búsqueda de colaboración.
     * @param destino            Usuario objetivo de la cadena de colaboración (puede ser {@code null}).
     * @param cadena             Lista ordenada de usuarios que conforman la ruta desde origen hasta destino.
     * @param program            Filtro de programa académico aplicado a intermediarios (puede ser {@code null}).
     * @param area               Filtro de área de interés aplicado a intermediarios (puede ser {@code null}).
     * @param tipoRelacion       Filtro de tipo de relación aplicado en la cadena (puede ser {@code null}).
     * @return Ruta final del archivo PDF generado en disco.
     * @throws Exception Si ocurre un error al construir o escribir el archivo PDF.
     */
    public Path generarReporteCadenaColaboracion(
            String rutaONombreArchivo,
            User solicitante,
            User destino,
            List<User> cadena,
            Program program,
            InteresArea area,
            RelationType tipoRelacion) throws Exception {

        return escribirPdf(rutaONombreArchivo, documento -> {
            agregarEncabezadoDocumento(documento, "REPORTE DE CADENA DE COLABORACIÓN ÓPTIMA");
            agregarSeccionSolicitante(documento, solicitante);

            if (destino != null) {
                Paragraph infoDestino = new Paragraph(
                        "Usuario Objetivo: " + valorSeguro(destino.getName(), "Sin nombre") + " (" + destino.getEmail() + ")",
                        FUENTE_NEGRITA);
                infoDestino.setSpacingAfter(10f);
                documento.add(infoDestino);
            }

            agregarSeccionFiltros(documento, program, area, tipoRelacion);

            Paragraph tituloRuta = new Paragraph("3. Ruta de Colaboración Sugerida", FUENTE_SECCION);
            tituloRuta.setSpacingAfter(8f);
            documento.add(tituloRuta);

            if (cadena == null || cadena.isEmpty()) {
                documento.add(new Paragraph("No existe una cadena de colaboración entre los usuarios bajo los filtros seleccionados.", FUENTE_TEXTO));
                return;
            }

            PdfPTable tabla = new PdfPTable(5);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[] { 1.0f, 2.5f, 2.7f, 2.3f, 2.0f });

            tabla.addCell(crearCeldaEncabezado("Paso"));
            tabla.addCell(crearCeldaEncabezado("Nombre"));
            tabla.addCell(crearCeldaEncabezado("Correo"));
            tabla.addCell(crearCeldaEncabezado("Programa"));
            tabla.addCell(crearCeldaEncabezado("Área de Interés"));

            for (int i = 0; i < cadena.size(); i++) {
                User integrante = cadena.get(i);
                BaseColor fondo = (i % 2 == 1) ? COLOR_FILA_ALTERNA : BaseColor.WHITE;

                tabla.addCell(crearCeldaDato(String.valueOf(i + 1), fondo));
                tabla.addCell(crearCeldaDato(valorSeguro(integrante.getName(), "Sin nombre"), fondo));
                tabla.addCell(crearCeldaDato(valorSeguro(integrante.getEmail(), "Sin correo"), fondo));
                tabla.addCell(crearCeldaDato(nombrePrograma(integrante.getProgram()), fondo));
                tabla.addCell(crearCeldaDato(valorSeguro(integrante.getInteresArea(), "No registra"), fondo));
            }

            documento.add(tabla);

            int saltos = Math.max(0, cadena.size() - 1);
            Paragraph resumen = new Paragraph(
                    String.format("\nResumen: Cadena conformada por %d integrantes (%d saltos de conexión).",
                            cadena.size(), saltos),
                    FUENTE_NEGRITA);
            resumen.setSpacingBefore(8f);
            documento.add(resumen);
        });
    }

    /**
     * Agrega el título principal y la fecha y hora de generación al documento.
     *
     * @param documento Documento PDF abierto.
     * @param titulo    Título institucional del informe.
     * @throws DocumentException Si ocurre un error al insertar los párrafos.
     */
    private void agregarEncabezadoDocumento(Document documento, String titulo) throws DocumentException {
        Paragraph pTitulo = new Paragraph(titulo, FUENTE_TITULO);
        pTitulo.setAlignment(Element.ALIGN_CENTER);
        pTitulo.setSpacingAfter(4f);
        documento.add(pTitulo);

        String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        Paragraph pFecha = new Paragraph("Fecha de generación: " + fechaHora, FUENTE_TEXTO);
        pFecha.setAlignment(Element.ALIGN_CENTER);
        pFecha.setSpacingAfter(14f);
        documento.add(pFecha);
    }

    /**
     * Agrega la sección con los datos públicos del usuario solicitante del reporte.
     *
     * @param documento   Documento PDF abierto.
     * @param solicitante Perfil del usuario que generó la consulta.
     * @throws DocumentException Si ocurre un error al insertar la tabla.
     */
    private void agregarSeccionSolicitante(Document documento, User solicitante) throws DocumentException {
        Paragraph titulo = new Paragraph("1. Información del Solicitante", FUENTE_SECCION);
        titulo.setSpacingAfter(6f);
        documento.add(titulo);

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[] { 1.2f, 3.8f });
        tabla.setSpacingAfter(12f);

        agregarFilaClaveValor(tabla, "Nombre:", solicitante != null ? valorSeguro(solicitante.getName(), "No registra") : "N/A");
        agregarFilaClaveValor(tabla, "Correo:", solicitante != null ? valorSeguro(solicitante.getEmail(), "No registra") : "N/A");
        agregarFilaClaveValor(tabla, "Programa:", solicitante != null ? nombrePrograma(solicitante.getProgram()) : "N/A");
        agregarFilaClaveValor(tabla, "Área de Interés:", solicitante != null ? valorSeguro(solicitante.getInteresArea(), "No registra") : "N/A");

        documento.add(tabla);
    }

    /**
     * Agrega la sección que detalla los parámetros de filtrado aplicados en la consulta.
     *
     * @param documento    Documento PDF abierto.
     * @param program      Programa académico filtrado (o {@code null}).
     * @param area         Área de interés filtrada (o {@code null}).
     * @param tipoRelacion Tipo de relación filtrada (o {@code null}).
     * @throws DocumentException Si ocurre un error al insertar la tabla.
     */
    private void agregarSeccionFiltros(Document documento, Program program, InteresArea area, RelationType tipoRelacion) throws DocumentException {
        Paragraph titulo = new Paragraph("2. Filtros Aplicados en la Consulta", FUENTE_SECCION);
        titulo.setSpacingAfter(6f);
        documento.add(titulo);

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[] { 1.2f, 3.8f });
        tabla.setSpacingAfter(12f);

        agregarFilaClaveValor(tabla, "Programa:", nombrePrograma(program) == null ? "Todos" : nombrePrograma(program));
        agregarFilaClaveValor(tabla, "Área de Interés:", valorSeguro(area, "Todas"));
        agregarFilaClaveValor(tabla, "Tipo de Relación:", valorSeguro(tipoRelacion, "Todas"));

        documento.add(tabla);
    }

    /**
     * Inserta una fila de dos columnas (etiqueta y valor) en una tabla de resumen.
     *
     * @param tabla Tabla destino.
     * @param clave Etiqueta descriptiva en negrita.
     * @param valor Contenido asociado.
     */
    private void agregarFilaClaveValor(PdfPTable tabla, String clave, String valor) {
        PdfPCell celdaClave = new PdfPCell(new Phrase(clave, FUENTE_NEGRITA));
        celdaClave.setBackgroundColor(COLOR_FILA_ALTERNA);
        celdaClave.setPadding(5f);

        PdfPCell celdaValor = new PdfPCell(new Phrase(valor, FUENTE_TEXTO));
        celdaValor.setPadding(5f);

        tabla.addCell(celdaClave);
        tabla.addCell(celdaValor);
    }

    /**
     * Crea una celda estilizada para los encabezados de tabla.
     *
     * @param texto Título de la columna.
     * @return Celda formateada con fondo primario y texto blanco centrado.
     */
    private PdfPCell crearCeldaEncabezado(String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, FUENTE_ENCABEZADO));
        celda.setBackgroundColor(COLOR_PRIMARIO);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setPadding(6f);
        return celda;
    }

    /**
     * Crea una celda estándar para los registros de una tabla.
     *
     * @param texto Contenido textual de la celda.
     * @param fondo Color de relleno de la fila.
     * @return Celda formateada con relleno y espaciado uniforme.
     */
    private PdfPCell crearCeldaDato(String texto, BaseColor fondo) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, FUENTE_TEXTO));
        celda.setBackgroundColor(fondo);
        celda.setPadding(5f);
        return celda;
    }

    /**
     * Devuelve la representación textual de un objeto o un texto alternativo si es nulo o vacío.
     *
     * @param valor   Objeto a convertir en texto.
     * @param defecto Texto por defecto si el valor es nulo o está en blanco.
     * @return Cadena segura para impresión en el PDF.
     */
    private String valorSeguro(Object valor, String defecto) {
        return (valor == null || valor.toString().trim().isEmpty()) ? defecto : valor.toString();
    }

    /**
     * Convierte un programa académico a su denominación institucional visible.
     *
     * @param program programa académico o {@code null}
     * @return nombre completo del programa o {@code null} si no está definido
     */
    private String nombrePrograma(Program program) {
        return program != null ? program.getDisplayName() : null;
    }
}