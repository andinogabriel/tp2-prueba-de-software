# Ejercicio 2 – Partición de equivalencia y valores límite (caja negra)

Firmas del enunciado adaptadas a Java (contrato en
[`ReglasNegocio.java`](../src/main/java/iso/tp2/reglas/ReglasNegocio.java)):

| Función | Firma Java |
| --- | --- |
| A | `double calcularDescuentoCompra(double montoTotal, boolean esVIP)` |
| B | `boolean validarContrasenia(String contrasenia, int longitudMinima, boolean requiereEspeciales)` |
| C | `double calcularPrecioEntrada(int edad, String tipoDia)` |
| D | `String evaluarSolicitudCredito(int puntajeCrediticio, double ingresosMensuales)` |
| E | `ResultadoMulta calcularMultaTransito(double velocidadMedida, double limitePermitido)` (record equivalente a la tupla `(multa, puntos)`) |
| F | `String evaluarUsoAlmacenamiento(double espacioUsadoGB, double capacidadTotalGB)` |

**Convenciones**

- **CV**: clase válida, **CNV**: clase no válida (fuera del dominio del problema), **CNE**:
  entrada válida cuyo resultado el enunciado no define.
- Para montos y magnitudes decimales se usa un paso de **0,01** (un centavo / una centésima)
  para los valores a ambos lados de cada frontera.
- Los identificadores de caso (A-01, B-08, …) son los mismos que usan las pruebas automatizadas
  ([`MatrizCasosReglasNegocio.java`](../src/test/java/iso/tp2/reglas/MatrizCasosReglasNegocio.java)).
- Para las CNV la implementación local lanza `IllegalArgumentException`. Al webservice se le exige,
  cuando hay una respuesta neutra evidente, que **rechace** la entrada (HTTP 4xx) o devuelva esa
  respuesta neutra (ver Ejercicio 3).

---

## A. Descuento en e-commerce según monto y membresía VIP

### Particiones de equivalencia

| Id | Variable | Clase | Tipo | Resultado |
| --- | --- | --- | --- | --- |
| A-CV1 | montoTotal | 0 ≤ monto < 50 | válida | 0 % (normal y VIP) |
| A-CV2 | montoTotal | 50 ≤ monto < 100 | válida | 5 % normal / 10 % VIP |
| A-CV3 | montoTotal | 100 ≤ monto < 500 | válida | 10 % normal / 15 % VIP |
| A-CV4 | montoTotal | monto ≥ 500 | válida | 15 % normal / 20 % VIP |
| A-CV5 | esVIP | `true` | válida | |
| A-CV6 | esVIP | `false` | válida | |
| A-CNV1 | montoTotal | monto < 0 | no válida | rechazo |
| A-CNV2 | montoTotal | no numérico (NaN) | no válida | rechazo |

### Valores límite

| Frontera | Debajo | En el límite | Encima |
| --- | --- | --- | --- |
| 0 (inicio del dominio) | −0,01 (CNV) | 0 | – |
| 50 | 49,99 | 50 | 50,01 |
| 100 | 99,99 | 100 | 100,01 |
| 500 | 499,99 | 500 | 500,01 |

### Casos de prueba

| Id | montoTotal | esVIP | Esperado | Clase / límite |
| --- | --- | --- | --- | --- |
| A-01 | 0 | false | 0 | CV1, límite inferior |
| A-02 | 0 | true | 0 | CV1, límite inferior |
| A-03 | 25 | false | 0 | CV1, representativo |
| A-04 | 49,99 | false | 0 | CV1, debajo de 50 |
| A-05 | 49,99 | true | 0 | CV1, debajo de 50 |
| A-06 | 50 | false | 5 | CV2, límite |
| A-07 | 50 | true | 10 | CV2, límite |
| A-08 | 50,01 | false | 5 | CV2, encima de 50 |
| A-09 | 75 | true | 10 | CV2, representativo |
| A-10 | 99,99 | false | 5 | CV2, debajo de 100 |
| A-11 | 99,99 | true | 10 | CV2, debajo de 100 |
| A-12 | 100 | false | 10 | CV3, límite |
| A-13 | 100 | true | 15 | CV3, límite |
| A-14 | 100,01 | true | 15 | CV3, encima de 100 |
| A-15 | 300 | false | 10 | CV3, representativo |
| A-16 | 499,99 | false | 10 | CV3, debajo de 500 |
| A-17 | 499,99 | true | 15 | CV3, debajo de 500 |
| A-18 | 500 | false | 15 | CV4, límite |
| A-19 | 500 | true | 20 | CV4, límite |
| A-20 | 500,01 | false | 15 | CV4, encima de 500 |
| A-21 | 10.000 | true | 20 | CV4, representativo |
| A-22 | 1.000.000 | false | 15 | CV4, valor grande |
| A-23 | −0,01 | false | rechazo / 0 % | CNV1, debajo de 0 |
| A-24 | −100 | true | rechazo / 0 % | CNV1 |
| A-25 | NaN | false | rechazo / 0 % | CNV2 |
| A-26 | NaN | true | rechazo / 0 % | CNV2 |

