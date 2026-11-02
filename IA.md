# Uso de inteligencia artificial

## Herramientas utilizadas

### Herramienta 1
- Nombre: Claude (Anthropic), modelo Claude Opus 5.5
- Forma de uso: chat web en un proyecto de claude.ai, con el enunciado y el repositorio de GitHub sincronizado como contexto. Un chat por fase; al cerrar cada fase actualizo `docs/decisiones.md`, que conecta un chat con el siguiente. Le pedí que primero me explicara cada propuesta y su porqué, y que solo escribiera código cuando yo lo pidiera, comentado. El código lo he revisado, probado y subido yo.

## Prompts principales

### Instrucciones del proyecto
> Soy Gabi. Este proyecto es la Práctica UD1 (Manejo de ficheros) de Acceso a Datos. El enunciado completo está en el contexto del proyecto. La temática es la Estrella de la Muerte.
> Utilizaré java con Maven en IntelliJ, Log4j 2 y JUnit 5. Para el formato estructurado uso XML con DOM y SAX.
>
> Mi objetivo es entender lo que hago, no solo tener el código. Si tienes una recomendación, primero dame la explicación en texto o con gráficos y no la solución completa en código a menos que te la pida. Explícame el porqué de cada propuesta tuya. Cuando escribas código, coméntalo y explícame las partes clave.
> Trabajaremos por fases en chats separados. Existirá un archivo decisiones.md en el contexto, al terminar cada fase, recuérdame actualizarlo.
>
> Quiero terminar el 31 de octubre; la entrega es el 2 de noviembre a las 23:59 (hora de Madrid)

---

## Fase 0 — Configuración

> Lo de decisiones.md lo pensé para dividir este proyecto de Claude en chats (cada chat con una fase avanzada) y comunicar los chats entre sí con ese archivo de decisiones previas. Mi duda es si en proyectos de la vida real tmb se documentan las fases o en el README van cosas puntuales.

> oki, ahora sobre la práctica te explico mi idea. El escenario será la Estrella de la muerte de Star Wars. [...] Para los incidentes pensé en referencias como una desactivación del rayo tractor, una fuga del bloque de detención AA-23, una sobrecarga del reactor, un fallo del compactador de basura, alguna intrusión en un hangar y una brecha en el conducto térmico de escape. [...]

> o sea que poner bom me garantiza la compatibilidad y el programa decide por mí? porque ya no especifico la versión del log

> Ahora mismo esta es mi estructura en Intellij. [...] Hay algo que hacer a partir de esto?

> el log es necesario configurarlo desde el inicio, ya lo tenemos hecho?

> mejor no seas breve, explícame bien y desde el inicio porque nunca hemos visto log4j



## Fase 1 — Diseño

### Arranque

Lee docs/decisiones.md para ver lo decidido en la fase 0. En esta fase quiero definir las entidades y sus IDs, la huella concreta de cada incidente en TXT, BIN y XML, el registro binario, la estructura del XML y los paquetes y clases. Empecemos por las entidades.

> Yo había pensado primero iniciar con la estructura de paquetes por responsabilidades para tenerlo más ordenado desde el inicio. Mi propuesta es crear 5 paquetes [...] Sugieres algo?

> En mi clase Oficial no sería conveniente en lugar de pasarle el id de sector, pasarle un ENUM? así evito que se asigne a un sector que no existe

> Sigamos con las clases de persistencia ahora. [...] Mis métodos serían, crearDirectorioSiNoExiste, leerSensor, guardarSensor, actualizarValorSensor. Es mi propuesta, tú qué propones? No me des el código

> Antes de seguir, Jose me dijo que info daba muy poca información en el terminal, que me recomendaba poner eso que estoy poniendo en debug en info y que trace no lo use [...]

> quiero DOM para escritura y SAX para lectura, qué opinas?

> cuando hablamos de evento, nos referimos a la excepción? o en realidad a cualquier accion que se pueda registrar? es que el nombre puede sonar a una excepción



