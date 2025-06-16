
package ALexico;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;

public class AnLexico {
	private Map<String, String> palReservadas;
	private Map<String, String> operadores;
    // Tabla unificada que se usaba originalmente
    private Map<String, Map<String, Object>> tablaSimbolos;
    // Tabla global para imprimir en orden de declaración
    private LinkedHashMap<String, Map<String, Object>> tablaGlobal;
    // Tablas locales por función, conservan el orden de inserción
    private LinkedHashMap<String, LinkedHashMap<String, Map<String, Object>>> tablasLocales;
    private LinkedHashMap<String, Map<String, Object>> tablaLocalActual;
    private String funcionActual;
    private boolean esperandoIdFuncion;
    private int profundidadFuncion;
    private int contadorGlobal;
    private int contadorLocal;
	private String codigoFuente;
	private int posicionCaracter, linea;
	private int contadorIds;
	private BufferedWriter escritorTokens;
	private BufferedWriter escritorTablaSimbolos;

        public AnLexico(String codigoFuente, String archivoTokens, String archivoTablaSimbolos) {
                this.codigoFuente = codigoFuente.trim();
                this.posicionCaracter = 0;
                this.linea = 1;
                this.contadorIds = 1;
                this.contadorGlobal = 1;
                this.contadorLocal = 1;
                tablaSimbolos = new LinkedHashMap<>();
                tablaGlobal = new LinkedHashMap<>();
                tablasLocales = new LinkedHashMap<>();
                tablaLocalActual = null;
                funcionActual = null;
                esperandoIdFuncion = false;
                profundidadFuncion = 0;
                guardarPalReservadas();
                guardarOperadores();

		try {
			this.escritorTokens = new BufferedWriter(new FileWriter(archivoTokens));
			this.escritorTablaSimbolos = new BufferedWriter(new FileWriter(archivoTablaSimbolos));
			inicializarTablaSimbolos();
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }
        // En AnLexico.java - añadir este método
        public Map<String, Map<String, Object>> getTablaSimbolos() {
                return tablaSimbolos;
        }

        // Indica si actualmente el análisis está dentro de una función
        public boolean dentroDeFuncion() {
                return funcionActual != null;
        }

        // Devuelve el nombre de la función actual o null si estamos en el ámbito global
        public String getFuncionActual() {
                return funcionActual;
        }

        // Verifica si un identificador pertenece a la tabla global
        public boolean esVariableGlobal(String lexema) {
                return tablaGlobal.containsKey(lexema);
        }

	private void guardarPalReservadas() {
		palReservadas = new HashMap<>();
		palReservadas.put("boolean", "boolean");
		palReservadas.put("function", "function");
		palReservadas.put("for", "for");
		palReservadas.put("if", "if");
		palReservadas.put("int", "int");
		palReservadas.put("input", "input");
		palReservadas.put("output", "output");
		palReservadas.put("return", "return");
		palReservadas.put("string", "string");
		palReservadas.put("void", "void");
		palReservadas.put("var", "var");
	}

	private void guardarOperadores() {
		operadores = new HashMap<>();
		operadores.put("=", "equal");
		operadores.put("|=", "asigOL");
		operadores.put(",", "coma");
		operadores.put(";", "punCom");
		operadores.put("(", "par1");
		operadores.put(")", "par2");
		operadores.put("{", "cor1");
		operadores.put("}", "cor2");
		operadores.put("+", "suma");
		operadores.put("!", "not");
		operadores.put(">", "mayor");
		operadores.put("EOF", "EOF");
	}

	public int getLinea() {
		return linea;
	}

	public Token obtenerToken() {
		if (posicionCaracter >= codigoFuente.length()) {
			Token eofToken = new Token("EOF", "");
			escribirToken(eofToken);
			cerrarArchivos();
			return eofToken;
		}

		while (posicionCaracter < codigoFuente.length()
				&& Character.isWhitespace(codigoFuente.charAt(posicionCaracter))) {
			if (codigoFuente.charAt(posicionCaracter) == '\n') {
				linea++;
			}
			posicionCaracter++;
		}

		char caracterActual = codigoFuente.charAt(posicionCaracter);
		// Manejar comentarios
		if (caracterActual == '/') {
			if (posicionCaracter + 1 < codigoFuente.length() && codigoFuente.charAt(posicionCaracter + 1) == '/') {
				// Comentario de línea
				posicionCaracter += 2;
				while (posicionCaracter < codigoFuente.length() && codigoFuente.charAt(posicionCaracter) != '\n') {
					posicionCaracter++;
				}
				return obtenerToken(); // Ignorar y continuar con el siguiente token
			}
		}
		if (Character.isDigit(caracterActual)) {
			StringBuilder numero = new StringBuilder();
			while (posicionCaracter < codigoFuente.length()
					&& Character.isDigit(codigoFuente.charAt(posicionCaracter))) {
				numero.append(codigoFuente.charAt(posicionCaracter));
				posicionCaracter++;
			}
			return new Token("entero", numero.toString());
		}

                String posibleOperador = Character.toString(caracterActual);
                if (operadores.containsKey(posibleOperador)) {
                        posicionCaracter++;
                        String codigo = operadores.get(posibleOperador);
                        if (funcionActual != null) {
                                if (codigo.equals("cor1")) {
                                        profundidadFuncion++;
                                } else if (codigo.equals("cor2")) {
                                        profundidadFuncion--;
                                        if (profundidadFuncion == 0) {
                                                funcionActual = null;
                                                tablaLocalActual = null;
                                                contadorLocal = 1;
                                        }
                                }
                        }
                        return new Token(codigo, "");
                }

		if (posicionCaracter + 1 < codigoFuente.length()) {
			String posibleOperadorDoble = codigoFuente.substring(posicionCaracter, posicionCaracter + 2);
			if (operadores.containsKey(posibleOperadorDoble)) {
				posicionCaracter += 2;
				return new Token(operadores.get(posibleOperadorDoble), "");
			}
		}

		if (caracterActual == '"') {
			posicionCaracter++;
			StringBuilder cadena = new StringBuilder();
			while (posicionCaracter < codigoFuente.length() && codigoFuente.charAt(posicionCaracter) != '"') {
				cadena.append(codigoFuente.charAt(posicionCaracter));
				posicionCaracter++;
			}
			if (posicionCaracter >= codigoFuente.length() || codigoFuente.charAt(posicionCaracter) != '"') {
				throw new RuntimeException("Cadena no cerrada correctamente en la línea " + linea + ".");
			}
			posicionCaracter++;
			return new Token("cadena", '"' + cadena.toString() + '"');
		}

		if (Character.isDigit(caracterActual)) {
			StringBuilder numero = new StringBuilder();
			while (posicionCaracter < codigoFuente.length()
					&& Character.isDigit(codigoFuente.charAt(posicionCaracter))) {
				numero.append(codigoFuente.charAt(posicionCaracter));
				posicionCaracter++;
			}
			Token token = new Token("entero", numero.toString());
			escribirToken(token);
			return token;
		}

		if (Character.isLetter(caracterActual)) {
			StringBuilder palabra = new StringBuilder();
			while (posicionCaracter < codigoFuente.length()
					&& (Character.isLetterOrDigit(codigoFuente.charAt(posicionCaracter))
							|| codigoFuente.charAt(posicionCaracter) == '_')) {
				palabra.append(codigoFuente.charAt(posicionCaracter));
				posicionCaracter++;
			}
			String palabraFinal = palabra.toString();
			Token token;

                        if (palReservadas.containsKey(palabraFinal)) {
                                token = new Token(palReservadas.get(palabraFinal), "");
                                if (palabraFinal.equals("function")) {
                                        esperandoIdFuncion = true;
                                }
                        } else {
                                if (esperandoIdFuncion) {
                                        if (!tablaSimbolos.containsKey(palabraFinal)) {
                                                registrarSimboloGlobal(palabraFinal);
                                        }
                                        funcionActual = palabraFinal;
                                        tablaLocalActual = new LinkedHashMap<>();
                                        tablasLocales.put(funcionActual, tablaLocalActual);
                                        contadorLocal = 1;
                                        esperandoIdFuncion = false;
                                } else if (!tablaSimbolos.containsKey(palabraFinal)) {
                                        if (funcionActual == null) {
                                                registrarSimboloGlobal(palabraFinal);
                                        } else {
                                                registrarSimboloLocal(palabraFinal);
                                        }
                                }

                                int despl = (int) tablaSimbolos.get(palabraFinal).get("despl");
                                token = new Token("id", String.valueOf(despl));
                        }
			escribirToken(token);
			return token;
		}

		throw new RuntimeException("Carácter no reconocido: " + caracterActual + " en la línea " + linea + ".");
	}

        private void registrarSimboloGlobal(String lexema) {
                Map<String, Object> attrsUnion = new LinkedHashMap<>();
                attrsUnion.put("despl", contadorIds++);
                attrsUnion.put("ambito", "global");
                tablaSimbolos.put(lexema, attrsUnion);

                Map<String, Object> attrs = new LinkedHashMap<>();
                attrs.put("despl", contadorGlobal++);
                attrs.put("ambito", "global");
                tablaGlobal.put(lexema, attrs);
        }

        private void registrarSimboloLocal(String lexema) {
                Map<String, Object> attrsUnion = new LinkedHashMap<>();
                attrsUnion.put("despl", contadorIds++);
                attrsUnion.put("ambito", funcionActual);
                tablaSimbolos.put(lexema, attrsUnion);

                Map<String, Object> attrs = new LinkedHashMap<>();
                attrs.put("despl", contadorLocal++);
                attrs.put("ambito", funcionActual);
                if (tablaLocalActual != null) {
                        tablaLocalActual.put(lexema, attrs);
                }
        }

        private void inicializarTablaSimbolos() throws IOException {
                escritorTablaSimbolos.write("Tabla Global:\n");
                escritorTablaSimbolos.write("TABLA DE SIMBOLOS PRINCIPAL #1:\n");
        }

        private void actualizarTablaSimbolos() throws IOException {
                escritorTablaSimbolos = new BufferedWriter(new FileWriter("src/ALexico/TablaSimbolos.txt"));
                escribirTablas();
                escritorTablaSimbolos.flush();
        }

        private void escribirTablas() throws IOException {
                escritorTablaSimbolos.write("Tabla Global:\n");
                escritorTablaSimbolos.write("TABLA DE SIMBOLOS PRINCIPAL #1:\n");

                for (Map.Entry<String, Map<String, Object>> entrada : tablaGlobal.entrySet()) {
                        String lexema = entrada.getKey();
                        Map<String, Object> atributos = entrada.getValue();

                        escritorTablaSimbolos.write("* LEXEMA: '" + lexema + "'\n");
                        escritorTablaSimbolos.write("Atributos:\n");
                        for (Map.Entry<String, Object> atributo : atributos.entrySet()) {
                                String nombreAtributo = atributo.getKey();
                                Object valorAtributo = atributo.getValue();
                                if (valorAtributo instanceof String) {
                                        escritorTablaSimbolos.write("+ " + nombreAtributo + ": '" + valorAtributo + "'\n");
                                } else {
                                        escritorTablaSimbolos.write("+ " + nombreAtributo + ": " + valorAtributo + "\n");
                                }
                        }
                        escritorTablaSimbolos.write("-------------------------------------\n");
                }

                int indice = 2;
                for (Map.Entry<String, LinkedHashMap<String, Map<String, Object>>> func : tablasLocales.entrySet()) {
                        String nombre = func.getKey();
                        LinkedHashMap<String, Map<String, Object>> tabla = func.getValue();
                        escritorTablaSimbolos.write("Tabla Local para la funcion " + nombre + ":\n");
                        escritorTablaSimbolos.write("TABLA DE SIMBOLOS FUNCION " + nombre + " #" + indice + ":\n");
                        for (Map.Entry<String, Map<String, Object>> entrada : tabla.entrySet()) {
                                String lexema = entrada.getKey();
                                Map<String, Object> atributos = entrada.getValue();
                                escritorTablaSimbolos.write("* LEXEMA: '" + lexema + "'\n");
                                escritorTablaSimbolos.write("Atributos:\n");
                                for (Map.Entry<String, Object> atributo : atributos.entrySet()) {
                                        String nombreAtributo = atributo.getKey();
                                        Object valorAtributo = atributo.getValue();
                                        if (valorAtributo instanceof String) {
                                                escritorTablaSimbolos.write("+ " + nombreAtributo + ": '" + valorAtributo + "'\n");
                                        } else {
                                                escritorTablaSimbolos.write("+ " + nombreAtributo + ": " + valorAtributo + "\n");
                                        }
                                }
                                escritorTablaSimbolos.write("-------------------------------------\n");
                        }
                        indice++;
                }
        }

	/**
	 * Genera la tabla de símbolos completa con información léxica + semántica
	 * Se llama al final del análisis para incluir todos los atributos
	 */
	public void generarTablaCompleta() {
		try {
			// Cerrar el escritor actual si está abierto
			if (escritorTablaSimbolos != null) {
				escritorTablaSimbolos.close();
			}

			// Crear nuevo escritor para regenerar el archivo completo
                        escritorTablaSimbolos = new BufferedWriter(new FileWriter("src/ALexico/TablaSimbolos.txt"));
                        escribirTablas();
                        escritorTablaSimbolos.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void escribirToken(Token token) {
		try {
			escritorTokens.write(token.toString());
			escritorTokens.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void cerrarArchivos() {
		try {
			if (escritorTokens != null)
				escritorTokens.close();
			// ❌ NO cerrar escritorTablaSimbolos aquí
			// Se cerrará en generarTablaCompleta()
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
