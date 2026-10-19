package cr.ac.tec.ecohydro.modelo;

/**
 * Modela al agente humano responsable de la instalación hidropónica.
 * Relación UML: Asociación con UnidadCentralControl (existe independientemente).
 *
 * IC-2101 Programación Orientada a Objetos — ITCR Campus San Carlos
 */
public class Operador {

    // ─── Atributos privados ───────────────────────────────────────────────────
    private String fichaEmpleado;     // ID único del empleado (no vacío)
    private String nombreCompleto;    // nombre completo del operador
    private String correoElectronico; // dirección de correo para alertas

    // ─── Constructor ──────────────────────────────────────────────────────────
    /**
     * Crea un operador con sus datos de identificación.
     *
     * @param fichaEmpleado     ID del empleado (no nulo ni vacío)
     * @param nombreCompleto    nombre completo (no nulo ni vacío)
     * @param correoElectronico correo electrónico válido (contiene '@')
     */
    public Operador(String fichaEmpleado,
                    String nombreCompleto,
                    String correoElectronico) {
        setFichaEmpleado(fichaEmpleado);
        setNombreCompleto(nombreCompleto);
        setCorreoElectronico(correoElectronico);
    }

    // ─── Getters ──────────────────────────────────────────────────────────────
    public String getFichaEmpleado() {
        return fichaEmpleado;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    // ─── Setters validados ────────────────────────────────────────────────────
    public void setFichaEmpleado(String fichaEmpleado) {
        if (fichaEmpleado == null || fichaEmpleado.isBlank()) {
            throw new IllegalArgumentException("La ficha de empleado no puede ser nula ni vacía.");
        }
        this.fichaEmpleado = fichaEmpleado.trim();
    }

    public void setNombreCompleto(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            throw new IllegalArgumentException("El nombre completo no puede ser nulo ni vacío.");
        }
        this.nombreCompleto = nombreCompleto.trim();
    }

    public void setCorreoElectronico(String correoElectronico) {
        if (correoElectronico == null || !correoElectronico.contains("@")) {
            throw new IllegalArgumentException(
                "El correo electrónico no es válido: " + correoElectronico);
        }
        this.correoElectronico = correoElectronico.trim();
    }

    // ─── toString ─────────────────────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format(
            "Operador { ficha='%s' | nombre='%s' | correo='%s' }",
            fichaEmpleado, nombreCompleto, correoElectronico);
    }
}
