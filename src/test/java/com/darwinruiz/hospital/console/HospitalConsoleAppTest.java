package com.darwinruiz.hospital.console;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para HospitalConsoleApp
 * Verifica la funcionalidad básica del menú y navegación
 */
public class HospitalConsoleAppTest {
    
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    
    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outputStream));
    }
    
    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }
    
    @Test
    void testInstanciacionAplicacion() {
        // Verificar que la aplicación se puede instanciar sin errores
        assertDoesNotThrow(() -> {
            HospitalConsoleApp app = new HospitalConsoleApp();
            assertNotNull(app);
        });
    }
    
    @Test
    void testMostrarBienvenida() {
        // Verificar que el mensaje de bienvenida se muestra correctamente
        HospitalConsoleApp app = new HospitalConsoleApp();
        
        // Usar reflexión para acceder al método privado para testing
        try {
            var method = HospitalConsoleApp.class.getDeclaredMethod("mostrarBienvenida");
            method.setAccessible(true);
            method.invoke(app);
            
            String output = outputStream.toString();
            assertTrue(output.contains("SISTEMA DE HOSPITAL"));
            assertTrue(output.contains("Gestión Médica Integral"));
            assertTrue(output.contains("Bienvenido"));
        } catch (Exception e) {
            fail("Error al probar mostrarBienvenida: " + e.getMessage());
        }
    }
    
    @Test
    void testMostrarMenuPrincipal() {
        // Verificar que el menú principal muestra todas las opciones requeridas
        HospitalConsoleApp app = new HospitalConsoleApp();
        
        try {
            var method = HospitalConsoleApp.class.getDeclaredMethod("mostrarMenuPrincipal");
            method.setAccessible(true);
            method.invoke(app);
            
            String output = outputStream.toString();
            
            // Verificar que todas las opciones del menú están presentes
            assertTrue(output.contains("MENÚ PRINCIPAL"));
            assertTrue(output.contains("1. Registrar paciente"));
            assertTrue(output.contains("2. Crear/editar historial médico"));
            assertTrue(output.contains("3. Registrar médico"));
            assertTrue(output.contains("4. Agendar cita"));
            assertTrue(output.contains("5. Cambiar estado de una cita"));
            assertTrue(output.contains("6. Consultas"));
            assertTrue(output.contains("7. Eliminar"));
            assertTrue(output.contains("8. Semilla de datos"));
            assertTrue(output.contains("9. Salir"));
        } catch (Exception e) {
            fail("Error al probar mostrarMenuPrincipal: " + e.getMessage());
        }
    }
    
    @Test
    void testMostrarMenuConsultas() {
        // Verificar que el submenú de consultas muestra las opciones correctas
        HospitalConsoleApp app = new HospitalConsoleApp();
        
        try {
            var method = HospitalConsoleApp.class.getDeclaredMethod("mostrarMenuConsultas");
            method.setAccessible(true);
            
            // Este test solo verifica que el método existe y puede ser llamado
            // La funcionalidad completa requiere entrada de usuario
            assertDoesNotThrow(() -> {
                // Solo verificamos que el método existe
                assertNotNull(method);
            });
        } catch (NoSuchMethodException e) {
            fail("El método mostrarMenuConsultas no existe");
        }
    }
    
    @Test
    void testMostrarMenuEliminacion() {
        // Verificar que el submenú de eliminación existe
        HospitalConsoleApp app = new HospitalConsoleApp();
        
        try {
            var method = HospitalConsoleApp.class.getDeclaredMethod("mostrarMenuEliminacion");
            method.setAccessible(true);
            
            // Este test solo verifica que el método existe
            assertDoesNotThrow(() -> {
                assertNotNull(method);
            });
        } catch (NoSuchMethodException e) {
            fail("El método mostrarMenuEliminacion no existe");
        }
    }
    
    @Test
    void testCerrarAplicacion() {
        // Verificar que el método de cierre existe y puede ser llamado
        HospitalConsoleApp app = new HospitalConsoleApp();
        
        try {
            var method = HospitalConsoleApp.class.getDeclaredMethod("cerrarAplicacion");
            method.setAccessible(true);
            
            assertDoesNotThrow(() -> {
                method.invoke(app);
            });
            
            String output = outputStream.toString();
            assertTrue(output.contains("HASTA LUEGO") || output.contains("Sistema de Hospital cerrado"));
        } catch (Exception e) {
            fail("Error al probar cerrarAplicacion: " + e.getMessage());
        }
    }
}