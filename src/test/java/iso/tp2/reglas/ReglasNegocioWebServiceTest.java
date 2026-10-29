package iso.tp2.reglas;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import iso.tp2.webservice.ReglasNegocioWebService;
import iso.tp2.webservice.RespuestaHttpException;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Ejecuta la misma matriz de casos contra los webservices de la cátedra. Cada falla es un
 * defecto del servicio (ver {@code docs/ejercicio3-resultados.md}).
 *
 * <p>Se ejecuta con {@code mvn test -Premoto}. Si el servicio no responde, las pruebas se
 * omiten en lugar de fallar.
 */
@Tag("remoto")
@DisplayName("Ejercicio 3 - Webservice de la cátedra")
class ReglasNegocioWebServiceTest extends MatrizCasosReglasNegocio {

    private static final ReglasNegocioWebService SERVICIO = ReglasNegocioWebService.desdeConfiguracion();

    @BeforeAll
    static void verificarDisponibilidad() {
        assumeTrue(SERVICIO.estaDisponible(), "El webservice no está disponible; se omiten las pruebas remotas");
    }

    @Override
    protected ReglasNegocio reglas() {
        return SERVICIO;
    }

    static Stream<Arguments> robustez() {
        return entradasConRespuestaSegura().map(i -> arguments(i, i.descripcionRespuestaSegura()));
    }

    @Nested
    @DisplayName("Robustez ante clases no válidas")
    class RobustezClasesNoValidas {

        @ParameterizedTest(name = "{0} -> rechazo (HTTP 4xx) o {1}")
        @MethodSource("iso.tp2.reglas.ReglasNegocioWebServiceTest#robustez")
        void rechazaORespondeEnFormaNeutra(final Invocacion invocacion, final String respuestaSegura) {
            invocar(invocacion).ifPresent(respuesta -> assertTrue(invocacion.respuestaSegura().test(respuesta),
                    () -> "Respondió " + respuesta + "; se esperaba rechazo (HTTP 4xx) o "
                            + invocacion.descripcionRespuestaSegura()));
        }
    }

    /** El enunciado no define estas salidas: solo se exige que no haya error interno (5xx). */
    @Nested
    @DisplayName("Observación de entradas no especificadas")
    class ObservacionNoEspecificados {

        @ParameterizedTest(name = "{0} -> sin error 5xx")
        @MethodSource("iso.tp2.reglas.MatrizCasosReglasNegocio#entradasSoloObservacion")
        void claseNoValidaSinOraculo(final Invocacion invocacion) {
            invocar(invocacion);
        }

        @ParameterizedTest(name = "{0} (supuesto local: {1}) -> sin error 5xx")
        @MethodSource("iso.tp2.reglas.MatrizCasosReglasNegocio#casosNoEspecificados")
        void casoValidoNoEspecificado(final Invocacion invocacion, final Object supuestoLocal) {
            invocar(invocacion);
        }
    }

    /**
     * Invoca el servicio y registra la respuesta. Un HTTP 4xx es un rechazo explícito (correcto)
     * y devuelve vacío; un HTTP 5xx hace fallar la prueba.
     */
    private static Optional<Object> invocar(final Invocacion invocacion) {
        try {
            final Object respuesta = invocacion.ejecutar().apply(SERVICIO);
            System.out.println("[OBSERVACION] " + invocacion + " => HTTP 200: " + respuesta);
            return Optional.of(respuesta);
        } catch (final RespuestaHttpException e) {
            System.out.println("[OBSERVACION] " + invocacion + " => HTTP " + e.codigoEstado() + ": " + e.cuerpo());
            assertTrue(e.codigoEstado() < 500, () -> "El servicio falló con un error interno: " + e.getMessage());
            return Optional.empty();
        }
    }
}
