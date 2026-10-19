package cr.ac.tec.ecohydro.modelo;

import java.time.LocalDateTime;

/**
 * Núcleo de operación del sistema EcoHydro-4.
 *
 * Relaciones UML implementadas:
 *   ◆ Composición   → BrazoRobotico     (instanciado internamente; destruido con la unidad)
 *   ◇ Agregación    → BitacoraHistorica  (inyectada por referencia; sobrevive a la unidad)
 *   — Asociación    → Operador           (inyectado por referencia; existe independientemente)
 *
 * IC-2101 Programación Orientada a Objetos — ITCR Campus San Carlos
 */
public class UnidadCentralControl {

    // ─── Constantes de estado ─────────────────────────────────────────────────
    public static final String ESTADO_ACTIVO       = "Activo";
    /** Estado RF1: sobrecarga eléctrica O brazo con más de 1000 h de uso. */
    public static final String ESTADO_BAJO_CONSUMO = "Bajo Consumo / Mantenimiento Requerido";
    public static final String ESTADO_FALLA        = "Falla Crítica";
    public static final String ESTADO_FALLA_PERM   = "Falla Crítica Permanente";

    /** Horas de uso acumuladas del brazo antes de requerir mantenimiento (RF1). */
    private static final double UMBRAL_HORAS_USO    = 1000.0;
    /** Potencia fija de sistemas auxiliares: iluminación, bombas, sensores (W). */
    private static final double POTENCIA_AUXILIARES = 50.0;
    /** Factor de reducción de velocidad aplicado en modo bajo consumo (RF1). */
    private static final double FACTOR_REDUCCION    = 0.40;
    /** Corrección de conductividad por calor extremo (RF4): +5 %. */
    private static final double FACTOR_COMPENSACION_TERMICA = 1.05;

    // ─── Atributos privados ───────────────────────────────────────────────────
    private String ubicacion;                // descripción física de la instalación
    private String codigoInterno;            // identificador único de la unidad
    private String estadoFuncionamiento;     // uno de los ESTADO_* definidos arriba
    private double limitePotenciaW;          // vatios máximos disponibles

    // Composición: BrazoRobotico vive y muere con esta clase
    private BrazoRobotico brazoRobotico;
    // Velocidad nominal original del brazo; la reducción al 40 % siempre parte de aquí
    // para evitar que llamadas repetidas a diagnosticarViabilidad() la bajen acumulativamente.
    private double velocidadNominalBrazo;

    // Agregación: BitacoraHistorica existe independientemente
    private BitacoraHistorica bitacora;

    // Asociación: Operador existe independientemente
    private Operador operador;

    // RF4: true si el último clima sincronizado superó 32 °C
    private boolean compensacionTermicaActiva = false;

    // ─── Constructor ──────────────────────────────────────────────────────────
    /**
     * Crea la unidad instanciando el brazo internamente (Composición UML).
     * La bitácora y el operador se inyectan como referencias externas.
     *
     * @param ubicacion          ubicación física de la instalación
     * @param codigoInterno      código identificador único
     * @param limitePotenciaW    límite de potencia en vatios (> 0)
     * @param velBrazo           velocidad inicial del brazo (cm/s)
     * @param anguloBrazo        ángulo inicial del brazo (0–180°)
     * @param bitacora           bitácora histórica (agregación)
     * @param operador           operador asignado (asociación; puede ser null)
     */
    public UnidadCentralControl(String ubicacion,
                                String codigoInterno,
                                double limitePotenciaW,
                                double velBrazo,
                                double anguloBrazo,
                                BitacoraHistorica bitacora,
                                Operador operador) {
        setUbicacion(ubicacion);
        setCodigoInterno(codigoInterno);
        setLimitePotenciaW(limitePotenciaW);

        // ── Composición: el brazo se crea AQUÍ, no fuera ──────────────────────
        this.brazoRobotico       = new BrazoRobotico(velBrazo, anguloBrazo, 0.0);
        this.velocidadNominalBrazo = velBrazo;  // guardar velocidad base para el fix RF1

        // ── Agregación y Asociación: referencias externas ─────────────────────
        this.bitacora            = bitacora;
        this.operador            = operador;

        this.estadoFuncionamiento = ESTADO_ACTIVO;
    }

