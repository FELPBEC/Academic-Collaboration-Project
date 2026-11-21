package co.edu.uptc.View;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import co.edu.uptc.Enum.RelationType;
import co.edu.uptc.Enum.TypeUser;
import javafx.scene.paint.Color;

class GrafoViewTest {

    @Test
    void debeAsignarColoresDistintosPorTipoDeUsuario() {
        assertNotEquals(
                GrafoView.getNodeColor(TypeUser.Estudents),
                GrafoView.getNodeColor(TypeUser.Teachers));
        assertNotEquals(
                GrafoView.getNodeColor(TypeUser.InvestigationGroud),
                GrafoView.getNodeColor(TypeUser.AcademicGroup));
    }

    @Test
    void debeUsarColorNeutralParaTipoDeUsuarioNulo() {
        assertEquals(Color.LIGHTSEAGREEN, GrafoView.getNodeColor(null));
    }

    @Test
    void debeAsignarColoresDistintosPorTipoDeRelacion() {
        assertNotEquals(
                GrafoView.getEdgeColor(RelationType.InvestigationCollaboration),
                GrafoView.getEdgeColor(RelationType.Mentoring));
        assertNotEquals(
                GrafoView.getEdgeColor(RelationType.Mentoring),
                GrafoView.getEdgeColor(RelationType.JointParticipation));
    }

    @Test
    void debeUsarColorNeutralParaRelacionNula() {
        assertEquals(Color.DARKGRAY, GrafoView.getEdgeColor(null));
    }
}
