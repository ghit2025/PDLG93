package main;

import ALexico.AnLexico;
import AnSintDesRec.AnSintactico;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            //Configuracion de archivos
            String archivoCodFuente = "src/ALexico/ArchivoBorrable.txt";
            String archivoTokens = "src/ALexico/Tokens.txt";
            String archivoTablaSimbolos = "src/ALexico/TablaSimbolos.txt";

            //Leer codigo fuente
            String codigoFuente = leerArchivo(archivoCodFuente);
            System.out.println("=== CODIGO FUENTE ===");
            System.out.println(codigoFuente);
            System.out.println("\n=== INICIANDO ANALISIS ===");

            //Crear analizador lexico
            System.out.println("1. Creando analizador lexico...");
            AnLexico lexico = new AnLexico(codigoFuente, archivoTokens, archivoTablaSimbolos);

            //Crear analizador sintactico (que internamente crea el semantico)
            System.out.println("2. Creando analizador sintactico...");
            AnSintactico sintactico = new AnSintactico(lexico);

            //Ejecutar analisis sintactico-semantico
            System.out.println("3. Ejecutando analisis sintactico-semantico...");
            sintactico.P(); //Funcion inicial de nuestra gramatica

            //Obtener y mostrar resultados
            String parse = sintactico.obtenerParse();
            System.out.println("\n=== RESULTADOS ===");
            System.out.println("Parse generado: " + parse);

            //Generar tabla unificada en lugar de cerrar archivos
            System.out.println("4. Generando tabla de simbolos unificada...");
            sintactico.generarTablaUnificada();

            System.out.println("\n ANALISIS COMPLETADO EXITOSAMENTE");
            System.out.println("    Archivos generados:");
            System.out.println("   - Tokens: " + archivoTokens);
            System.out.println("   - Tabla Simbolos Completa: " + archivoTablaSimbolos);

        } catch (RuntimeException e) {
            //Manejo de errores sintacticos y semanticos
            System.err.println("\n ERROR DURANTE EL ANALISIS:");
            System.err.println(e.getMessage());

            //Mostrar tipo de error
            if (e.getMessage().contains("Error sintactico")) {
                System.err.println("Tipo: Error Sintactico");
            } else if (e.getMessage().contains("Error semantico")) {
                System.err.println("Tipo: Error Semantico");
            } else {
                System.err.println("Tipo: Error General");
            }

        } catch (IOException e) {
            //Manejo de errores de archivo
            System.err.println("\n ERROR DE ARCHIVO:");
            System.err.println("No se pudo leer el archivo: " + e.getMessage());
            System.err.println("Verifica que el archivo existe y tiene permisos de lectura.");

        } catch (Exception e) {
            //Manejo de errores inesperados
            System.err.println("\n ERROR INESPERADO:");
            System.err.println(e.getMessage());
            e.printStackTrace();
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
