package iso.tp2.webservice;

/**
 * El webservice respondió con un código distinto de 2xx (por ejemplo 400 o 500).
 */
public class RespuestaHttpException extends RuntimeException {

    private final int codigoEstado;
    private final String cuerpo;

    public RespuestaHttpException(final String url, final int codigoEstado, final String cuerpo) {
        super("HTTP " + codigoEstado + " en " + url + ": " + cuerpo);
        this.codigoEstado = codigoEstado;
        this.cuerpo = cuerpo;
    }

    public int codigoEstado() {
        return codigoEstado;
    }

    public String cuerpo() {
        return cuerpo;
    }
}
