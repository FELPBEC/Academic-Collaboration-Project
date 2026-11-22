package co.edu.uptc.Enum;

/**
 * Programas académicos admitidos por la institución.
 *
 * @author @jm1407db
 * @version 1.0
 */
public enum Program {
    IngenieriaDeMinas("Ingeniería de Minas"),
    IngenieriaElectronica("Ingeniería Electrónica"),
    IngenieriaGeologica("Ingeniería Geológica"),
    IngenieriaIndustrial("Ingeniería Industrial"),
    IngenieriaDeSistemasYComputacion("Ingeniería de Sistemas y Computación"),
    AdministracionDeEmpresas("Administración de Empresas"),
    ContaduriaPublica("Contaduría Pública"),
    FinanzasYComercioInternacional("Finanzas y Comercio Internacional");

    private final String displayName;

    /**
     * Crea un programa con su denominación institucional.
     *
     * @param displayName nombre completo mostrado en interfaces y reportes
     */
    Program(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Obtiene el nombre completo del programa.
     *
     * @return denominación institucional del programa
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Busca un programa por su denominación institucional o por su nombre de constante.
     *
     * @param text texto a convertir
     * @return programa correspondiente o {@code null} si no coincide
     */
    public static Program fromText(String text) {
        if (text == null) {
            return null;
        }
        String normalized = text.trim();
        for (Program program : values()) {
            if (program.name().equalsIgnoreCase(normalized)
                    || program.displayName.equalsIgnoreCase(normalized)) {
                return program;
            }
        }
        return null;
    }
}
