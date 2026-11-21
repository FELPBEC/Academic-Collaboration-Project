package co.edu.uptc.Controller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.uptc.Enum.RelationType;
import co.edu.uptc.Exceptions.DuplicateUserException;
import co.edu.uptc.Model.Solicitud;

class ServicioSolicitudesTest {

    private ServicioSolicitudes servicioSolicitudes;
    private List<Solicitud> listaSolicitudes;

    @BeforeEach
    void setUp() {
        listaSolicitudes = new ArrayList<>();
        servicioSolicitudes = new ServicioSolicitudes(listaSolicitudes);
    }

    @Test
    void testEnviarSolicitudExitosa() throws DuplicateUserException {
        String solicitante = "ana@uptc.edu.co";
        String destinatario = "carlos@uptc.edu.co";
        String comentario = "Me gustaría colaborar en el proyecto";

        Solicitud resultado = servicioSolicitudes.enviarSolicitud(
                solicitante,
                destinatario,
                RelationType.Mentoring, // Ajusta el valor del enum según tu definición
                comentario
        );

        assertNotNull(resultado);
        assertEquals(solicitante, resultado.getEmailSolicitante());
        assertEquals(destinatario, resultado.getEmailDestinatario());
        assertEquals(RelationType.Mentoring, resultado.getTipoRelacion());
        assertEquals(comentario, resultado.getComentario());
    }

    @Test
    void testEnviarSolicitudMismoUsuarioLanzaExcepcion() {
        String mismoEmail = "ana@uptc.edu.co";

        DuplicateUserException exception = assertThrows(
                DuplicateUserException.class,
                () -> servicioSolicitudes.enviarSolicitud(
                        mismoEmail,
                        mismoEmail,
                        RelationType.Mentoring,
                        "Auto-solicitud"
                )
        );

        assertEquals("No se puede enviar una solicitud a si mismo", exception.getMessage());
    }

    @Test
    void testAceptarSolicitudEliminaDeListaYRetornaSolicitud() throws DuplicateUserException {
        Solicitud solicitud1 = servicioSolicitudes.enviarSolicitud(
                "ana@uptc.edu.co",
                "carlos@uptc.edu.co",
                RelationType.Mentoring,
                "Solicitud 1"
        );
        Solicitud solicitud2 = servicioSolicitudes.enviarSolicitud(
                "luis@uptc.edu.co",
                "carlos@uptc.edu.co",
                RelationType.Mentoring,
                "Solicitud 2"
        );

        listaSolicitudes.add(solicitud1);
        listaSolicitudes.add(solicitud2);

        Solicitud aceptada = servicioSolicitudes.aceptarSolicitud(solicitud1);

        assertEquals(solicitud1, aceptada);
        assertFalse(listaSolicitudes.contains(solicitud1));
        assertTrue(listaSolicitudes.contains(solicitud2));
        assertEquals(1, servicioSolicitudes.getSolicitudes().size());
    }

    @Test
    void testRechazarSolicitudEliminaDeLista() throws DuplicateUserException {
        Solicitud solicitud1 = servicioSolicitudes.enviarSolicitud(
                "ana@uptc.edu.co",
                "carlos@uptc.edu.co",
                RelationType.Mentoring,
                "Solicitud a rechazar"
        );
        Solicitud solicitud2 = servicioSolicitudes.enviarSolicitud(
                "luis@uptc.edu.co",
                "carlos@uptc.edu.co",
                RelationType.Mentoring,
                "Solicitud a conservar"
        );

        listaSolicitudes.add(solicitud1);
        listaSolicitudes.add(solicitud2);

        servicioSolicitudes.rechazarSolicitud(solicitud1);

        assertFalse(listaSolicitudes.contains(solicitud1));
        assertTrue(listaSolicitudes.contains(solicitud2));
        assertEquals(1, servicioSolicitudes.getSolicitudes().size());
    }

    @Test
    void testGetYSetSolicitudes() {
        List<Solicitud> nuevaLista = new ArrayList<>();
        servicioSolicitudes.setSolicitudes(nuevaLista);

        assertSame(nuevaLista, servicioSolicitudes.getSolicitudes());
    }
}
