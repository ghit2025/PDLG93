package main;

import ALexico.AnLexico;
import AnSintDesRec.AnSintactico;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        AnSintactico sintactico = null;
        try {
            // ✅ Configuración de archivos
            // Para la demostración generamos la tabla de símbolos utilizando
            // un ejemplo más completo con varias funciones.
            // Analizaremos ahora un programa que contiene la función RR
            // para comprobar que se genera su tabla local correctamente.
            String archivoCodFuente = "src/ALexico/rr_valid.txt";
            String archivoTokens = "src/ALexico/Tokens.txt";
            String archivoTablaSimbolos = "src/ALexico/TablaSimbolos.txt";

            // ✅ Leer código fuente
            String codigoFuente = leerArchivo(archivoCodFuente);
            System.out.println("=== CÓDIGO FUENTE ===");
            System.out.println(codigoFuente);
            System.out.println("\n=== INICIANDO ANÁLISIS ===");

            // ✅ Crear analizador léxico
            System.out.println("1. Creando analizador léxico...");
            AnLexico lexico = new AnLexico(codigoFuente, archivoTokens, archivoTablaSimbolos);

            // ✅ Crear analizador sintáctico (que internamente crea el semántico)
            System.out.println("2. Creando analizador sintáctico...");
            sintactico = new AnSintactico(lexico);

            // ✅ Ejecutar análisis sintáctico-semántico
            System.out.println("3. Ejecutando análisis sintáctico-semántico...");
            sintactico.P(); // Función inicial de tu gramática

            // ✅ Obtener y mostrar resultados
            String parse = sintactico.obtenerParse();
            System.out.println("\n=== RESULTADOS ===");
            System.out.println("Parse generado: " + parse);

            System.out.println("\n✅ ANÁLISIS COMPLETADO EXITOSAMENTE");
            System.out.println("📄 Archivos generados:");
            System.out.println("   - Tokens: " + archivoTokens);
            System.out.println("   - Tabla Símbolos Completa: " + archivoTablaSimbolos);

        } catch (RuntimeException e) {
            // ✅ Manejo de errores sintácticos y semánticos
            System.err.println("\n❌ ERROR DURANTE EL ANÁLISIS:");
            System.err.println(e.getMessage());

            // Mostrar tipo de error
            if (e.getMessage().contains("Error sintactico")) {
                System.err.println("🔍 Tipo: Error Sintáctico");
            } else if (e.getMessage().contains("Error semántico")) {
                System.err.println("🔍 Tipo: Error Semántico");
            } else {
                System.err.println("🔍 Tipo: Error General");
            }

        } catch (IOException e) {
            // ✅ Manejo de errores de archivo
            System.err.println("\n❌ ERROR DE ARCHIVO:");
            System.err.println("No se pudo leer el archivo: " + e.getMessage());
            System.err.println("Verifica que el archivo existe y tiene permisos de lectura.");

        } catch (Exception e) {
            // ✅ Manejo de errores inesperados
            System.err.println("\n❌ ERROR INESPERADO:");
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            if (sintactico != null) {
                try {
                    System.out.println("4. Generando tabla de símbolos unificada...");
                    sintactico.generarTablaUnificada();
                } catch (Exception ex) {
                    System.err.println("No se pudo generar la tabla de símbolos: " + ex.getMessage());
                }
            }
        }
    }

    /**
     * Lee el contenido completo de un archivo de texto
     */
    private static String leerArchivo(String nombreArchivo) throws IOException {
        StringBuilder contenido = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(nombreArchivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                contenido.append(linea).append("\n");
            }
        }

        return contenido.toString();
    }
}
