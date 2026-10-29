package iso.tp2.reglas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Ejecuta la matriz de casos contra la implementación de referencia. Que todo pase aquí
 * confirma que los resultados esperados de la matriz son coherentes con la especificación.
 */
@DisplayName("Ejercicio 3 - Implementación local (referencia)")
class ReglasNegocioLocalTest extends MatrizCasosReglasNegocio {

    private final ReglasNegocio reglas = new ReglasNegocioImpl();

    @Override
    protected ReglasNegocio reglas() {
        return reglas;
    }

    @Nested
    @DisplayName("Clases no válidas (fuera de dominio)")
    class ClasesNoValidas {

        @ParameterizedTest(name = "{0} -> IllegalArgumentException")
        @MethodSource("iso.tp2.reglas.MatrizCasosReglasNegocio#entradasFueraDeDominio")
        void rechazaLaEntrada(final Invocacion invocacion) {
            assertThrows(IllegalArgumentException.class, () -> invocacion.ejecutar().apply(reglas));
        }
    }

    @Nested
    @DisplayName("Casos no especificados (supuestos adoptados)")
    class CasosNoEspecificados {

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("iso.tp2.reglas.MatrizCasosReglasNegocio#casosNoEspecificados")
        void aplicaElSupuesto(final Invocacion invocacion, final Object esperado) {
            assertEquals(esperado, invocacion.ejecutar().apply(reglas));
        }
    }
}
