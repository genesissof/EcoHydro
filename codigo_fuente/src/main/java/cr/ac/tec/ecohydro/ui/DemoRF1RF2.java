package cr.ac.tec.ecohydro.ui;

import cr.ac.tec.ecohydro.modelo.BitacoraHistorica;
import cr.ac.tec.ecohydro.modelo.Operador;
import cr.ac.tec.ecohydro.modelo.ReporteMuestreo;
import cr.ac.tec.ecohydro.modelo.UnidadCentralControl;

/**
 * Demostración ejecutable de RF1 (diagnosticarViabilidad) y RF2 (ejecutarCicloMuestreo).
 *
 * Escenarios cubiertos:
 *   A. Sistema en condiciones normales → RF1 retorna "Activo", RF2 genera reporte.
 *   B. Brazo con más de 1000 h de uso → RF1 detecta mantenimiento, reduce velocidad al 40 %.
 *   C. Potencia insuficiente           → RF1 detecta sobrecarga, reduce velocidad al 40 %.
 *   D. Patrón de Dependencia UML       → el brazo NO retiene el ReporteMuestreo.
 *
 * IC-2101 Programación Orientada a Objetos — ITCR Campus San Carlos
 */
public class DemoRF1RF2 {

    // ─── Separadores visuales ────────────────────────────────────────────────
    private static final String SEP  = "────────────────────────────────────────────";
    private static final String SEPC = "============================================";

    public static void main(String[] args) {

        System.out.println(SEPC);
        System.out.println("  EcoHydro-4 — Demo RF1 y RF2");
        System.out.println("  IC-2101 POO — ITCR Campus San Carlos");
        System.out.println(SEPC);

        // ══════════════════════════════════════════════════════════════════════
        // ESCENARIO A — Sistema en condiciones normales
        // ══════════════════════════════════════════════════════════════════════
        System.out.println("\n[ESCENARIO A] Sistema NORMAL — Activo y apto para muestreo");
        System.out.println(SEP);

        BitacoraHistorica bitacora = new BitacoraHistorica("BIT-001");
        Operador operador = new Operador("EMP-042", "David Zamora", "david@tec.ac.cr");

        // Límite 500 W, velocidad 5 cm/s, ángulo inicial 30°, 0 h de uso → sin problemas
        UnidadCentralControl unidad = new UnidadCentralControl(
                "Invernadero Norte — Nivel 2",
                "UCC-2024-A",
                500.0,   // limitePotenciaW
                5.0,     // velBrazo cm/s
                30.0,    // anguloBrazo
                bitacora,
                operador);

        // RF1
        System.out.println(unidad.diagnosticarViabilidad());

        // RF2 — pH y conductividad normales
        ReporteMuestreo r1 = unidad.ejecutarCicloMuestreo(6.0, 1.8, 0.5);
        System.out.println("  Reporte generado: " + r1);
        System.out.printf("  Horas uso brazo post-ciclo: %.2f h%n",
                unidad.getBrazoRobotico().getHorasUsoAcumuladas());

        // ══════════════════════════════════════════════════════════════════════
        // ESCENARIO B — Brazo con más de 1000 horas → mantenimiento requerido
        // ══════════════════════════════════════════════════════════════════════
        System.out.println("\n[ESCENARIO B] HORAS USO > 1000 h → RF1 reduce velocidad al 40 %");
        System.out.println(SEP);

        BitacoraHistorica bitacoraB = new BitacoraHistorica("BIT-002");
        UnidadCentralControl unidadB = new UnidadCentralControl(
                "Invernadero Sur — Nivel 1",
                "UCC-2024-B",
                500.0,
                10.0,    // velBrazo
                45.0,
                bitacoraB,
                null);

        // Simular horas de uso acumuladas superiores al umbral
        unidadB.getBrazoRobotico().setHorasUsoAcumuladas(1050.0);

        double velAntes = unidadB.getBrazoRobotico().getVelocidadDesplazamiento();
        System.out.println("  Velocidad ANTES del diagnóstico : " + velAntes + " cm/s");

        System.out.println(unidadB.diagnosticarViabilidad());

        double velDespues = unidadB.getBrazoRobotico().getVelocidadDesplazamiento();
        System.out.printf("  Velocidad DESPUÉS del diagnóstico: %.2f cm/s  (40 %% de %.1f)%n",
                velDespues, velAntes);

        // RF2 sigue funcionando aunque esté en modo reducido
        ReporteMuestreo r2 = unidadB.ejecutarCicloMuestreo(5.2, 1.0, 0.25);
        System.out.println("  Reporte en modo reducido: " + r2);

        // ══════════════════════════════════════════════════════════════════════
        // ESCENARIO C — Potencia insuficiente → sobrecarga
        // ══════════════════════════════════════════════════════════════════════
        System.out.println("\n[ESCENARIO C] SOBRECARGA ELÉCTRICA → RF1 reduce velocidad al 40 %");
        System.out.println(SEP);

        BitacoraHistorica bitacoraC = new BitacoraHistorica("BIT-003");
        // Límite muy bajo (100 W): brazo a 20 cm/s genera ~296 W → supera el límite
        UnidadCentralControl unidadC = new UnidadCentralControl(
                "Módulo Experimental — Lab 3",
                "UCC-2024-C",
                100.0,   // limitePotenciaW bajo a propósito
                20.0,    // velBrazo alta → mucha potencia
                60.0,
                bitacoraC,
                operador);

        System.out.printf("  Potencia brazo estimada: %.2f W + %.2f W aux = %.2f W total%n",
                (60.0 * 1.5) + (20.0 * 12.8), 50.0,
                (60.0 * 1.5) + (20.0 * 12.8) + 50.0);
        System.out.println("  Límite disponible: 100.00 W → SOBRECARGA");

        System.out.println(unidadC.diagnosticarViabilidad());

        // ══════════════════════════════════════════════════════════════════════
        // ESCENARIO D — Demostración explícita del patrón de Dependencia UML
        // ══════════════════════════════════════════════════════════════════════
        System.out.println("\n[ESCENARIO D] PATRÓN DEPENDENCIA UML — El brazo NO retiene el reporte");
        System.out.println(SEP);

        BitacoraHistorica bitacoraD = new BitacoraHistorica("BIT-004");
        UnidadCentralControl unidadD = new UnidadCentralControl(
                "Invernadero Oriente", "UCC-2024-D",
                500.0, 8.0, 45.0, bitacoraD, operador);

        System.out.println("  Ejecutando RF2...");
        ReporteMuestreo reporteD = unidadD.ejecutarCicloMuestreo(6.2, 2.0, 0.1);

        System.out.println("  Reporte retornado (en manos del llamador):");
        System.out.println("    " + reporteD);

        // El brazo NO tiene atributo de tipo ReporteMuestreo → demostración de dependencia
        System.out.println();
        System.out.println("  ✔  BrazoRobotico NO tiene campo ReporteMuestreo.");
        System.out.println("     El reporte fue creado como variable local en");
        System.out.println("     ejecutarCicloMuestreo() y retornado al llamador.");
        System.out.println("     Esto implementa la Dependencia UML: uso temporal,");
        System.out.println("     sin atributo persistente en el brazo.");
        System.out.println("  ✔  Ciclo de vida del reporte es INDEPENDIENTE del brazo.");
        System.out.println("     Si el brazo se destruye, el reporte sigue existiendo");
        System.out.println("     mientras el llamador mantenga la referencia.");

        System.out.println();
        System.out.println(SEPC);
        System.out.println("  Demo RF1 + RF2 completada exitosamente.");
        System.out.println(SEPC);
    }
}