## Fases 2-3 — Simulador

### Arranque
> Soy Gabi. Vengo de la fase 1 (diseño), ya cerrada: lee docs/decisiones.md y el código del repositorio. Estoy en la rama feature/simulador y empiezo la fase de simulación: TipoSensor (CELDAS_ABIERTAS y SUJETOS_DETECTADOS), constructor de EventoCriticoException, enum TipoIncidente y la clase Simulacion. Explícame primero el porqué de cada paso y no me des código completo salvo que te lo pida. La entrega es el 2 de noviembre a las 23:59, hora de Madrid.

### Enums y huella
> Vamos con el paso 1, a qué te refieres con recuento?

> Estoy en el paso 2 de elaborar el enum TipoIncidente, RAYO_TRACTOR no es un incidente como tal es un dispositivo, lo renombraré

> es válido usar un enum de los incidentes como especie de catálogo para el investigador?

> me hubiese gustado lo de las 6 clases con sus huellas, pero entonces qué propones?

### Excepción
> Dale, hice esto, pero mi duda ahora es si hereda de Exception o para que mi código sea más "limpio" de Runtime? porque sino tengo que usar throws

> me refiero porque creo que es importante tener un nombre específico que englobe los incidentes, así lo hacemos


### Simulacion
> sigo sin entender del todo qué hace esta clase, explícame

> pero si borras los bin que ya existian quiere decir que esos datos no persisten?

> ya pero yo quiero implementar un modo de ejecutar sin incidente, no solo debería poner dos constructores uno con y otro sin?


### SimuladorApp
> explícame mejor a qué te refieres con la propuesta de args

> oye pero he visto que pones un log.fatal cuando quedamos en que ese nivel no usaríamos



## Fase 4 — Analizador

### Arranque
> Soy Gabi. Vengo de las fases 2-3 (simulador), ya cerradas: lee docs/decisiones.md y el código del repositorio (rama develop). Estoy en la rama feature/analizador y empiezo la fase 4: el analizador forense (lectura del XML con SAX, lectura del BIN y del TXT, reglas para los 6 incidentes e INDETERMINADO, las tres salidas TXT/BIN/XML y AnalizadorApp). Explícame el porqué de cada paso y dame código solo cuando te lo pida. Cuando me digas que haga commit, incluye siempre el mensaje. La entrega es hoy 2 de noviembre a las 23:59 (hora de Madrid) y la defensa el miércoles.


### Veredicto y errores
> con indeterminado te refieres a que el modelo del código no pudo gestionar eso? no sería una deficiencia?

> el profesor en clase dijo algo como "si yo al revisar el código, borro un fichero por ej, debería el log informar de ello" entonces eso tiene que ver con el indeterminado?

> pero entonces es correcto poner al sin incidente como indeterminado? en lugar de poner que no hay incidente?

### Código
> qué reglas crees que tengo que incluir en esta clase para el analizador?

> pero en subsistema no añades con el ciclo del último cambio sino con 0 directamente

> revisa mi código, si tienes propuestas de cambio dame tu código y el porqué


## Fase 5 — Pruebas y entrega

> Soy Gabi. Vengo de la fase 4 (analizador), ya cerrada y fusionada en develop: lee docs/decisiones.md y el código del repositorio. [...] Tests con JUnit 5 (mínimo 6) que cubran lo que pide el enunciado [...] Arreglar el aviso de Log4j cuando no está definida la propiedad "programa" [...] README.md e IA.md según el enunciado. [...] Merge a main.

> oye ya andamos cortos de tiempo harás será decirme los pasos a seguir, no me hagas hacer cosas de más, solo lo necesario. No me des el código completo, puedes darme ejemplos

> hagamos un repaso de los test y qué me propones a partir de eso?

> creo que 11 test están bien

> este es mi código, si hay mejoras puedes agregarlas pero explícame el porqué

> genérame un readme para pegarlo tal cual, tiene que tener un estilo sobrio pero puedes incluir los elementos necesarios para hacer entendible

