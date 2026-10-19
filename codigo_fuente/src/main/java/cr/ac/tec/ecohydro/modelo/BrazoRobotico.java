package cr.ac.tec.ecohydro.modelo;

/**
 * Modela el actuador físico que desplaza los sensores dentro del cultivo.
 * Relación UML: Composición con UnidadCentralControl (no existe fuera de ella).
 *
 * IC-2101 Programación Orientada a Objetos — ITCR Campus San Carlos
 */
public class BrazoRobotico {

    // ─── Atributos privados ───────────────────────────────────────────────────
    private double velocidadDesplazamiento; // cm/s
    private double anguloExtension;         // grados (0–180)
    private double horasUsoAcumuladas;      // horas de operación

    // ─── Constructor ──────────────────────────────────────────────────────────
    /**
     * Crea un brazo con parámetros iniciales validados.
     *
     * @param velocidadDesplazamiento cm/s; debe ser > 0
     * @param anguloExtension         grados; rango 0–180
     * @param horasUsoAcumuladas      horas previas; debe ser >= 0
     */
    public BrazoRobotico(double velocidadDesplazamiento,
                         double anguloExtension,
                         double horasUsoAcumuladas) {
        setVelocidadDesplazamiento(velocidadDesplazamiento);
        setAnguloExtension(anguloExtension);
        setHorasUsoAcumuladas(horasUsoAcumuladas);
    }

    // ─── Getters ──────────────────────────────────────────────────────────────
    public double getVelocidadDesplazamiento() {
        return velocidadDesplazamiento;
    }

    public double getAnguloExtension() {
        return anguloExtension;
    }

    public double getHorasUsoAcumuladas() {
        return horasUsoAcumuladas;
    }

    // ─── Setters validados ────────────────────────────────────────────────────
    public void setVelocidadDesplazamiento(double velocidadDesplazamiento) {
        if (velocidadDesplazamiento <= 0) {
            throw new IllegalArgumentException(
                "La velocidad de desplazamiento debe ser mayor que 0. Valor recibido: "
                + velocidadDesplazamiento);
        }
        this.velocidadDesplazamiento = velocidadDesplazamiento;
    }

    public void setAnguloExtension(double anguloExtension) {
        if (anguloExtension < 0 || anguloExtension > 180) {
            throw new IllegalArgumentException(
                "El ángulo de extensión debe estar entre 0 y 180 grados. Valor recibido: "
                + anguloExtension);
        }
        this.anguloExtension = anguloExtension;
    }

    public void setHorasUsoAcumuladas(double horasUsoAcumuladas) {
        if (horasUsoAcumuladas < 0) {
            throw new IllegalArgumentException(
                "Las horas de uso acumuladas no pueden ser negativas. Valor recibido: "
                + horasUsoAcumuladas);
        }
        this.horasUsoAcumuladas = horasUsoAcumuladas;
    }

    // ─── toString ─────────────────────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format(
            "BrazoRobotico { velocidad=%.2f cm/s | angulo=%.1f° | horasUso=%.2f h }",
            velocidadDesplazamiento, anguloExtension, horasUsoAcumuladas);
    }
}
