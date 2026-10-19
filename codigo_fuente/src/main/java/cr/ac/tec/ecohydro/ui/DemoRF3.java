package cr.ac.tec.ecohydro.ui;

import cr.ac.tec.ecohydro.modelo.BitacoraHistorica;
import cr.ac.tec.ecohydro.modelo.Operador;
import cr.ac.tec.ecohydro.modelo.ReporteMuestreo;
import cr.ac.tec.ecohydro.modelo.UnidadCentralControl;

import java.time.LocalDateTime;

/**
 * Demostración ejecutable de RF3 — Bitácora y Anomalías Químicas.
 *
 * Escenarios cubiertos:
 *   A. Bitácora vacía → calcularPorcentaje y generarResumen manejan 0 registros.
 *   B. Mezcla de reportes normales y con anomalía → estadísticas completas.
 *   C. Todos los reportes son anómalos → 100 % de anomalías.
 *   D. Descripción detallada de causa por reporte (pH bajo/alto, cond. baja/alta).
 *   E. Integración con UnidadCentralControl: RF2 → registrarReporte → generarResumen.
 *
 * IC-2101 Programación Orientada a Objetos — ITCR Campus San Carlos
 */
public class DemoRF3 {

    private static final String SEP  = "────────────────────────────────────────────────────────────";
    private static final String SEPC = "════════════════════════════════════════════════════════════";

