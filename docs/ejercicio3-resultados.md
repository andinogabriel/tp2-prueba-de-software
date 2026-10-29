# Ejercicio 3 – Automatización de pruebas y reporte de resultados

## Qué se implementó

| Pieza | Archivo | Rol |
| --- | --- | --- |
| Contrato | [`ReglasNegocio`](../src/main/java/iso/tp2/reglas/ReglasNegocio.java) | Las 6 firmas del Ejercicio 2 y la constante `SMVM_VIGENTE`. |
| Implementación propia | [`ReglasNegocioImpl`](../src/main/java/iso/tp2/reglas/ReglasNegocioImpl.java) | Escrita a partir del enunciado (implementación de referencia). |
| Cliente del webservice | [`ReglasNegocioWebService`](../src/main/java/iso/tp2/webservice/ReglasNegocioWebService.java) | Implementa el mismo contrato llamando a `GET /api/Test/*` del servicio de la cátedra. |
| Matriz de casos | [`MatrizCasosReglasNegocio`](../src/test/java/iso/tp2/reglas/MatrizCasosReglasNegocio.java) | Clase abstracta JUnit 5 con todos los casos del Ejercicio 2 (`@ParameterizedTest` + `@CsvSource` / `@MethodSource`). |
| Pruebas locales | [`ReglasNegocioLocalTest`](../src/test/java/iso/tp2/reglas/ReglasNegocioLocalTest.java) | Corre la matriz contra la implementación propia y verifica el rechazo de las clases no válidas. |
| Pruebas remotas | [`ReglasNegocioWebServiceTest`](../src/test/java/iso/tp2/reglas/ReglasNegocioWebServiceTest.java) | Corre **la misma matriz** contra el webservice (`@Tag("remoto")`). |

Como las dos implementaciones cumplen el mismo contrato, un caso que pasa en local y falla en
remoto señala un defecto del servicio y no un error del caso de prueba.

**Oráculo para las clases no válidas.** El enunciado no dice qué hacer con entradas fuera de
dominio. Al servicio se le exige lo mínimo razonable: que una entrada inválida **no produzca un
resultado de negocio que beneficie o perjudique**. Es decir, debe rechazarla (HTTP 4xx) o
devolver su respuesta neutra: 0 % de descuento, contraseña no válida, "Rechazada", multa 0 con 0
puntos, o un estado distinto de Normal / Atención / Saturado. Son las mismas respuestas que el
propio servicio da ante valores negativos. Cuando no existe una respuesta neutra evidente (precio
de la entrada, longitud mínima negativa), el caso solo se **observa**: se registra la respuesta y
se exige únicamente que no haya error interno (HTTP 5xx).

## Ejecución

| Corrida | Comando | Fecha |
| --- | --- | --- |
| Local (Ej. 1 + implementación propia) | `mvn test` | 2026-10-22 |
| Remota (webservice `http://iso-2026.somee.com/test`) | `mvn test -Premoto` | 2026-10-22 |

> El enlace HTTPS del enunciado corta el handshake TLS (*connection reset*), así que el cliente usa
> HTTP. Se puede cambiar con `-Dws.baseUrl=<url>`.

## Resultados

### Implementación local – 188 / 188 pruebas OK

| Grupo | Casos | OK | Fallas |
| --- | --- | --- | --- |
| Ej. 1 – Caminos básicos (CP1..CP5) | 5 | 5 | 0 |
| Ej. 1 – Complementarios (CX1..CX8) | 8 | 8 | 0 |
| A. CalcularDescuentoCompra | 22 | 22 | 0 |
| B. ValidarContrasenia | 26 | 26 | 0 |
| C. CalcularPrecioEntrada | 37 | 37 | 0 |
| D. EvaluarSolicitudCredito | 19 | 19 | 0 |
| E. CalcularMultaTransito | 24 | 24 | 0 |
| F. EvaluarUsoAlmacenamiento | 21 | 21 | 0 |
| Clases no válidas (rechazo con excepción) | 24 | 24 | 0 |
| Casos no especificados (supuestos) | 2 | 2 | 0 |
| **Total** | **188** | **188** | **0** |

Cobertura JaCoCo: `DescuentoCliente` 8/8 ramas, `ReglasNegocioImpl` 80/80 ramas (100 %).

### Webservice de la cátedra – 175 pruebas, 165 OK, **10 fallas**

| Grupo | Casos | OK | Fallas | Casos que fallan |
| --- | --- | --- | --- | --- |
| A. CalcularDescuentoCompra | 22 | 22 | 0 | |
| B. ValidarContrasenia | 26 | 23 | **3** | B-08, B-09, B-10 |
| C. CalcularPrecioEntrada | 37 | 37 | 0 | |
| D. EvaluarSolicitudCredito | 19 | 19 | 0 | |
| E. CalcularMultaTransito | 24 | 24 | 0 | |
| F. EvaluarUsoAlmacenamiento | 21 | 21 | 0 | |
| Robustez ante clases no válidas | 19 | 12 | **7** | A-25, A-26, D-23, E-28, E-29, F-25, F-26 |
| Observación (sin oráculo) | 7 | 7 | 0 | |
| **Total** | **175** | **165** | **10** | |

