package iso.tp2.reglas;

import static iso.tp2.reglas.ReglasNegocio.ALMACENAMIENTO_ATENCION;
import static iso.tp2.reglas.ReglasNegocio.ALMACENAMIENTO_NORMAL;
import static iso.tp2.reglas.ReglasNegocio.ALMACENAMIENTO_SATURADO;
import static iso.tp2.reglas.ReglasNegocio.CREDITO_APROBADA;
import static iso.tp2.reglas.ReglasNegocio.CREDITO_PENDIENTE;
import static iso.tp2.reglas.ReglasNegocio.CREDITO_RECHAZADA;
import static iso.tp2.reglas.ReglasNegocio.SMVM_VIGENTE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Matriz de casos del Ejercicio 2 (particiones de equivalencia + valores límite), automatizada
 * para el Ejercicio 3.
 *
 * <p>Es abstracta: cada subclase indica contra qué implementación de {@link ReglasNegocio} se
 * ejecuta. Así la misma matriz valida la implementación local y detecta errores en el
 * webservice de la cátedra. Los identificadores (A-01, B-07, ...) son los de
 * {@code docs/ejercicio2-caja-negra.md}.
 */
public abstract class MatrizCasosReglasNegocio {

    protected static final double DELTA = 1e-9;
    protected static final double UN_CENTAVO = 0.01;

    protected abstract ReglasNegocio reglas();

    @Nested
    @DisplayName("A. CalcularDescuentoCompra")
    class DescuentoCompra {

        @ParameterizedTest(name = "{0} | monto={1}, VIP={2} -> {3} %")
        @CsvSource(textBlock = """
            # CE1 [0, 50) -> 0 %
            A-01, 0,        false,  0
            A-02, 0,        true,   0
            A-03, 25,       false,  0
            A-04, 49.99,    false,  0
            A-05, 49.99,    true,   0
            # CE2 [50, 100) -> 5 % normal / 10 % VIP
            A-06, 50,       false,  5
            A-07, 50,       true,  10
            A-08, 50.01,    false,  5
            A-09, 75,       true,  10
            A-10, 99.99,    false,  5
            A-11, 99.99,    true,  10
            # CE3 [100, 500) -> 10 % normal / 15 % VIP
            A-12, 100,      false, 10
            A-13, 100,      true,  15
            A-14, 100.01,   true,  15
            A-15, 300,      false, 10
            A-16, 499.99,   false, 10
            A-17, 499.99,   true,  15
            # CE4 [500, +inf) -> 15 % normal / 20 % VIP
            A-18, 500,      false, 15
            A-19, 500,      true,  20
            A-20, 500.01,   false, 15
            A-21, 10000,    true,  20
            A-22, 1000000,  false, 15
            """)
        void porcentajeSegunTramoYMembresia(final String id, final double monto, final boolean esVIP,
                final double esperado) {
            assertEquals(esperado, reglas().calcularDescuentoCompra(monto, esVIP), DELTA);
        }
    }

    @Nested
    @DisplayName("B. ValidarContrasenia")
    class Contrasenia {

        @ParameterizedTest(name = "{0} | \"{1}\", mín={2}, especiales={3} -> {4}")
        @CsvSource(textBlock = """
            # Sin exigir especiales: solo importa la longitud (min-1, min, min+1)
            B-01, 'abc1234',     8, false, false
            B-02, 'abcd1234',    8, false, true
            B-03, 'abcd12345',   8, false, true
            B-04, 'abc 1234',    8, false, true
            B-05, 'abcd123!',    8, false, true
            B-06, 'a',           1, false, true
            B-07, 'abc',         0, false, true
            # Los espacios cuentan para la longitud aunque la contraseña sea solo espacios
            B-08, '        ',    8, false, true
            B-09, ' ',           1, false, true
            B-10, '',            0, false, true
            # Exigiendo especiales: longitud insuficiente aunque tenga especial
            B-11, 'abc!234',     8, true,  false
            # Exigiendo especiales: longitud suficiente, con / sin especial
            B-12, 'abcd!234',    8, true,  true
            B-13, '#abcd123',    8, true,  true
            B-14, 'abcd123#',    8, true,  true
            B-15, '!!!!!!!!',    8, true,  true
            B-16, 'abc_1234',    8, true,  true
            B-17, 'abcd-123',    8, true,  true
            B-18, 'abcd123€',    8, true,  true
            B-19, 'abcd1234',    8, true,  false
            B-20, 'abc',         0, true,  false
            # Espacios en blanco (espacio, tab, espacio duro) y letras Unicode no son especiales
            B-21, 'abcd 1234',   8, true,  false
            B-22, 'abcd\t1234',  8, true,  false
            B-23, 'abcd 1234', 8, true, false
            B-24, 'contraseña',  8, true,  false
            B-25, 'áéíóú123',    8, true,  false
            B-26, 'ÁRBOL1234',   8, true,  false
            """)
        void validezSegunLongitudYEspeciales(final String id, final String contrasenia, final int longitudMinima,
                final boolean requiereEspeciales, final boolean esperado) {
            assertEquals(esperado, reglas().validarContrasenia(contrasenia, longitudMinima, requiereEspeciales));
        }
    }

