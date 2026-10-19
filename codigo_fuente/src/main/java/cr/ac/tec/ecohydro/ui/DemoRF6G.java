package cr.ac.tec.ecohydro.ui;

import cr.ac.tec.ecohydro.modelo.BitacoraHistorica;
import cr.ac.tec.ecohydro.modelo.Operador;
import cr.ac.tec.ecohydro.modelo.UnidadCentralControl;

/**
 * Demo autónoma de la simulación de falla (sin menú): crea la unidad, genera
 * 3 reportes (uno anómalo), los registra y ejecuta la simulación.
 */
public class DemoRF6G {
    public static void main(String[] args) {
        BitacoraHistorica bitacora = new BitacoraHistorica("BIT-001");
        Operador op = new Operador("EMP-001", "Ana Rodríguez", "ana@example.com");
        UnidadCentralControl unidad = new UnidadCentralControl(
                "Invernadero ITCR San Carlos", "ECO-001", 500.0, 2.0, 45.0, bitacora, op);

        bitacora.registrarReporte(unidad.ejecutarCicloMuestreo(6.0, 1.8, 2.0));
        bitacora.registrarReporte(unidad.ejecutarCicloMuestreo(6.3, 2.1, 1.5));
        bitacora.registrarReporte(unidad.ejecutarCicloMuestreo(7.4, 2.9, 1.0)); // anomalía

        SimulacionFalla.ejecutar(unidad, bitacora);
    }
}
