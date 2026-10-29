package iso.tp2.ejercicio1;

/**
 * Ejercicio 1 - Prueba de camino básico.
 *
 * <p>Reglas de negocio del descuento especial de un cliente:
 * <ul>
 *   <li>El monto de compra debe ser mayor que cero.</li>
 *   <li>Los socios tienen 10 % de descuento.</li>
 *   <li>Los socios con 5 años o más de antigüedad suman 5 % adicional.</li>
 *   <li>Las compras desde $100.000 suman 5 % adicional (sea socio o no).</li>
 * </ul>
 *
 * <p>La función tiene 4 nodos predicado simples, por lo que V(G) = 5. Los números de nodo
 * de los comentarios coinciden con el grafo de flujo de {@code docs/ejercicio1-camino-basico.md}.
 */
public final class DescuentoCliente {

    public static final int ANTIGUEDAD_MINIMA_FIDELIDAD = 5;
    public static final double MONTO_COMPRA_MAYORISTA = 100_000d;

    private DescuentoCliente() {
    }

    /**
     * Calcula el importe final a pagar luego de aplicar el descuento especial.
     *
     * @param antiguedadAnios antigüedad del cliente en años
     * @param montoCompra     monto bruto de la compra
     * @param esSocio         {@code true} si el cliente es socio
     * @return importe final con el descuento aplicado
     * @throws IllegalArgumentException si el monto no es mayor que cero
     */
    public static double procesarDescuentoCliente(final int antiguedadAnios, final double montoCompra,
            final boolean esSocio) {
        if (montoCompra <= 0) {                                              // 1 (P1)
            throw new IllegalArgumentException("El monto de compra debe ser mayor que cero"); // 2
        }
        int porcentaje = 0;                                                  // 3
        if (esSocio) {                                                       // 3 (P2)
            porcentaje = 10;                                                 // 4
            if (antiguedadAnios >= ANTIGUEDAD_MINIMA_FIDELIDAD) {            // 4 (P3)
                porcentaje += 5;                                             // 5
            }
        }
        if (montoCompra >= MONTO_COMPRA_MAYORISTA) {                         // 6 (P4)
            porcentaje += 5;                                                 // 7
        }
        return montoCompra * (100 - porcentaje) / 100;                       // 8
    }                                                                        // 9 (fin)
}