    @Nested
    @DisplayName("C. CalcularPrecioEntrada")
    class PrecioEntrada {

        @ParameterizedTest(name = "{0} | edad={1}, día={2} -> {3}")
        @CsvSource(textBlock = """
            # [0, 3) no abonan, sin importar el tipo de día
            C-01,   0, Normal,       0
            C-02,   0, Feriado,      0
            C-03,   2, Normal,       0
            C-04,   2, FinDeSemana,  0
            C-05,   2, Feriado,      0
            # [3, 12) base 15
            C-06,   3, Normal,      15
            C-07,   3, FinDeSemana, 20
            C-08,   3, Feriado,     25
            C-09,  11, Normal,      15
            C-10,  11, FinDeSemana, 20
            C-11,  11, Feriado,     25
            # [12, 18) base 25
            C-12,  12, Normal,      25
            C-13,  12, FinDeSemana, 30
            C-14,  12, Feriado,     35
            C-15,  17, Normal,      25
            C-16,  17, FinDeSemana, 30
            C-17,  17, Feriado,     35
            # [18, 65) base 40
            C-18,  18, Normal,      40
            C-19,  18, FinDeSemana, 45
            C-20,  18, Feriado,     50
            C-21,  64, Normal,      40
            C-22,  64, FinDeSemana, 45
            C-23,  64, Feriado,     50
            # [65, +inf) base 30
            C-24,  65, Normal,      30
            C-25,  65, FinDeSemana, 35
            C-26,  65, Feriado,     40
            C-27, 100, Normal,      30
            C-28, 100, Feriado,     40
            """)
        void precioSegunEdadYTipoDia(final String id, final int edad, final String tipoDia, final double esperado) {
            assertEquals(esperado, reglas().calcularPrecioEntrada(edad, tipoDia), DELTA);
        }

        @ParameterizedTest(name = "{0} | edad=30, día=\"{1}\" -> {2}")
        @CsvSource(textBlock = """
            C-29, 'normal',            40
            C-30, 'NORMAL',            40
            C-31, '  Normal  ',        40
            C-32, 'findesemana',       45
            C-33, 'FINDESEMANA',       45
            C-34, '  FinDeSemana  ',   45
            C-35, 'feriado',           50
            C-36, 'FERIADO',           50
            C-37, ' fErIaDo ',         50
            """)
        void tipoDiaSinDistinguirMayusculasNiEspaciosExtremos(final String id, final String tipoDia,
                final double esperado) {
            assertEquals(esperado, reglas().calcularPrecioEntrada(30, tipoDia), DELTA);
        }
    }

    @Nested
    @DisplayName("D. EvaluarSolicitudCredito")
    class SolicitudCredito {

        static Stream<Arguments> casos() {
            final double dosSmvm = SMVM_VIGENTE * 2;
            return Stream.of(
                    // P1 [0, 300) con ingresos >= SMVM -> Rechazada
                    arguments("D-01", 0, SMVM_VIGENTE, CREDITO_RECHAZADA),
                    arguments("D-02", 0, dosSmvm, CREDITO_RECHAZADA),
                    arguments("D-03", 150, dosSmvm - UN_CENTAVO, CREDITO_RECHAZADA),
                    arguments("D-04", 299, SMVM_VIGENTE, CREDITO_RECHAZADA),
                    arguments("D-05", 299, dosSmvm, CREDITO_RECHAZADA),
                    // P2 [300, 700) con ingresos >= SMVM -> Pendiente
                    arguments("D-06", 300, SMVM_VIGENTE, CREDITO_PENDIENTE),
                    arguments("D-07", 300, dosSmvm, CREDITO_PENDIENTE),
                    arguments("D-08", 500, dosSmvm - UN_CENTAVO, CREDITO_PENDIENTE),
                    arguments("D-09", 699, SMVM_VIGENTE, CREDITO_PENDIENTE),
                    arguments("D-10", 699, dosSmvm, CREDITO_PENDIENTE),
                    // P3 [700, 850] con ingresos en [SMVM, 2 SMVM) -> Pendiente
                    arguments("D-11", 700, SMVM_VIGENTE, CREDITO_PENDIENTE),
                    arguments("D-12", 700, SMVM_VIGENTE + UN_CENTAVO, CREDITO_PENDIENTE),
                    arguments("D-13", 700, dosSmvm - UN_CENTAVO, CREDITO_PENDIENTE),
                    arguments("D-14", 850, SMVM_VIGENTE, CREDITO_PENDIENTE),
                    arguments("D-15", 850, dosSmvm - UN_CENTAVO, CREDITO_PENDIENTE),
                    // P3 [700, 850] con ingresos >= 2 SMVM -> Aprobada
                    arguments("D-16", 700, dosSmvm, CREDITO_APROBADA),
                    arguments("D-17", 700, dosSmvm + UN_CENTAVO, CREDITO_APROBADA),
                    arguments("D-18", 780, SMVM_VIGENTE * 3, CREDITO_APROBADA),
                    arguments("D-19", 850, dosSmvm, CREDITO_APROBADA));
        }

