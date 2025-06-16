
package ALexico;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AnLexico {
	private Map<String, String> palReservadas;
	private Map<String, String> operadores;
	private LinkedHashMap<String, Map<String, Object>> tablaSimbolos;
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
		tablaSimbolos = new LinkedHashMap<>();
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
	
	public LinkedHashMap<String, Map<String, Object>> getTablaSimbolos() {
		return tablaSimbolos;
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
		while (posicionCaracter < codigoFuente.length()
				&& Character.isWhitespace(codigoFuente.charAt(posicionCaracter))) {
			if (codigoFuente.charAt(posicionCaracter) == '\n') {
				linea++;
			}
			posicionCaracter++;
		}
		
		if (posicionCaracter >= codigoFuente.length()) {
			Token eofToken = new Token("EOF", "");
			escribirToken(eofToken);
			cerrarArchivos();
			return eofToken;
		}

		char caracterActual = codigoFuente.charAt(posicionCaracter);
		if (Character.isDigit(caracterActual)) {
			StringBuilder numero = new StringBuilder();
			while (posicionCaracter < codigoFuente.length()
					&& Character.isDigit(codigoFuente.charAt(posicionCaracter))) {
				numero.append(codigoFuente.charAt(posicionCaracter));
				posicionCaracter++;
			}
			int valor = Integer.parseInt(numero.toString());
            if (valor > 32767) {
                throw new RuntimeException("Entero fuera de rango (maximo permitido: 32767) en la linea " + linea + ".");
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
			} else {
				if (!tablaSimbolos.containsKey(palabraFinal)) {
					registrarSimbolo(palabraFinal);
				}
				int despl = (int) tablaSimbolos.get(palabraFinal).get("despl");
				token = new Token("id", String.valueOf(despl));
			}
			escribirToken(token);
			return token;
		}
		
		// Manejar comentarios
		if (caracterActual == '/') {
			if (posicionCaracter + 1 < codigoFuente.length() && codigoFuente.charAt(posicionCaracter + 1) == '/') {
				// Comentario de linea
				posicionCaracter += 2;
				while (posicionCaracter < codigoFuente.length() && codigoFuente.charAt(posicionCaracter) != '\n') {
					posicionCaracter++;
				}
				return obtenerToken(); // Ignorar y continuar con el siguiente token
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
				throw new RuntimeException("Cadena no cerrada correctamente en la linea " + linea + ".");
			}
			Token token = new Token("cadena", '"' + cadena.toString() + '"');
			posicionCaracter++;
			escribirToken(token);
			return token;
		}

		if (posicionCaracter + 1 < codigoFuente.length()) {
			String posibleOperadorDoble = codigoFuente.substring(posicionCaracter, posicionCaracter + 2);
			if (operadores.containsKey(posibleOperadorDoble)) {
				posicionCaracter += 2;
				Token token = new Token(operadores.get(posibleOperadorDoble), "");
	            escribirToken(token);
	            return token;
			}
		}
		
		String posibleOperador = String.valueOf(caracterActual);
        if (operadores.containsKey(posibleOperador)) {
            posicionCaracter++;
            Token token = new Token(operadores.get(posibleOperador), "");
            escribirToken(token);
            return token;
        }
        
		throw new RuntimeException("Caracter no reconocido: " + caracterActual + " en la linea " + linea + ".");
	}

	private void registrarSimbolo(String lexema) {
		try {
			Map<String, Object> atributos = new HashMap<>();
			//atributos.put("tipo", "id");
			atributos.put("despl", contadorIds++);
			tablaSimbolos.put(lexema, atributos);
			actualizarTablaSimbolos();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void inicializarTablaSimbolos() throws IOException {
		escritorTablaSimbolos.write("TABLA PRINCIPAL #1:\n");
	}

	private void actualizarTablaSimbolos() throws IOException {
		escritorTablaSimbolos = new BufferedWriter(new FileWriter("src/ALexico/TablaSimbolos.txt"));
		escritorTablaSimbolos.write("TABLA PRINCIPAL #1:\n");

		for (Map.Entry<String, Map<String, Object>> entrada : tablaSimbolos.entrySet()) {
			String lexema = entrada.getKey();
			Map<String, Object> atributos = entrada.getValue();

			//Escribir lexema segun formato del PDF
			escritorTablaSimbolos.write("* LEXEMA : '" + lexema + "'\n");

			//Escribir TODOS los atributos (lexicos + semanticos)
			for (Map.Entry<String, Object> atributo : atributos.entrySet()) {
				String nombreAtributo = atributo.getKey();
				Object valorAtributo = atributo.getValue();

				if (valorAtributo instanceof String) {
					escritorTablaSimbolos.write("  + " + nombreAtributo + " : '" + valorAtributo + "'\n");
				} else {
					escritorTablaSimbolos.write("  + " + nombreAtributo + " : " + valorAtributo + "\n");
				}
			}
		}

		escritorTablaSimbolos.flush();
	}

	/**
	 * Genera la tabla de simbolos completa con informacion lexica + semantica
	 * Se llama al final del analisis para incluir todos los atributos
	 */
	public void generarTablaCompleta() {
	    try {
	        if (escritorTablaSimbolos != null) {
	            escritorTablaSimbolos.close();
	        }

	        escritorTablaSimbolos = new BufferedWriter(new FileWriter("src/ALexico/TablaSimbolos.txt"));

	        int n = 1;
	        StringBuilder TS = new StringBuilder();
	        ArrayList<String> subtablas = new ArrayList<>();

	        TS.append("TABLA DE SIMBOLOS PRINCIPAL # ").append(n).append(":\n\n");

	        int desplazamientoGlobal = 0;

	        for (Map.Entry<String, Map<String, Object>> entrada : tablaSimbolos.entrySet()) {
	            String lexema = entrada.getKey();
	            Map<String, Object> atributos = entrada.getValue();

	            Object paramObj = atributos.get("param");
	            int param = (paramObj instanceof Integer) ? (Integer) paramObj : 0;

	            if (param != 1) {
	                TS.append("* LEXEMA: '").append(lexema).append("'\n");
	                TS.append("  Atributos: \n");

	                String tipo = (String) atributos.getOrDefault("tipo", "");
	                TS.append("  + tipo: '").append(tipo).append("'\n");

	                if ("funcion".equals(tipo)) {
	                    int numParam = (int) atributos.getOrDefault("numParam", 0);
	                    TS.append("    + numParam: ").append(numParam).append("\n");

	                    @SuppressWarnings("unchecked")
	                    ArrayList<Map<String, Object>> parametros = (ArrayList<Map<String, Object>>) atributos.get("parametros");

	                    @SuppressWarnings("unchecked")
	                    ArrayList<Map<String, Object>> variables = (ArrayList<Map<String, Object>>) atributos.get("variables");

	                    StringBuilder sub = new StringBuilder();
	                    sub.append("\nTABLA DE SIMBOLOS FUNCION ").append(lexema).append(" # ").append(++n).append(":\n\n");

	                    int desplazamientoLocal = 0;

	                    if (parametros != null) {
	                        for (Map<String, Object> paramInfo : parametros) {
	                            String tipoParam = (String) paramInfo.get("tipo");
	                            String lexParam = (String) paramInfo.get("lexema");

	                            int tam = switch (tipoParam) {
	                                case "int" -> 4;
	                                case "string" -> 64;
	                                case "boolean" -> 1;
	                                default -> 0;
	                            };
	                            paramInfo.put("despl", desplazamientoLocal);

	                            sub.append("* LEXEMA: '").append(lexParam).append("'\n");
	                            sub.append("  Atributos: \n");
	                            sub.append("  + tipo: '").append(tipoParam).append("'\n");
	                            sub.append("  + despl : ").append(desplazamientoLocal).append("\n");
	                            sub.append("  + param : 1\n");
	                            sub.append("-------------------------------------\n");

	                            desplazamientoLocal += tam;

	                            //Insertar tipo del parametro tambien a la TS principal
	                            TS.append("        + TipoParam0").append(parametros.indexOf(paramInfo) + 1).append(": '").append(tipoParam).append("'\n");
	                        }
	                    }

	                    if (variables != null) {
	                        for (Map<String, Object> var : variables) {
	                            String tipoVar = (String) var.get("tipo");
	                            String lexVar = (String) var.get("lexema");

	                            int tam = switch (tipoVar) {
	                                case "int" -> 4;
	                                case "string" -> 64;
	                                case "boolean" -> 1;
	                                default -> 0;
	                            };
	                            var.put("despl", desplazamientoLocal);

	                            sub.append("* LEXEMA: '").append(lexVar).append("'\n");
	                            sub.append("  Atributos: \n");
	                            sub.append("  + tipo: '").append(tipoVar).append("'\n");
	                            sub.append("  + despl : ").append(desplazamientoLocal).append("\n");
	                            sub.append("-------------------------------------\n");

	                            desplazamientoLocal += tam;
	                        }
	                    }

	                    subtablas.add(sub.toString());

	                    TS.append("    + TipoRetorno: '").append(atributos.getOrDefault("TipoRetorno", "")).append("'\n");
	                    TS.append("    + EtiqFuncion: 'Et").append(lexema).append("'\n");

	                } else {
	                    int tam = switch (tipo) {
	                        case "int" -> 4;
	                        case "string" -> 64;
	                        case "boolean" -> 1;
	                        default -> 0;
	                    };
	                    atributos.put("despl", desplazamientoGlobal);
	                    TS.append("  + despl : ").append(desplazamientoGlobal).append("\n");
	                    desplazamientoGlobal += tam;
	                }

	                TS.append("-------------------------------------\n");
	            }
	        }

	        for (String subtabla : subtablas) {
	            TS.append(subtabla);
	        }

	        escritorTablaSimbolos.write(TS.toString());
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
			//NO cerrar escritorTablaSimbolos aqui
			//Se cerrara en generarTablaCompleta()
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
