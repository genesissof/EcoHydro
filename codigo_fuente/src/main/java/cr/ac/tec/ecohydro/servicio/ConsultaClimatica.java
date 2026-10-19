package cr.ac.tec.ecohydro.servicio;

/**
 * Objeto de resultado de una consulta climatológica a Open-Meteo.
 * Resultado de RF4 (Prompt 2D), producido por ServicioClimatologico.
 *
 * IC-2101 Programación Orientada a Objetos — ITCR Campus San Carlos
 */
public class ConsultaClimatica {

    private double  temperaturaC;          // temperatura en grados Celsius
    private int     humedadRelativa;       // humedad relativa en porcentaje (0–100)
    private String  fuente;                // URL o nombre de la fuente de datos
    private boolean compensacionTermicaActiva; // true si temperatura > 32 °C

    public ConsultaClimatica(double temperaturaC,
                             int humedadRelativa,
                             String fuente) {
        this.temperaturaC             = temperaturaC;
        this.humedadRelativa          = humedadRelativa;
        this.fuente                   = fuente;
        this.compensacionTermicaActiva = temperaturaC > 32.0;
    }

    // ─── Getters / Setters ────────────────────────────────────────────────────
    public double  getTemperaturaC()               { return temperaturaC; }
    public int     getHumedadRelativa()            { return humedadRelativa; }
    public String  getFuente()                     { return fuente; }
    public boolean isCompensacionTermicaActiva()   { return compensacionTermicaActiva; }

    public void setTemperaturaC(double temperaturaC) {
        this.temperaturaC             = temperaturaC;
        this.compensacionTermicaActiva = temperaturaC > 32.0;
    }
    public void setHumedadRelativa(int humedadRelativa) { this.humedadRelativa = humedadRelativa; }
    public void setFuente(String fuente)                { this.fuente = fuente; }

    @Override
    public String toString() {
        return String.format(
            "ConsultaClimatica { temp=%.1f °C | humedad=%d%% | compensación=%s | fuente=%s }",
            temperaturaC, humedadRelativa,
            compensacionTermicaActiva ? "ACTIVA" : "INACTIVA", fuente);
    }
}