        @ParameterizedTest(name = "{0} | puntaje={1}, ingresos={2} -> {3}")
        @MethodSource("casos")
        void resultadoSegunPuntajeEIngresos(final String id, final int puntaje, final double ingresos,
                final String esperado) {
            assertEquals(esperado, reglas().evaluarSolicitudCredito(puntaje, ingresos));
        }
    }

    @Nested
    @DisplayName("E. CalcularMultaTransito")
    class MultaTransito {

        @ParameterizedTest(name = "{0} | velocidad={1}, límite={2} -> multa={3}, puntos={4}")
        @CsvSource(textBlock = """
            # Exceso <= 0 -> 0 / 0
            E-01,   0,      60,      0,  0
            E-02,  59.99,   60,      0,  0
            E-03,  60,      60,      0,  0
            # (0, 10] -> 5.000 / 0
            E-04,  60.01,   60,   5000,  0
            E-05,  65,      60,   5000,  0
            E-06,  70,      60,   5000,  0
            # (10, 30] -> 15.000 / 2
            E-07,  70.01,   60,  15000,  2
            E-08,  70.5,    60,  15000,  2
            E-09,  80,      60,  15000,  2
            E-10,  90,      60,  15000,  2
            # (30, 60] -> 30.000 / 5
            E-11,  90.01,   60,  30000,  5
            E-12, 100,      60,  30000,  5
            E-13, 120,      60,  30000,  5
            # (60, +inf) -> 60.000 / 10
            E-14, 120.01,   60,  60000, 10
            E-15, 200,      60,  60000, 10
            # Otros límites: los tramos dependen del exceso, no de la velocidad absoluta
            E-16, 100,     100,      0,  0
            E-17, 110,     100,   5000,  0
            E-18, 110.01,  100,  15000,  2
            E-19, 130,     100,  15000,  2
            E-20, 130.01,  100,  30000,  5
            E-21, 160,     100,  30000,  5
            E-22, 160.01,  100,  60000, 10
            E-23,  45,      40,   5000,  0
            E-24,  30,      40,      0,  0
            """)
        void multaYPuntosSegunExceso(final String id, final double velocidad, final double limite,
                final double multaEsperada, final int puntosEsperados) {
            final ResultadoMulta resultado = reglas().calcularMultaTransito(velocidad, limite);
            assertAll(
                    () -> assertEquals(multaEsperada, resultado.multa(), DELTA, "multa"),
                    () -> assertEquals(puntosEsperados, resultado.puntos(), "puntos"));
        }
    }

    @Nested
    @DisplayName("F. EvaluarUsoAlmacenamiento")
    class UsoAlmacenamiento {

        @ParameterizedTest(name = "{0} | usado={1} GB, capacidad={2} GB -> {3}")
        @CsvSource(textBlock = """
            # [0 %, 80 %) -> Normal
            F-01,   0,       100,      Normal
            F-02,  50,       100,      Normal
            F-03,  79.99,    100,      Normal
            # [80 %, 100 %) -> Atención
            F-04,  80,       100,      Atención
            F-05,  80.01,    100,      Atención
            F-06,  90,       100,      Atención
            F-07,  99.99,    100,      Atención
            # [100 %, +inf) -> Saturado
            F-08, 100,       100,      Saturado
            F-09, 100.01,    100,      Saturado
            F-10, 150,       100,      Saturado
            # Otras capacidades: se evalúa el porcentaje, no los GB absolutos
            F-11, 399,       500,      Normal
            F-12, 400,       500,      Atención
            F-13, 499,       500,      Atención
            F-14, 500,       500,      Saturado
            F-15,  90,      1000,      Normal
            F-16,  40,        50,      Atención
            F-17,  50,        50,      Saturado
            F-18,   1.6,       2,      Atención
            F-19,   0,         0.5,    Normal
            F-20, 799999,  1000000,    Normal
            F-21, 800000,  1000000,    Atención
            """)
        void estadoSegunPorcentajeDeUso(final String id, final double usado, final double capacidad,
                final String esperado) {
            assertEquals(esperado, reglas().evaluarUsoAlmacenamiento(usado, capacidad),
                    () -> "porcentaje = " + (usado / capacidad) * 100 + " %");
        }
    }