    // ─── RF1 — Diagnóstico de viabilidad ─────────────────────────────────────
    /**
     * Evalúa si el sistema puede operar y ajusta su estado en consecuencia.
     *
     * <p>Condiciones de degradación (cualquiera es suficiente):
     * <ul>
     *   <li>potenciaRequeridaBrazo + auxiliares &gt; limitePotenciaW (sobrecarga).</li>
     *   <li>brazo.horasUsoAcumuladas &gt; 1000 h (mantenimiento requerido).</li>
     * </ul>
     *
     * <p>Acción ante degradación: estado → {@code ESTADO_BAJO_CONSUMO} y
     * velocidad del brazo reducida al 40 %.
     *
     * @return String con estado actual, potencia disponible y aptitud para muestreo
     */
    public String diagnosticarViabilidad() {
        if (brazoRobotico == null) {
            return "Estado: " + estadoFuncionamiento
                   + " | El brazo robótico no está disponible (unidad destruida).";
        }

        // ── Potencia requerida por el brazo en el ciclo actual ────────────────
        double potenciaRequeridaBrazo = (brazoRobotico.getAnguloExtension() * 1.5)
                                      + (brazoRobotico.getVelocidadDesplazamiento() * 12.8);

        // ── Condiciones de degradación (RF1) ──────────────────────────────────
        boolean sobrecarga    = (potenciaRequeridaBrazo + POTENCIA_AUXILIARES) > limitePotenciaW;
        boolean mantenimiento = brazoRobotico.getHorasUsoAcumuladas() > UMBRAL_HORAS_USO;

        if (sobrecarga || mantenimiento) {
            estadoFuncionamiento = ESTADO_BAJO_CONSUMO;
            // Reducir velocidad del brazo al 40 % de la velocidad NOMINAL (no del valor actual),
            // para que múltiples llamadas a diagnosticarViabilidad() no la reduzcan acumulativamente.
            brazoRobotico.setVelocidadDesplazamiento(velocidadNominalBrazo * FACTOR_REDUCCION);
        } else {
            estadoFuncionamiento = ESTADO_ACTIVO;
        }

        double potenciaDisponible = limitePotenciaW
                                  - (potenciaRequeridaBrazo + POTENCIA_AUXILIARES);
        String aptitud = ESTADO_ACTIVO.equals(estadoFuncionamiento)
                         ? "APTO para muestreo"
                         : "NO apto — velocidad reducida al 40 %, revisar brazo";

        // Causa detallada para el diagnóstico
        String causa = "";
        if (sobrecarga)    causa += "[SOBRECARGA ELÉCTRICA] ";
        if (mantenimiento) causa += "[HORAS USO > 1000 h]";
        if (causa.isBlank()) causa = "Sin anomalías detectadas";

        return String.format(
            "════════════════════════════════════════════%n"
            + "  DIAGNÓSTICO RF1 — Unidad: %s%n"
            + "════════════════════════════════════════════%n"
            + "  Estado actual          : %s%n"
            + "  Potencia requerida     : %.2f W (brazo) + %.2f W (aux) = %.2f W%n"
            + "  Límite disponible      : %.2f W%n"
            + "  Potencia libre         : %.2f W%n"
            + "  Horas uso brazo        : %.2f h%n"
            + "  Causa degradación      : %s%n"
            + "  Aptitud de muestreo    : %s%n"
            + "════════════════════════════════════════════%n",
            codigoInterno,
            estadoFuncionamiento,
            potenciaRequeridaBrazo, POTENCIA_AUXILIARES,
            potenciaRequeridaBrazo + POTENCIA_AUXILIARES,
            limitePotenciaW,
            Math.max(potenciaDisponible, 0.0),
            brazoRobotico.getHorasUsoAcumuladas(),
            causa,
            aptitud);
    }

    // ─── RF2 — Ciclo de muestreo ──────────────────────────────────────────────
    /**
     * Ejecuta un ciclo de muestreo hidropónico y retorna el reporte generado.
     *
     * <p><b>Patrón de Dependencia UML:</b> {@link ReporteMuestreo} se crea como
     * variable local dentro de este método y se retorna; el brazo robótico
     * NO almacena ninguna referencia a él. Su ciclo de vida es independiente.
     *
     * <p>Secuencia interna:
     * <ol>
     *   <li>Extender brazo al ángulo óptimo (90°).</li>
     *   <li>Calcular gastoW = (anguloExtension × 1.5) + (velocidad × 12.8).</li>
     *   <li>Incrementar horasUsoAcumuladas con tiempoPrueba.</li>
     *   <li>Crear y retornar ReporteMuestreo (variable local).</li>
     * </ol>
     *
     * @param ph            nivel de pH de la solución
     * @param conductividad conductividad eléctrica en mS/cm
     * @param tiempoPrueba  duración del ciclo en horas
     * @return nuevo {@link ReporteMuestreo} (dependencia: no retenido por el brazo)
     * @throws IllegalStateException si la unidad fue destruida y el brazo es null
     */
    public ReporteMuestreo ejecutarCicloMuestreo(double ph,
                                                  double conductividad,
                                                  double tiempoPrueba) {
        if (brazoRobotico == null) {
            throw new IllegalStateException(
                "RF2 abortado: el brazo robótico no está disponible (unidad destruida).");
        }

        // Paso 1 — Extender el brazo al ángulo óptimo de muestreo
        brazoRobotico.setAnguloExtension(90.0);

        // Paso 2 — Calcular gasto energético del ciclo (fórmula RF2)
        double gastoW = (brazoRobotico.getAnguloExtension() * 1.5)
                      + (brazoRobotico.getVelocidadDesplazamiento() * 12.8);

        // Paso 3 — Acumular horas de uso del brazo
        brazoRobotico.setHorasUsoAcumuladas(
            brazoRobotico.getHorasUsoAcumuladas() + tiempoPrueba);

        // Paso 3b — RF4: corrección térmica +5 % a la conductividad, SOLO en el
        //           siguiente muestreo tras detectar > 32 °C (se consume la bandera)
        if (compensacionTermicaActiva) {
            conductividad = conductividad * FACTOR_COMPENSACION_TERMICA;
            compensacionTermicaActiva = false;
        }

        // Paso 4 — Crear el reporte como VARIABLE LOCAL (Dependencia UML)
        //          El brazo NO guarda referencia → ciclo de vida independiente
        ReporteMuestreo reporte = new ReporteMuestreo(
                LocalDateTime.now(), ph, conductividad, gastoW);

        return reporte;  // El llamador es dueño del objeto; el brazo lo olvida
    }

