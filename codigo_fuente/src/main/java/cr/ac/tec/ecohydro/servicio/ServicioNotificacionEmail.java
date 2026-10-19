package cr.ac.tec.ecohydro.servicio;

import cr.ac.tec.ecohydro.modelo.BitacoraHistorica;
import cr.ac.tec.ecohydro.modelo.Operador;
import cr.ac.tec.ecohydro.modelo.ReporteMuestreo;
import cr.ac.tec.ecohydro.modelo.UnidadCentralControl;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Properties;

/**
 * RF5 — Envío de alertas de anomalía química por correo SMTP (Gmail) con
 * Jakarta Mail.
 *
 * <p>Configuración: host smtp.gmail.com, puerto 587, STARTTLS obligatorio.
 * Las credenciales se leen de variables de entorno (nunca van en el código):
 * <ul>
 *   <li>{@code ECOHYDRO_SMTP_USER} — cuenta Gmail remitente.</li>
 *   <li>{@code ECOHYDRO_SMTP_PASS} — contraseña de aplicación de Gmail.</li>
 * </ul>
 * También pueden fijarse con {@link #configurarCredenciales(String, String)}.
 *
 * <p>Cualquier fallo (credenciales ausentes, MessagingException, correo
 * inválido) se informa con una advertencia en consola y NO se propaga.
 *
 * Dependencia Maven: com.sun.mail:jakarta.mail:2.0.1.
 *
 * IC-2101 Programación Orientada a Objetos — ITCR Campus San Carlos
 */
public class ServicioNotificacionEmail {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int    SMTP_PORT = 587;

    private static final String VAR_USUARIO = "ECOHYDRO_SMTP_USER";
    private static final String VAR_CLAVE   = "ECOHYDRO_SMTP_PASS";

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private String usuario = System.getenv(VAR_USUARIO);
    private String clave   = System.getenv(VAR_CLAVE);

    /** Permite configurar las credenciales sin variables de entorno. */
    public void configurarCredenciales(String usuario, String clave) {
        this.usuario = usuario;
        this.clave   = clave;
    }

    /**
     * Envía un correo de alerta al operador cuando se detecta anomalía química.
     * Captura MessagingException internamente: solo registra advertencia.
     *
     * @param operador el operador a notificar
     * @param unidad   la unidad donde ocurrió la anomalía
     * @param reporte  el reporte con los valores fuera de rango
     */
    public void enviarAlertaAnomaliaQuimica(Operador operador,
                                            UnidadCentralControl unidad,
                                            ReporteMuestreo reporte) {
        if (operador == null || unidad == null || reporte == null) {
            System.out.println("[ADVERTENCIA] Correo no enviado: faltan datos "
                    + "(operador, unidad o reporte nulo).");
            return;
        }
        if (usuario == null || usuario.isBlank() || clave == null || clave.isBlank()) {
            System.out.println("[ADVERTENCIA] Correo no enviado: defina las variables "
                    + VAR_USUARIO + " y " + VAR_CLAVE + ".");
            return;
        }

        try {
            Session sesion = Session.getInstance(crearPropiedades(), new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(usuario, clave);
                }
            });

            MimeMessage mensaje = construirMensaje(sesion, operador, unidad, reporte);
            Transport.send(mensaje);
            System.out.println("[CORREO] Alerta enviada a " + operador.getCorreoElectronico());

        } catch (MessagingException e) {
            System.out.println("[ADVERTENCIA] No se pudo enviar el correo de alerta: "
                    + e.getMessage());
        }
    }

    /** Propiedades SMTP: STARTTLS habilitado y requerido, con timeouts. */
    private Properties crearPropiedades() {
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        return props;
    }

    private MimeMessage construirMensaje(Session sesion, Operador operador,
                                         UnidadCentralControl unidad,
                                         ReporteMuestreo reporte) throws MessagingException {
        MimeMessage mensaje = new MimeMessage(sesion);
        mensaje.setFrom(new InternetAddress(usuario));
        mensaje.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(operador.getCorreoElectronico()));
        mensaje.setSubject("[EcoHydro-4] Alerta: anomalía química en "
                + unidad.getCodigoInterno(), "UTF-8");
        mensaje.setText(construirCuerpo(operador, unidad, reporte), "UTF-8");
        return mensaje;
    }

    /** Cuerpo del correo (visible en el paquete para poder probarlo sin SMTP). */
    String construirCuerpo(Operador operador, UnidadCentralControl unidad,
                           ReporteMuestreo reporte) {
        double ph   = reporte.getPh();
        double cond = reporte.getConductividadElectrica();

        String estadoPh = (ph < BitacoraHistorica.PH_MIN)
                ? "FUERA DE RANGO (bajo; rango normal "
                : (ph > BitacoraHistorica.PH_MAX) ? "FUERA DE RANGO (alto; rango normal "
                : "Normal (rango ";
        String estadoCond = (cond < BitacoraHistorica.COND_MIN)
                ? "FUERA DE RANGO (baja; rango normal "
                : (cond > BitacoraHistorica.COND_MAX) ? "FUERA DE RANGO (alta; rango normal "
                : "Normal (rango ";

        return String.format(Locale.ROOT,
            "Estimado/a %s:%n%n"
            + "El sistema EcoHydro-4 detectó una anomalía química.%n%n"
            + "Unidad central : %s (%s)%n"
            + "Fecha y hora   : %s%n%n"
            + "pH detectado               : %.2f -> %s%.1f - %.1f)%n"
            + "Conductividad detectada    : %.2f mS/cm -> %s%.1f - %.1f)%n%n"
            + "Se recomienda revisar la solución nutritiva a la brevedad.%n%n"
            + "-- EcoHydro-4 | IC-2101 POO, ITCR%n",
            operador.getNombreCompleto(),
            unidad.getCodigoInterno(), unidad.getUbicacion(),
            reporte.getFechaHora().format(FMT),
            ph, estadoPh, BitacoraHistorica.PH_MIN, BitacoraHistorica.PH_MAX,
            cond, estadoCond, BitacoraHistorica.COND_MIN, BitacoraHistorica.COND_MAX);
    }
}