    // ---------------------------------------------------------------------------------------
    // Entradas que el enunciado no cubre. Cada subclase decide cómo verificarlas.
    // ---------------------------------------------------------------------------------------

    /**
     * Invocación etiquetada para clases no válidas o casos no especificados.
     *
     * @param respuestaSegura oráculo de robustez: qué respuesta HTTP 200 se acepta como rechazo
     *                        implícito (además de un HTTP 4xx). {@code null} si no hay una respuesta
     *                        neutra evidente y el caso es solo de observación.
     */
    public record Invocacion(String id, String descripcion, Function<ReglasNegocio, Object> ejecutar,
            String descripcionRespuestaSegura, Predicate<Object> respuestaSegura) {

        static Invocacion conRespuestaSegura(final String id, final String descripcion,
                final Function<ReglasNegocio, Object> ejecutar, final String descripcionRespuestaSegura,
                final Predicate<Object> respuestaSegura) {
            return new Invocacion(id, descripcion, ejecutar, descripcionRespuestaSegura, respuestaSegura);
        }

        static Invocacion soloObservacion(final String id, final String descripcion,
                final Function<ReglasNegocio, Object> ejecutar) {
            return new Invocacion(id, descripcion, ejecutar, null, null);
        }

        @Override
        public String toString() {
            return id + " | " + descripcion;
        }
    }

    private static final String SIN_DESCUENTO = "0 % (sin descuento)";
    private static final String NO_VALIDA = "false (contraseña no válida)";
    private static final String RECHAZADA = "\"Rechazada\"";
    private static final String SIN_MULTA = "multa 0 y 0 puntos";
    private static final String SIN_ESTADO = "un valor distinto de Normal / Atención / Saturado";

    private static final Predicate<Object> ES_SIN_DESCUENTO = r -> r instanceof Double d && d == 0;
    private static final Predicate<Object> ES_NO_VALIDA = Boolean.FALSE::equals;
    private static final Predicate<Object> ES_RECHAZADA = CREDITO_RECHAZADA::equals;
    private static final Predicate<Object> ES_SIN_MULTA = new ResultadoMulta(0, 0)::equals;
    private static final Predicate<Object> ES_SIN_ESTADO =
            r -> !Set.of(ALMACENAMIENTO_NORMAL, ALMACENAMIENTO_ATENCION, ALMACENAMIENTO_SATURADO).contains(r);

