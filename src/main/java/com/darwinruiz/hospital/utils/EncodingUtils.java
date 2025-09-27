package com.darwinruiz.hospital.utils;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class EncodingUtils {

    public static void configurarConsola() {
        try {
            // Intentar configurar UTF-8 para la salida estándar
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
            System.setErr(new PrintStream(System.err, true, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // Si no se puede configurar UTF-8, continuar con la configuración por defecto
            System.err.println("Advertencia: No se pudo configurar UTF-8 para la consola");
        }
    }

    public static void printSafe(String texto) {
        try {
            System.out.println(texto);
        } catch (Exception e) {
            // Si hay problemas con caracteres especiales, usar versión ASCII
            String textoSeguro = texto
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("ñ", "n")
                .replace("É", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Ú", "U")
                .replace("Ñ", "N")
                .replace("↔", "<->")
                .replace("═", "=")
                .replace("─", "-");
            System.out.println(textoSeguro);
        }
    }
}