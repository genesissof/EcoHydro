package cr.ac.tec.ecohydro.ui;

import cr.ac.tec.ecohydro.modelo.BitacoraHistorica;
import cr.ac.tec.ecohydro.modelo.Operador;
import cr.ac.tec.ecohydro.modelo.ReporteMuestreo;
import cr.ac.tec.ecohydro.modelo.UnidadCentralControl;
import cr.ac.tec.ecohydro.servicio.ConsultaClimatica;
import cr.ac.tec.ecohydro.servicio.ServicioClimatologico;
import cr.ac.tec.ecohydro.servicio.ServicioNotificacionEmail;

import java.nio.charset.StandardCharsets;
import java.util.InputMismatchException;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Predicate;

/**
 * RF6 — Interfaz de consola de EcoHydro-4 (opciones 1–7) con resiliencia
 * ante entradas inválidas.
 *
 * <p>Resiliencia: toda lectura numérica se hace dentro de un bucle
 * {@code while} con {@code try-catch(InputMismatchException)}; tras la
 * excepción se limpia el buffer con {@code scanner.nextLine()} y se vuelve a
 * pedir el dato. Nunca se usa {@code System.exit()}.
 *
 * <p>Los decimales se escriben con punto (por ejemplo 6.2).
 *
 * IC-2101 Programación Orientada a Objetos — ITCR Campus San Carlos
 */
public class MenuPrincipal {

    // Coordenadas por defecto: ITCR Campus San Carlos (Santa Clara, Alajuela)
    private static final double LAT_DEFECTO = 10.36;
    private static final double LON_DEFECTO = -84.51;

    private final Scanner scanner;
    private final ServicioClimatologico servicioClima = new ServicioClimatologico();
    private final ServicioNotificacionEmail servicioCorreo = new ServicioNotificacionEmail();

    // Estado de la sesión
    private UnidadCentralControl unidad;
    private BitacoraHistorica bitacora;
    private ReporteMuestreo reportePendiente;   // generado pero aún no registrado

    public MenuPrincipal(Scanner scanner) {
        this.scanner = scanner;
    }

