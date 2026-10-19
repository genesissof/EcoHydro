package cr.ac.tec.ecohydro.ui;

import cr.ac.tec.ecohydro.modelo.BitacoraHistorica;
import cr.ac.tec.ecohydro.modelo.BrazoRobotico;
import cr.ac.tec.ecohydro.modelo.ReporteMuestreo;
import cr.ac.tec.ecohydro.modelo.UnidadCentralControl;

import java.util.ArrayList;
import java.util.List;

/**
 * Simulación de falla crítica — demostración visual de Composición vs
 * Agregación para la defensa oral.
 *
 * <ul>
 *   <li><b>Composición</b>: al destruir la unidad, su referencia al
 *       BrazoRobotico queda en null; fuera de la unidad el brazo no existe.</li>
 *   <li><b>Agregación</b>: la BitacoraHistorica y sus ReporteMuestreo se
 *       acceden por referencias independientes y siguen intactos.</li>
 * </ul>
 *
 * IC-2101 Programación Orientada a Objetos — ITCR Campus San Carlos
 */
public final class SimulacionFalla {

    private static final String SEP = "──────────────────────────────────────────────────────";

    private SimulacionFalla() { }

    /**
     * Ejecuta la simulación completa (antes → destrucción → después).
     *
     * @param unidad   unidad central operativa (brazo no nulo)
     * @param bitacora bitácora del sistema (referencia independiente de la unidad)
     */
    public static void ejecutar(UnidadCentralControl unidad, BitacoraHistorica bitacora) {

        // ── 1. Estado ANTES de la falla ──────────────────────────────────────
        System.out.println("\n" + SEP);
        System.out.println(" 1) ESTADO ANTES DE LA FALLA");
        System.out.println(SEP);
        BrazoRobotico brazo = unidad.getBrazoRobotico();
        System.out.printf("  Unidad  : %s — estado: %s%n",
                unidad.getCodigoInterno(), unidad.getEstadoFuncionamiento());
        System.out.printf("  Brazo   : velocidad=%.2f | horas de uso=%.2f → FUNCIONANDO%n",
                brazo.getVelocidadDesplazamiento(), brazo.getHorasUsoAcumuladas());
        System.out.printf("  Bitácora: %d reportes existentes%n", bitacora.getTotalReportes());

        // Copia de las referencias a los reportes para probar después que son los MISMOS objetos
        List<ReporteMuestreo> antes = new ArrayList<>(bitacora.getReportes());
        int idBitacoraAntes = System.identityHashCode(bitacora);

        // ── 2. Destrucción de la unidad central ──────────────────────────────
        System.out.println("\n" + SEP);
        System.out.println(" 2) DESTRUCCIÓN: unidadCentral.destruir()");
        System.out.println(SEP);
        unidad.destruir();   // this.brazoRobotico = null; estado = "Falla Crítica Permanente"
        System.out.println("  💥 brazoRobotico = null | estado = "
                + unidad.getEstadoFuncionamiento());

        // ── 3. Estado DESPUÉS de la falla ────────────────────────────────────
        System.out.println("\n" + SEP);
        System.out.println(" 3) ESTADO DESPUÉS DE LA FALLA");
        System.out.println(SEP);

        // 3a. Composición: el brazo ya no es alcanzable a través de la unidad
        System.out.println("  [Composición ◆] Intentando acceder al brazo vía la unidad...");
        try {
            double v = unidad.getBrazoRobotico().getVelocidadDesplazamiento();
            System.out.println("  Velocidad: " + v);   // no se alcanza
        } catch (NullPointerException e) {
            System.out.println("  ✗ Brazo robótico destruido: no existe fuera de la unidad central");
        }

        // 3b. Agregación: la bitácora sigue accesible y con todos sus reportes
        System.out.println("\n  [Agregación ◇] La bitácora sigue accesible (referencia independiente):");
        System.out.printf("  ✔ Misma bitácora en memoria: %s (identidad %s)%n",
                bitacora.getId(),
                System.identityHashCode(bitacora) == idBitacoraAntes ? "idéntica" : "DISTINTA");
        System.out.printf("  ✔ Reportes conservados: %d de %d%n",
                bitacora.getTotalReportes(), antes.size());

        // 3c. Los ReporteMuestreo siguen en memoria: mostrar su contenido
        List<ReporteMuestreo> despues = bitacora.getReportes();
        if (despues.isEmpty()) {
            System.out.println("  (La bitácora estaba vacía: registre reportes con las opciones 2 y 4 "
                    + "para ver esta parte de la demostración.)");
        } else {
            System.out.println("\n  Contenido de los reportes (objetos que NO dependían del brazo):");
            for (int i = 0; i < despues.size(); i++) {
                boolean mismo = despues.get(i) == antes.get(i);
                System.out.printf("   %d. %s %s%n", i + 1, despues.get(i),
                        mismo ? "[mismo objeto]" : "[¡objeto distinto!]");
            }
        }

        System.out.println("\n  Conclusión: destruir el TODO (unidad) elimina la parte compuesta (brazo)"
                + "\n  pero no los objetos agregados/independientes (bitácora, reportes).");
    }
}