    public static void main(String[] args) {

        System.out.println(SEPC);
        System.out.println("  EcoHydro-4 — Demo RF3: Bitácora y Anomalías");
        System.out.println("  IC-2101 POO — ITCR Campus San Carlos");
        System.out.println(SEPC);

        // ══════════════════════════════════════════════════════════════════════
        // ESCENARIO A — Bitácora vacía
        // Demuestra: calcularPorcentajeAnomalias() maneja división por cero
        // ══════════════════════════════════════════════════════════════════════
        System.out.println("\n[ESCENARIO A] Bitácora VACÍA — división por cero controlada");
        System.out.println(SEP);

        BitacoraHistorica bitacoraVacia = new BitacoraHistorica("BIT-VACÍA");
        System.out.println("  Total reportes   : " + bitacoraVacia.getTotalReportes());
        System.out.printf ("  %% anomalías      : %.1f%%%n",
                bitacoraVacia.calcularPorcentajeAnomalias());
        System.out.println("  Resumen          :\n  "
                + bitacoraVacia.generarResumen());

        // ══════════════════════════════════════════════════════════════════════
        // ESCENARIO B — Mezcla de reportes normales y anómalos
        // Demuestra: registrarReporte, esAnomaliaQuimica, calcularPorcentaje,
        //            generarResumen con detalle de causa por reporte
        // ══════════════════════════════════════════════════════════════════════
        System.out.println("\n[ESCENARIO B] Mezcla de reportes NORMALES y ANÓMALOS");
        System.out.println(SEP);

        BitacoraHistorica bitacoraB = new BitacoraHistorica("BIT-001");

        // ── Reportes con valores normales ─────────────────────────────────────
        ReporteMuestreo rNormal1 = new ReporteMuestreo(
                LocalDateTime.now().minusHours(5), 6.0, 1.8, 135.0);
        ReporteMuestreo rNormal2 = new ReporteMuestreo(
                LocalDateTime.now().minusHours(4), 5.8, 2.0, 140.0);

        // ── pH por debajo del mínimo (< 5.5) ──────────────────────────────────
        ReporteMuestreo rPhBajo = new ReporteMuestreo(
                LocalDateTime.now().minusHours(3), 4.9, 1.7, 130.0);

        // ── pH por encima del máximo (> 6.5) ──────────────────────────────────
        ReporteMuestreo rPhAlto = new ReporteMuestreo(
                LocalDateTime.now().minusHours(2), 7.1, 2.1, 132.0);

        // ── Conductividad baja (< 1.2) ────────────────────────────────────────
        ReporteMuestreo rCondBaja = new ReporteMuestreo(
                LocalDateTime.now().minusHours(1), 6.1, 0.9, 128.0);

        // ── Conductividad alta (> 2.4) ────────────────────────────────────────
        ReporteMuestreo rCondAlta = new ReporteMuestreo(
                LocalDateTime.now(), 5.9, 3.0, 145.0);

        // RF3.1 — registrarReporte
        bitacoraB.registrarReporte(rNormal1);
        bitacoraB.registrarReporte(rNormal2);
        bitacoraB.registrarReporte(rPhBajo);
        bitacoraB.registrarReporte(rPhAlto);
        bitacoraB.registrarReporte(rCondBaja);
        bitacoraB.registrarReporte(rCondAlta);

        // RF3.2 — esAnomaliaQuimica (verificación individual)
        System.out.println("  esAnomaliaQuimica() por reporte:");
        System.out.printf("    rNormal1  (pH=6.0,  cond=1.8) → %b%n",
                bitacoraB.esAnomaliaQuimica(rNormal1));
        System.out.printf("    rNormal2  (pH=5.8,  cond=2.0) → %b%n",
                bitacoraB.esAnomaliaQuimica(rNormal2));
        System.out.printf("    rPhBajo   (pH=4.9,  cond=1.7) → %b  ← pH < 5.5%n",
                bitacoraB.esAnomaliaQuimica(rPhBajo));
        System.out.printf("    rPhAlto   (pH=7.1,  cond=2.1) → %b  ← pH > 6.5%n",
                bitacoraB.esAnomaliaQuimica(rPhAlto));
        System.out.printf("    rCondBaja (pH=6.1,  cond=0.9) → %b  ← cond < 1.2%n",
                bitacoraB.esAnomaliaQuimica(rCondBaja));
        System.out.printf("    rCondAlta (pH=5.9,  cond=3.0) → %b  ← cond > 2.4%n",
                bitacoraB.esAnomaliaQuimica(rCondAlta));

        // RF3.3 — calcularPorcentajeAnomalias
        System.out.printf("%n  calcularPorcentajeAnomalias() : %.1f%% (4 de 6 reportes)%n",
                bitacoraB.calcularPorcentajeAnomalias());

        // RF3.4 — generarResumen
        System.out.println("\n  generarResumen():");
        System.out.println(bitacoraB.generarResumen());

        // ══════════════════════════════════════════════════════════════════════
        // ESCENARIO C — Todos los reportes son anómalos → 100 %
        // ══════════════════════════════════════════════════════════════════════
        System.out.println("\n[ESCENARIO C] TODOS los reportes anómalos → 100 %");
        System.out.println(SEP);

        BitacoraHistorica bitacoraC = new BitacoraHistorica("BIT-CRIT");
        bitacoraC.registrarReporte(new ReporteMuestreo(
                LocalDateTime.now().minusMinutes(30), 4.0, 0.5, 110.0));
        bitacoraC.registrarReporte(new ReporteMuestreo(
                LocalDateTime.now().minusMinutes(15), 8.0, 3.5, 180.0));
        bitacoraC.registrarReporte(new ReporteMuestreo(
                LocalDateTime.now(), 3.5, 5.0, 200.0));

        System.out.printf("  Total: %d | %% anomalías: %.1f%%%n",
                bitacoraC.getTotalReportes(),
                bitacoraC.calcularPorcentajeAnomalias());
        System.out.println(bitacoraC.generarResumen());

        // ══════════════════════════════════════════════════════════════════════
        // ESCENARIO D — describirAnomalia: causa específica por parámetro
        // ══════════════════════════════════════════════════════════════════════
        System.out.println("\n[ESCENARIO D] describirAnomalia() — causa específica por reporte");
        System.out.println(SEP);

        BitacoraHistorica aux = new BitacoraHistorica("AUX");
        ReporteMuestreo[] casos = {
            new ReporteMuestreo(LocalDateTime.now(), 4.9, 1.8, 100.0),  // pH bajo
            new ReporteMuestreo(LocalDateTime.now(), 7.2, 1.9, 100.0),  // pH alto
            new ReporteMuestreo(LocalDateTime.now(), 6.0, 0.8, 100.0),  // cond baja
            new ReporteMuestreo(LocalDateTime.now(), 6.0, 3.1, 100.0),  // cond alta
            new ReporteMuestreo(LocalDateTime.now(), 4.0, 3.5, 100.0),  // pH bajo + cond alta
        };
        String[] etiquetas = {"pH bajo", "pH alto", "cond. baja", "cond. alta", "ambos"};

        for (int i = 0; i < casos.length; i++) {
            System.out.printf("  [%-10s] causa → %s%n",
                    etiquetas[i], aux.describirAnomalia(casos[i]));
        }

        // ══════════════════════════════════════════════════════════════════════
        // ESCENARIO E — Integración RF2 → RF3: ciclo completo con unidad
        // ══════════════════════════════════════════════════════════════════════
        System.out.println("\n[ESCENARIO E] Integración RF2 → RF3: ciclo completo");
        System.out.println(SEP);

        BitacoraHistorica bitacoraE = new BitacoraHistorica("BIT-INT");
        Operador op = new Operador("EMP-001", "David Zamora", "david@tec.ac.cr");
        UnidadCentralControl unidad = new UnidadCentralControl(
                "Invernadero Principal", "UCC-E01",
                500.0, 5.0, 30.0, bitacoraE, op);

        System.out.println("  Ejecutando 4 ciclos de muestreo (RF2)...");

        // Ciclo 1 — normal
        ReporteMuestreo re1 = unidad.ejecutarCicloMuestreo(6.1, 1.9, 0.25);
        bitacoraE.registrarReporte(re1);
        System.out.println("  Ciclo 1 registrado: " + re1);

        // Ciclo 2 — pH anómalo
        ReporteMuestreo re2 = unidad.ejecutarCicloMuestreo(5.0, 2.0, 0.25);
        bitacoraE.registrarReporte(re2);
        System.out.println("  Ciclo 2 registrado: " + re2
                + (bitacoraE.esAnomaliaQuimica(re2) ? " ⚠ ANOMALÍA" : ""));

        // Ciclo 3 — normal
        ReporteMuestreo re3 = unidad.ejecutarCicloMuestreo(5.9, 2.2, 0.25);
        bitacoraE.registrarReporte(re3);
        System.out.println("  Ciclo 3 registrado: " + re3);

        // Ciclo 4 — conductividad alta
        ReporteMuestreo re4 = unidad.ejecutarCicloMuestreo(6.0, 2.8, 0.25);
        bitacoraE.registrarReporte(re4);
        System.out.println("  Ciclo 4 registrado: " + re4
                + (bitacoraE.esAnomaliaQuimica(re4) ? " ⚠ ANOMALÍA" : ""));

        System.out.println();
        System.out.println(bitacoraE.generarResumen());

        System.out.println(SEPC);
        System.out.println("  Demo RF3 completada exitosamente.");
        System.out.println(SEPC);
    }
}
