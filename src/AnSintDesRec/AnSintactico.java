package AnSintDesRec;

import ALexico.AnLexico;
import ALexico.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnSintactico {
	private AnLexico lexico;
	private Token tokenActual;
	//private List<Integer> parse; //Lista con el orden de las reglas aplicadas
	private String parse;
	private AnSemantico semantico; //Insertar analizador semantico
	private Token tokenAnterior; //Para mantener referencia al token
	private List<Map<String, Object>> listaParametros;
	private List<Map<String, Object>> listaVariablesLocales;


	
	//Constructor
	public AnSintactico(AnLexico lexico) {
		this.lexico = lexico;
		this.tokenActual = lexico.obtenerToken();
		//this.parse = new ArrayList<>();
		this.parse = "Descendente ";
		this.semantico = new AnSemantico(lexico, lexico.getTablaSimbolos());
	}

	//Metodo para pedir el siguiente token
	private void sigToken() {
		tokenActual = lexico.obtenerToken();
	}
	
	// Metodo equiparar
	private void equipara(String tokenEsperado) {
		if(tokenActual.getCodigo().equals(tokenEsperado)) {
			tokenAnterior = tokenActual; //Guardar token anterior
			sigToken();
		} else {
			error("Se esperaba " + tokenEsperado + " pero se encontro el token " + tokenActual.getCodigo() + " en la linea " + lexico.getLinea());
		}
	}
	// ==================== FUNCIONES AUXILIARES SEMANTICAS ====================

	/**
	 * Obtiene el lexema de un token id usando su desplazamiento
	 */
	private String obtenerLexemaId() {
		try {
			int despl = Integer.parseInt(tokenActual.getAtributo());
			return semantico.obtenerLexemaPorDespl(despl);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Error: Token id con atributo invalido en linea " + lexico.getLinea());
		}
	}

	/**
	 * Obtiene el lexema del token anterior (para asignaciones)
	 */
	private String obtenerLexemaAnterior() {
		try {
			int despl = Integer.parseInt(tokenAnterior.getAtributo());
			return semantico.obtenerLexemaPorDespl(despl);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Error: Token id anterior con atributo invalido en linea " + lexico.getLinea());
		}
	}
	/**
	 * Generar la tabla de simbolos unificada al final del analisis
	 */
	public void generarTablaUnificada() {
		//Solicitar al lexico que regenere el archivo con informacion completa
		lexico.generarTablaCompleta();
	}



	//Metodo auxiliar para imprimir errores
	private void error(String mensaje) {
		throw new RuntimeException("Error sintactico: " + mensaje);
	}
	
	//-------------------- FUNCIONES DE LAS REGLAS DEL DESCENDENTE RECURSIVO ---------------------------
	
	//Funcion P
	public void P() {
		switch(tokenActual.getCodigo()) {
			case "if":
			case "var":
			case "for":
			case "id":
			case "output":
			case "input":
			case "return":
				parse += " 1";//parse.add(1); //Regla P --> BP
				B();
				P();
				break;
			case "function":
				parse += " 2";//parse.add(2); //Regla P --> FP
				F();
				P();
				break;
			case "EOF":
				parse += " 3";//parse.add(3); //Regla P --> lambda»
				break;
			default:
				error("Token inesperado en P. Se encontro el token " + tokenActual.getCodigo() + " en la linea " + lexico.getLinea());
				break;
		}
	}

	// Funcion B
	public void B() {
		switch(tokenActual.getCodigo()) {
			case "if":
				parse += " 4";
				equipara("if");
				equipara("par1");
				String tipoCondicion = E(); //E debe devolver tipo
				//Validar que la condicion sea boolean
				if (!tipoCondicion.equals("boolean")) {
					throw new RuntimeException("Error semantico: Condicion del 'if' debe ser de tipo 'boolean', encontrado '" + tipoCondicion + "' en linea " + lexico.getLinea());
				}
				equipara("par2");
				S();
				break;
			case "var":
				parse += " 5";
				equipara("var");
				String tipo = T(); // T devuelve el tipo
				String lexema = obtenerLexemaId(); // Obtener lexema del id
				semantico.validarDeclaracion(lexema, tipo); // Validación semántica

				// Solo insertar en listaVariablesLocales si estamos dentro de una función
				if (lexico.dentroDeFuncion()) {
					if (listaVariablesLocales == null) {
						listaVariablesLocales = new ArrayList<>();
					}
					Map<String, Object> variable = new HashMap<>();
					variable.put("lexema", lexema);
					variable.put("tipo", tipo);
					listaVariablesLocales.add(variable);
				}
				equipara("id");
				equipara("punCom");
				break;
			case "for":
				parse += " 6";
				equipara("for");
				equipara("par1");
				S();
				String tipoCondicionFor = E(); //E debe devolver tipo
				//Validar que la condicion sea boolean
				if (!tipoCondicionFor.equals("boolean")) {
					throw new RuntimeException("Error semantico: Condicion del 'for' debe ser de tipo 'boolean', encontrado '" + tipoCondicionFor + "' en linea " + lexico.getLinea());
				}
				equipara("punCom");
				D();
				equipara("par2");
				equipara("cor1");
				C();
				equipara("cor2");
				break;
			case "id":
			case "output":
			case "input":
			case "return":
				parse += " 7";
				S();
				break;
			default:
				error("Token inesperado en B. Se encontro el token " + tokenActual.getCodigo() + " en la linea " + lexico.getLinea());
				break;
		}
	}


	//Funcion T modificada para devolver el tipo
	public String T() {
		switch(tokenActual.getCodigo()) {
			case "int":
				parse += " 8";
				equipara("int");
				return "int"; //Devolver tipo
			case "boolean":
				parse += " 9";
				equipara("boolean");
				return "boolean"; //Devolver tipo
			case "string":
				parse += " 10";
				equipara("string");
				return "string"; //Devolver tipo
			default:
				error("Token inesperado en T. Se encontro el token " + tokenActual.getCodigo() + " en la linea " + lexico.getLinea());
				return "tipo_error"; //Devolver tipo de error
		}
	}


	//Funcion S
	public void S() {
		switch(tokenActual.getCodigo()) {
			case "id":
				parse += " 11";
				equipara("id");
				S_prime();
				break;
			case "output":
				parse += " 12";
				equipara("output");
				String tipoOutput = E(); //Validar tipo de expresion
				equipara("punCom");
				break;
			case "input":
				parse += " 13";
				equipara("input");
				String lexemaInput = obtenerLexemaId(); //Obtener lexema del id
				String tipoInput = semantico.buscaTipoTS(lexemaInput); //Validar que esta declarada
				equipara("id");
				equipara("punCom");
				break;
			case "return":
				parse += " 14";
				equipara("return");
				X();
				equipara("punCom");
				break;
			default:
				error("Token inesperado en S. Se encontro el token " + tokenActual.getCodigo() + " en la linea " + lexico.getLinea());
				break;
		}
	}


	//Funcion S' modificada con analisis semantico
	public void S_prime() {
		switch(tokenActual.getCodigo()) {
			case "equal":
				parse += " 15";
				String lexemaId = obtenerLexemaAnterior(); //Del id previo
				equipara("equal");
				String tipoExpresion = E(); //E devuelve tipo
				semantico.validarAsignacion(lexemaId, tipoExpresion, "="); //Validacion semantica
				equipara("punCom");
				break;
			case "asigOL":
				parse += " 16";
				String lexemaIdOL = obtenerLexemaAnterior(); //Del id previo
				equipara("asigOL");
				String tipoExpresionOL = E(); //E devuelve tipo
				semantico.validarAsignacion(lexemaIdOL, tipoExpresionOL, "|="); //Validacion semantica
				equipara("punCom");
				break;
			case "par1":
				parse += " 17";
				equipara("par1");
				L();
				equipara("par2");
				equipara("punCom");
				break;
			default:
				error("Token inesperado en S'. Se encontro el token " + tokenActual.getCodigo() + " en la linea " + lexico.getLinea());
				break;
		}
	}

	
	//Funcion L
	public void L() {
		switch(tokenActual.getCodigo()) {
			case "id":
			case "par1":
			case "entero":
			case "cadena":
			case "not":
				parse += " 18";//parse.add(18); //Regla L --> EQ
				E();
				Q();
				break;
			case "par2":
				parse += " 19";//parse.add(19); //Regla L --> Î»
				break;
			default:
				error("Token inesperado en L. Se encontro el token " + tokenActual.getCodigo() + " en la linea " + lexico.getLinea());
				break;
		}
	}
	
	//Funcion Q
	public void Q() {
		switch(tokenActual.getCodigo()) {
			case "coma":
				parse += " 20";//parse.add(20); //Regla Q --> , E Q
				equipara("coma");
				E();
				Q();
				break;
			case "par2":
				parse += " 21";//parse.add(21); //Regla Q --> Î»
				break;
			default:
				error("Token inesperado en Q. Se encontro el token " + tokenActual.getCodigo() + " en la linea " + lexico.getLinea());
				break;
		}
		
			
	}
	
	//Funcion X
	public void X() {
		switch(tokenActual.getCodigo()) {
			case "id":
			case "par1":
			case "entero":
			case "cadena":
			case "not":
				parse += " 22";//parse.add(22); //Regla X --> E
				E();
				break;
			case "punCom":
				parse += " 23";//parse.add(23); //Regla X --> Î»
				break;
			default:
				error("Token inesperado en X. Se encontro el token " + tokenActual.getCodigo() + " en la linea " + lexico.getLinea());
				break;
		}
	}

	//Funcion F
	public void F() {
		switch(tokenActual.getCodigo()) {
			case "function":
				listaParametros = new ArrayList<>();
				listaVariablesLocales = new ArrayList<>();
				parse += " 24";
				equipara("function");
				String tipoRetorno = H(); //H devuelve tipo de retorno
				String lexemaFuncion = obtenerLexemaId(); //Obtener lexema de la funcion
                                equipara("id");
                                lexico.entrarFuncion(lexemaFuncion);
                                equipara("par1");
                                int numParametros = A(); //A debe devolver numero de parametros
                                semantico.validarDeclaracionFuncion(lexemaFuncion, tipoRetorno, numParametros,
                        listaParametros, listaVariablesLocales);
                                //Validacion semantica
                                equipara("par2");
                                equipara("cor1");
                                C();
                                equipara("cor2");
                                lexico.salirFuncion();
                                break;
			default:
				error("Token inesperado en F. Se encontro el token " + tokenActual.getCodigo() + " en la linea " + lexico.getLinea());
				break;
		}
	}


	// Funcion H 
	public String H() {
		switch(tokenActual.getCodigo()) {
			case "int":
			case "boolean":
			case "string":
				parse += " 25";
				return T(); //Devolver tipo usando T()
			case "void":
				parse += " 26";
				equipara("void");
				return "void"; //Devolver tipo void
			default:
				error("Token inesperado en H. Se encontro el token " + tokenActual.getCodigo() + " en la linea " + lexico.getLinea());
				return "tipo_error"; //Devolver tipo de error
		}
	}


	//Funcion A
	public int A() { //Devolver numero de parametros
		switch(tokenActual.getCodigo()) {
			case "int":
			case "boolean":
			case "string":
				parse += " 27";
				String tipoParam = T(); //Obtener tipo del parametro
				String lexemaParam = obtenerLexemaId(); //Obtener lexema del parametro
				semantico.validarDeclaracion(lexemaParam, tipoParam); //Validar parametro
				//Insertar a lista de parametros
			    Map<String, Object> parametro = new HashMap<>();
			    parametro.put("lexema", lexemaParam);
			    parametro.put("tipo", tipoParam);
			    parametro.put("param", 1);
			    listaParametros.add(parametro);
				equipara("id");
				return 1 + K(); //Contar este parametro + los de K()
			case "void":
				parse += " 28";
				equipara("void");
				return 0; //Sin parametros
			default:
				error("Token inesperado en A. Se encontro el token " + tokenActual.getCodigo() + " en la linea " + lexico.getLinea());
				return 0;
		}
	}


	//Funcion K
	public int K() { //Devolver numero de parametros adicionales
		switch(tokenActual.getCodigo()) {
			case "coma":
				parse += " 29";
				equipara("coma");
				String tipoParam = T(); //Obtener tipo del parametro
				String lexemaParam = obtenerLexemaId(); //Obtener lexema del parametro
				semantico.validarDeclaracion(lexemaParam, tipoParam); //Validar parametro
				//Insertar a lista de parametros
			    Map<String, Object> parametro = new HashMap<>();
			    parametro.put("lexema", lexemaParam);
			    parametro.put("tipo", tipoParam);
			    parametro.put("param", 1);
			    listaParametros.add(parametro);
				equipara("id");
				return 1 + K(); //Contar este parametro + los siguientes
			case "par2":
				parse += " 30";
				return 0; //No hay mas parametros
			default:
				error("Token inesperado en K. Se encontro el token " + tokenActual.getCodigo() + " en la linea " + lexico.getLinea());
				return 0;
		}
	}

	//Funcion C
	public void C() {
		switch(tokenActual.getCodigo()) {
			case "if":
			case "var":
			case "for":
			case "id":
			case "output":
			case "input":
			case "return":
				parse += " 31";//parse.add(31); //Regla C --> BC
				B();
				C();
				break;
			case "cor2":
				parse += " 32";//parse.add(32); //Regla C --> Î»
				break;
			default:
				error("Token inesperado en C. Se encontro el token " + tokenActual.getCodigo() + " en la linea " + lexico.getLinea());
				break;
		}
	}



	// Funcion E
	public String E() {
		parse += " 33";
		String tipoR = R(); //R devuelve tipo
		return E_prime(tipoR); //E_prime recibe y devuelve tipo
	}

	// Funcion E'
	public String E_prime(String tipoIzquierdo) {
		switch(tokenActual.getCodigo()) {
			case "mayor":
				parse += " 34";
				equipara("mayor");
				String tipoDerecho = R(); //R devuelve tipo
				String tipoResultado = semantico.comprobarTipos(tipoIzquierdo, tipoDerecho, ">"); //Validacion semantica
				return E_prime(tipoResultado); //Continuar con el tipo resultante
			case "par2":
			case "coma":
			case "punCom":
				parse += " 35";
				return tipoIzquierdo; //Devolver tipo sin modificar
			default:
				error("Token inesperado en E'. Se encontro el token " + tokenActual.getCodigo() + " en la linea " + lexico.getLinea());
				return "tipo_error";
		}
	}

	// Funcion R
	public String R() {
		parse += " 36";
		String tipoU = U(); //U devuelve tipo
		return R_prime(tipoU); //R_prime recibe y devuelve tipo
	}

	// Funcion R' 
	public String R_prime(String tipoIzquierdo) {
		switch(tokenActual.getCodigo()) {
			case "suma":
				parse += " 37";
				equipara("suma");
				String tipoDerecho = U(); //U devuelve tipo
				String tipoResultado = semantico.comprobarTipos(tipoIzquierdo, tipoDerecho, "+"); //Validacion semantica
				return R_prime(tipoResultado); //Continuar con el tipo resultante
			case "mayor":
			case "par2":
			case "punCom":
			case "coma":
				parse += " 38";
				return tipoIzquierdo; //Devolver tipo sin modificar
			default:
				error("Token inesperado en R'. Se encontro el token " + tokenActual.getCodigo() + " en la linea " + lexico.getLinea());
				return "tipo_error";
		}
	}

	// Funcion U
	public String U() {
		switch(tokenActual.getCodigo()) {
			case "not":
				parse += " 39";
				equipara("not");
				String tipoU = U(); //U devuelve tipo
				return semantico.comprobarTipos(tipoU, "", "!"); //Validacion semantica
			case "id":
			case "par1":
			case "entero":
			case "cadena":
				parse += " 40";
				return V(); //V devuelve tipo
			default:
				error("Token inesperado en U. Se encontro el token " + tokenActual.getCodigo() + " en la linea " + lexico.getLinea());
				return "tipo_error";
		}
	}

	// Funcion V
	public String V() {
		switch(tokenActual.getCodigo()) {
		case "id":
		    parse += " 41";

		    String lexemaId = obtenerLexemaId();  // aún no hemos consumido el id
		    equipara("id");

		    if (tokenActual.getCodigo().equals("par1")) {        // llamada a función
		        String tipoRet = semantico.getTipoRetornoFuncion(lexemaId);
		        equipara("par1");
		        L();                      // lista de argumentos
		        equipara("par2");
		        return tipoRet;           // devolvemos el tipo de retorno (p.ej. int)
		    } else {                      // uso como variable
		        String tipoVar = semantico.buscaTipoTS(lexemaId);
		        V_prime();                // por si viene ‘( L )’ en otra regla
		        return tipoVar;
		    }
			case "par1":
				parse += " 42";
				equipara("par1");
				String tipoExpr = E(); //E devuelve tipo
				equipara("par2");
				return tipoExpr; //Devolver tipo de la expresion
			case "entero":
				parse += " 43";
				equipara("entero");
				return "int"; //Devolver tipo entero
			case "cadena":
				parse += " 44";
				equipara("cadena");
				return "string"; //Devolver tipo string
			default:
				error("Token inesperado en V. Se encontro el token " + tokenActual.getCodigo() + " en la linea " + lexico.getLinea());
				return "tipo_error";
		}
	}

	
	//Funcion V'
	public void V_prime() {
		switch(tokenActual.getCodigo()) {
			case "par1":
				parse += " 45";//parse.add(45); //Regla V' --> (L)
				equipara("par1");
				L();
				equipara("par2");
				break;
			case "mayor":
			case "suma":
			case "punCom":
			case "coma":
			case "par2":
				
			case "asigOL":
				parse += " 46";//parse.add(46); //Regla V' --> Î»
				break;
			default:
				error("Token inesperado en V'. Se encontro el token " + tokenActual.getCodigo() + " en la linea " + lexico.getLinea());
				break;
		}
	}

	//Funcion D
	public void D() {
		switch(tokenActual.getCodigo()) {
			case "id":
				parse += " 47";
				equipara("id");
				D_prime();
				break;
			default:
				error("Token inesperado en D. Se esperaba 'id'. Se encontro el token " + tokenActual.getCodigo() + " en la linea " + lexico.getLinea());
				break;
		}
	}

	//Funcion D'
	public void D_prime() {
		switch(tokenActual.getCodigo()) {
			case "equal":
				parse += " 48";
				String lexemaAsig = obtenerLexemaAnterior(); //Del id anterior
				equipara("equal");
				String tipoExprAsig = E(); //E devuelve tipo
				semantico.validarAsignacion(lexemaAsig, tipoExprAsig, "="); //Validacion semantica
				break;
			default:
				error("Token inesperado en D'. Se esperaba '='. Se encontro el token " + tokenActual.getCodigo() + " en la linea " + lexico.getLinea());
				break;
		}
	}

	
	//------------------------------------------ FIN FUNCIONES DESCENDENTE RECURSIVO -----------------
	
	//Devolver la lista de las reglas aplicadas o parse.
	public String obtenerParse() {
        return parse;
    }
}
