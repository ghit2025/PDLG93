package AnSintDesRec;

import ALexico.AnLexico;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AnSemantico {
	private LinkedHashMap<String, Map<String, Object>> tablaSimbolos; //Referencia a la tabla del lexico
	private AnLexico lexico;

	//Constructor que recibe la tabla de simbolos ya creada por el lexico
	public AnSemantico(AnLexico lexico, LinkedHashMap<String, Map<String, Object>> tablaSimbolosExistente) {
		this.lexico = lexico;
		this.tablaSimbolos = tablaSimbolosExistente; //Usa la tabla existente del lexico

	}

	// ==================== FUNCIONES PRINCIPALES SEGÃšN PDF SEMÃ�NTICO ====================

	/**
	 * Insertar el tipo de un identificador a la tabla de simbolos
	 * Funcion obligatoria segun el PDF de analisis semantico
	 */
	public void anadeTipoTS(String lexema, String tipo) {
		if (!tablaSimbolos.containsKey(lexema)) {
			//Si no existe, crear entrada nueva
			Map<String, Object> atributos = new HashMap<>();
			atributos.put("tipo", tipo);
			tablaSimbolos.put(lexema, atributos);
		} else {
			//Si existe (del lexico), solo insertar el tipo
			Map<String, Object> atributos = tablaSimbolos.get(lexema);
			atributos.put("tipo", tipo);
			tablaSimbolos.put(lexema, atributos);
		}
	}


	/**
	 * Busca el tipo de un identificador en la tabla de simbolos
	 * Funcion obligatoria segun el PDF de analisis semantico
	 */
	public String buscaTipoTS(String lexema) {
		if (!tablaSimbolos.containsKey(lexema)) {
			throw new RuntimeException("Error semantico: Variable '" + lexema +
					"' no declarada en la linea " + lexico.getLinea());
		}

                Map<String, Object> atributos = tablaSimbolos.get(lexema);

                String ambito = (String) atributos.get("ambito");
                if (ambito != null && !"global".equals(ambito)) {
                        if (!lexico.dentroDeFuncion() || !ambito.equals(lexico.getAmbitoActual())) {
                                throw new RuntimeException("Error semantico: Variable '" + lexema + "' fuera de su ambito en la linea " + lexico.getLinea());
                        }
                }

                String tipo = (String) atributos.get("tipo");

		if (tipo == null) {
			throw new RuntimeException("Error semantico: Variable '" + lexema +
					"' usada antes de ser declarada en la linea " + lexico.getLinea());
		}

		return tipo;
	}

	/**
	 * Insertar el desplazamiento de un identificador (ya implementado en lexico)
	 */
	public void anadeDesplTS(String lexema, int despl) {
		if (!tablaSimbolos.containsKey(lexema)) {
			Map<String, Object> atributos = new HashMap<>();
			atributos.put("despl", despl);
			tablaSimbolos.put(lexema, atributos);
		} else {
			Map<String, Object> atributos = tablaSimbolos.get(lexema);
			atributos.put("despl", despl);
			tablaSimbolos.put(lexema, atributos);
		}
	}

	/**
	 * Busca el desplazamiento de un identificador
	 */
	public int buscaDesplTS(String lexema) {
		if (!tablaSimbolos.containsKey(lexema)) {
			throw new RuntimeException("Error semantico: Variable '" + lexema +
					"' no declarada en la linea " + lexico.getLinea());
		}

		Map<String, Object> atributos = tablaSimbolos.get(lexema);
		Integer despl = (Integer) atributos.get("despl");

		if (despl == null) {
			throw new RuntimeException("Error semantico: Variable '" + lexema +
					"' sin desplazamiento asignado en la linea " + lexico.getLinea());
		}

		return despl;
	}

	// ==================== FUNCIONES SEMANTICAS ESPECIFICAS ====================

	public void validarDeclaracion(String lexema, String tipo) {
		if (tablaSimbolos.containsKey(lexema)) {
			Map<String, Object> atributos = tablaSimbolos.get(lexema);
			String tipoExistente = (String) atributos.get("tipo");
			String categoriaExistente = (String) atributos.get("categoria");

			//Solo error si ya esta semanticamente declarada
			if (tipoExistente != null && categoriaExistente != null) {
				throw new RuntimeException("Error semantico: Variable '" + lexema +
						"' ya declarada en la linea " + lexico.getLinea());
			}
		}

                //Insertar tipo, categoria y ambito
                anadeTipoTS(lexema, tipo);
                anadeCategoriaTS(lexema, "variable");
                anadeAmbitoTS(lexema, lexico.dentroDeFuncion() ? lexico.getAmbitoActual() : "global");
        }



	/**
	 * Valida una asignacion (reglas S_prime -> = E ; S_prime -> |= E ;)
	 */
        public void validarAsignacion(String lexema, String tipoExpresion, String operador) {
                Map<String, Object> atributos = tablaSimbolos.get(lexema);
                if (atributos != null) {
                        String ambito = (String) atributos.get("ambito");
                        if (ambito != null && !"global".equals(ambito)) {
                                if (!lexico.dentroDeFuncion() || !ambito.equals(lexico.getAmbitoActual())) {
                                        throw new RuntimeException("Error semantico: Variable '" + lexema + "' fuera de su ambito en la linea " + lexico.getLinea());
                                }
                        }
                }

                String tipoVariable = buscaTipoTS(lexema);

		if (operador.equals("|=")) {
			// Operador |= solo valido para boolean (especificacion de tu grupo)
			if (!tipoVariable.equals("boolean") || !tipoExpresion.equals("boolean")) {
				throw new RuntimeException("Error semantico: Operador '|=' solo valido para tipo 'boolean' en la linea " + lexico.getLinea());
			}
		} else if (operador.equals("=")) {
			// Asignacion normal - tipos deben ser compatibles
			if (!esCompatibleAsignacion(tipoVariable, tipoExpresion)) {
				throw new RuntimeException("Error semantico: No se puede asignar '" + tipoExpresion +
						"' a variable de tipo '" + tipoVariable + "' en la linea " + lexico.getLinea());
			}
		}
	}

	/**
	 * Comprueba compatibilidad de tipos en operaciones (reglas de expresiones)
	 */
	public String comprobarTipos(String tipo1, String tipo2, String operacion) {
		if (tipo1.equals("tipo_error") || tipo2.equals("tipo_error")) {
			return "tipo_error";
		}

		switch (operacion) {
		case "+":
			if (tipo1.equals("int") && tipo2.equals("int")) {
				return "int";
			} else if (tipo1.equals("string") && tipo2.equals("string")) {
				return "string";
			}
			break;
		case ">":
			if (tipo1.equals("int") && tipo2.equals("int")) {
				return "boolean";
			} else if (tipo1.equals("string") && tipo2.equals("string")) {
				return "boolean";
			}
			break;
		case "!":
			if (tipo1.equals("boolean")) {
				return "boolean";
			}
			break;
		}

		throw new RuntimeException("Error semantico: Operador '" + operacion +
				"' no valido entre '" + tipo1 + "' y '" + tipo2 + "' en la linea " + lexico.getLinea());
	}

	/**
	 * Insertar categoria a un identificador (variable, funcion, etc.)
	 */
        public void anadeCategoriaTS(String lexema, String categoria) {
                if (!tablaSimbolos.containsKey(lexema)) {
                        Map<String, Object> atributos = new HashMap<>();
                        atributos.put("categoria", categoria);
                        tablaSimbolos.put(lexema, atributos);
                } else {
                        Map<String, Object> atributos = tablaSimbolos.get(lexema);
                        atributos.put("categoria", categoria);
                        tablaSimbolos.put(lexema, atributos);
                }
        }

        public void anadeAmbitoTS(String lexema, String ambito) {
                if (!tablaSimbolos.containsKey(lexema)) {
                        Map<String, Object> atributos = new HashMap<>();
                        atributos.put("ambito", ambito);
                        tablaSimbolos.put(lexema, atributos);
                } else {
                        Map<String, Object> atributos = tablaSimbolos.get(lexema);
                        atributos.put("ambito", ambito);
                        tablaSimbolos.put(lexema, atributos);
                }
        }

	/**
	 * Insertar informacion de funcion a la tabla de simbolos (regla F -> function H id (A) {C})
	 */
	public void validarDeclaracionFuncion(String lexema, String tipoRetorno, int numParam,
			List<Map<String, Object>> parametros,
			List<Map<String, Object>> variablesLocales) {

		if (tablaSimbolos.containsKey(lexema)) {
			Map<String, Object> atributos = tablaSimbolos.get(lexema);
			String tipoExistente = (String) atributos.get("tipo");
			if (tipoExistente != null) {
				throw new RuntimeException("Error semÃ¡ntico: FunciÃ³n '" + lexema +
						"' ya declarada en la lÃ­nea " + lexico.getLinea());
			}
		}

                anadeTipoTS(lexema, "funcion");
                anadeCategoriaTS(lexema, "funcion");
                anadeAmbitoTS(lexema, "global");

		Map<String, Object> atributos = tablaSimbolos.get(lexema);
		atributos.put("TipoRetorno", tipoRetorno);
		atributos.put("numParam", numParam);
		atributos.put("parametros", parametros);       
		atributos.put("variables", variablesLocales);  

		tablaSimbolos.put(lexema, atributos);
	}


	/**
	 * Insertar tipo de parametro a una funcion
	 */
	public void anadeTipoParamTS(String lexema, int numeroParam, String tipoParam) {
		if (!tablaSimbolos.containsKey(lexema)) {
			throw new RuntimeException("Error semantico: Funcion '" + lexema +
					"' no declarada en la linea " + lexico.getLinea());
		}

		Map<String, Object> atributos = tablaSimbolos.get(lexema);
		atributos.put("TipoParam" + String.format("%02d", numeroParam), tipoParam);
		tablaSimbolos.put(lexema, atributos);
	}

	// ==================== FUNCIONES AUXILIARES ====================

	/**
	 * Verifica compatibilidad de tipos en asignacion
	 */
	private boolean esCompatibleAsignacion(String tipoVariable, String tipoExpresion) {
		if (tipoExpresion.equals("tipo_error")) {
			return false;
		}
		return tipoVariable.equals(tipoExpresion);
	}

	/**
	 * Obtiene el tipo basico de un literal segun el codigo del token
	 */
	public String obtenerTipoLiteral(String codigoToken) {
		switch (codigoToken) {
		case "entero":
			return "int";
		case "cadena":
			return "string";
		default:
			return "tipo_error";
		}
	}

	/**
	 * Obtiene el lexema de un token id usando su desplazamiento
	 */
	public String obtenerLexemaPorDespl(int desplazamiento) {
		for (Map.Entry<String, Map<String, Object>> entrada : tablaSimbolos.entrySet()) {
			String lexema = entrada.getKey();
			Map<String, Object> atributos = entrada.getValue();
			Integer despl = (Integer) atributos.get("despl");
			if (despl != null && despl == desplazamiento) {
				return lexema;
			}
		}
		throw new RuntimeException("Error semantico: No se encontra lexema con desplazamiento " +
				desplazamiento + " en la linea " + lexico.getLinea());
	}


	// ==================== GETTERS PARA DEBUGGING ====================

	public LinkedHashMap<String, Map<String, Object>> getTablaSimbolos() {
		return tablaSimbolos;
	}

	public AnLexico getLexico() {
		return lexico;
	}
}
