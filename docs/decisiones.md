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

## Fase 1 — Diseño (cerrada)

### Paquetes
- app (solo main) · modelo · persistencia (texto, binario, xml) · simulacion · forense · excepcion.
- forense nunca importa simulacion; persistencia solo conoce modelo.

### Modelo e IDs
- EstacionImperial (raíz: sectores + oficiales) → Sector (SEC-01…06) → Subsistema (HG-327, SV-0X…) → Sensor (int 1…12).
- Oficial (OF-XX). Rango: GRAN_MOFF > ALMIRANTE > GENERAL > CORONEL > CAPITAN > COMANDANTE > MAYOR > TENIENTE > ALFEREZ (mezcla ejército/marina como en el canon). Oficial al mando = mayor rango del sector.
- Cada hijo guarda el ID de su padre; el padre, la lista de hijos (List.copyOf al exponerla).
- Subsistema guarda cicloUltimoCambio (0 = nunca cambió).
- Lectura (record) y Operacion (record) en modelo, con validación en constructor compacto.

### Sensores (IDs consecutivos, conjunto fijo)
1 RX-01 POTENCIA · 2 RT-01 POTENCIA · 3 AA-23 CELDAS_ABIERTAS · 4 HG-327 SUJETOS_DETECTADOS · 5 CP-3263827 PRESION · 6 CT-01 TEMPERATURA · 7–12 SV-01…06 OXIGENO.

### Huellas de los incidentes
| Incidente | BIN | XML | TXT |
|---|---|---|---|
| Rayo tractor | potencia RT-01 ≈ 0 | RT-01 fuera de servicio; SEC-04 operativo | acceso a terminal del núcleo sin orden |
| Fuga AA-23 | celdas abiertas > 0 | AA-23 fuera de servicio; SEC-02 alerta | traslado sin origen; fallo de comunicación |
| Soporte vital | O2 < 19,5 en un sector al azar | SV fuera de servicio; ese sector emergencia | dificultad respiratoria |
| Compactador | presión CP > máximo | CP fuera de servicio; SEC-05 alerta | activación fuera de ciclo |
| Intrusión hangar | sujetos HG-327 > máximo | HG-327 degradado; SEC-03 alerta | acceso con TK-421 (no está en la plantilla) |
| Conducto térmico | temperatura CT-01 > máximo | CT-01 fuera de servicio; SEC-06 emergencia | impactos en la trinchera |

### Ficheros del simulador (en datos/)
- lecturas.bin: registro de 20 bytes (ciclo int, idSensor int, valor double, estado int).
  Posición = ((ciclo−1)×12 + (idSensor−1))×20. Añadir al final, leer por posición, modificar solo el estado (offset 16).
  Estado: NORMAL / SOSPECHOSA (fuera de rango 1 vez) / ANOMALA (3 ciclos seguidos, se marca hacia atrás).
- estructura_estacion.xml: DOM al escribir, SAX al leer. Datos como atributos. Nombres en EtiquetasXml.
  ultimoCiclo en la raíz; cicloUltimoCambio solo en subsistemas que cambian.
- historico_operaciones.txt: cabecera con #, una línea por operación separada por " | ":
  fecha_hora | ciclo | sector | subsistema | credencial | tipo | mensaje.

### Persistencia
- NIO.2 (Path, Files) para rutas y carpetas; java.io para el contenido (RandomAccessFile, FileWriter/FileReader + Buffered).
- Se abre y cierra el fichero en cada operación (try-with-resources): cada dato queda en disco antes de lanzar la excepción.
- UTF-8 explícito en escritura y lectura del TXT.
- Errores de formato al leer → IOException con número de línea y causa original.

### Logs (cambio respecto a la fase 0)
- Sin TRACE. Root en DEBUG, consola en INFO.
- INFO: creación de objetos, altas, cambios de estado, resúmenes. DEBUG: solo el detalle por elemento (cada lectura y cada operación).
- WARN para anomalías confirmadas; ERROR con la excepción como causa.

### Implementado por adelantado
FicheroLecturas, EscritorEstacionXml, FicheroHistorico, EtiquetasXml.

## Fase 2-3 — Simulador (cerrada)

### Modelo
- TipoSensor: campo esRecuento (CELDAS_ABIERTAS y SUJETOS_DETECTADOS); se generan como enteros. Unidad de CELDAS_ABIERTAS: "celdas".
- TipoIncidente en modelo, con descripción para el informe: es el catálogo de incidentes posibles, no la respuesta. El simulador nunca escribe su nombre en los ficheros.
  - Nombres de evento, no de dispositivo (DESACTIVACION_RAYO_TRACTOR, FUGA_AA23...), para no chocar con TipoSubsistema.
- Excepción renombrada a AnomaliaCriticaException: comprobada (extends Exception), con ciclo e id del sensor. Un único constructor.
  - Una sola para los 6 incidentes: representa la detección, que es genérica.

