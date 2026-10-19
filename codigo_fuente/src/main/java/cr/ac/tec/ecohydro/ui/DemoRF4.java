package cr.ac.tec.ecohydro.ui;

import cr.ac.tec.ecohydro.modelo.BitacoraHistorica;
import cr.ac.tec.ecohydro.modelo.ReporteMuestreo;
import cr.ac.tec.ecohydro.modelo.UnidadCentralControl;
import cr.ac.tec.ecohydro.servicio.ConsultaClimatica;
import cr.ac.tec.ecohydro.servicio.ServicioClimatologico;

/**
 * Demostración ejecutable de RF4 — API climatológica Open-Meteo.
 *
 *   A. Consulta real (San Carlos, Costa Rica). Si no hay internet, se muestra
 *      la advertencia y el programa continúa.
 *   B. Compensación térmica: con 35 °C simulados, el siguiente muestreo
 *      aplica +5 % a la conductividad y luego vuelve a la normalidad.
 *   C. Coordenadas inválidas → advertencia sin detener el programa.
 *
 * IC-2101 Programación Orientada a Objetos — ITCR Campus San Carlos
 */
public class DemoRF4 {

    public static void main(String[] args) {
        ServicioClimatologico servicio = new ServicioClimatologico();
        UnidadCentralControl unidad = new UnidadCentralControl(
                "Invernadero ITCR San Carlos", "ECO-001", 500.0, 2.0, 45.0,
                new BitacoraHistorica("BIT-001"), null);

        System.out.println("[A] Consulta real a Open-Meteo (lat 10.36, lon -84.51)");
        ConsultaClimatica real = servicio.consultarClima(10.36, -84.51);
        System.out.println(real != null ? real : "Sin datos (el programa continúa).");
        unidad.aplicarClima(real);

        System.out.println("\n[B] Compensación térmica con 35 °C simulados");
        ConsultaClimatica caliente = new ConsultaClimatica(35.0, 60, "Simulado");
        System.out.println(caliente);
        unidad.aplicarClima(caliente);
        ReporteMuestreo r1 = unidad.ejecutarCicloMuestreo(6.0, 2.0, 1.0);
        ReporteMuestreo r2 = unidad.ejecutarCicloMuestreo(6.0, 2.0, 1.0);
        System.out.printf("Conductividad medida 2.00 -> muestreo 1: %.2f (corregida +5%%)%n",
                r1.getConductividadElectrica());
        System.out.printf("Conductividad medida 2.00 -> muestreo 2: %.2f (sin corrección)%n",
                r2.getConductividadElectrica());

        System.out.println("\n[C] Coordenadas inválidas");
        System.out.println("Resultado: " + servicio.consultarClima(200, 0));
    }
}
