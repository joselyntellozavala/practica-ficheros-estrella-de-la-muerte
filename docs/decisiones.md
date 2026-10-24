# Decisiones del proyecto — Estrella de la Muerte

Plan: 0 Configuración · 1 Diseño · 2 Base simulador (modelo, TXT, BIN) · 3 Simulador completo (XML, incidentes, excepción) · 4 Analizador · 5 Tests y entrega.
Código listo el lunes 26; revisión hasta el 31; entrega el 2/11 a las 23:59 (hora de Madrid).

## Fase 0 — Configuración (cerrada)

### Escenario
- **Temática:** Estrella de la Muerte. El analizador es un oficial imperial que redacta un informe para Darth Vader, con conclusión en tono imperial (ej.: "se recomienda reasignar al comandante del sector 7 a Kessel").
- **Incidentes (6):** desactivación del rayo tractor, fuga del bloque AA-23, fallo del soporte vital en un sector, fallo del compactador de basura, intrusión en hangar, brecha del conducto térmico.
  - Descartado: sobrecarga del reactor, porque su huella se confundía con la brecha del conducto (ambos térmicos). El reactor sigue como subsistema normal (fuente de energía).
  - Cada ejecución produce un solo incidente; no se encadenan como en la película.
  - El TXT del simulador registra síntomas, nunca el nombre del incidente.
  - Cada incidente dispara un sensor distinto en el BIN, para que el analizador pueda distinguirlos.

### Herramientas
- Java 25, Maven, IntelliJ.
- Log4j 2.26.1 y JUnit 5.14.4, con versiones fijadas mediante BOM.
  - Motivo: un solo número por librería, sin riesgo de mezclar versiones de api y core.
- Plugins: maven-compiler-plugin 3.14.0 y maven-surefire-plugin 3.5.5.
- XML: DOM en el simulador (modifica nodos), SAX en el analizador (solo recorre).

### Log4j 2 (configurado en fase 0, src/main/resources/log4j2.xml)
- Consola: desde INFO, para ver la ejecución sin ruido.
- Evidencias: un fichero por ejecución en logs/evidencias/, nombre programa_yyyy-MM-dd_HH-mm-ss.log, desde TRACE. No rota ni se borra: es el registro completo.
- Runtime: logs/runtime/programa.log, desde TRACE, rotación por tamaño a 10 KB, históricos programa-%i.log.gz (compresión .gz), máximo 5 conservados.
  - Solo índice en los históricos (sin fecha): con fecha, el límite max se aplicaría por día y no limitaría el total.
- Formato de línea: fecha y hora con milisegundos, nivel, hilo, clase y mensaje.
- El nombre del programa ("simulador" / "analizador") se fija con System.setProperty("programa", ...) en un bloque static ANTES de declarar el Logger. Si no, Log4j se inicializa antes de que exista la propiedad. Valor por defecto: "app".
- Niveles en el código: TRACE para pasos mecánicos (posiciones del binario, nodos SAX), DEBUG para creación de objetos y valores, INFO para hitos, WARN/ERROR/FATAL para anomalías y fallos. Sin mensajes artificiales.
- El fichero prueba-log4j_... de logs/evidencias y los logs actuales de logs/runtime corresponden a la prueba de configuración, no a una ejecución del simulador.

### Repositorio
- Ramas: main (entregable), develop (integración), feature/* (cada fase).
- Commits en imperativo ("Añade…"), un commit por cambio lógico.
- Carpetas vacías con .gitkeep; el código las crea igualmente con Files.createDirectories.
- El .gitignore no debe ignorar datos, salidas ni logs de evidencias (la entrega exige ejemplos). target/ ya está ignorado.
- Main.java de plantilla vaciado; se sustituirá por los main del simulador y del analizador.

## Pendiente para la Fase 1 (diseño)
- Entidades e IDs que relacionan los tres ficheros (sector, subsistema, sensor, oficial al mando).
- Huella concreta de cada incidente en TXT, BIN y XML.
- Campos y tamaño fijo del registro binario; qué se añade, lee y modifica con RandomAccessFile.
- Estructura del XML del simulador.
- Paquetes, clases y nombre de la excepción propia.