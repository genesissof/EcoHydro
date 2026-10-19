package cr.ac.tec.ecohydro.ui;

import cr.ac.tec.ecohydro.modelo.BitacoraHistorica;
import cr.ac.tec.ecohydro.modelo.Operador;
import cr.ac.tec.ecohydro.modelo.ReporteMuestreo;
import cr.ac.tec.ecohydro.modelo.UnidadCentralControl;
import cr.ac.tec.ecohydro.servicio.ServicioNotificacionEmail;

import java.time.LocalDateTime;

/**
 * Demostración de RF5. Sin variables ECOHYDRO_SMTP_USER / ECOHYDRO_SMTP_PASS
 * muestra la advertencia y continúa; con ellas envía el correo real.
 * Cambie el correo del operador por uno suyo para probar.
 */
public class DemoRF5 {
    public static void main(String[] args) {
        Operador op = new Operador("EMP-001", "Ana Rodríguez", "operador@example.com");
        UnidadCentralControl unidad = new UnidadCentralControl(
                "Invernadero ITCR San Carlos", "ECO-001", 500.0, 2.0, 45.0,
                new BitacoraHistorica("BIT-001"), op);
        ReporteMuestreo r = new ReporteMuestreo(LocalDateTime.now(), 7.2, 2.9, 120.0);

        new ServicioNotificacionEmail().enviarAlertaAnomaliaQuimica(op, unidad, r);
        System.out.println("El programa continúa normalmente.");
    }
}
