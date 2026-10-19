package cr.ac.tec.ecohydro.servicio;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;

/**
 * RF4 — Consumo de la API climatológica Open-Meteo mediante
 * {@link java.net.http.HttpClient} y parseo JSON con org.json.
 *
 * <p>Los errores de red o de formato (IOException, InterruptedException,
 * JSONException) NO detienen el programa: se imprime una advertencia y
 * {@link #consultarClima(double, double)} retorna {@code null}.
 *
 * <p>Dependencia Maven: org.json:json:20240303.
 *
 * IC-2101 Programación Orientada a Objetos — ITCR Campus San Carlos
 */
public class ServicioClimatologico {

    private static final String BASE_URL =
        "https://api.open-meteo.com/v1/forecast"
        + "?latitude=%s&longitude=%s&current_weather=true&hourly=relativehumidity_2m";

    private static final String FUENTE = "Open-Meteo (api.open-meteo.com)";

    /** Un solo cliente reutilizable: HttpClient es inmutable y thread-safe. */
    private final HttpClient cliente;

    public ServicioClimatologico() {
        this.cliente = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Consulta el clima actual en las coordenadas indicadas.
     *
     * @param latitud  latitud geográfica (-90 a 90)
     * @param longitud longitud geográfica (-180 a 180)
     * @return {@link ConsultaClimatica}, o {@code null} si la consulta falló
     *         (en ese caso ya se imprimió la advertencia correspondiente)
     */
    public ConsultaClimatica consultarClima(double latitud, double longitud) {
        if (latitud < -90 || latitud > 90 || longitud < -180 || longitud > 180) {
            System.out.println("[ADVERTENCIA] Coordenadas fuera de rango: "
                    + latitud + ", " + longitud);
            return null;
        }

        // Locale.ROOT garantiza punto decimal (no coma) en la URL
        String url = String.format(Locale.ROOT, BASE_URL, latitud, longitud);

        try {
            HttpRequest solicitud = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> respuesta =
                    cliente.send(solicitud, HttpResponse.BodyHandlers.ofString());

            if (respuesta.statusCode() != 200) {
                System.out.println("[ADVERTENCIA] Open-Meteo respondió HTTP "
                        + respuesta.statusCode() + ". Se continúa sin datos climáticos.");
                return null;
            }
            return parsearRespuesta(respuesta.body());

        } catch (IOException e) {
            System.out.println("[ADVERTENCIA] Error de red al consultar el clima: "
                    + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();   // restaurar la bandera de interrupción
            System.out.println("[ADVERTENCIA] Consulta climática interrumpida.");
        } catch (JSONException e) {
            System.out.println("[ADVERTENCIA] Respuesta JSON inválida de Open-Meteo: "
                    + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("[ADVERTENCIA] URL de consulta inválida: " + e.getMessage());
        }
        return null;
    }

    /**
     * Convierte el JSON de Open-Meteo en un {@link ConsultaClimatica}.
     * Visible en el paquete para poder probarlo sin conexión a internet.
     *
     * @throws JSONException si faltan campos esperados
     */
    ConsultaClimatica parsearRespuesta(String json) throws JSONException {
        JSONObject raiz = new JSONObject(json);

        JSONObject actual = raiz.getJSONObject("current_weather");
        double temperatura = actual.getDouble("temperature");
        String horaActual  = actual.optString("time", "");

        int humedad = extraerHumedad(raiz.getJSONObject("hourly"), horaActual);
        return new ConsultaClimatica(temperatura, humedad, FUENTE);
    }

    /** Busca la humedad de la hora actual; si no coincide, usa la primera disponible. */
    private int extraerHumedad(JSONObject hourly, String horaActual) throws JSONException {
        JSONArray valores = hourly.has("relativehumidity_2m")
                ? hourly.getJSONArray("relativehumidity_2m")
                : hourly.getJSONArray("relative_humidity_2m");   // nombre nuevo de la API
        JSONArray horas = hourly.optJSONArray("time");

        int indice = 0;
        if (horas != null) {
            for (int i = 0; i < horas.length(); i++) {
                if (horas.getString(i).equals(horaActual)) {
                    indice = i;
                    break;
                }
            }
        }
        return valores.getInt(indice);
    }
}
