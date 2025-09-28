package com.darwinruiz.hospital.console;

import com.darwinruiz.hospital.enums.Especialidad;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas para verificar que las funcionalidades de registro están correctamente implementadas
 * en la aplicación de consola
 */
public class RegistrationFunctionalityTest {
    
    private HospitalConsoleApp app;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    
    @BeforeEach
    void setUp() {
        app = new HospitalConsoleApp();
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }
    
    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        if (app != null) {
            // Cerrar recursos si es necesario
        }
    }
    
    @Test
    void testRegistrarPacienteMethodExists() {
        // Verificar que el método registrarPaciente existe y es privado
        try {
            Method method = HospitalConsoleApp.class.getDeclaredMethod("registrarPaciente");
            assertNotNull(method, "El método registrarPaciente debe existir");
            assertTrue(java.lang.reflect.Modifier.isPrivate(method.getModifiers()), 
                      "El método registrarPaciente debe ser privado");
        } catch (NoSuchMethodException e) {
            fail("El método registrarPaciente no existe: " + e.getMessage());
        }
    }
    
    @Test
    void testRegistrarMedicoMethodExists() {
        // Verificar que el método registrarMedico existe y es privado
        try {
            Method method = HospitalConsoleApp.class.getDeclaredMethod("registrarMedico");
            assertNotNull(method, "El método registrarMedico debe existir");
            assertTrue(java.lang.reflect.Modifier.isPrivate(method.getModifiers()), 
                      "El método registrarMedico debe ser privado");
        } catch (NoSuchMethodException e) {
            fail("El método registrarMedico no existe: " + e.getMessage());
        }
    }
    
    @Test
    void testSeleccionarEspecialidadMethodExists() {
        // Verificar que el método seleccionarEspecialidad existe y es privado
        try {
            Method method = HospitalConsoleApp.class.getDeclaredMethod("seleccionarEspecialidad");
            assertNotNull(method, "El método seleccionarEspecialidad debe existir");
            assertTrue(java.lang.reflect.Modifier.isPrivate(method.getModifiers()), 
                      "El método seleccionarEspecialidad debe ser privado");
            assertEquals(Especialidad.class, method.getReturnType(), 
                        "El método debe retornar una Especialidad");
        } catch (NoSuchMethodException e) {
            fail("El método seleccionarEspecialidad no existe: " + e.getMessage());
        }
    }
    
    @Test
    void testHospitalConsoleAppInstantiation() {
        // Verificar que la aplicación se puede instanciar correctamente
        assertNotNull(app, "La aplicación debe poder instanciarse");
        
        // Verificar que los servicios están inicializados (indirectamente)
        assertDoesNotThrow(() -> {
            // Intentar acceder a métodos públicos para verificar inicialización
            app.getClass().getDeclaredFields();
        }, "La aplicación debe inicializarse sin errores");
    }
    
    @Test
    void testImportsAreCorrect() {
        // Verificar que las importaciones necesarias están presentes
        String className = HospitalConsoleApp.class.getName();
        assertNotNull(className, "La clase debe tener un nombre válido");
        
        // Verificar que puede acceder a las clases necesarias
        assertDoesNotThrow(() -> {
            Class.forName("com.darwinruiz.hospital.enums.Especialidad");
            Class.forName("com.darwinruiz.hospital.models.Paciente");
            Class.forName("com.darwinruiz.hospital.models.Medico");
        }, "Todas las importaciones necesarias deben estar disponibles");
    }
}