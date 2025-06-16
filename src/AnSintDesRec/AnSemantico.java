package AnSintDesRec;

import ALexico.AnLexico;
import java.util.HashMap;
import java.util.Map;

public class AnSemantico {
    private Map<String, Map<String, Object>> tablaSimbolos; // Referencia a la tabla del léxico
    private AnLexico lexico;

    // Constructor que recibe la tabla de símbolos ya creada por el léxico
    public AnSemantico(AnLexico lexico, Map<String, Map<String, Object>> tablaSimbolosExistente) {
        this.lexico = lexico;
        this.tablaSimbolos = tablaSimbolosExistente; // ✅ Usa la tabla existente del léxico

    }

    // ==================== FUNCIONES PRINCIPALES SEGÚN PDF SEMÁNTICO ====================

    /**
     * Añade el tipo de un identificador a la tabla de símbolos
     * Función obligatoria según el PDF de análisis semántico
     */
    public void anadeTipoTS(String lexema, String tipo) {
        if (!tablaSimbolos.containsKey(lexema)) {
            // ✅ Si no existe, crear entrada nueva
            Map<String, Object> atributos = new HashMap<>();
            atributos.put("tipo", tipo);
            tablaSimbolos.put(lexema, atributos);
        } else {
            // ✅ Si existe (del léxico), solo añadir el tipo
            Map<String, Object> atributos = tablaSimbolos.get(lexema);
            atributos.put("tipo", tipo);
            tablaSimbolos.put(lexema, atributos);
        }
    }


    /**
     * Busca el tipo de un identificador en la tabla de símbolos
     * Función obligatoria según el PDF de análisis semántico
     */
    public String buscaTipoTS(String lexema) {
        if (!tablaSimbolos.containsKey(lexema)) {
            throw new RuntimeException("Error semántico: Variable '" + lexema +
                    "' no declarada en la línea " + lexico.getLinea());
        }

        Map<String, Object> atributos = tablaSimbolos.get(lexema);
        String tipo = (String) atributos.get("tipo");

        if (tipo == null) {
            throw new RuntimeException("Error semántico: Variable '" + lexema +
                    "' usada antes de ser declarada en la línea " + lexico.getLinea());
        }

        return tipo;
    }

