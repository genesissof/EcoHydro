package cr.ac.tec.ecohydro.modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Representa un evento de muestreo puntual e inmutable.
 * Relación UML:
 *   - Dependencia con BrazoRobotico (parámetro temporal, no atributo).
 *   - Agregación con BitacoraHistorica (puede existir fuera de ella).
 *
 * IC-2101 Programación Orientada a Objetos — ITCR Campus San Carlos
 */
public class ReporteMuestreo {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // ─── Atributos privados ───────────────────────────────────────────────────
    private final LocalDateTime fechaHora;          // instante del muestreo
    private final double ph;                        // rango óptimo: 5.5–6.5
    private final double conductividadElectrica;    // rango óptimo: 1.2–2.4 mS/cm
    private final double gastoEnergeticoW;          // vatios consumidos en el ciclo

    // ─── Constructor ──────────────────────────────────────────────────────────
    /**
     * Crea un reporte de muestreo con validación de rangos.
     *
     * @param fechaHora             instante de la medición (no nulo)
     * @param ph                    nivel de pH (0–14)
     * @param conductividadElectrica conductividad en mS/cm (>= 0)
     * @param gastoEnergeticoW       vatios consumidos (>= 0)
     */
    public ReporteMuestreo(LocalDateTime fechaHora,
                           double ph,
                           double conductividadElectrica,
                           double gastoEnergeticoW) {
        if (fechaHora == null) {
            throw new IllegalArgumentException("La fecha/hora del reporte no puede ser nula.");
        }
        if (ph < 0 || ph > 14) {
            throw new IllegalArgumentException(
                "El pH debe estar en el rango [0, 14]. Valor recibido: " + ph);
        }
        if (conductividadElectrica < 0) {
            throw new IllegalArgumentException(
                "La conductividad eléctrica no puede ser negativa. Valor recibido: "
                + conductividadElectrica);
        }
        if (gastoEnergeticoW < 0) {
            throw new IllegalArgumentException(
                "El gasto energético no puede ser negativo. Valor recibido: "
                + gastoEnergeticoW);
        }
        this.fechaHora            = fechaHora;
        this.ph                   = ph;
        this.conductividadElectrica = conductividadElectrica;
        this.gastoEnergeticoW     = gastoEnergeticoW;
    }

    // ─── Getters (clase de valor — sin setters) ───────────────────────────────
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public double getPh() {
        return ph;
    }

    public double getConductividadElectrica() {
        return conductividadElectrica;
    }

    public double getGastoEnergeticoW() {
        return gastoEnergeticoW;
    }

    // ─── toString ─────────────────────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format(
            "ReporteMuestreo { fecha=%s | pH=%.2f | conductividad=%.3f mS/cm | gasto=%.2f W }",
            fechaHora.format(FORMATO_FECHA), ph, conductividadElectrica, gastoEnergeticoW);
    }
}
