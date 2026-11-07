package co.edu.uptc.Model;

import co.edu.uptc.Enum.RelationType;
/**Método que representa las relaciones entre los usuarios de la plataforma, incluyendo colaboraciones de investigación, mentorías y participación conjunta en proyectos académicos.
 * 
 * @author @FELPBEC
 * @since 2024-06-10
 * @version 1.0
 */ 
public class Relation {
    private RelationType relationType;

    /**Constructor vacío de la clase para persistencia */
    public Relation() { }
    
    /**Constructor de la clase que permite crear una relación con un tipo específico
     * 
     * @param relationType tipo de relación
     */
    public Relation(RelationType relationType) {
        this.relationType = relationType;
    }

    /**Método que permite obtener el tipo de relación
     * 
     * @return tipo de relación
     */
    public RelationType getRelationType() {
        return relationType;
    }
    /**Método que permite establecer el tipo de relación
     * 
     * @param relationType tipo de relación
     */

    public void setRelationType(RelationType relationType) {
        this.relationType = relationType;
    }
    /**Método que permite obtener la proximidad de la relación según su tipo
     * 
     * @return proximidad de la relación
     */
    public int getProximity() {
        switch (relationType) {
            case InvestigationCollaboration:
                return 3;
            case Mentoring:
                return 2;
            case JointParticipation:
                return 1;
            default:
                return 0;
        }
    }
    /**Método que permite obtener el peso de la relación según su proximidad
     * cuanto mayor la proximidad menor será el peso de la relación, y viceversa.
     * Lo que permitirá hacer los recorridos más cortos y eficientes en el grafo de relaciones.
     * 
     * @return peso de la relación
     */
    public double getWeight() {
        return 1/getProximity(); 
    }
    @Override
    /** 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
    @Override
    /**   
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return super.hashCode();
    }
}
