package iso.tp2.ejercicio1;

import static iso.tp2.ejercicio1.DescuentoCliente.procesarDescuentoCliente;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Ejercicio 1 - ProcesarDescuentoCliente")
class DescuentoClienteTest {

    private static final double DELTA = 1e-6;

    /** Un caso de prueba por cada camino independiente del conjunto básico (V(G) = 5). */
    @Nested
    @DisplayName("Caminos básicos")
    class CaminosBasicos {

        @Test
        @DisplayName("CP1 | camino 1-2-9 | monto = 0 -> IllegalArgumentException")
        void camino1MontoNoPositivo() {
            assertThrows(IllegalArgumentException.class, () -> procesarDescuentoCliente(3, 0, true));
        }

        @Test
        @DisplayName("CP2 | camino 1-3-6-8-9 | no socio, monto 50.000 -> 50.000 (0 %)")
        void camino2NoSocioCompraComun() {
            assertEquals(50_000, procesarDescuentoCliente(2, 50_000, false), DELTA);
        }

        @Test
        @DisplayName("CP3 | camino 1-3-4-6-8-9 | socio, 2 años, monto 50.000 -> 45.000 (10 %)")
        void camino3SocioSinFidelidad() {
            assertEquals(45_000, procesarDescuentoCliente(2, 50_000, true), DELTA);
        }

        @Test
        @DisplayName("CP4 | camino 1-3-4-5-6-8-9 | socio, 8 años, monto 50.000 -> 42.500 (15 %)")
        void camino4SocioConFidelidad() {
            assertEquals(42_500, procesarDescuentoCliente(8, 50_000, true), DELTA);
        }

        @Test
        @DisplayName("CP5 | camino 1-3-6-7-8-9 | no socio, monto 200.000 -> 190.000 (5 %)")
        void camino5NoSocioCompraMayorista() {
            assertEquals(190_000, procesarDescuentoCliente(1, 200_000, false), DELTA);
        }
    }

    /** Casos adicionales: no son necesarios para la cobertura de caminos, refuerzan las fronteras. */
    @Nested
    @DisplayName("Complementarios (valores límite)")
    class Complementarios {

        @ParameterizedTest(name = "{0} | antigüedad={1}, monto={2}, socio={3} -> {4}")
        @CsvSource(textBlock = """
            CX1,  4, 50000,     true,  45000
            CX2,  5, 50000,     true,  42500
            CX3,  0, 99999.99,  false, 99999.99
            CX4,  0, 100000,    false, 95000
            CX5,  0, 0.01,      false, 0.01
            CX6, 10, 100000,    true,  80000
            CX7, 10, 99999.99,  true,  84999.9915
            """)
        void fronterasDeAntiguedadYMonto(final String id, final int antiguedad, final double monto,
                final boolean socio, final double esperado) {
            assertEquals(esperado, procesarDescuentoCliente(antiguedad, monto, socio), DELTA);
        }

        @Test
        @DisplayName("CX8 | monto negativo -> IllegalArgumentException")
        void montoNegativo() {
            assertThrows(IllegalArgumentException.class, () -> procesarDescuentoCliente(5, -0.01, true));
        }
    }
}