    // ─── RF4 — Integración del clima externo ──────────────────────────────────
    /**
     * Registra el resultado de una consulta climática. Si la temperatura supera
     * 32 °C se activa la compensación térmica (+5 % de conductividad) que se
     * aplicará en el siguiente ciclo de muestreo.
     *
     * @param clima consulta obtenida de ServicioClimatologico (null se ignora)
     */
    public void aplicarClima(cr.ac.tec.ecohydro.servicio.ConsultaClimatica clima) {
        if (clima == null) {
            return; // consulta fallida: no se altera el estado
        }
        this.compensacionTermicaActiva = clima.isCompensacionTermicaActiva();
    }

    public boolean isCompensacionTermicaActiva() { return compensacionTermicaActiva; }

    // ─── RF6-Opt6 — Destrucción (demo de Composición) ─────────────────────────
    /**
     * Destruye la unidad central: el brazo robótico queda inaccesible (Composición),
     * pero la bitácora y el operador permanecen (Agregación/Asociación).
     */
    public void destruir() {
        this.brazoRobotico    = null;
        this.estadoFuncionamiento = ESTADO_FALLA_PERM;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────
    public String getUbicacion()            { return ubicacion; }
    public String getCodigoInterno()        { return codigoInterno; }
    public String getEstadoFuncionamiento() { return estadoFuncionamiento; }
    public double getLimitePotenciaW()      { return limitePotenciaW; }

    /** Puede retornar null si la unidad fue destruida (Composición). */
    public BrazoRobotico getBrazoRobotico()      { return brazoRobotico; }
    public BitacoraHistorica getBitacora()        { return bitacora; }
    public Operador getOperador()                 { return operador; }

    // ─── Setters validados ────────────────────────────────────────────────────
    public void setUbicacion(String ubicacion) {
        if (ubicacion == null || ubicacion.isBlank()) {
            throw new IllegalArgumentException("La ubicación no puede ser nula ni vacía.");
        }
        this.ubicacion = ubicacion.trim();
    }

    public void setCodigoInterno(String codigoInterno) {
        if (codigoInterno == null || codigoInterno.isBlank()) {
            throw new IllegalArgumentException("El código interno no puede ser nulo ni vacío.");
        }
        this.codigoInterno = codigoInterno.trim();
    }

    public void setEstadoFuncionamiento(String estado) {
        if (estado == null
                || (!estado.equals(ESTADO_ACTIVO)
                    && !estado.equals(ESTADO_BAJO_CONSUMO)   // "Bajo Consumo / Mantenimiento Requerido"
                    && !estado.equals(ESTADO_FALLA)
                    && !estado.equals(ESTADO_FALLA_PERM))) {
            throw new IllegalArgumentException(
                "Estado no válido: '" + estado + "'.\n"
                + "Valores aceptados: ESTADO_ACTIVO | ESTADO_BAJO_CONSUMO "
                + "| ESTADO_FALLA | ESTADO_FALLA_PERM");
        }
        this.estadoFuncionamiento = estado;
    }

    public void setLimitePotenciaW(double limitePotenciaW) {
        if (limitePotenciaW <= 0) {
            throw new IllegalArgumentException(
                "El límite de potencia debe ser mayor que 0. Valor recibido: " + limitePotenciaW);
        }
        this.limitePotenciaW = limitePotenciaW;
    }

    public void setOperador(Operador operador) {
        this.operador = operador; // puede ser null (sin operador asignado)
    }

    public void setBitacora(BitacoraHistorica bitacora) {
        if (bitacora == null) {
            throw new IllegalArgumentException("La bitácora no puede ser nula.");
        }
        this.bitacora = bitacora;
    }

    // ─── toString ─────────────────────────────────────────────────────────────
    @Override
    public String toString() {
        String brazoInfo = (brazoRobotico != null) ? brazoRobotico.toString() : "DESTRUIDO";
        String opInfo    = (operador != null)       ? operador.getNombreCompleto() : "Sin asignar";
        return String.format(
            "UnidadCentralControl {%n"
            + "  código      = %s%n"
            + "  ubicación   = %s%n"
            + "  estado      = %s%n"
            + "  límite(W)   = %.2f%n"
            + "  brazo       = %s%n"
            + "  bitácora    = %s%n"
            + "  operador    = %s%n"
            + "}",
            codigoInterno, ubicacion, estadoFuncionamiento,
            limitePotenciaW, brazoInfo, bitacora, opInfo);
    }
}
