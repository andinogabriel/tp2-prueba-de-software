package iso.tp2.webservice;

import iso.tp2.reglas.ReglasNegocio;
import iso.tp2.reglas.ResultadoMulta;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Adaptador que implementa {@link ReglasNegocio} invocando los webservices de la cátedra
 * ({@code /api/Test/*}, ver Swagger en {@code <baseUrl>/swagger/index.html}).
 */
public class ReglasNegocioWebService implements ReglasNegocio {

    public static final String URL_BASE_POR_DEFECTO = "http://iso-2026.somee.com/test";

    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final Pattern CAMPO_MULTA = Pattern.compile("\"multa\"\\s*:\\s*(-?[0-9.eE+-]+)");
    private static final Pattern CAMPO_PUNTOS = Pattern.compile("\"puntos\"\\s*:\\s*(-?[0-9]+)");

    private final String urlApi;
    private final HttpClient cliente;

    public ReglasNegocioWebService(final String urlBase) {
        this.urlApi = urlBase.replaceAll("/+$", "") + "/api/Test/";
        this.cliente = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
    }

    /** Usa la propiedad de sistema {@code ws.baseUrl} o, si no está, la URL de la cátedra. */
    public static ReglasNegocioWebService desdeConfiguracion() {
        return new ReglasNegocioWebService(System.getProperty("ws.baseUrl", URL_BASE_POR_DEFECTO));
    }

    @Override
    public double calcularDescuentoCompra(final double montoTotal, final boolean esVIP) {
        return Double.parseDouble(get("descuento-compra", parametros(
                "montoTotal", numero(montoTotal),
                "esVIP", esVIP)));
    }

    @Override
    public boolean validarContrasenia(final String contrasenia, final int longitudMinima,
            final boolean requiereEspeciales) {
        return Boolean.parseBoolean(get("contrasenia", parametros(
                "contrasenia", contrasenia,
                "longitudMinima", longitudMinima,
                "requiereEspeciales", requiereEspeciales)));
    }

    @Override
    public double calcularPrecioEntrada(final int edad, final String tipoDia) {
        return Double.parseDouble(get("precio-entrada", parametros(
                "edad", edad,
                "tipoDia", tipoDia)));
    }

    @Override
    public String evaluarSolicitudCredito(final int puntajeCrediticio, final double ingresosMensuales) {
        return get("credito", parametros(
                "puntajeCrediticio", puntajeCrediticio,
                "ingresosMensuales", numero(ingresosMensuales)));
    }

    @Override
    public ResultadoMulta calcularMultaTransito(final double velocidadMedida, final double limitePermitido) {
        final String json = get("multa-transito", parametros(
                "velocidadMedida", numero(velocidadMedida),
                "limitePermitido", numero(limitePermitido)));
        return new ResultadoMulta(
                Double.parseDouble(extraer(CAMPO_MULTA, json)),
                Integer.parseInt(extraer(CAMPO_PUNTOS, json)));
    }

    @Override
    public String evaluarUsoAlmacenamiento(final double espacioUsadoGB, final double capacidadTotalGB) {
        return get("almacenamiento", parametros(
                "espacioUsadoGB", numero(espacioUsadoGB),
                "capacidadTotalGB", numero(capacidadTotalGB)));
    }

    /** Verifica que el servicio responda; se usa para omitir las pruebas remotas si no hay red. */
    public boolean estaDisponible() {
        try {
            calcularDescuentoCompra(0, false);
            return true;
        } catch (final RuntimeException e) {
            return false;
        }
    }

    private String get(final String operacion, final Map<String, Object> parametros) {
        final String query = parametros.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .map(e -> e.getKey() + "=" + codificar(String.valueOf(e.getValue())))
                .collect(Collectors.joining("&"));
        final URI uri = URI.create(urlApi + operacion + "?" + query);
        final HttpRequest pedido = HttpRequest.newBuilder(uri)
                .timeout(TIMEOUT)
                .header("Accept", "text/plain")
                .GET()
                .build();
        final HttpResponse<String> respuesta;
        try {
            respuesta = cliente.send(pedido, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (final IOException e) {
            throw new UncheckedIOException("No se pudo invocar " + uri, e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Invocación interrumpida: " + uri, e);
        }
        if (respuesta.statusCode() / 100 != 2) {
            throw new RespuestaHttpException(uri.toString(), respuesta.statusCode(), respuesta.body());
        }
        return quitarComillas(respuesta.body().strip());
    }

    private static Map<String, Object> parametros(final Object... claveValor) {
        final Map<String, Object> mapa = new LinkedHashMap<>();
        for (int i = 0; i < claveValor.length; i += 2) {
            mapa.put((String) claveValor[i], claveValor[i + 1]);
        }
        return mapa;
    }

    /** Sin notación científica: 10000000 en lugar de 1.0E7. */
    private static String numero(final double valor) {
        return Double.isFinite(valor) ? BigDecimal.valueOf(valor).toPlainString() : String.valueOf(valor);
    }

    /** URLEncoder codifica el espacio como '+'; se usa %20 para que no haya ambigüedad. */
    private static String codificar(final String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static String quitarComillas(final String cuerpo) {
        if (cuerpo.length() >= 2 && cuerpo.startsWith("\"") && cuerpo.endsWith("\"")) {
            return cuerpo.substring(1, cuerpo.length() - 1);
        }
        return cuerpo;
    }

    private static String extraer(final Pattern patron, final String json) {
        final Matcher m = patron.matcher(json);
        if (!m.find()) {
            throw new IllegalStateException("Respuesta inesperada del servicio: " + json);
        }
        return m.group(1);
    }
}
