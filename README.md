# TP 2 - Prueba de Software

Resolución del Trabajo Práctico Nro 2 de Prueba de Software. El enunciado pedía C# con xUnit "o equivalente", así que lo hice en Java 25 con JUnit 5 y Maven.

El TP tiene tres partes:

**1. Caja blanca (camino básico).** Escribí la función `procesarDescuentoCliente`, que calcula un descuento según la antigüedad del cliente, el monto de la compra y si es socio, armada para que tenga complejidad ciclomática 5. Sobre esa función hice el grafo de flujo y calculé V(G) de las tres formas (aristas y nodos, nodos predicado y regiones). Después saqué los 5 caminos independientes y escribí un caso de prueba para cada uno.

**2. Caja negra.** Para las seis funciones del enunciado (descuento por monto y VIP, validación de contraseña, precio de entrada a un parque, solicitud de crédito, multa de tránsito y uso de almacenamiento) definí las clases de equivalencia y los valores límite, y con eso armé una matriz de casos de prueba.

**3. Automatización.** Implementé las seis funciones y automaticé toda la matriz con JUnit 5. Los mismos casos corren contra dos implementaciones: la mía y los webservices que dio la cátedra (`http://iso-2026.somee.com/test`). Si un caso pasa en mi versión y falla en el servicio, el error está en el servicio.

## Resultados

- Mi implementación pasa los 188 tests, con 100 % de cobertura de ramas según JaCoCo.
- Contra el webservice corren 175 tests y fallan 10. Las fronteras de las seis funciones están bien; los errores aparecen en dos lugares:
  - Contraseñas vacías o formadas solo por espacios: el servicio las rechaza aunque cumplan la longitud mínima.
  - Valores `NaN`: el servicio los clasifica en el tramo más alto (descuento máximo, multa de 60.000 con 10 puntos, almacenamiento "Saturado", crédito "Pendiente").

El objetivo del ejercicio era encontrar errores en el servicio, así que la corrida remota termina con esas 10 fallas a propósito.

## Cómo correrlo

Hace falta JDK 25 y Maven.

```bash
mvn test            # ejercicio 1 + mi implementación
mvn test -Premoto   # los mismos casos contra el webservice
```

## Dónde está cada cosa

- `src/main/java/iso/tp2/ejercicio1`: la función del ejercicio 1
- `src/main/java/iso/tp2/reglas`: las seis funciones del ejercicio 2
- `src/main/java/iso/tp2/webservice`: el cliente HTTP del servicio de la cátedra
- `src/test/java/iso/tp2`: los tests
- `docs/`: el informe completo en Word, el grafo de flujo y el detalle de cada ejercicio
