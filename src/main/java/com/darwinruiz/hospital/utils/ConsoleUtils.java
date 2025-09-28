package com.darwinruiz.hospital.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Utilidades para entrada y validación de datos en consola
 */
public class ConsoleUtils {
    
    private static final Scanner scanner = new Scanner(System.in);
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9]+([.-][a-zA-Z0-9]+)*\\.[a-zA-Z]{2,}$"
    );
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static String leerTexto(String mensaje) {
        System.out.print(mensaje + ": ");
        return scanner.nextLine().trim();
    }

    public static String leerTextoNoVacio(String mensaje) {
        String texto;
        do {
            texto = leerTexto(mensaje);
            if (texto.isEmpty()) {
                System.out.println("Error: El campo no puede estar vacío.");
            }
        } while (texto.isEmpty());
        return texto;
    }

    public static int leerEntero(String mensaje) {
        while (true) {
            try {
                System.out.print(mensaje + ": ");
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Error: Debe ingresar un número válido.");
            }
        }
    }

    public static int leerEnteroEnRango(String mensaje, int min, int max) {
        int numero;
        do {
            numero = leerEntero(mensaje);
            if (numero < min || numero > max) {
                System.out.printf("Error: El número debe estar entre %d y %d.%n", min, max);
            }
        } while (numero < min || numero > max);
        return numero;
    }

    public static String leerEmail(String mensaje) {
        String email;
        do {
            email = leerTextoNoVacio(mensaje);
            if (!esEmailValido(email)) {
                System.out.println("Error: El formato del email no es válido.");
            }
        } while (!esEmailValido(email));
        return email;
    }

    public static LocalDate leerFecha(String mensaje) {
        while (true) {
            try {
                String fechaStr = leerTextoNoVacio(mensaje + " (dd/MM/yyyy)");
                return LocalDate.parse(fechaStr, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Error: Formato de fecha inválido. Use dd/MM/yyyy");
            }
        }
    }

    public static LocalDate leerFechaFutura(String mensaje) {
        LocalDate fecha;
        do {
            fecha = leerFecha(mensaje);
            if (!esFechaFutura(fecha)) {
                System.out.println("Error: La fecha debe ser futura.");
            }
        } while (!esFechaFutura(fecha));
        return fecha;
    }

    public static LocalDateTime leerFechaHora(String mensaje) {
        while (true) {
            try {
                String fechaHoraStr = leerTextoNoVacio(mensaje + " (dd/MM/yyyy HH:mm)");
                return LocalDateTime.parse(fechaHoraStr, DATETIME_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Error: Formato de fecha y hora inválido. Use dd/MM/yyyy HH:mm");
            }
        }
    }

    public static LocalDateTime leerFechaHoraFutura(String mensaje) {
        LocalDateTime fechaHora;
        do {
            fechaHora = leerFechaHora(mensaje);
            if (!esFechaHoraFutura(fechaHora)) {
                System.out.println("Error: La fecha y hora debe ser futura.");
            }
        } while (!esFechaHoraFutura(fechaHora));
        return fechaHora;
    }

    public static boolean esEmailValido(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean esFechaFutura(LocalDate fecha) {
        return fecha != null && fecha.isAfter(LocalDate.now());
    }

    public static boolean esFechaHoraFutura(LocalDateTime fechaHora) {
        return fechaHora != null && fechaHora.isAfter(LocalDateTime.now());
    }

    public static boolean confirmar(String mensaje) {
        String respuesta = leerTexto(mensaje + " (s/n)").toLowerCase();
        return respuesta.equals("s") || respuesta.equals("si") || respuesta.equals("sí");
    }

    public static void pausar() {
        System.out.print("Presione Enter para continuar...");
        scanner.nextLine();
    }

    public static void limpiarPantalla() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }

    public static void mostrarError(String mensaje) {
        System.out.println("❌ ERROR: " + mensaje);
    }

    public static void mostrarExito(String mensaje) {
        System.out.println("✅ ÉXITO: " + mensaje);
    }

    public static void mostrarInfo(String mensaje) {
        System.out.println("INFO: " + mensaje);
    }

    public static void mostrarAdvertencia(String mensaje) {
        System.out.println("⚠️ ADVERTENCIA: " + mensaje);
    }

    public static String formatearFecha(LocalDate fecha) {
        return fecha != null ? fecha.format(DATE_FORMATTER) : "N/A";
    }

    public static String formatearFechaHora(LocalDateTime fechaHora) {
        return fechaHora != null ? fechaHora.format(DATETIME_FORMATTER) : "N/A";
    }
}