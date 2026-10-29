package iso.tp2.reglas;

/**
 * Contrato de las seis funciones del Ejercicio 2 (firmas del enunciado adaptadas a Java).
 *
 * <p>Tiene dos implementaciones: {@link ReglasNegocioImpl}, escrita a partir de la
 * especificación, y {@code iso.tp2.webservice.ReglasNegocioWebService}, que invoca los
 * webservices de la cátedra. Ambas se prueban con la misma matriz de casos (Ejercicio 3).
 */
public interface ReglasNegocio {

    /** Salario Mínimo, Vital y Móvil vigente. */
    double SMVM_VIGENTE = 376_000d;

    String CREDITO_RECHAZADA = "Rechazada";
    String CREDITO_PENDIENTE = "Pendiente";
    String CREDITO_APROBADA = "Aprobada";

    String ALMACENAMIENTO_NORMAL = "Normal";
    String ALMACENAMIENTO_ATENCION = "Atención";
    String ALMACENAMIENTO_SATURADO = "Saturado";

    /** A. Porcentaje de descuento (0, 5, 10, 15 o 20) según el monto y la membresía VIP. */
    double calcularDescuentoCompra(double montoTotal, boolean esVIP);

    /** B. Indica si la contraseña cumple la longitud mínima y, si se exige, tiene un carácter especial. */
    boolean validarContrasenia(String contrasenia, int longitudMinima, boolean requiereEspeciales);

    /** C. Precio de la entrada según la edad y el tipo de día (Normal, FinDeSemana, Feriado). */
    double calcularPrecioEntrada(int edad, String tipoDia);

    /** D. Resultado de la solicitud: "Rechazada", "Pendiente" o "Aprobada". */
    String evaluarSolicitudCredito(int puntajeCrediticio, double ingresosMensuales);

    /** E. Multa y puntos según el exceso de velocidad sobre el límite permitido. */
    ResultadoMulta calcularMultaTransito(double velocidadMedida, double limitePermitido);

    /** F. Estado del almacenamiento: "Normal", "Atención" o "Saturado". */
    String evaluarUsoAlmacenamiento(double espacioUsadoGB, double capacidadTotalGB);
}