---

## B. Validez de contraseña

Carácter especial: no es letra, ni dígito, ni espacio en blanco (equivalente a
`!char.IsLetterOrDigit(c) && !char.IsWhiteSpace(c)` en C#). Las letras acentuadas y la ñ son
letras, por lo que **no** son especiales.

### Particiones de equivalencia

| Id | Clase | Tipo | Resultado |
| --- | --- | --- | --- |
| B-CV1 | longitud < longitudMinima | válida | false |
| B-CV2 | longitud ≥ mínima, no exige especiales (cualquier contenido, incluso solo espacios) | válida | true |
| B-CV3 | longitud ≥ mínima, exige especiales, tiene al menos un especial | válida | true |
| B-CV4 | longitud ≥ mínima, exige especiales, solo letras y dígitos (ASCII o Unicode) | válida | false |
| B-CV5 | longitud ≥ mínima, exige especiales, solo letras, dígitos y espacios en blanco (espacio, tab, espacio duro) | válida | false |
| B-CNV1 | contraseña nula | no válida | rechazo |
| B-CNV2 | longitudMinima < 0 | no válida | rechazo |

### Valores límite

Longitud con mínima = 8: **7** (min − 1), **8** (min), **9** (min + 1). Mínima en los extremos
bajos: **0** y **1** (incluida la cadena vacía con mínima 0).

### Casos de prueba

| Id | contrasenia | mín. | especiales | Esperado | Clase / límite |
| --- | --- | --- | --- | --- | --- |
| B-01 | `abc1234` (7) | 8 | false | false | CV1, min − 1 |
| B-02 | `abcd1234` (8) | 8 | false | true | CV2, min |
| B-03 | `abcd12345` (9) | 8 | false | true | CV2, min + 1 |
| B-04 | `abc 1234` (8, con espacio) | 8 | false | true | CV2, el espacio cuenta para la longitud |
| B-05 | `abcd123!` | 8 | false | true | CV2, especial irrelevante |
| B-06 | `a` | 1 | false | true | CV2, mínima 1 |
| B-07 | `abc` | 0 | false | true | CV2, mínima 0 |
| B-08 | 8 espacios | 8 | false | true | CV2, solo espacios |
| B-09 | 1 espacio | 1 | false | true | CV2, solo espacios |
| B-10 | cadena vacía | 0 | false | true | CV2, longitud 0 = mínima 0 |
| B-11 | `abc!234` (7) | 8 | true | false | CV1, longitud insuficiente con especial |
| B-12 | `abcd!234` | 8 | true | true | CV3, min |
| B-13 | `#abcd123` | 8 | true | true | CV3, especial al inicio |
| B-14 | `abcd123#` | 8 | true | true | CV3, especial al final |
| B-15 | `!!!!!!!!` | 8 | true | true | CV3, solo especiales |
| B-16 | `abc_1234` | 8 | true | true | CV3, guion bajo (no es alfanumérico) |
| B-17 | `abcd-123` | 8 | true | true | CV3, guion |
| B-18 | `abcd123€` | 8 | true | true | CV3, símbolo Unicode |
| B-19 | `abcd1234` | 8 | true | false | CV4 |
| B-20 | `abc` | 0 | true | false | CV4, mínima 0 |
| B-21 | `abcd 1234` | 8 | true | false | CV5, espacio |
| B-22 | `abcd⇥1234` (tab) | 8 | true | false | CV5, tabulación |
| B-23 | `abcd␣1234` (U+00A0) | 8 | true | false | CV5, espacio duro |
| B-24 | `contraseña` | 8 | true | false | CV4, ñ es letra |
| B-25 | `áéíóú123` | 8 | true | false | CV4, vocales acentuadas |
| B-26 | `ÁRBOL1234` | 8 | true | false | CV4, mayúscula acentuada |
| B-27 | `null` | 8 | false | rechazo / false | CNV1 |
| B-28 | `abc` | −1 | false | rechazo (solo observación en el servicio) | CNV2 |

---

## C. Entrada a parque temático

### Particiones de equivalencia

| Id | Variable | Clase | Tipo | Resultado |
| --- | --- | --- | --- | --- |
| C-CV1 | edad | 0 ≤ edad < 3 | válida | 0 (no abona, sin importar el día) |
| C-CV2 | edad | 3 ≤ edad < 12 | válida | base 15 |
| C-CV3 | edad | 12 ≤ edad < 18 | válida | base 25 |
| C-CV4 | edad | 18 ≤ edad < 65 | válida | base 40 |
| C-CV5 | edad | edad ≥ 65 | válida | base 30 |
| C-CV6 | tipoDia | "Normal" (sin distinguir mayúsculas, sin espacios extremos) | válida | +0 |
| C-CV7 | tipoDia | "FinDeSemana" (ídem) | válida | +5 |
| C-CV8 | tipoDia | "Feriado" (ídem) | válida | +10 |
| C-CNV1 | edad | edad < 0 | no válida | rechazo |
| C-CNV2 | tipoDia | valor no reconocido ("Lunes", "Fin de semana") | no válida | rechazo |
| C-CNV3 | tipoDia | nulo | no válida | rechazo |

### Valores límite

Edad: **0**, **2 | 3**, **11 | 12**, **17 | 18**, **64 | 65** (y −1 como CNV). Cada edad límite se
combina con los tres tipos de día, para detectar recargos mal aplicados en algún tramo.

### Casos de prueba

| Id | edad | tipoDia | Esperado | | Id | edad | tipoDia | Esperado |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| C-01 | 0 | Normal | 0 | | C-15 | 17 | Normal | 25 |
| C-02 | 0 | Feriado | 0 | | C-16 | 17 | FinDeSemana | 30 |
| C-03 | 2 | Normal | 0 | | C-17 | 17 | Feriado | 35 |
| C-04 | 2 | FinDeSemana | 0 | | C-18 | 18 | Normal | 40 |
| C-05 | 2 | Feriado | 0 | | C-19 | 18 | FinDeSemana | 45 |
| C-06 | 3 | Normal | 15 | | C-20 | 18 | Feriado | 50 |
| C-07 | 3 | FinDeSemana | 20 | | C-21 | 64 | Normal | 40 |
| C-08 | 3 | Feriado | 25 | | C-22 | 64 | FinDeSemana | 45 |
| C-09 | 11 | Normal | 15 | | C-23 | 64 | Feriado | 50 |
| C-10 | 11 | FinDeSemana | 20 | | C-24 | 65 | Normal | 30 |
| C-11 | 11 | Feriado | 25 | | C-25 | 65 | FinDeSemana | 35 |
| C-12 | 12 | Normal | 25 | | C-26 | 65 | Feriado | 40 |
| C-13 | 12 | FinDeSemana | 30 | | C-27 | 100 | Normal | 30 |
| C-14 | 12 | Feriado | 35 | | C-28 | 100 | Feriado | 40 |

Formato del tipo de día (edad 30, tramo base 40):

| Id | tipoDia | Esperado | Qué verifica |
| --- | --- | --- | --- |
| C-29 | `normal` | 40 | minúsculas |
| C-30 | `NORMAL` | 40 | mayúsculas |
| C-31 | `␣␣Normal␣␣` | 40 | espacios extremos |
| C-32 | `findesemana` | 45 | minúsculas |
| C-33 | `FINDESEMANA` | 45 | mayúsculas |
| C-34 | `␣␣FinDeSemana␣␣` | 45 | espacios extremos |
| C-35 | `feriado` | 50 | minúsculas |
| C-36 | `FERIADO` | 50 | mayúsculas |
| C-37 | `␣fErIaDo␣` | 50 | mezcla + espacios |
| C-38 | edad −1, `Normal` | rechazo | CNV1 |
| C-39 | `Lunes` | rechazo | CNV2 |
| C-40 | `Fin de semana` | rechazo | CNV2 (espacios internos) |
| C-41 | `null` | rechazo | CNV3 |

---

## D. Solicitud de crédito

`SMVM = SmvmVigente = 376.000`. En las pruebas se usan `SMVM_VIGENTE`, `SMVM_VIGENTE * 2` y
`UN_CENTAVO = 0,01`, nunca los importes literales.

### Particiones de equivalencia

| Id | Variable | Clase | Tipo |
| --- | --- | --- | --- |
| D-CV1 | puntaje | 0 ≤ puntaje < 300 | válida |
| D-CV2 | puntaje | 300 ≤ puntaje < 700 | válida |
| D-CV3 | puntaje | 700 ≤ puntaje ≤ 850 | válida |
| D-CV4 | ingresos | SMVM ≤ ingresos < 2·SMVM | válida |
| D-CV5 | ingresos | ingresos ≥ 2·SMVM | válida |
| D-CNE1 | ingresos | 0 ≤ ingresos < SMVM | no especificada (supuesto: "Rechazada") |
| D-CNV1 | puntaje | puntaje < 0 | no válida |
| D-CNV2 | puntaje | puntaje > 850 | no válida |
| D-CNV3 | ingresos | ingresos < 0 | no válida |
| D-CNV4 | ingresos | no numérico (NaN) | no válida |

Tabla de decisión resultante (para ingresos ≥ SMVM):

| puntaje \ ingresos | [SMVM, 2·SMVM) | ≥ 2·SMVM |
| --- | --- | --- |
| [0, 300) | Rechazada | Rechazada |
| [300, 700) | Pendiente | Pendiente |
| [700, 850] | Pendiente | Aprobada |

### Valores límite

Puntaje: **0**, **299 | 300**, **699 | 700**, **850** (y −1, 851 como CNV).
Ingresos: **SMVM − 0,01 | SMVM | SMVM + 0,01**, **2·SMVM − 0,01 | 2·SMVM | 2·SMVM + 0,01**.

### Casos de prueba

| Id | puntaje | ingresos | Esperado | Clase / límite |
| --- | --- | --- | --- | --- |
| D-01 | 0 | SMVM | Rechazada | CV1 × CV4, límites inferiores |
| D-02 | 0 | 2·SMVM | Rechazada | CV1 × CV5 |
| D-03 | 150 | 2·SMVM − 0,01 | Rechazada | CV1 × CV4 |
| D-04 | 299 | SMVM | Rechazada | CV1, debajo de 300 |
| D-05 | 299 | 2·SMVM | Rechazada | CV1 × CV5: ingresos altos no compensan |
| D-06 | 300 | SMVM | Pendiente | CV2, límite |
| D-07 | 300 | 2·SMVM | Pendiente | CV2 × CV5 |
| D-08 | 500 | 2·SMVM − 0,01 | Pendiente | CV2 × CV4 |
| D-09 | 699 | SMVM | Pendiente | CV2, debajo de 700 |
| D-10 | 699 | 2·SMVM | Pendiente | CV2 × CV5, debajo de 700 |
| D-11 | 700 | SMVM | Pendiente | CV3 × CV4, límites |
| D-12 | 700 | SMVM + 0,01 | Pendiente | CV3 × CV4 |
| D-13 | 700 | 2·SMVM − 0,01 | Pendiente | CV3 × CV4, debajo de 2·SMVM |
| D-14 | 850 | SMVM | Pendiente | CV3 × CV4, puntaje máximo |
| D-15 | 850 | 2·SMVM − 0,01 | Pendiente | CV3 × CV4 |
| D-16 | 700 | 2·SMVM | Aprobada | CV3 × CV5, límites |
| D-17 | 700 | 2·SMVM + 0,01 | Aprobada | CV3 × CV5 |
| D-18 | 780 | 3·SMVM | Aprobada | CV3 × CV5, representativo |
| D-19 | 850 | 2·SMVM | Aprobada | CV3 × CV5, puntaje máximo |
| D-20 | −1 | SMVM | rechazo / Rechazada | CNV1 |
| D-21 | 851 | 2·SMVM | rechazo / Rechazada | CNV2 |
| D-22 | 700 | −0,01 | rechazo / Rechazada | CNV3 |
| D-23 | 700 | NaN | rechazo / Rechazada | CNV4 |
| D-24 | 850 | SMVM − 0,01 | Rechazada (supuesto) | CNE1, debajo de SMVM |
| D-25 | 500 | 0 | Rechazada (supuesto) | CNE1 |

---

## E. Multa y puntos de tránsito

Exceso = velocidadMedida − limitePermitido.

### Particiones de equivalencia

| Id | Clase | Tipo | multa | puntos |
| --- | --- | --- | --- | --- |
| E-CV1 | exceso ≤ 0 | válida | 0 | 0 |
| E-CV2 | 0 < exceso ≤ 10 | válida | 5.000 | 0 |
| E-CV3 | 10 < exceso ≤ 30 | válida | 15.000 | 2 |
| E-CV4 | 30 < exceso ≤ 60 | válida | 30.000 | 5 |
| E-CV5 | exceso > 60 | válida | 60.000 | 10 |
| E-CNV1 | velocidadMedida < 0 | no válida | rechazo | |
| E-CNV2 | limitePermitido ≤ 0 | no válida | rechazo | |
| E-CNV3 | velocidad o límite no numérico (NaN) | no válida | rechazo | |

### Valores límite (sobre el exceso)

**−0,01 | 0 | 0,01**, **10 | 10,01**, **30 | 30,01**, **60 | 60,01**. Se prueban con límite 60
y se repiten con límite 100 y 40, para confirmar que el tramo depende del exceso y no de la
velocidad absoluta.

### Casos de prueba

| Id | velocidad | límite | exceso | Esperado (multa / puntos) | Clase / límite |
| --- | --- | --- | --- | --- | --- |
| E-01 | 0 | 60 | −60 | 0 / 0 | CV1 |
| E-02 | 59,99 | 60 | −0,01 | 0 / 0 | CV1, debajo de 0 |
| E-03 | 60 | 60 | 0 | 0 / 0 | CV1, límite |
| E-04 | 60,01 | 60 | 0,01 | 5.000 / 0 | CV2, encima de 0 |
| E-05 | 65 | 60 | 5 | 5.000 / 0 | CV2 |
| E-06 | 70 | 60 | 10 | 5.000 / 0 | CV2, límite |
| E-07 | 70,01 | 60 | 10,01 | 15.000 / 2 | CV3, encima de 10 |
| E-08 | 70,5 | 60 | 10,5 | 15.000 / 2 | CV3 |
| E-09 | 80 | 60 | 20 | 15.000 / 2 | CV3 |
| E-10 | 90 | 60 | 30 | 15.000 / 2 | CV3, límite |
| E-11 | 90,01 | 60 | 30,01 | 30.000 / 5 | CV4, encima de 30 |
| E-12 | 100 | 60 | 40 | 30.000 / 5 | CV4 |
| E-13 | 120 | 60 | 60 | 30.000 / 5 | CV4, límite |
| E-14 | 120,01 | 60 | 60,01 | 60.000 / 10 | CV5, encima de 60 |
| E-15 | 200 | 60 | 140 | 60.000 / 10 | CV5 |
| E-16 | 100 | 100 | 0 | 0 / 0 | CV1, otro límite |
| E-17 | 110 | 100 | 10 | 5.000 / 0 | CV2, otro límite |
| E-18 | 110,01 | 100 | 10,01 | 15.000 / 2 | CV3, otro límite |
| E-19 | 130 | 100 | 30 | 15.000 / 2 | CV3, otro límite |
| E-20 | 130,01 | 100 | 30,01 | 30.000 / 5 | CV4, otro límite |
| E-21 | 160 | 100 | 60 | 30.000 / 5 | CV4, otro límite |
| E-22 | 160,01 | 100 | 60,01 | 60.000 / 10 | CV5, otro límite |
| E-23 | 45 | 40 | 5 | 5.000 / 0 | CV2, límite bajo |
| E-24 | 30 | 40 | −10 | 0 / 0 | CV1, límite bajo |
| E-25 | −1 | 60 | – | rechazo / 0 y 0 | CNV1 |
| E-26 | 50 | 0 | – | rechazo / 0 y 0 | CNV2, límite |
| E-27 | 100 | −60 | – | rechazo / 0 y 0 | CNV2 |
| E-28 | NaN | 60 | – | rechazo / 0 y 0 | CNV3 |
| E-29 | 100 | NaN | – | rechazo / 0 y 0 | CNV3 |

---

## F. Uso de almacenamiento en la nube

Porcentaje = (espacioUsadoGB / capacidadTotalGB) × 100. El porcentaje se calcula en la prueba
y se muestra en el mensaje de falla.

### Particiones de equivalencia

| Id | Clase | Tipo | Resultado |
| --- | --- | --- | --- |
| F-CV1 | 0 % ≤ porcentaje < 80 % | válida | Normal |
| F-CV2 | 80 % ≤ porcentaje < 100 % | válida | Atención |
| F-CV3 | porcentaje ≥ 100 % (incluye usado > capacidad) | válida | Saturado |
| F-CNV1 | capacidadTotalGB ≤ 0 | no válida | rechazo |
| F-CNV2 | espacioUsadoGB < 0 | no válida | rechazo |
| F-CNV3 | usado o capacidad no numérico (NaN) | no válida | rechazo |

### Valores límite

Con capacidad 100: **79,99 | 80 | 80,01**, **99,99 | 100 | 100,01**. Se repiten fronteras con
capacidades 500, 50, 2 y 1.000.000, y un caso con capacidad 1.000 y 90 GB usados, para detectar
una implementación que compare los GB absolutos contra 80 en lugar del porcentaje.

### Casos de prueba

| Id | usado (GB) | capacidad (GB) | % | Esperado | Clase / límite |
| --- | --- | --- | --- | --- | --- |
| F-01 | 0 | 100 | 0 | Normal | CV1, límite inferior |
| F-02 | 50 | 100 | 50 | Normal | CV1 |
| F-03 | 79,99 | 100 | 79,99 | Normal | CV1, debajo de 80 |
| F-04 | 80 | 100 | 80 | Atención | CV2, límite |
| F-05 | 80,01 | 100 | 80,01 | Atención | CV2, encima de 80 |
| F-06 | 90 | 100 | 90 | Atención | CV2 |
| F-07 | 99,99 | 100 | 99,99 | Atención | CV2, debajo de 100 |
| F-08 | 100 | 100 | 100 | Saturado | CV3, límite |
| F-09 | 100,01 | 100 | 100,01 | Saturado | CV3, encima de 100 |
| F-10 | 150 | 100 | 150 | Saturado | CV3 |
| F-11 | 399 | 500 | 79,8 | Normal | CV1, otra capacidad |
| F-12 | 400 | 500 | 80 | Atención | CV2, otra capacidad |
| F-13 | 499 | 500 | 99,8 | Atención | CV2, otra capacidad |
| F-14 | 500 | 500 | 100 | Saturado | CV3, otra capacidad |
| F-15 | 90 | 1.000 | 9 | Normal | CV1: 90 GB no es 90 % |
| F-16 | 40 | 50 | 80 | Atención | CV2, capacidad chica |
| F-17 | 50 | 50 | 100 | Saturado | CV3, capacidad chica |
| F-18 | 1,6 | 2 | 80 | Atención | CV2, decimales |
| F-19 | 0 | 0,5 | 0 | Normal | CV1, capacidad decimal |
| F-20 | 799.999 | 1.000.000 | 79,9999 | Normal | CV1, capacidad grande |
| F-21 | 800.000 | 1.000.000 | 80 | Atención | CV2, capacidad grande |
| F-22 | 50 | 0 | – | rechazo / sin estado | CNV1, límite |
| F-23 | 50 | −100 | – | rechazo / sin estado | CNV1 |
| F-24 | −1 | 100 | – | rechazo / sin estado | CNV2 |
| F-25 | NaN | 100 | – | rechazo / sin estado | CNV3 |
| F-26 | 50 | NaN | – | rechazo / sin estado | CNV3 |

---

## Supuestos adoptados

1. **Entradas fuera de dominio** (montos, edades, velocidades o espacio negativos; capacidad o
   límite ≤ 0; puntaje fuera de 0..850; tipo de día no reconocido; valores nulos o NaN): la
   implementación de referencia las rechaza con `IllegalArgumentException`.
2. **Ingresos menores al SMVM** (D-CNE1): el enunciado no define el resultado; se adopta
   "Rechazada" por ser la respuesta conservadora.
3. **Longitud de la contraseña**: se cuenta cada carácter, espacios incluidos, sin recortar
   (el enunciado no indica que se ignoren).
4. **Tipo de día**: solo se ignoran mayúsculas/minúsculas y espacios al inicio y al final;
   "Fin de semana" (con espacios internos) no es un tipo reconocido.