El servicio respeta **todas las fronteras especificadas** de las seis funciones: tramos de
monto, edad, puntaje, exceso y porcentaje; mayúsculas y espacios del tipo de día; espacios y
letras Unicode en contraseñas. Los defectos aparecen con contraseñas formadas solo por espacios
en blanco y con valores no numéricos (NaN).

## Defectos encontrados

| Id | Función | Casos | Entrada | Esperado | Obtenido | Severidad |
| --- | --- | --- | --- | --- | --- | --- |
| DEF-01 | B. ValidarContrasenia | B-08, B-09, B-10 | `"        "` (8 espacios), mín. 8, sin especiales · `" "`, mín. 1 · `""`, mín. 0 | `true` | `false` | Media |
| DEF-02 | A. CalcularDescuentoCompra | A-25, A-26 | monto = NaN | rechazo o 0 % | **15 %** (normal) / **20 %** (VIP) | Alta |
| DEF-03 | D. EvaluarSolicitudCredito | D-23 | puntaje 700, ingresos = NaN | rechazo o "Rechazada" | **"Pendiente"** | Media |
| DEF-04 | E. CalcularMultaTransito | E-28, E-29 | velocidad = NaN · límite = NaN | rechazo o 0 / 0 | **60.000 / 10 puntos** | Alta |
| DEF-05 | F. EvaluarUsoAlmacenamiento | F-25, F-26 | usado = NaN · capacidad = NaN | rechazo o sin estado | **"Saturado"** | Media |

**DEF-01 – Contraseñas de solo espacios en blanco.** Según el enunciado, si no se exigen
especiales la validez depende solo de la longitud, y los espacios son caracteres (de hecho
`"abc 1234"` con mínima 8 da `true` en el servicio). Aun así, una contraseña formada
**únicamente** por espacios se rechaza. La prueba exploratoria confirma el patrón: `"       a"`
da `true`, mientras que 8 tabulaciones dan `false`. Probable causa: una validación del tipo
`string.IsNullOrWhiteSpace`, o el *model binding* de ASP.NET Core, que convierte cadenas vacías
en `null`. En cualquiera de los dos casos, el comportamiento observable contradice la
especificación.

**DEF-02 a DEF-05 – NaN cae en el último tramo.** Las cuatro fallas comparten la causa: toda
comparación con `NaN` da `false`, así que una cadena `if (x < a) … else if (x < b) … else …`
termina en la rama final, que es el tramo más alto. El servicio le da el descuento máximo a un
monto inexistente, aplica la multa máxima con 10 puntos a una medición inválida y reporta
"Saturado" sin datos. Además es **inconsistente consigo mismo**: con valores negativos responde
0 %, 0 / 0 o "No disponible", pero con NaN responde lo contrario. En crédito, NaN pasa el control
de ingresos mínimos (`NaN < SMVM` es falso) y el resultado depende solo del puntaje. Corrección
sugerida: validar `double.IsNaN` / `double.IsFinite` antes de evaluar los tramos y responder
HTTP 400.

## Observaciones (no se cuentan como defectos)

Respuestas a entradas fuera de dominio o no especificadas, registradas por las pruebas remotas
(`[OBSERVACION]` en la salida de Maven):

| Casos | Entrada | Respuesta del servicio | Comentario |
| --- | --- | --- | --- |
| A-23, A-24 | monto negativo | 0 | Neutra: aceptable. |
| B-27 | contraseña nula | `false` | Neutra: aceptable. |
| B-28 | longitud mínima −1 | `true` | Acepta un parámetro inválido sin avisar. |
| C-38 | edad −1 | 0 | Una edad negativa obtiene entrada gratuita. |
| C-39, C-40, C-41 | tipo de día `Lunes`, `Fin de semana`, nulo | 40 | Lo trata como "Normal" en lugar de rechazarlo. |
| D-20, D-21, D-22 | puntaje −1 / 851, ingresos negativos | "Rechazada" | Neutra: aceptable. |
| D-24, D-25 | ingresos < SMVM | "Rechazada" | Coincide con el supuesto de la implementación local. |
| E-25, E-26, E-27 | velocidad negativa, límite ≤ 0 | 0 / 0 | Neutra: aceptable. |
| F-22, F-23, F-24 | capacidad ≤ 0, usado negativo | "No disponible" | Estado no previsto en la especificación, pero neutro. |

En general, el servicio **nunca devuelve HTTP 400** ante valores fuera de dominio. Solo lo hace el
framework cuando el valor no se puede convertir (por ejemplo `montoTotal=abc`). Una prueba
exploratoria manual (no automatizada) con `Infinity` dio 20 % de descuento (VIP) y "Aprobada"
para ingresos infinitos (puntaje 700).

## Cómo reproducir

```bash
mvn test            # Ej. 1 + matriz contra la implementación local (188 pruebas)
mvn test -Premoto   # misma matriz contra el webservice (175 pruebas, 10 fallas esperadas)
mvn test -Ptodos    # ambas corridas juntas
```

Los resultados detallados quedan en `target/surefire-reports/` (XML y TXT con el nombre visible
de cada caso) y la cobertura en `target/site/jacoco/index.html`.
