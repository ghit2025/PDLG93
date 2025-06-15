Expansión del Analizador Léxico
El analizador léxico de tu práctica está diseñado para realizar las siguientes funciones clave:
1. Funcionalidades principales
1. Identificación de patrones:
• Reconoce palabras clave como int, function, if, etc.
• Detecta operadores como +, =, |=.
• Maneja identificadores, constantes numéricas y cadenas de texto.
• Ignora espacios en blanco y comentarios.
2. Generación de tokens:
• Cada elemento léxico se transforma en un token con un formato predefinido, por
ejemplo:
• <id, x> para identificadores.
• <entero, 123> para números enteros.
• Estos tokens se escriben en Tokens.txt.
3. Gestión de errores léxicos:
• Detecta caracteres no reconocidos y lanza excepciones con el mensaje de error y la
línea donde ocurre.
4. Actualización de la tabla de símbolos:
• Introduce identificadores en la tabla de símbolos si no están previamente registrados.
2. Estructura del código
1. Inicialización:
• Se inicializan las estructuras necesarias:
• palReservadas: Almacena las palabras clave.
• operadores: Contiene los operadores admitidos.
• tablaSimbolos: Registra información de los identificadores.
Ejemplo de inicialización de palabras clave:
• palReservadas.put("int", "int");
palReservadas.put("function", "function");
• Procesamiento del código fuente:
• Saltos de línea y espacios: Ignora caracteres en blanco y actualiza el número de línea al
encontrar \n.
• Reconocimiento de tokens:
• Números: Acumula dígitos y devuelve un token <entero, valor>.
• Cadenas: Captura el contenido entre comillas dobles y genera un token <cadena,
valor>.
• Identificadores y palabras clave: Diferencia entre palabras clave e identificadores.
• Errores léxicos: Lanza excepciones si encuentra caracteres no válidos.
Ejemplo:
2. if (Character.isLetter(caracterActual)) {
// Manejo de identificadores y palabras clave
}
3. Gestión de la tabla de símbolos:
• Los identificadores nuevos se registran con:
• Tipo: id.
• Desplazamiento: Un contador que incrementa con cada entrada.
• Escribe la tabla en TablaSimbolos.txt.
3. Ejemplo de funcionamiento
Código fuente:
var int x;
function void imprimir() {
var string mensaje;
output mensaje;
}
Salida en Tokens.txt:
<var, - >
<int, - >
<id, x>
<punCom, - >
<function, - >
<void, - >
<id, imprimir>
<par1, - >
<par2, - >
<cor1, - >
<var, - >
<string, - >
<id, mensaje>
<punCom, - >
<output, - >
<id, mensaje>
<punCom, - >
<cor2, - >
Salida en TablaSimbolos.txt:
TABLA PRINCIPAL #1:
* LEXEMA: 'x'
+ tipo: 'int'
+ despl: 1
-------------------------------------
* LEXEMA: 'imprimir'
+ tipo: 'funcion'
-------------------------------------
* LEXEMA: 'mensaje'
+ tipo: 'string'
+ despl: 2
-------------------------------------
4. Casos de error
1. Caracteres no válidos:
• Código: x = ?;
• Error: Carácter no reconocido: ? en la línea 1.
2. Cadenas no cerradas:
• Código: var string mensaje = "Hola;
• Error: Cadena no cerrada correctamente en la línea 1.
3. Comentarios no cerrados:
• Código:
• /* Esto es un comentario
• Error: Comentario de bloque no cerrado correctamente en la
línea 2.
Expansión sobre el Analizador Sintáctico
El analizador sintáctico es la fase del procesador que verifica si el código fuente sigue las reglas
gramaticales del lenguaje. En tu práctica, el analizador sintáctico es descendente recursivo y sigue
una gramática LL(1).
1. Funcionalidades principales
1. Entrada del analizador sintáctico:
• Recibe como entrada los tokens generados por el analizador léxico.
• Procesa estos tokens siguiendo las reglas de la gramática del lenguaje.
2. Verificación de estructuras gramaticales:
• Comprueba que las declaraciones de variables, funciones, expresiones y estructuras
de control sigan el orden y las reglas gramaticales definidas.
3. Gestión de errores:
• Detecta y reporta errores si se encuentra un token inesperado o si falta algún
elemento requerido.
4. Generación del "parse":
• Mantiene un registro de las reglas gramaticales aplicadas para analizar el código, que
se guarda en salidaVAST.txt.
2. Gramática utilizada
La gramática es LL(1) y está diseñada para ser adecuada para un analizador descendente recursivo.
Gramática simplificada:
1. Sentencias principales:
• P → BP | FP | λ
B → if (E) S | var T id; | for (S; E; D) {C} | S
F → function H id (A) {C}
• Tipos de datos:
• T → int | boolean | string
• Sentencias:
• S → id = E; | id |= E; | id (L); | output E; | input id; | return X;
• Expresiones:
• E → E > R | R
R → R + U | U
U → !U | V
V → id | (E) | id (L) | entero | cadena
• Declaraciones y parámetros:
5. A → T id K | void
K → , T id K | λ
3. Estructura del código
1. Método principal:
• El punto de entrada es la función P(), que aplica las reglas iniciales de la gramática.
Ejemplo:
• public void P() {
switch(tokenActual.getCodigo()) {
case "if":
case "var":
case "for":
case "id":
case "output":
case "input":
case "return":
parse += " 1"; // Regla P --> BP
B();
P();
break;
case "function":
parse += " 2"; // Regla P --> FP
F();
P();
break;
case "EOF":
parse += " 3"; // Regla P --> λ
break;
default:
error("Token inesperado en P.");
break;
}
}
• Funciones para cada regla gramatical:
• Cada regla de la gramática tiene una función correspondiente.
• Las funciones llaman a otras de manera recursiva para analizar las producciones.
Ejemplo: Función B() (sentencias compuestas y declaraciones):
• public void B() {
switch(tokenActual.getCodigo()) {
case "if":
parse += " 4"; // Regla B --> if (E) S
equipara("if");
equipara("par1");
E();
equipara("par2");
S();
break;
case "var":
parse += " 5"; // Regla B --> var T id ;
equipara("var");
T();
equipara("id");
equipara("punCom");
break;
case "for":
parse += " 6"; // Regla B --> for (S; E; D) {C}
equipara("for");
equipara("par1");
S();
equipara("punCom");
E();
equipara("punCom");
D();
equipara("par2");
equipara("cor1");
C();
equipara("cor2");
break;
default:
error("Token inesperado en B.");
break;
}
}
• Gestión de errores:
• Si un token no coincide con el esperado, el analizador lanza un error indicando el token
encontrado y el esperado.
Ejemplo:
3. private void equipara(String tokenEsperado) {
if (tokenActual.getCodigo().equals(tokenEsperado)) {
sigToken();
} else {
error("Se esperaba " + tokenEsperado + " pero se encontró " +
tokenActual.getCodigo());
}
}
4. Ejemplo de funcionamiento
Código fuente:
function int suma(int a, int b) {
return a + b;
}
Parse generado (salidaVAST.txt):
D 2 24 25 27 30 31 33 36 37 38 35 32 3
Explicación:
• 2: Regla P → FP.
• 24: Regla F → function H id (A) {C}.
• 25: Regla H → int.
• 27: Regla A → T id K.
• 30: Regla K → λ (sin más parámetros).
• 31: Regla C → BC.
• 33: Regla E → RE'.
• 36: Regla R → UR'.
• 37: Regla R' → + UR'.
5. Gestión de errores comunes
1. Falta de delimitadores:
• Código: if (a > b S;
• Error: Se esperaba ')' pero se encontró 'S' en la línea 2.
2. Orden incorrecto:
• Código: var id x int;
• Error: Se esperaba un tipo pero se encontró 'id' en la
línea 1.

Expansión sobre el Analizador Sintáctico
El analizador sintáctico es la fase del procesador que verifica si el código fuente sigue las reglas
gramaticales del lenguaje. En tu práctica, el analizador sintáctico es descendente recursivo y sigue
una gramática LL(1).
1. Funcionalidades principales
1. Entrada del analizador sintáctico:
• Recibe como entrada los tokens generados por el analizador léxico.
• Procesa estos tokens siguiendo las reglas de la gramática del lenguaje.
2. Verificación de estructuras gramaticales:
• Comprueba que las declaraciones de variables, funciones, expresiones y estructuras
de control sigan el orden y las reglas gramaticales definidas.
3. Gestión de errores:
• Detecta y reporta errores si se encuentra un token inesperado o si falta algún
elemento requerido.
4. Generación del "parse":
• Mantiene un registro de las reglas gramaticales aplicadas para analizar el código, que
se guarda en salidaVAST.txt.
2. Gramática utilizada
La gramática es LL(1) y está diseñada para ser adecuada para un analizador descendente recursivo.
Gramática simplificada:
1. Sentencias principales:
• P → BP | FP | λ
B → if (E) S | var T id; | for (S; E; D) {C} | S
F → function H id (A) {C}
• Tipos de datos:
• T → int | boolean | string
• Sentencias:
• S → id = E; | id |= E; | id (L); | output E; | input id; | return X;
• Expresiones:
• E → E > R | R
R → R + U | U
U → !U | V
V → id | (E) | id (L) | entero | cadena
• Declaraciones y parámetros:
5. A → T id K | void
K → , T id K | λ
3. Estructura del código
1. Método principal:
• El punto de entrada es la función P(), que aplica las reglas iniciales de la gramática.
Ejemplo:
• public void P() {
switch(tokenActual.getCodigo()) {
case "if":
case "var":
case "for":
case "id":
case "output":
case "input":
case "return":
parse += " 1"; // Regla P --> BP
B();
P();
break;
case "function":
parse += " 2"; // Regla P --> FP
F();
P();
break;
case "EOF":
parse += " 3"; // Regla P --> λ
break;
default:
error("Token inesperado en P.");
break;
}
}
• Funciones para cada regla gramatical:
• Cada regla de la gramática tiene una función correspondiente.
• Las funciones llaman a otras de manera recursiva para analizar las producciones.
Ejemplo: Función B() (sentencias compuestas y declaraciones):
• public void B() {
switch(tokenActual.getCodigo()) {
case "if":
parse += " 4"; // Regla B --> if (E) S
equipara("if");
equipara("par1");
E();
equipara("par2");
S();
break;
case "var":
parse += " 5"; // Regla B --> var T id ;
equipara("var");
T();
equipara("id");
equipara("punCom");
break;
case "for":
parse += " 6"; // Regla B --> for (S; E; D) {C}
equipara("for");
equipara("par1");
S();
equipara("punCom");
E();
equipara("punCom");
D();
equipara("par2");
equipara("cor1");
C();
equipara("cor2");
break;
default:
error("Token inesperado en B.");
break;
}
}
• Gestión de errores:
• Si un token no coincide con el esperado, el analizador lanza un error indicando el token
encontrado y el esperado.
Ejemplo:
3. private void equipara(String tokenEsperado) {
if (tokenActual.getCodigo().equals(tokenEsperado)) {
sigToken();
} else {
error("Se esperaba " + tokenEsperado + " pero se encontró " +
tokenActual.getCodigo());
}
}
4. Ejemplo de funcionamiento
Código fuente:
function int suma(int a, int b) {
return a + b;
}
Parse generado (salidaVAST.txt):
D 2 24 25 27 30 31 33 36 37 38 35 32 3
Explicación:
• 2: Regla P → FP.
• 24: Regla F → function H id (A) {C}.
• 25: Regla H → int.
• 27: Regla A → T id K.
• 30: Regla K → λ (sin más parámetros).
• 31: Regla C → BC.
• 33: Regla E → RE'.
• 36: Regla R → UR'.
• 37: Regla R' → + UR'.
5. Gestión de errores comunes
1. Falta de delimitadores:
• Código: if (a > b S;
• Error: Se esperaba ')' pero se encontró 'S' en la línea 2.
2. Orden incorrecto:
• Código: var id x int;
• Error: Se esperaba un tipo pero se encontró 'id' en la
línea 1
Expansión sobre la Tabla de Símbolos
La tabla de símbolos es una estructura clave en el diseño de tu procesador de lenguajes. Se utiliza
para almacenar información relevante sobre los identificadores del programa, como variables,
funciones y parámetros. A continuación, se detallan sus aspectos principales:
1. Funcionalidades principales de la tabla de símbolos
1. Organización jerárquica:
• Tabla Global: Contiene variables globales y funciones.
• Tablas Locales: Cada función tiene su propia tabla que almacena parámetros y
variables locales.
2. Información almacenada: Cada entrada incluye:
• Lexema: El nombre del identificador.
• Tipo: int, string, boolean, funcion, etc.
• Categoría: variable, funcion, parametro.
• Desplazamiento: Dirección de memoria asignada al identificador.
• Información adicional para funciones:
• Número de parámetros.
• Tipos de parámetros.
• Tipo de retorno.
• Etiqueta de función (Etsuma, EtimprimirMensaje).
3. Gestión dinámica:
• Los identificadores se añaden a la tabla cuando se procesan las declaraciones.
• Los desplazamientos se calculan automáticamente en función del tipo:
• int: +4.
• boolean: +1.
• string: +64.
2. Ejemplo de diseño de la tabla de símbolos
Código fuente:
var int x;
var string mensaje;
function int suma(int a, int b) {
var int resultado;
resultado = a + b;
return resultado;
}
function void imprimirMensaje(string mensaje) {
output mensaje;
}
Salida en la tabla de símbolos:
Tabla Global:
TABLA DE SIMBOLOS PRINCIPAL #1:
* LEXEMA: 'suma'
Atributos:
+ tipo: 'funcion'
+ numParam: 2
+ TipoParam01: 'int'
+ TipoParam02: 'int'
+ TipoRetorno: 'int'
+ EtiqFuncion: 'Etsuma'
-------------------------------------
* LEXEMA: 'imprimirMensaje'
Atributos:
+ tipo: 'funcion'
+ numParam: 1
+ TipoParam01: 'string'
+ TipoRetorno: 'void'
+ EtiqFuncion: 'EtimprimirMensaje'
-------------------------------------
* LEXEMA: 'x'
Atributos:
+ tipo: 'int'
+ despl: 0
-------------------------------------
* LEXEMA: 'mensaje'
Atributos:
+ tipo: 'string'
+ despl: 4
-------------------------------------
Tabla Local para la función suma:
TABLA DE SIMBOLOS FUNCION suma #2:
* LEXEMA: 'a'
Atributos:
+ tipo: 'int'
+ despl: 0
-------------------------------------
* LEXEMA: 'b'
Atributos:
+ tipo: 'int'
+ despl: 4
-------------------------------------
* LEXEMA: 'resultado'
Atributos:
+ tipo: 'int'
+ despl: 8
-------------------------------------
Tabla Local para la función imprimirMensaje:
TABLA DE SIMBOLOS FUNCION imprimirMensaje #3:
* LEXEMA: 'mensaje'
Atributos:
+ tipo: 'string'
+ despl: 0
-------------------------------------
3. Integración en el código
1. Añadir identificadores a la tabla:
• Variables globales: Se añaden en el contexto global.
• Parámetros y variables locales: Se registran en la tabla local de la función
correspondiente.
Ejemplo:
• public void añadirVariable(String id, String tipo, boolean esGlobal, int
linea) {
Map<String, EntradaTS> tabla = esGlobal ? tablaSimbolosGlobal :
tablaSimbolosLocal;
int desplazamiento = esGlobal ? desplazamientoGlobal : desplazamientoLocal;
EntradaTS entrada = new EntradaTS(tipo, "variable");
entrada.desplazamiento = desplazamiento;
tabla.put(id, entrada);
// Actualizar desplazamiento
desplazamiento += tipo.equals("int") ? 4 : (tipo.equals("boolean") ? 1 :
64);
}
• Recuperar información:
• Al acceder a un identificador, primero se busca en la tabla local y luego en la global.
• Si no se encuentra, se lanza un error semántico:
• public EntradaTS buscarIdentificador(String id, int linea) {
if (tablaSimbolosLocal.containsKey(id)) {
return tablaSimbolosLocal.get(id);
} else if (tablaSimbolosGlobal.containsKey(id)) {
return tablaSimbolosGlobal.get(id);
} else {
throw new RuntimeException("Error semántico: El
identificador '" + id + "' no está declarado. Línea " + linea);
}
}
4. Gestión de errores
1. Errores comunes relacionados con la tabla de símbolos:
• Duplicados: Si se intenta registrar un identificador ya existente. Ejemplo:
• Error semántico: La variable 'x' ya está declarada. Línea 3.
• Identificadores no declarados: Si se usa un identificador antes de declararlo. Ejemplo:
• Error semántico: El identificador 'y' no está declarado. Línea 5.
2. Cómo gestionarlos:
• Los errores se detectan en tiempo de análisis semántico y detienen la ejecución del
programa