    /**
     * Añade el desplazamiento de un identificador (ya implementado en léxico, pero por completitud)
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
            throw new RuntimeException("Error semántico: Variable '" + lexema +
                    "' no declarada en la línea " + lexico.getLinea());
        }

        Map<String, Object> atributos = tablaSimbolos.get(lexema);
        Integer despl = (Integer) atributos.get("despl");

        if (despl == null) {
            throw new RuntimeException("Error semántico: Variable '" + lexema +
                    "' sin desplazamiento asignado en la línea " + lexico.getLinea());
        }

        return despl;
    }

    // ==================== FUNCIONES SEMÁNTICAS ESPECÍFICAS ====================

    public void validarDeclaracion(String lexema, String tipo) {
        if (tablaSimbolos.containsKey(lexema)) {
            Map<String, Object> atributos = tablaSimbolos.get(lexema);
            String tipoExistente = (String) atributos.get("tipo");
            String categoriaExistente = (String) atributos.get("categoria");

            // ✅ Solo error si ya está semánticamente declarada
            if (tipoExistente != null && categoriaExistente != null) {
                throw new RuntimeException("Error semántico: Variable '" + lexema +
                        "' ya declarada en la línea " + lexico.getLinea());
            }
        }

        // Añadir tipo y categoría
        anadeTipoTS(lexema, tipo);
        anadeCategoriaTS(lexema, "variable");
    }



    /**
     * Valida una asignación (reglas S' → = E ; y S' → |= E ;)
     */
    public void validarAsignacion(String lexema, String tipoExpresion, String operador) {
        // No permitir asignaciones a variables globales fuera de una función
        if (!lexico.dentroDeFuncion() && lexico.esVariableGlobal(lexema)) {
            throw new RuntimeException(
                    "Error semántico: No se puede asignar a la variable global '" + lexema +
                    "' fuera de una función en la línea " + lexico.getLinea());
        }

        String tipoVariable = buscaTipoTS(lexema);

        if (operador.equals("|=")) {
            // Operador |= solo válido para boolean (especificación de tu grupo)
            if (!tipoVariable.equals("boolean") || !tipoExpresion.equals("boolean")) {
                throw new RuntimeException("Error semántico: Operador '|=' solo válido para tipo 'boolean' en la línea " + lexico.getLinea());
            }
        } else if (operador.equals("=")) {
            // Asignación normal - tipos deben ser compatibles
            if (!esCompatibleAsignacion(tipoVariable, tipoExpresion)) {
                throw new RuntimeException("Error semántico: No se puede asignar '" + tipoExpresion +
                        "' a variable de tipo '" + tipoVariable + "' en la línea " + lexico.getLinea());
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

        throw new RuntimeException("Error semántico: Operador '" + operacion +
                "' no válido entre '" + tipo1 + "' y '" + tipo2 + "' en la línea " + lexico.getLinea());
    }

    /**
     * Añade categoría a un identificador (variable, funcion, etc.)
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

    /**
     * Añade información de función a la tabla de símbolos (regla F → function H id (A) {C})
     */
    public void validarDeclaracionFuncion(String lexema, String tipoRetorno, int numParam) {
        // Verificar si ya está declarada
        if (tablaSimbolos.containsKey(lexema)) {
            Map<String, Object> atributos = tablaSimbolos.get(lexema);
            String tipoExistente = (String) atributos.get("tipo");
            if (tipoExistente != null) {
                throw new RuntimeException("Error semántico: Función '" + lexema +
                        "' ya declarada en la línea " + lexico.getLinea());
            }
        }

        anadeTipoTS(lexema, "funcion");
        anadeCategoriaTS(lexema, "funcion");

        Map<String, Object> atributos = tablaSimbolos.get(lexema);
        atributos.put("TipoRetorno", tipoRetorno);
        atributos.put("numParam", numParam);
        tablaSimbolos.put(lexema, atributos);
    }

    /**
     * Añade tipo de parámetro a una función
     */
    public void anadeTipoParamTS(String lexema, int numeroParam, String tipoParam) {
        if (!tablaSimbolos.containsKey(lexema)) {
            throw new RuntimeException("Error semántico: Función '" + lexema +
                    "' no declarada en la línea " + lexico.getLinea());
        }

        Map<String, Object> atributos = tablaSimbolos.get(lexema);
        atributos.put("TipoParam" + String.format("%02d", numeroParam), tipoParam);
        tablaSimbolos.put(lexema, atributos);
    }

    /**
     * Obtiene el tipo de retorno de una función
     */
    public String obtenerTipoRetorno(String lexema) {
        if (!tablaSimbolos.containsKey(lexema)) {
            throw new RuntimeException("Error semántico: Identificador '" + lexema +
                    "' no declarado en la línea " + lexico.getLinea());
        }

        Map<String, Object> atributos = tablaSimbolos.get(lexema);
        String tipo = (String) atributos.get("tipo");
        if (!"funcion".equals(tipo)) {
            return null;
        }
        return (String) atributos.get("TipoRetorno");
    }

    // ==================== FUNCIONES AUXILIARES ====================

    /**
     * Verifica compatibilidad de tipos en asignación
     */
    private boolean esCompatibleAsignacion(String tipoVariable, String tipoExpresion) {
        if (tipoExpresion.equals("tipo_error")) {
            return false;
        }
        return tipoVariable.equals(tipoExpresion);
    }

    /**
     * Obtiene el tipo básico de un literal según el código del token
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
        throw new RuntimeException("Error semántico: No se encontró lexema con desplazamiento " +
                desplazamiento + " en la línea " + lexico.getLinea());
    }


    // ==================== GETTERS PARA DEBUGGING ====================

    public Map<String, Map<String, Object>> getTablaSimbolos() {
        return tablaSimbolos;
    }

    public AnLexico getLexico() {
        return lexico;
    }
}
