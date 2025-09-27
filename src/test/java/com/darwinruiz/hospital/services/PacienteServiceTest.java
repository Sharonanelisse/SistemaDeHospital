package com.darwinruiz.hospital.services;

import com.darwinruiz.hospital.exceptions.EmailInvalidoException;
import com.darwinruiz.hospital.exceptions.PacienteYaExisteException;
import com.darwinruiz.hospital.models.Paciente;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para PacienteService.
 * Verifica las validaciones de DPI único y formato de email.
 * Requerimientos: 1.2, 1.3
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PacienteServiceTest {
    
    private static EntityManagerFactory emf;
    private static PacienteService pacienteService;
    
    @BeforeAll
    static void setUpClass() {
        emf = Persistence.createEntityManagerFactory("HospitalSystemPU");
        pacienteService = new PacienteService(emf);
        
        // Limpiar datos de prueba previos
        limpiarDatosPrueba();
    }
    
    @AfterAll
    static void tearDownClass() {
        limpiarDatosPrueba();
        if (pacienteService != null) {
            pacienteService.close();
        }
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
    
    private static void limpiarDatosPrueba() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Cita c WHERE c.paciente.dpi LIKE 'TEST%'").executeUpdate();
            em.createQuery("DELETE FROM HistorialMedico h WHERE h.paciente.dpi LIKE 'TEST%'").executeUpdate();
            em.createQuery("DELETE FROM Paciente p WHERE p.dpi LIKE 'TEST%'").executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
    
    @Test
    @Order(1)
    @DisplayName("Debe registrar un paciente con datos válidos")
    void testRegistrarPacienteValido() {
        // Arrange
        String nombre = "Juan Pérez";
        String dpi = "TEST123456789";
        LocalDate fechaNacimiento = LocalDate.of(1990, 5, 15);
        String telefono = "12345678";
        String email = "juan.perez@email.com";
        
        // Act
        Paciente paciente = pacienteService.registrarPaciente(nombre, dpi, fechaNacimiento, telefono, email);
        
        // Assert
        assertNotNull(paciente);
        assertNotNull(paciente.getId());
        assertEquals(nombre, paciente.getNombre());
        assertEquals(dpi, paciente.getDpi());
        assertEquals(fechaNacimiento, paciente.getFechaNacimiento());
        assertEquals(telefono, paciente.getTelefono());
        assertEquals(email, paciente.getEmail());
    }
    
    @Test
    @Order(2)
    @DisplayName("Debe lanzar excepción al registrar paciente con DPI duplicado")
    void testRegistrarPacienteDpiDuplicado() {
        // Arrange
        String dpiDuplicado = "TEST123456789"; // Ya existe del test anterior
        
        // Act & Assert
        PacienteYaExisteException exception = assertThrows(
            PacienteYaExisteException.class,
            () -> pacienteService.registrarPaciente(
                "María García", 
                dpiDuplicado, 
                LocalDate.of(1985, 3, 20), 
                "87654321", 
                "maria.garcia@email.com"
            )
        );
        
        assertTrue(exception.getMessage().contains(dpiDuplicado));
    }
    
    @Test
    @Order(3)
    @DisplayName("Debe lanzar excepción con email inválido")
    void testRegistrarPacienteEmailInvalido() {
        // Arrange
        String emailInvalido = "email-invalido";
        
        // Act & Assert
        EmailInvalidoException exception = assertThrows(
            EmailInvalidoException.class,
            () -> pacienteService.registrarPaciente(
                "Carlos López", 
                "TEST987654321", 
                LocalDate.of(1992, 8, 10), 
                "11223344", 
                emailInvalido
            )
        );
        
        assertTrue(exception.getMessage().contains(emailInvalido));
    }
    
    @Test
    @Order(4)
    @DisplayName("Debe lanzar excepción con datos nulos o vacíos")
    void testRegistrarPacienteDatosInvalidos() {
        LocalDate fechaValida = LocalDate.of(1990, 1, 1);
        
        // Nombre nulo
        assertThrows(IllegalArgumentException.class, 
            () -> pacienteService.registrarPaciente(null, "TEST111", fechaValida, "123", "test@email.com"));
        
        // Nombre vacío
        assertThrows(IllegalArgumentException.class, 
            () -> pacienteService.registrarPaciente("", "TEST222", fechaValida, "123", "test@email.com"));
        
        // DPI nulo
        assertThrows(IllegalArgumentException.class, 
            () -> pacienteService.registrarPaciente("Test", null, fechaValida, "123", "test@email.com"));
        
        // DPI vacío
        assertThrows(IllegalArgumentException.class, 
            () -> pacienteService.registrarPaciente("Test", "", fechaValida, "123", "test@email.com"));
        
        // Fecha nula
        assertThrows(IllegalArgumentException.class, 
            () -> pacienteService.registrarPaciente("Test", "TEST333", null, "123", "test@email.com"));
        
        // Email nulo
        assertThrows(IllegalArgumentException.class, 
            () -> pacienteService.registrarPaciente("Test", "TEST444", fechaValida, "123", null));
        
        // Email vacío
        assertThrows(IllegalArgumentException.class, 
            () -> pacienteService.registrarPaciente("Test", "TEST555", fechaValida, "123", ""));
    }
    
    @Test
    @Order(5)
    @DisplayName("Debe lanzar excepción con fecha de nacimiento futura")
    void testRegistrarPacienteFechaFutura() {
        // Arrange
        LocalDate fechaFutura = LocalDate.now().plusDays(1);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> pacienteService.registrarPaciente(
                "Test Futuro", 
                "TESTFUTURO", 
                fechaFutura, 
                "123", 
                "futuro@email.com"
            ));
    }
    
    @Test
    @Order(6)
    @DisplayName("Debe validar longitud máxima de campos")
    void testValidarLongitudCampos() {
        LocalDate fechaValida = LocalDate.of(1990, 1, 1);
        
        // Nombre muy largo (más de 100 caracteres)
        String nombreLargo = "A".repeat(101);
        assertThrows(IllegalArgumentException.class, 
            () -> pacienteService.registrarPaciente(nombreLargo, "TEST666", fechaValida, "123", "test@email.com"));
        
        // DPI muy largo (más de 20 caracteres)
        String dpiLargo = "TEST" + "1".repeat(20);
        assertThrows(IllegalArgumentException.class, 
            () -> pacienteService.registrarPaciente("Test", dpiLargo, fechaValida, "123", "test@email.com"));
        
        // Email muy largo (más de 100 caracteres)
        String emailLargo = "test" + "a".repeat(100) + "@email.com";
        assertThrows(IllegalArgumentException.class, 
            () -> pacienteService.registrarPaciente("Test", "TEST777", fechaValida, "123", emailLargo));
    }
    
    @Test
    @Order(7)
    @DisplayName("Debe buscar paciente por DPI")
    void testBuscarPorDpi() {
        // Arrange - Registrar un paciente
        String dpi = "TESTBUSCAR123";
        pacienteService.registrarPaciente("Test Buscar", dpi, LocalDate.of(1990, 1, 1), "123", "buscar@email.com");
        
        // Act
        Optional<Paciente> pacienteEncontrado = pacienteService.buscarPorDpi(dpi);
        
        // Assert
        assertTrue(pacienteEncontrado.isPresent());
        assertEquals(dpi, pacienteEncontrado.get().getDpi());
        assertEquals("Test Buscar", pacienteEncontrado.get().getNombre());
    }
    
    @Test
    @Order(8)
    @DisplayName("Debe retornar vacío al buscar DPI inexistente")
    void testBuscarPorDpiInexistente() {
        // Act
        Optional<Paciente> paciente = pacienteService.buscarPorDpi("DPI_INEXISTENTE");
        
        // Assert
        assertFalse(paciente.isPresent());
    }
    
    @Test
    @Order(9)
    @DisplayName("Debe listar todos los pacientes")
    void testListarPacientes() {
        // Act
        List<Paciente> pacientes = pacienteService.listarPacientes();
        
        // Assert
        assertNotNull(pacientes);
        assertFalse(pacientes.isEmpty());
        
        // Verificar que contiene al menos los pacientes de prueba
        boolean contieneTestPaciente = pacientes.stream()
            .anyMatch(p -> p.getDpi().startsWith("TEST"));
        assertTrue(contieneTestPaciente);
    }
    
    @Test
    @Order(10)
    @DisplayName("Debe actualizar paciente existente")
    void testActualizarPaciente() {
        // Arrange - Registrar un paciente
        String dpiOriginal = "TESTACTUALIZAR";
        Paciente pacienteOriginal = pacienteService.registrarPaciente(
            "Original", dpiOriginal, LocalDate.of(1990, 1, 1), "123", "original@email.com");
        
        // Act - Actualizar datos
        String nuevoNombre = "Nombre Actualizado";
        String nuevoEmail = "actualizado@email.com";
        Paciente pacienteActualizado = pacienteService.actualizarPaciente(
            pacienteOriginal.getId(), nuevoNombre, dpiOriginal, 
            LocalDate.of(1990, 1, 1), "456", nuevoEmail);
        
        // Assert
        assertNotNull(pacienteActualizado);
        assertEquals(nuevoNombre, pacienteActualizado.getNombre());
        assertEquals(nuevoEmail, pacienteActualizado.getEmail());
        assertEquals("456", pacienteActualizado.getTelefono());
    }
    
    @Test
    @Order(11)
    @DisplayName("Debe eliminar paciente existente")
    void testEliminarPaciente() {
        // Arrange - Registrar un paciente
        Paciente paciente = pacienteService.registrarPaciente(
            "Para Eliminar", "TESTELIMINAR", LocalDate.of(1990, 1, 1), "123", "eliminar@email.com");
        
        // Act
        boolean eliminado = pacienteService.eliminarPaciente(paciente.getId());
        
        // Assert
        assertTrue(eliminado);
        
        // Verificar que ya no existe
        Optional<Paciente> pacienteBuscado = pacienteService.buscarPorId(paciente.getId());
        assertFalse(pacienteBuscado.isPresent());
    }
    
    @Test
    @Order(12)
    @DisplayName("Debe retornar false al eliminar paciente inexistente")
    void testEliminarPacienteInexistente() {
        // Act
        boolean eliminado = pacienteService.eliminarPaciente(99999L);
        
        // Assert
        assertFalse(eliminado);
    }
}