package cr.ac.tec.ecohydro.modelo;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Encapsula el historial de reportes de muestreo del sistema EcoHydro-4.
 *
 * <p>Relaciones UML:</p>
 * <ul>
 *   <li>Agregación con {@link UnidadCentralControl}: sobrevive a la destrucción de la unidad.</li>
 *   <li>Agregación con {@link ReporteMuestreo}: administra la colección pero no posee el
 *       ciclo de vida de cada reporte.</li>
 * </ul>
 *
 * <p><b>RF3 implementado en esta clase:</b>
 * {@link #registrarReporte}, {@link #esAnomaliaQuimica},
 * {@link #calcularPorcentajeAnomalias}, {@link #generarResumen}.</p>
 *
 * IC-2101 Programación Orientada a Objetos — ITCR Campus San Carlos
 */
public class BitacoraHistorica {

    // ─── Umbrales de anomalía química (RF3) ──────────────────────────────────
    /** pH mínimo aceptable en solución hidropónica. */
    public static final double PH_MIN   = 5.5;
    /** pH máximo aceptable en solución hidropónica. */
    public static final double PH_MAX   = 6.5;
    /** Conductividad eléctrica mínima aceptable (mS/cm). */
    public static final double COND_MIN = 1.2;
    /** Conductividad eléctrica máxima aceptable (mS/cm). */
    public static final double COND_MAX = 2.4;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // ─── Atributos privados ───────────────────────────────────────────────────
    private final String id;
    private final ArrayList<ReporteMuestreo> reportes;

    // ─── Constructor ──────────────────────────────────────────────────────────
    /**
     * Crea una bitácora histórica vacía.
     *
     * @param id identificador único (no nulo ni vacío)
     */
    public BitacoraHistorica(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El ID de la bitácora no puede ser nulo ni vacío.");
        }
        this.id       = id.trim();
        this.reportes = new ArrayList<>();
    }

    // ─── Getters ──────────────────────────────────────────────────────────────
    public String getId() { return id; }

    /** Retorna copia defensiva de la lista para no exponer el estado interno. */
    public ArrayList<ReporteMuestreo> getReportes() {
        return new ArrayList<>(reportes);
    }

    public int getTotalReportes() { return reportes.size(); }

    // ══════════════════════════════════════════════════════════════════════════
    // RF3 — OPERACIONES DE BITÁCORA
    // ══════════════════════════════════════════════════════════════════════════

    // ─── RF3.1 — registrarReporte ─────────────────────────────────────────────
    /**
     * Agrega un reporte de muestreo a la colección histórica.
     *
     * <p>El reporte continúa existiendo en memoria con independencia de esta
     * bitácora (Agregación UML): si se destruye la bitácora, el reporte
     * permanece accesible mientras exista otra referencia a él.</p>
     *
     * @param reporte objeto {@link ReporteMuestreo} a registrar (no nulo)
     * @throws IllegalArgumentException si {@code reporte} es nulo
     */
    public void registrarReporte(ReporteMuestreo reporte) {
        if (reporte == null) {
            throw new IllegalArgumentException("No se puede registrar un reporte nulo.");
        }
        reportes.add(reporte);
    }

    // ─── RF3.2 — esAnomaliaQuimica ────────────────────────────────────────────
    /**
     * Evalúa si un reporte presenta anomalía en los parámetros químicos.
     *
     * <p>Criterios de anomalía (basta uno):
     * <ul>
     *   <li>pH &lt; {@value #PH_MIN} o pH &gt; {@value #PH_MAX}</li>
     *   <li>Conductividad &lt; {@value #COND_MIN} o &gt; {@value #COND_MAX} mS/cm</li>
     * </ul>
     *
     * @param r el reporte a evaluar (no nulo)
     * @return {@code true} si alguno de los parámetros está fuera del rango óptimo
     * @throws IllegalArgumentException si {@code r} es nulo
     */
    public boolean esAnomaliaQuimica(ReporteMuestreo r) {
        if (r == null) {
            throw new IllegalArgumentException("El reporte a evaluar no puede ser nulo.");
        }
        boolean phAnomalo   = r.getPh() < PH_MIN || r.getPh() > PH_MAX;
        boolean condAnomala = r.getConductividadElectrica() < COND_MIN
                           || r.getConductividadElectrica() > COND_MAX;
        return phAnomalo || condAnomala;
    }

    // ─── RF3.3 — calcularPorcentajeAnomalias ─────────────────────────────────
    /**
     * Calcula el porcentaje de reportes con anomalía química sobre el total.
     *
     * <p>Maneja la división por cero: retorna {@code 0.0} si la bitácora está vacía.</p>
     *
     * @return valor en el rango [0.0, 100.0], o 0.0 si no hay reportes
     */
    public double calcularPorcentajeAnomalias() {
        if (reportes.isEmpty()) {
            return 0.0;   // ← división por cero controlada
        }
        long cantidadAnomalias = 0;
        for (ReporteMuestreo r : reportes) {
            if (esAnomaliaQuimica(r)) {
                cantidadAnomalias++;
            }
        }
        return ((double) cantidadAnomalias / reportes.size()) * 100.0;
    }

    // ─── RF3.4 — generarResumen ───────────────────────────────────────────────
    /**
     * Genera un resumen completo del historial de la bitácora.
     *
     * <p>El resumen incluye: total de muestras, cantidad normal vs. anómala,
     * porcentaje de anomalías, y el detalle de cada reporte con su fecha,
     * valores de pH y conductividad, y la causa específica de la anomalía.</p>
     *
     * @return cadena multilínea con estadísticas y detalle de cada reporte
     */
    public String generarResumen() {
        if (reportes.isEmpty()) {
            return "Bitácora [" + id + "] — Sin registros disponibles.";
        }

        int    total    = reportes.size();
        int    anomalos = 0;
        for (ReporteMuestreo r : reportes) {
            if (esAnomaliaQuimica(r)) anomalos++;
        }
        int    normales = total - anomalos;
        double pct      = calcularPorcentajeAnomalias();

        StringBuilder sb = new StringBuilder();
        sb.append("╔═══════════════════════════════════════════════════════════╗\n");
        sb.append(String.format("║  RESUMEN BITÁCORA HISTÓRICA — ID: %-24s║%n", id));
        sb.append("╠═══════════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║  Total muestras  : %-40d║%n", total));
        sb.append(String.format("║  Normales        : %-40d║%n", normales));
        sb.append(String.format("║  Con anomalía    : %-40d║%n", anomalos));
        sb.append(String.format("║  %% anomalías     : %-39s║%n",
                String.format("%.1f%%", pct)));
        sb.append("╠═══════════════════════════════════════════════════════════╣\n");
        sb.append("║  DETALLE POR REPORTE:                                     ║\n");
        sb.append("╠═══════════════════════════════════════════════════════════╣\n");

        for (int i = 0; i < reportes.size(); i++) {
            ReporteMuestreo r  = reportes.get(i);
            boolean anomalia   = esAnomaliaQuimica(r);
            String etiqueta    = anomalia ? "⚠ ANOMALÍA" : "✔ NORMAL  ";

            sb.append(String.format("║  #%02d [%s]%n", i + 1, etiqueta));
            sb.append(String.format("║       Fecha/hora    : %s%n",
                    r.getFechaHora().format(FMT)));
            sb.append(String.format("║       pH            : %.2f  %s%n",
                    r.getPh(), flechaPh(r.getPh())));
            sb.append(String.format("║       Conductividad : %.3f mS/cm  %s%n",
                    r.getConductividadElectrica(), flechaCond(r.getConductividadElectrica())));
            sb.append(String.format("║       Gasto         : %.2f W%n",
                    r.getGastoEnergeticoW()));
            if (anomalia) {
                sb.append(String.format("║       ► Causa       : %s%n",
                        describirAnomalia(r)));
            }
            sb.append("║  ─────────────────────────────────────────────────────────║\n");
        }
        sb.append("╚═══════════════════════════════════════════════════════════╝\n");
        return sb.toString();
    }

    // ─── Helpers privados ─────────────────────────────────────────────────────

    /**
     * Describe en texto la causa específica de la anomalía de un reporte.
     * Útil para mensajes de alerta y para la defensa oral.
     *
     * @param r reporte a analizar (debe ser anomalía)
     * @return descripción de los parámetros fuera de rango
     */
    public String describirAnomalia(ReporteMuestreo r) {
        StringBuilder causa = new StringBuilder();
        if (r.getPh() < PH_MIN) {
            causa.append(String.format("pH BAJO (%.2f < %.1f)  ", r.getPh(), PH_MIN));
        } else if (r.getPh() > PH_MAX) {
            causa.append(String.format("pH ALTO (%.2f > %.1f)  ", r.getPh(), PH_MAX));
        }
        if (r.getConductividadElectrica() < COND_MIN) {
            causa.append(String.format("COND. BAJA (%.3f < %.1f mS/cm)",
                    r.getConductividadElectrica(), COND_MIN));
        } else if (r.getConductividadElectrica() > COND_MAX) {
            causa.append(String.format("COND. ALTA (%.3f > %.1f mS/cm)",
                    r.getConductividadElectrica(), COND_MAX));
        }
        return causa.length() > 0 ? causa.toString().trim() : "Sin anomalía";
    }

    /** Indicador visual del estado del pH. */
    private String flechaPh(double ph) {
        if (ph < PH_MIN) return String.format("↓ bajo (óptimo ≥ %.1f)", PH_MIN);
        if (ph > PH_MAX) return String.format("↑ alto (óptimo ≤ %.1f)", PH_MAX);
        return String.format("  en rango [%.1f–%.1f]", PH_MIN, PH_MAX);
    }

    /** Indicador visual del estado de la conductividad. */
    private String flechaCond(double cond) {
        if (cond < COND_MIN) return String.format("↓ baja (óptimo ≥ %.1f mS/cm)", COND_MIN);
        if (cond > COND_MAX) return String.format("↑ alta (óptimo ≤ %.1f mS/cm)", COND_MAX);
        return String.format("  en rango [%.1f–%.1f] mS/cm", COND_MIN, COND_MAX);
    }

    // ─── toString ─────────────────────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format(
            "BitacoraHistorica { id='%s' | totalReportes=%d | %%anomalias=%.1f%% }",
            id, reportes.size(), calcularPorcentajeAnomalias());
    }
}