    /**
     * Clases no válidas con oráculo de robustez: una entrada inválida no debe producir un
     * resultado que beneficie o perjudique (descuento, aprobación, multa, estado de uso).
     * El servicio debe rechazarla (HTTP 4xx) o devolver su respuesta neutra.
     */
    static Stream<Invocacion> entradasConRespuestaSegura() {
        return Stream.of(
                Invocacion.conRespuestaSegura("A-23", "monto = -0,01",
                        r -> r.calcularDescuentoCompra(-UN_CENTAVO, false), SIN_DESCUENTO, ES_SIN_DESCUENTO),
                Invocacion.conRespuestaSegura("A-24", "monto = -100 (VIP)",
                        r -> r.calcularDescuentoCompra(-100, true), SIN_DESCUENTO, ES_SIN_DESCUENTO),
                Invocacion.conRespuestaSegura("A-25", "monto = NaN",
                        r -> r.calcularDescuentoCompra(Double.NaN, false), SIN_DESCUENTO, ES_SIN_DESCUENTO),
                Invocacion.conRespuestaSegura("A-26", "monto = NaN (VIP)",
                        r -> r.calcularDescuentoCompra(Double.NaN, true), SIN_DESCUENTO, ES_SIN_DESCUENTO),
                Invocacion.conRespuestaSegura("B-27", "contraseña nula",
                        r -> r.validarContrasenia(null, 8, false), NO_VALIDA, ES_NO_VALIDA),
                Invocacion.conRespuestaSegura("D-20", "puntaje = -1",
                        r -> r.evaluarSolicitudCredito(-1, SMVM_VIGENTE), RECHAZADA, ES_RECHAZADA),
                Invocacion.conRespuestaSegura("D-21", "puntaje = 851",
                        r -> r.evaluarSolicitudCredito(851, SMVM_VIGENTE * 2), RECHAZADA, ES_RECHAZADA),
                Invocacion.conRespuestaSegura("D-22", "ingresos = -0,01",
                        r -> r.evaluarSolicitudCredito(700, -UN_CENTAVO), RECHAZADA, ES_RECHAZADA),
                Invocacion.conRespuestaSegura("D-23", "puntaje 700, ingresos = NaN",
                        r -> r.evaluarSolicitudCredito(700, Double.NaN), RECHAZADA, ES_RECHAZADA),
                Invocacion.conRespuestaSegura("E-25", "velocidad = -1",
                        r -> r.calcularMultaTransito(-1, 60), SIN_MULTA, ES_SIN_MULTA),
                Invocacion.conRespuestaSegura("E-26", "límite = 0",
                        r -> r.calcularMultaTransito(50, 0), SIN_MULTA, ES_SIN_MULTA),
                Invocacion.conRespuestaSegura("E-27", "límite = -60",
                        r -> r.calcularMultaTransito(100, -60), SIN_MULTA, ES_SIN_MULTA),
                Invocacion.conRespuestaSegura("E-28", "velocidad = NaN",
                        r -> r.calcularMultaTransito(Double.NaN, 60), SIN_MULTA, ES_SIN_MULTA),
                Invocacion.conRespuestaSegura("E-29", "límite = NaN",
                        r -> r.calcularMultaTransito(100, Double.NaN), SIN_MULTA, ES_SIN_MULTA),
                Invocacion.conRespuestaSegura("F-22", "capacidad = 0",
                        r -> r.evaluarUsoAlmacenamiento(50, 0), SIN_ESTADO, ES_SIN_ESTADO),
                Invocacion.conRespuestaSegura("F-23", "capacidad = -100",
                        r -> r.evaluarUsoAlmacenamiento(50, -100), SIN_ESTADO, ES_SIN_ESTADO),
                Invocacion.conRespuestaSegura("F-24", "espacio usado = -1",
                        r -> r.evaluarUsoAlmacenamiento(-1, 100), SIN_ESTADO, ES_SIN_ESTADO),
                Invocacion.conRespuestaSegura("F-25", "espacio usado = NaN",
                        r -> r.evaluarUsoAlmacenamiento(Double.NaN, 100), SIN_ESTADO, ES_SIN_ESTADO),
                Invocacion.conRespuestaSegura("F-26", "capacidad = NaN",
                        r -> r.evaluarUsoAlmacenamiento(50, Double.NaN), SIN_ESTADO, ES_SIN_ESTADO));
    }

    /** Clases no válidas sin una respuesta neutra evidente: solo se registra lo que responde el servicio. */
    static Stream<Invocacion> entradasSoloObservacion() {
        return Stream.of(
                Invocacion.soloObservacion("B-28", "longitud mínima = -1",
                        r -> r.validarContrasenia("abc", -1, false)),
                Invocacion.soloObservacion("C-38", "edad = -1",
                        r -> r.calcularPrecioEntrada(-1, "Normal")),
                Invocacion.soloObservacion("C-39", "tipo de día 'Lunes'",
                        r -> r.calcularPrecioEntrada(30, "Lunes")),
                Invocacion.soloObservacion("C-40", "tipo de día 'Fin de semana'",
                        r -> r.calcularPrecioEntrada(30, "Fin de semana")),
                Invocacion.soloObservacion("C-41", "tipo de día nulo",
                        r -> r.calcularPrecioEntrada(30, null)));
    }

    /** Todas las clases no válidas (la implementación local debe rechazarlas con excepción). */
    static Stream<Invocacion> entradasFueraDeDominio() {
        return Stream.concat(entradasConRespuestaSegura(), entradasSoloObservacion());
    }

    /**
     * Entradas válidas cuyo resultado el enunciado no define. El segundo argumento es el
     * resultado adoptado como supuesto en la implementación local.
     */
    static Stream<Arguments> casosNoEspecificados() {
        return Stream.of(
                arguments(Invocacion.soloObservacion("D-24", "puntaje 850, ingresos = SMVM - 0,01",
                        r -> r.evaluarSolicitudCredito(850, SMVM_VIGENTE - UN_CENTAVO)), CREDITO_RECHAZADA),
                arguments(Invocacion.soloObservacion("D-25", "puntaje 500, ingresos = 0",
                        r -> r.evaluarSolicitudCredito(500, 0)), CREDITO_RECHAZADA));
    }
}
