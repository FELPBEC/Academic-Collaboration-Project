package co.edu.uptc.Controller;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import co.edu.uptc.Enum.TypeUser;
import co.edu.uptc.Model.Arista;
import co.edu.uptc.Model.Grafo;
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
    private Grafo<String> grafo;

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
        this.grafo = null;
    }

    /**
     * Construye el servicio de reportes con acceso al grafo de relaciones.
     *
     * @param usuarios mapa indexado por correo electrónico
     * @param grafo grafo con las relaciones entre participantes
     */
    public ServicioReportesPDF(Map<String, User> usuarios, Grafo<String> grafo) {
        this.usuarios = usuarios;
        this.grafo = grafo;
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
     * Genera un informe PDF con los resultados de la pantalla de búsqueda de usuarios.
     * Incluye los filtros por programa, área de interés y puntuación de relaciones
     * relevantes, y muestra el detalle de la actividad registrada para cada participante.
     *
     * @param rutaONombreArchivo nombre o ruta completa del archivo PDF
     * @param solicitante usuario que realizó la búsqueda
     * @param participantes resultados ya filtrados por la búsqueda
     * @param program filtro de programa aplicado o {@code null}
     * @param area filtro de área de interés aplicado o {@code null}
     * @param puntuacionMinima puntuación mínima de relaciones relevantes
     * @return ruta final del archivo PDF generado
     * @throws Exception si los datos son inválidos o ocurre un error al escribir el PDF
     */
    public Path generarReporteBusquedaUsuarios(
            String rutaONombreArchivo,
            User solicitante,
            List<User> participantes,
            Program program,
            InteresArea area,
            int puntuacionMinima) throws Exception {
        return generarReporteBusquedaUsuarios(
                rutaONombreArchivo,
                solicitante,
                participantes,
                program,
                area,
                puntuacionMinima,
                "General",
                "Todos");
    }

    /**
     * Genera el informe de búsqueda indicando el modo y el tipo de participante consultado.
     *
     * @param rutaONombreArchivo nombre o ruta completa del archivo PDF
     * @param solicitante usuario que realizó la búsqueda
     * @param participantes resultados ya filtrados
     * @param program filtro de programa aplicado
     * @param area filtro de área aplicada
     * @param puntuacionMinima puntuación mínima aplicada
     * @param modoBusqueda modo seleccionado: general, relevancia o puentes
     * @param tipoParticipante ventana consultada: personas, grupos o todos
     * @return ruta final del PDF
     * @throws Exception si ocurre un error al generar el documento
     */
    public Path generarReporteBusquedaUsuarios(
            String rutaONombreArchivo,
            User solicitante,
            List<User> participantes,
            Program program,
            InteresArea area,
            int puntuacionMinima,
            String modoBusqueda,
            String tipoParticipante) throws Exception {
        if (solicitante == null || solicitante.getEmail() == null) {
            throw new IllegalArgumentException("El usuario solicitante es obligatorio para generar el reporte.");
        }
        if (puntuacionMinima < 0) {
            puntuacionMinima = 0;
        }
        if (modoBusqueda == null || modoBusqueda.isBlank()) {
            modoBusqueda = "General";
        }
        if (tipoParticipante == null || tipoParticipante.isBlank()) {
            tipoParticipante = "Todos";
        }

        int puntuacionAplicada = puntuacionMinima;
        String modoAplicado = modoBusqueda;
        String tipoAplicado = tipoParticipante;
        boolean modoPuentes = modoAplicado.toLowerCase().contains("puente");
        List<User> resultados = participantes != null
                ? new java.util.ArrayList<>(participantes)
                : new java.util.ArrayList<>();
        Map<String, ConteoRelevancia> conteos = calcularConteosRelevancia();
        Map<String, ConteoPuente> puentes = modoPuentes
                ? calcularConteosPuente(modoAplicado)
                : new HashMap<>();
        resultados.removeIf(participante ->
                participante == null
                        || participante.getEmail() == null
                        || participante.getEmail().equals(solicitante.getEmail()));
        if ("Personas".equalsIgnoreCase(tipoAplicado)) {
            resultados.removeIf(participante ->
                    participante.getTypeUser() != TypeUser.Estudents
                            && participante.getTypeUser() != TypeUser.Teachers);
        } else if ("Grupos".equalsIgnoreCase(tipoAplicado)) {
            resultados.removeIf(participante ->
                    participante.getTypeUser() != TypeUser.InvestigationGroup
                            && participante.getTypeUser() != TypeUser.AcademicGroup);
        }
        if (modoPuentes) {
            resultados.sort((uno, dos) ->
                    compararParticipantesPuente(uno, dos, puentes));
        } else {
            resultados.sort((uno, dos) ->
                    compararParticipantesPorRelevancia(uno, dos, conteos));
        }

        return escribirPdf(rutaONombreArchivo, documento -> {
            agregarEncabezadoDocumento(documento, "REPORTE DE BÚSQUEDA DE USUARIOS");
            agregarSeccionSolicitante(documento, solicitante);
            agregarSeccionFiltrosBusqueda(
                    documento,
                    program,
                    area,
                    puntuacionAplicada,
                    modoAplicado,
                    tipoAplicado);

            Paragraph tituloResultados = new Paragraph("3. Usuarios Encontrados", FUENTE_SECCION);
            tituloResultados.setSpacingAfter(8f);
            documento.add(tituloResultados);

            if (resultados.isEmpty()) {
                documento.add(new Paragraph(
                        "No se encontraron usuarios que cumplan con los filtros aplicados.",
                        FUENTE_TEXTO));
                return;
            }

            PdfPTable tabla = new PdfPTable(modoPuentes ? 11 : 9);
            tabla.setWidthPercentage(100);
            tabla.setWidths(modoPuentes
                    ? new float[] { 1.7f, 1.6f, 1.2f, 1.5f, 1.2f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f }
                    : new float[] { 1.8f, 1.7f, 1.4f, 1.7f, 1.4f, 0.9f, 0.9f, 0.9f, 0.9f });

            tabla.addCell(crearCeldaEncabezado("Nombre"));
            tabla.addCell(crearCeldaEncabezado("Correo"));
            tabla.addCell(crearCeldaEncabezado("Tipo"));
            tabla.addCell(crearCeldaEncabezado("Programa"));
            tabla.addCell(crearCeldaEncabezado("Área"));
            if (modoPuentes) {
                tabla.addCell(crearCeldaEncabezado("Grupos"));
                tabla.addCell(crearCeldaEncabezado("Cruces"));
            }
            tabla.addCell(crearCeldaEncabezado("Colab."));
            tabla.addCell(crearCeldaEncabezado("Ment."));
            tabla.addCell(crearCeldaEncabezado("Particip."));
            tabla.addCell(crearCeldaEncabezado("Puntaje"));

            for (int i = 0; i < resultados.size(); i++) {
                User participante = resultados.get(i);
                BaseColor fondo = (i % 2 == 1) ? COLOR_FILA_ALTERNA : BaseColor.WHITE;
                ConteoRelevancia conteo = conteos.getOrDefault(
                        participante.getEmail(),
                        new ConteoRelevancia());

                tabla.addCell(crearCeldaDato(valorSeguro(participante.getName(), "Sin nombre"), fondo));
                tabla.addCell(crearCeldaDato(valorSeguro(participante.getEmail(), "Sin correo"), fondo));
                tabla.addCell(crearCeldaDato(participante.getTypeUser() != null
                        ? participante.getTypeUser().name()
                        : "No registra", fondo));
                tabla.addCell(crearCeldaDato(
                        nombrePrograma(participante.getProgram()) != null
                                ? nombrePrograma(participante.getProgram())
                                : "No registra",
                        fondo));
                tabla.addCell(crearCeldaDato(valorSeguro(participante.getInteresArea(), "No registra"), fondo));
                if (modoPuentes) {
                    ConteoPuente puente = puentes.getOrDefault(
                            participante.getEmail(),
                            new ConteoPuente());
                    tabla.addCell(crearCeldaDato(String.valueOf(puente.grupos.size()), fondo));
                    tabla.addCell(crearCeldaDato(String.valueOf(puente.relacionesCruzadas), fondo));
                }
                tabla.addCell(crearCeldaDato(String.valueOf(conteo.colaboraciones), fondo));
                tabla.addCell(crearCeldaDato(String.valueOf(conteo.mentorias), fondo));
                tabla.addCell(crearCeldaDato(String.valueOf(conteo.participaciones), fondo));
                tabla.addCell(crearCeldaDato(String.valueOf(conteo.calcularPuntaje()), fondo));
            }

            documento.add(tabla);

            Paragraph resumen = new Paragraph(
                    String.format("\nResumen: %d usuarios encontrados que cumplen los filtros.",
                            resultados.size()),
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
     * Agrega los filtros específicos de la pantalla de búsqueda de usuarios.
     *
     * @param documento documento PDF abierto
     * @param program programa filtrado o {@code null}
     * @param area área filtrada o {@code null}
     * @param puntuacionMinima puntuación mínima aplicada
     * @throws DocumentException si ocurre un error al insertar la tabla
     */
    private void agregarSeccionFiltrosBusqueda(
            Document documento,
            Program program,
            InteresArea area,
            int puntuacionMinima,
            String modoBusqueda,
            String tipoParticipante) throws DocumentException {
        Paragraph titulo = new Paragraph("2. Filtros Aplicados en la Consulta", FUENTE_SECCION);
        titulo.setSpacingAfter(6f);
        documento.add(titulo);

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[] { 1.2f, 3.8f });
        tabla.setSpacingAfter(12f);

        agregarFilaClaveValor(tabla, "Programa:", nombrePrograma(program) == null ? "Todos" : nombrePrograma(program));
        agregarFilaClaveValor(tabla, "Área de Interés:", valorSeguro(area, "Todas"));
        agregarFilaClaveValor(tabla, "Tipo de Participante:", tipoParticipante);
        agregarFilaClaveValor(tabla, "Modo de Búsqueda:", modoBusqueda);
        agregarFilaClaveValor(tabla, "Puntuación mínima:", String.valueOf(puntuacionMinima));

        documento.add(tabla);
    }

    /**
     * Cuenta las relaciones relevantes de cada participante usando el grafo asociado.
     *
     * @return mapa de correos a conteos de colaboración, mentoría y participación
     */
    private Map<String, ConteoRelevancia> calcularConteosRelevancia() {
        Map<String, ConteoRelevancia> conteos = new HashMap<>();
        if (grafo == null) {
            return conteos;
        }

        Set<Arista<String>> procesadas = new HashSet<>();
        for (Nodo<String> nodo : grafo.obtenerTodosLosNodos()) {
            for (Arista<String> arista : grafo.obtenerAristas(nodo)) {
                if (!procesadas.add(arista)
                        || arista.getDatosRelacion() == null
                        || arista.getDatosRelacion().getRelationType() == null) {
                    continue;
                }

                String correoInicial = arista.getNodoInicial().getContenido();
                String correoFinal = arista.getNodoFinal().getContenido();
                RelationType tipo = arista.getDatosRelacion().getRelationType();

                ConteoRelevancia inicial = conteos.computeIfAbsent(
                        correoInicial, correo -> new ConteoRelevancia());
                ConteoRelevancia finalConteo = conteos.computeIfAbsent(
                        correoFinal, correo -> new ConteoRelevancia());

                inicial.registrar(tipo);
                finalConteo.registrar(tipo);
            }
        }
        return conteos;
    }

    /**
     * Ordena participantes para que el PDF refleje el mismo criterio de relevancia de la búsqueda.
     *
     * @param uno primer participante
     * @param dos segundo participante
     * @param conteos conteos calculados por correo
     * @return comparación descendente por relevancia
     */
    private int compararParticipantesPorRelevancia(
            User uno,
            User dos,
            Map<String, ConteoRelevancia> conteos) {
        String correoUno = uno != null && uno.getEmail() != null ? uno.getEmail() : "";
        String correoDos = dos != null && dos.getEmail() != null ? dos.getEmail() : "";
        ConteoRelevancia conteoUno = conteos.getOrDefault(correoUno, new ConteoRelevancia());
        ConteoRelevancia conteoDos = conteos.getOrDefault(correoDos, new ConteoRelevancia());

        int comparacion = Integer.compare(conteoDos.calcularPuntaje(), conteoUno.calcularPuntaje());
        if (comparacion != 0) {
            return comparacion;
        }
        comparacion = Integer.compare(conteoDos.colaboraciones, conteoUno.colaboraciones);
        if (comparacion != 0) {
            return comparacion;
        }
        comparacion = Integer.compare(conteoDos.mentorias, conteoUno.mentorias);
        if (comparacion != 0) {
            return comparacion;
        }
        return correoUno.compareToIgnoreCase(correoDos);
    }

    /**
     * Calcula los grupos académicos distintos y relaciones cruzadas de los candidatos puente.
     *
     * @param modoBusqueda modo que indica si los grupos se separan por programa o área
     * @return conteos por correo
     */
    private Map<String, ConteoPuente> calcularConteosPuente(String modoBusqueda) {
        Map<String, ConteoPuente> conteos = new HashMap<>();
        if (grafo == null) {
            return conteos;
        }

        boolean porPrograma = modoBusqueda != null
                && modoBusqueda.toLowerCase().contains("programa");
        Set<Arista<String>> procesadas = new HashSet<>();

        for (Nodo<String> nodo : grafo.obtenerTodosLosNodos()) {
            for (Arista<String> arista : grafo.obtenerAristas(nodo)) {
                if (!procesadas.add(arista)
                        || arista.getNodoInicial() == null
                        || arista.getNodoFinal() == null) {
                    continue;
                }

                String correoInicial = arista.getNodoInicial().getContenido();
                String correoFinal = arista.getNodoFinal().getContenido();
                User usuarioInicial = usuarios.get(correoInicial);
                User usuarioFinal = usuarios.get(correoFinal);
                String grupoInicial = obtenerGrupoPuente(usuarioInicial, porPrograma);
                String grupoFinal = obtenerGrupoPuente(usuarioFinal, porPrograma);

                if (grupoInicial == null || grupoFinal == null
                        || grupoInicial.equals(grupoFinal)) {
                    continue;
                }

                ConteoPuente inicial = conteos.computeIfAbsent(
                        correoInicial, correo -> new ConteoPuente());
                ConteoPuente finalConteo = conteos.computeIfAbsent(
                        correoFinal, correo -> new ConteoPuente());

                inicial.grupos.add(grupoFinal);
                inicial.relacionesCruzadas++;
                finalConteo.grupos.add(grupoInicial);
                finalConteo.relacionesCruzadas++;
            }
        }
        return conteos;
    }

    /**
     * Obtiene el nombre del programa o área usado para clasificar puentes.
     */
    private String obtenerGrupoPuente(User usuario, boolean porPrograma) {
        if (usuario == null) {
            return null;
        }
        if (porPrograma) {
            return usuario.getProgram() != null
                    ? usuario.getProgram().getDisplayName()
                    : null;
        }
        return usuario.getInteresArea() != null
                ? usuario.getInteresArea().name()
                : null;
    }

    /**
     * Ordena candidatos puente por cantidad de grupos y relaciones cruzadas.
     */
    private int compararParticipantesPuente(
            User uno,
            User dos,
            Map<String, ConteoPuente> conteos) {
        String correoUno = uno != null && uno.getEmail() != null ? uno.getEmail() : "";
        String correoDos = dos != null && dos.getEmail() != null ? dos.getEmail() : "";
        ConteoPuente conteoUno = conteos.getOrDefault(correoUno, new ConteoPuente());
        ConteoPuente conteoDos = conteos.getOrDefault(correoDos, new ConteoPuente());

        int comparacion = Integer.compare(conteoDos.grupos.size(), conteoUno.grupos.size());
        if (comparacion != 0) {
            return comparacion;
        }
        comparacion = Integer.compare(
                conteoDos.relacionesCruzadas,
                conteoUno.relacionesCruzadas);
        if (comparacion != 0) {
            return comparacion;
        }
        return correoUno.compareToIgnoreCase(correoDos);
    }

    /**
     * Conteo interno de grupos y relaciones cruzadas para el modo puente.
     */
    private static class ConteoPuente {
        private final Set<String> grupos = new HashSet<>();
        private int relacionesCruzadas;
    }

    /**
     * Conteo interno de relaciones usadas para calcular relevancia en el PDF.
     */
    private static class ConteoRelevancia {
        private int colaboraciones;
        private int mentorias;
        private int participaciones;

        private void registrar(RelationType tipo) {
            if (tipo == RelationType.InvestigationCollaboration) {
                colaboraciones++;
            } else if (tipo == RelationType.Mentoring) {
                mentorias++;
            } else if (tipo == RelationType.JointParticipation) {
                participaciones++;
            }
        }

        private int calcularPuntaje() {
            return colaboraciones * 3 + mentorias * 2 + participaciones;
        }
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