### Estación
- Rangos: RX-01 800-1200 MW · RT-01 40-60 MW · AA-23 0-0 · HG-327 0-6 · CP 90-110 kPa · CT-01 300-350 K · SV 19,5-23,5 %.
- Plantilla de 9 oficiales (OF-01…OF-09), al menos uno por sector y sin rangos repetidos en el mismo sector.
- Credenciales: OF-XX (oficiales), SISTEMA (avisos automáticos), SIN-IDENTIFICAR (rayo tractor), TK-421 (intrusión, no está en la plantilla).

### Simulación
- Huella (record en simulacion): sensor, valor anómalo, tipo de operación, credencial, síntoma, estado de subsistema y de sector. Se crea con un switch sin default.
- Tres modos con métodos de fábrica: normal (incidente sorteado), sinIncidente, conIncidente. Constructor privado.
- SimuladorApp: sin argumento → normal; SIN_INCIDENTE; o el nombre del incidente. Configuraciones en .run/.
- Al empezar se borran los tres ficheros: el BIN exige empezar en la posición 0 y el TXT se abre en modo añadir.
- Incidente entre los ciclos 3 y 6; máximo 10 ciclos; 10 minutos simulados por ciclo; 2 operaciones de rutina por ciclo.
- Orden de cada ciclo: BIN → TXT → XML (ultimoCiclo) → detección.
- Detección: lectura fuera de rango → se leen por posición los 2 ciclos anteriores; si los 3 son sospechosos, se marcan ANOMALA (modificar). Usa las tres operaciones de RandomAccessFile.
- Las consecuencias (XML y AVISO) se escriben antes del throw: en el catch todo está persistido.

### Logs
- Excepciones capturadas con ERROR y la excepción como causa (sin FATAL)

## Fase 4 — Analizador (cerrada)

### Lectura
- XML con SAX: LectorEstacionXml + ManejadorEstacionXml (sin public, solo lo usa el lector). Reconstruye EstacionImperial: rangos y número de sensores salen del XML, no del simulador.
  - El padre se sabe por anidamiento: sectorActual y subsistemaActual se asignan en startElement y se limpian en endElement.
  - Errores con número de línea (Locator) → SAXParseException → IOException.
- Subsistema: segundo constructor con cicloUltimoCambio para reconstruir sin un "cambio de estado" falso en el log. El de 5 parámetros delega con this(..., 0).
- BIN y TXT: se reutilizan FicheroLectura y FicheroHistorico. leerRegistro convierte valores imposibles en IOException con la posición del registro.
- Comprobación cruzada: lecturas del BIN = ultimoCiclo × sensores. Si no cuadra, WARN sin abortar (el BIN se escribe antes que el XML en cada ciclo).

### Reglas
- Veredicto en modelo: IDENTIFICADO, SIN_INCIDENTE, INDETERMINADO. Fuera de TipoIncidente porque el simulador sortea con values().
- Correlación por IDs: lecturas ANOMALA de un solo sensor (BIN) → subsistema y sector (XML) → operaciones de ese subsistema en la ventana de ciclos (TXT).
- El XML confirma: subsistema no OPERATIVO y cicloUltimoCambio = último ciclo anómalo. El estado del sector es efecto, no condición (el rayo tractor deja SEC-04 OPERATIVO).
- Regla: switch sin default sobre TipoSubsistema + dirección (bajo mínimo / sobre máximo). REACTOR no tiene incidente.
- TXT: rayo tractor e intrusión → ACCESO con credencial ajena a la plantilla (ni SISTEMA ni un oficial del XML); el resto → AVISO sobre el subsistema. No se buscan textos de mensajes.
- SIN_INCIDENTE solo si no hay lecturas ANOMALA ni SOSPECHOSA y todo el XML está OPERATIVO. Cualquier contradicción → INDETERMINADO con su motivo.
- ResultadoAnalisis (record) en modelo, para que persistencia lo escriba sin conocer forense.
- Oficial al mando = menor ordinal de RangoOficial en el sector.

### Salidas (salida/)
- informe_forense.txt: BufferedWriter UTF-8 sin modo añadir. Veredicto, motivo, efectos y conclusión imperial.
- lecturas_anomalas.bin: solo las ANOMALA, mismo registro de 20 bytes, con DataOutputStream secuencial (no RandomAccessFile: no hay acceso por posición). Vacío en SIN_INCIDENTE.
- elementos_afectados.xml: DOM. Raíz con veredicto e incidente; solo sector → subsistema → sensor afectados y oficialAlMando. Raíz vacía en SIN_INCIDENTE.

### AnalizadorApp
- Argumentos opcionales [carpetaDatos] [carpetaSalida]; por defecto datos/ y salida/.
- Comprueba los tres ficheros antes de analizar: si falta alguno, ERROR con todos los que faltan y fin sin salidas.
- IOException capturada con ERROR y la excepción como causa.

### Pendiente para la fase 5
- Log4j muestra un aviso interno cuando no está definida la propiedad "programa" (ocurrirá en los tests).