    /** Punto de entrada de la aplicación. */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8).useLocale(Locale.US);
        new MenuPrincipal(sc).ejecutar();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Bucle principal
    // ═════════════════════════════════════════════════════════════════════════
    public void ejecutar() {
        boolean salir = false;
        try {
            while (!salir) {
                mostrarMenu();
                int opcion = leerEntero("Seleccione una opción (1-7): ", 1, 7);
                switch (opcion) {
                    case 1 -> configuracionInicial();
                    case 2 -> ejecutarMuestreo();
                    case 3 -> sincronizarClima();
                    case 4 -> registrarYVerificar();
                    case 5 -> verBitacora();
                    case 6 -> simularFalla();
                    case 7 -> {
                        System.out.println("\nCerrando EcoHydro-4. ¡Hasta pronto!");
                        salir = true;
                    }
                    default -> System.out.println("Opción no válida.");
                }
            }
        } catch (NoSuchElementException e) {
            // La entrada se cerró (Ctrl+D / fin de archivo): terminar con elegancia
            System.out.println("\nEntrada finalizada. Cerrando EcoHydro-4.");
        }
    }

    private void mostrarMenu() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║             EcoHydro-4 — Menú principal          ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.println("║ 1. Configuración inicial                         ║");
        System.out.println("║ 2. Ejecutar ciclo de muestreo (RF1 + RF2)        ║");
        System.out.println("║ 3. Sincronizar clima externo (RF4)               ║");
        System.out.println("║ 4. Registrar en bitácora y verificar (RF3 + RF5) ║");
        System.out.println("║ 5. Ver estado de bitácora e historial            ║");
        System.out.println("║ 6. Simulación de falla crítica                   ║");
        System.out.println("║ 7. Salir                                         ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Opción 1 — Configuración inicial
    // ═════════════════════════════════════════════════════════════════════════
    private void configuracionInicial() {
        System.out.println("\n── Configuración inicial ──");

        System.out.println("[Unidad central]");
        String ubicacion = leerTexto("Ubicación: ", t -> !t.isBlank(), "No puede estar vacía.");
        String codigo    = leerTexto("Código interno: ", t -> !t.isBlank(), "No puede estar vacío.");
        double limite    = leerDouble("Límite de potencia (W, > 0): ", 0.0001, 100000);

        System.out.println("[Brazo robótico]");
        double velocidad = leerDouble("Velocidad de desplazamiento (> 0): ", 0.0001, 1000);
        double angulo    = leerDouble("Ángulo de extensión inicial (0-180°): ", 0, 180);

        System.out.println("[Operador]");
        String ficha  = leerTexto("Ficha de empleado: ", t -> !t.isBlank(), "No puede estar vacía.");
        String nombre = leerTexto("Nombre completo: ", t -> !t.isBlank(), "No puede estar vacío.");
        String correo = leerTexto("Correo electrónico: ", t -> t.contains("@"),
                                  "Debe contener '@'.");

        // ── Credenciales SMTP (RF5) ───────────────────────────────────────────
        // Se solicitan aquí para que el correo de anomalía funcione en la demo
        // sin depender de variables de entorno externas.
        System.out.println("[Notificaciones por correo — RF5]");
        System.out.println("  (Presione Enter para omitir; el sistema avisará si hay anomalía"
                + " pero no enviará correo.)");
        String smtpUser = leerTexto("Cuenta Gmail remitente (o Enter para omitir): ",
                                    t -> true, "");
        if (!smtpUser.isBlank()) {
            String smtpPass = leerTexto("Contraseña de aplicación Gmail: ",
                                        t -> !t.isBlank(), "No puede estar vacía.");
            servicioCorreo.configurarCredenciales(smtpUser, smtpPass);
            System.out.println("  ✔ Credenciales SMTP configuradas.");
        } else {
            System.out.println("  ℹ Correo omitido: la anomalía se mostrará solo en consola.");
        }

        // La bitácora se conserva si ya existía (Agregación: vive aparte de la unidad)
        if (bitacora == null) {
            bitacora = new BitacoraHistorica("BIT-" + codigo.trim());
        }
        Operador operador = new Operador(ficha, nombre, correo);
        unidad = new UnidadCentralControl(ubicacion, codigo, limite,
                                          velocidad, angulo, bitacora, operador);
        reportePendiente = null;

        System.out.println("\n✔ Configuración completada.");
        System.out.println(unidad);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Opción 2 — Ciclo de muestreo (RF1 → RF2)
    // ═════════════════════════════════════════════════════════════════════════
    private void ejecutarMuestreo() {
        if (!unidadOperativa()) return;

        System.out.println("\n── Ciclo de muestreo ──");
        System.out.println(unidad.diagnosticarViabilidad());               // RF1

        if (UnidadCentralControl.ESTADO_BAJO_CONSUMO.equals(unidad.getEstadoFuncionamiento())) {
            System.out.println("⚠ Unidad en bajo consumo: el muestreo continúa con "
                             + "velocidad reducida.");
        }

        double ph     = leerDouble("pH medido (0-14): ", 0, 14);
        double cond   = leerDouble("Conductividad eléctrica (mS/cm, ≥ 0): ", 0, 100);
        double tiempo = leerDouble("Duración de la prueba (horas, > 0): ", 0.0001, 1000);

        if (unidad.isCompensacionTermicaActiva()) {
            System.out.println("🌡 Compensación térmica activa: se aplicará +5% a la "
                             + "conductividad.");
        }

        try {
            reportePendiente = unidad.ejecutarCicloMuestreo(ph, cond, tiempo);   // RF2
            System.out.println("\n✔ Muestreo completado. Reporte generado:");
            System.out.println(reportePendiente);
            System.out.println("(Use la opción 4 para registrarlo en la bitácora.)");
        } catch (IllegalStateException | IllegalArgumentException e) {
            System.out.println("[ADVERTENCIA] No se pudo completar el muestreo: " + e.getMessage());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Opción 3 — Clima externo (RF4)
    // ═════════════════════════════════════════════════════════════════════════
    private void sincronizarClima() {
        if (!unidadOperativa()) return;

        System.out.println("\n── Sincronizar clima externo ──");
        System.out.printf("Coordenadas por defecto: %.2f, %.2f (ITCR San Carlos)%n",
                          LAT_DEFECTO, LON_DEFECTO);
        boolean otras = leerSiNo("¿Usar otras coordenadas? (s/n): ");
        double lat = LAT_DEFECTO;
        double lon = LON_DEFECTO;
        if (otras) {
            lat = leerDouble("Latitud (-90 a 90): ", -90, 90);
            lon = leerDouble("Longitud (-180 a 180): ", -180, 180);
        }

        ConsultaClimatica clima = servicioClima.consultarClima(lat, lon);
        if (clima == null) {
            System.out.println("No se obtuvieron datos climáticos; el sistema continúa sin cambios.");
            return;
        }
        unidad.aplicarClima(clima);
        System.out.println(clima);
        if (clima.isCompensacionTermicaActiva()) {
            System.out.println("🌡 > 32 °C: el próximo muestreo aplicará +5% a la conductividad.");
        } else {
            System.out.println("Temperatura normal: sin compensación térmica.");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Opción 4 — Bitácora + anomalías (RF3 + RF5)
    // ═════════════════════════════════════════════════════════════════════════
    private void registrarYVerificar() {
        if (unidad == null) {
            System.out.println("\n⚠ Primero realice la configuración inicial (opción 1).");
            return;
        }
        if (reportePendiente == null) {
            System.out.println("\n⚠ No hay un reporte pendiente. Ejecute un muestreo (opción 2).");
            return;
        }

        ReporteMuestreo reporte = reportePendiente;
        bitacora.registrarReporte(reporte);                                    // RF3
        reportePendiente = null;
        System.out.println("\n✔ Reporte registrado. Total en bitácora: "
                           + bitacora.getTotalReportes());

        if (bitacora.esAnomaliaQuimica(reporte)) {
            System.out.println("🚨 ANOMALÍA QUÍMICA: " + bitacora.describirAnomalia(reporte));
            Operador op = unidad.getOperador();
            if (op == null) {
                System.out.println("[ADVERTENCIA] No hay operador asignado; no se envía correo.");
            } else {
                servicioCorreo.enviarAlertaAnomaliaQuimica(op, unidad, reporte);   // RF5
            }
        } else {
            System.out.println("Parámetros químicos dentro del rango óptimo.");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Opción 5 — Estado de la bitácora
    // ═════════════════════════════════════════════════════════════════════════
    private void verBitacora() {
        if (bitacora == null) {
            System.out.println("\n⚠ Aún no existe una bitácora. Realice la configuración inicial.");
            return;
        }
        System.out.println("\n── Estado de la bitácora ──");
        System.out.printf("Reportes totales : %d%n", bitacora.getTotalReportes());
        System.out.printf("Porcentaje anomalías : %.2f%%%n", bitacora.calcularPorcentajeAnomalias());
        System.out.println();
        System.out.println(bitacora.generarResumen());
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Opción 6 — Simulación de falla crítica (Composición vs Agregación)
    // ═════════════════════════════════════════════════════════════════════════
    private void simularFalla() {
        if (!unidadOperativa()) return;

        System.out.println("\n── Simulación de falla crítica ──");
        SimulacionFalla.ejecutar(unidad, bitacora);
        reportePendiente = null;
        System.out.println("\nPara continuar operando, reconfigure con la opción 1 "
                         + "(la bitácora se conserva).");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Validaciones de estado
    // ═════════════════════════════════════════════════════════════════════════
    /** @return true si hay unidad configurada y su brazo aún existe. */
    private boolean unidadOperativa() {
        if (unidad == null) {
            System.out.println("\n⚠ Primero realice la configuración inicial (opción 1).");
            return false;
        }
        if (unidad.getBrazoRobotico() == null) {
            System.out.println("\n⚠ La unidad está destruida (" + unidad.getEstadoFuncionamiento()
                             + "). Reconfigure con la opción 1.");
            return false;
        }
        return true;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Lectura resiliente de datos
    // ═════════════════════════════════════════════════════════════════════════
    /** Lee un double en [min, max]; repite hasta recibir una entrada válida. */
    private double leerDouble(String mensaje, double min, double max) {
        while (true) {
            try {
                System.out.print(mensaje);
                double valor = scanner.nextDouble();
                scanner.nextLine();                     // consumir el salto de línea
                if (Double.isNaN(valor) || Double.isInfinite(valor)) {
                    System.out.println("  ✗ Valor no válido. Intente de nuevo.");
                } else if (valor < min || valor > max) {
                    System.out.println("  ✗ Fuera de rango (" + min + " a " + max + ").");
                } else {
                    return valor;
                }
            } catch (InputMismatchException e) {
                scanner.nextLine();                     // limpiar el buffer
                System.out.println("  ✗ Debe ingresar un número (use punto decimal).");
            }
        }
    }

    /** Lee un entero en [min, max]; repite hasta recibir una entrada válida. */
    private int leerEntero(String mensaje, int min, int max) {
        while (true) {
            try {
                System.out.print(mensaje);
                int valor = scanner.nextInt();
                scanner.nextLine();
                if (valor < min || valor > max) {
                    System.out.println("  ✗ Ingrese un número entre " + min + " y " + max + ".");
                } else {
                    return valor;
                }
            } catch (InputMismatchException e) {
                scanner.nextLine();
                System.out.println("  ✗ Debe ingresar un número entero.");
            }
        }
    }

    /** Lee una línea de texto que cumpla la condición dada. */
    private String leerTexto(String mensaje, Predicate<String> valido, String error) {
        while (true) {
            System.out.print(mensaje);
            String texto = scanner.nextLine().trim();
            if (valido.test(texto)) {
                return texto;
            }
            System.out.println("  ✗ " + error);
        }
    }

    private boolean leerSiNo(String mensaje) {
        while (true) {
            String r = leerTexto(mensaje, t -> true, "").toLowerCase(Locale.ROOT);
            if (r.equals("s") || r.equals("si") || r.equals("sí")) return true;
            if (r.equals("n") || r.equals("no")) return false;
            System.out.println("  ✗ Responda 's' o 'n'.");
        }
    }
}
