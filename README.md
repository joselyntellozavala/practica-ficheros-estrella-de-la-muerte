# Estrella de la Muerte · Simulador y analizador forense

![Java](https://img.shields.io/badge/Java-25-orange)
![Maven](https://img.shields.io/badge/Maven-3.9+-blue)
![Log4j](https://img.shields.io/badge/Log4j-2.26.1-red)
![JUnit](https://img.shields.io/badge/JUnit-5.14.4-green)

Simula la operativa de la Estrella de la Muerte, registra todo lo que ocurre en ficheros de texto, binario y XML, y luego un analizador forense independiente investiga esos ficheros para descubrir qué incidente se ha producido.

Funciona como la caja negra de un avión: se graba durante el vuelo y se analiza después.

---

## Cómo funciona

```
 SIMULADOR ──────► historico_operaciones.txt ─┐
     │             lecturas.bin              ─┼──► ANALIZADOR FORENSE ──► informe_forense.txt
     │             estructura_estacion.xml   ─┘                          lecturas_anomalas.bin
     ▼                                                                   elementos_afectados.xml
 AnomaliaCriticaException
```

1. **Simulador.** Hace funcionar la estación durante varios ciclos. En cada ciclo, 12 sensores toman medidas, la tripulación realiza operaciones y la estación actualiza su estado. Puede producirse un incidente, que deja rastro en los ficheros. Cuando un sensor lleva 3 ciclos seguidos fuera de rango, el sistema lanza `AnomaliaCriticaException` y se detiene de forma controlada, con todos los datos ya guardados.
2. **Analizador forense.** No recibe ninguna pista del simulador. Lee los tres ficheros, cruza sus datos mediante identificadores y emite un veredicto: `IDENTIFICADO`, `SIN_INCIDENTE` o `INDETERMINADO`. Con el resultado redacta un informe para Lord Vader.

## Incidentes

| Incidente | Señal en los sensores |
|---|---|
| Desactivación del rayo tractor | la potencia de RT-01 cae a cero |
| Fuga del bloque de detención AA-23 | celdas abiertas en AA-23 |
| Fallo del soporte vital | oxígeno por debajo del 19,5 % en un sector |
| Fallo del compactador de basura | presión de CP-3263827 por encima del máximo |
| Intrusión en el hangar 327 | más sujetos de los permitidos en HG-327 |
| Brecha del conducto térmico | temperatura de CT-01 por encima del máximo |

Cada incidente afecta a un sensor distinto, de modo que el analizador puede distinguirlos sin ambigüedad.

## Ficheros de datos

### Generados por el simulador (`datos/`)

| Fichero | Contenido | Formato |
|---|---|---|
| `historico_operaciones.txt` | Operaciones de la tripulación: órdenes, relevos, accesos y avisos | Texto, una línea por operación |
| `lecturas.bin` | Medidas de los 12 sensores en cada ciclo | Binario, registros fijos de 20 bytes |
| `estructura_estacion.xml` | Sectores, subsistemas, sensores, oficiales y estado actual de cada elemento | XML |

**Relación entre ficheros.** Cada lectura del binario lleva su `idSensor`. En el XML, ese sensor pertenece a un subsistema y a un sector. Con el subsistema y el ciclo se localizan en el texto las operaciones relacionadas. La credencial de cada operación se contrasta con la plantilla de oficiales del XML.

**Acceso directo al binario.** Cada registro ocupa 20 bytes: `ciclo` (int), `idSensor` (int), `valor` (double) y `estado` (int). Como todos miden lo mismo, la posición de cualquier lectura se calcula sin recorrer el fichero:

```
posición = ((ciclo − 1) × 12 + (idSensor − 1)) × 20
```

Con `RandomAccessFile` se añaden registros al final, se lee uno concreto con `seek()` y se modifica solo su estado (`NORMAL` → `SOSPECHOSA` → `ANOMALA`) sin reescribir el resto.

### Generados por el analizador (`salida/`)

| Fichero | Contenido |
|---|---|
| `informe_forense.txt` | Veredicto, incidente, efectos detectados y conclusión |
| `lecturas_anomalas.bin` | Solo las lecturas anómalas que sirvieron de prueba |
| `elementos_afectados.xml` | Solo el sector, el subsistema, el sensor y el oficial al mando implicados |

## Primeros pasos

### Requisitos

- JDK 25
- Maven 3.9 o superior

### Instalación

```bash
git clone https://github.com/joselyntellozavala/practica-ficheros-estrella-de-la-muerte.git
cd practica-ficheros-estrella-de-la-muerte
mvn compile
```

Maven descarga automáticamente Log4j y JUnit.

### Uso

**Simulador**

```bash
# Modo normal: incidente aleatorio
mvn compile exec:java -Dexec.mainClass=es.joselyn.proyecto.app.SimuladorApp

# Sin incidente
mvn compile exec:java -Dexec.mainClass=es.joselyn.proyecto.app.SimuladorApp -Dexec.args=SIN_INCIDENTE

# Forzar un incidente concreto
mvn compile exec:java -Dexec.mainClass=es.joselyn.proyecto.app.SimuladorApp -Dexec.args=FUGA_AA23
```

Valores admitidos: `DESACTIVACION_RAYO_TRACTOR`, `FUGA_AA23`, `FALLO_SOPORTE_VITAL`, `FALLO_COMPACTADOR`, `INTRUSION_HANGAR`, `BRECHA_CONDUCTO_TERMICO`.

En IntelliJ, la carpeta `.run/` incluye una configuración para cada modo.

**Analizador forense**

```bash
# Lee datos/ y escribe en salida/
mvn compile exec:java -Dexec.mainClass=es.joselyn.proyecto.app.AnalizadorApp

# Carpetas personalizadas
mvn compile exec:java -Dexec.mainClass=es.joselyn.proyecto.app.AnalizadorApp -Dexec.args="datos salida"
```

## Pruebas

```bash
mvn test
```

11 tests en 6 clases con JUnit 5. Todos se ejecutan en directorios temporales (`@TempDir`) y usan semillas fijas, así que son reproducibles y no tocan los ficheros reales.

| Clase | Qué comprueba |
|---|---|
| `FicheroHistoricoTest` | Las operaciones escritas en el TXT se recuperan idénticas |
| `FicheroLecturaTest` | Alta, lectura directa y modificación de un registro con `RandomAccessFile` |
| `EstacionXmlTest` | La estructura escrita con DOM se reconstruye al leerla con SAX |
| `SimulacionTest` | Un incidente forzado lanza `AnomaliaCriticaException` con los datos ya persistidos |
| `AnalizadorForenseTest` | Identificación de los 6 incidentes (test parametrizado) |
| `IntegracionTest` | Flujo completo: simulador → analizador → tres salidas |

## Logs

Configurados en `src/main/resources/log4j2.xml`. Son solo trazas técnicas: el analizador no los usa como fuente.

- **`logs/evidencias/`**: un fichero por ejecución con fecha y hora en el nombre, por ejemplo `simulador_2026-11-02_20-15-30.log`.
- **`logs/runtime/`**: log con rotación automática a 10 KB. Los históricos se comprimen en `.gz` y se conservan un máximo de 5.

## Estructura del proyecto

```
├── src/main/java/es/joselyn/proyecto/
│   ├── app/            SimuladorApp y AnalizadorApp
│   ├── modelo/         entidades de la estación
│   ├── simulacion/     lógica del simulador
│   ├── forense/        lógica del analizador
│   ├── persistencia/   texto, binario y XML
│   └── excepcion/      AnomaliaCriticaException
├── src/main/resources/ log4j2.xml
├── src/test/java/      pruebas JUnit 5
├── datos/              salida del simulador
├── salida/             salida del analizador
├── logs/               evidencias y logs rotados
├── docs/decisiones.md  decisiones de diseño
└── IA.md               herramientas de IA utilizadas
```

## Tecnologías

| Uso | Tecnología |
|---|---|
| Lenguaje y build | Java 25, Maven |
| Texto | `BufferedReader` / `BufferedWriter` (UTF-8) |
| Binario | `RandomAccessFile` (acceso directo), `DataOutputStream` (secuencial) |
| Rutas y directorios | NIO.2: `Path`, `Files` |
| XML | DOM (escritura y modificación), SAX (lectura) |
| Logs | Log4j 2 |
| Pruebas | JUnit 5 |

## Autora

**Joselyn Gabriela Tello Zavala** · [@joselyntellozavala](https://github.com/joselyntellozavala)