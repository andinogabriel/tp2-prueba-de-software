package iso.tp2.reglas;

import java.util.Locale;

/**
 * Implementación de referencia de las reglas del Ejercicio 2, escrita a partir del enunciado.
 *
 * <p>Las entradas fuera del dominio del problema (montos negativos, edades negativas, puntajes
 * fuera de 0..850, tipos de día no reconocidos, etc.) se rechazan con
 * {@link IllegalArgumentException}. Los supuestos tomados donde el enunciado no define el
 * comportamiento están documentados en {@code docs/ejercicio2-caja-negra.md}.
 */
public class ReglasNegocioImpl implements ReglasNegocio {

    @Override
    public double calcularDescuentoCompra(final double montoTotal, final boolean esVIP) {
        exigir(montoTotal >= 0, "El monto total no puede ser negativo");
        if (montoTotal < 50) {
            return 0;
        }
        if (montoTotal < 100) {
            return esVIP ? 10 : 5;
        }
        if (montoTotal < 500) {
            return esVIP ? 15 : 10;
        }
        return esVIP ? 20 : 15;
    }

    @Override
    public boolean validarContrasenia(final String contrasenia, final int longitudMinima,
            final boolean requiereEspeciales) {
        exigir(contrasenia != null, "La contraseña es obligatoria");
        exigir(longitudMinima >= 0, "La longitud mínima no puede ser negativa");
        if (contrasenia.length() < longitudMinima) {
            return false;
        }
        if (!requiereEspeciales) {
            return true;
        }
        return contrasenia.codePoints().anyMatch(ReglasNegocioImpl::esCaracterEspecial);
    }

    @Override
    public double calcularPrecioEntrada(final int edad, final String tipoDia) {
        exigir(edad >= 0, "La edad no puede ser negativa");
        final double recargo = recargoPorTipoDia(tipoDia);
        if (edad < 3) {
            return 0;
        }
        final double precioBase;
        if (edad < 12) {
            precioBase = 15;
        } else if (edad < 18) {
            precioBase = 25;
        } else if (edad < 65) {
            precioBase = 40;
        } else {
            precioBase = 30;
        }
        return precioBase + recargo;
    }

    @Override
    public String evaluarSolicitudCredito(final int puntajeCrediticio, final double ingresosMensuales) {
        exigir(puntajeCrediticio >= 0 && puntajeCrediticio <= 850, "El puntaje debe estar entre 0 y 850");
        exigir(ingresosMensuales >= 0, "Los ingresos no pueden ser negativos");
        // Supuesto: el enunciado solo define resultados con ingresos desde el SMVM.
        if (ingresosMensuales < SMVM_VIGENTE || puntajeCrediticio < 300) {
            return CREDITO_RECHAZADA;
        }
        if (puntajeCrediticio < 700 || ingresosMensuales < SMVM_VIGENTE * 2) {
            return CREDITO_PENDIENTE;
        }
        return CREDITO_APROBADA;
    }

    @Override
    public ResultadoMulta calcularMultaTransito(final double velocidadMedida, final double limitePermitido) {
        exigir(velocidadMedida >= 0, "La velocidad medida no puede ser negativa");
        exigir(limitePermitido > 0, "El límite permitido debe ser mayor que cero");
        final double exceso = velocidadMedida - limitePermitido;
        if (exceso <= 0) {
            return new ResultadoMulta(0, 0);
        }
        if (exceso <= 10) {
            return new ResultadoMulta(5_000, 0);
        }
        if (exceso <= 30) {
            return new ResultadoMulta(15_000, 2);
        }
        if (exceso <= 60) {
            return new ResultadoMulta(30_000, 5);
        }
        return new ResultadoMulta(60_000, 10);
    }

    @Override
    public String evaluarUsoAlmacenamiento(final double espacioUsadoGB, final double capacidadTotalGB) {
        exigir(espacioUsadoGB >= 0, "El espacio usado no puede ser negativo");
        exigir(capacidadTotalGB > 0, "La capacidad total debe ser mayor que cero");
        final double porcentaje = (espacioUsadoGB / capacidadTotalGB) * 100;
        if (porcentaje < 80) {
            return ALMACENAMIENTO_NORMAL;
        }
        if (porcentaje < 100) {
            return ALMACENAMIENTO_ATENCION;
        }
        return ALMACENAMIENTO_SATURADO;
    }

    private static double recargoPorTipoDia(final String tipoDia) {
        exigir(tipoDia != null, "El tipo de día es obligatorio");
        return switch (tipoDia.strip().toLowerCase(Locale.ROOT)) {
            case "normal" -> 0;
            case "findesemana" -> 5;
            case "feriado" -> 10;
            default -> throw new IllegalArgumentException("Tipo de día no reconocido: " + tipoDia);
        };
    }

    /** Especial: no es letra, ni dígito, ni espacio en blanco (equivale a char.IsLetterOrDigit/IsWhiteSpace de .NET). */
    private static boolean esCaracterEspecial(final int codePoint) {
        return !Character.isLetterOrDigit(codePoint)
                && !Character.isWhitespace(codePoint)
                && !Character.isSpaceChar(codePoint);
    }

    /** Rechaza valores fuera del dominio, incluido NaN (toda comparación con NaN da false). */
    private static void exigir(final boolean condicion, final String mensaje) {
        if (!condicion) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}
