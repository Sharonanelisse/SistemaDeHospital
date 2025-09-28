package com.darwinruiz.hospital.services;

import com.darwinruiz.hospital.models.HistorialMedico;
import com.darwinruiz.hospital.models.Paciente;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para HistorialMedicoService.
 * Verifica las operaciones CRUD y validaciones de negocio para historiales médicos.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HistorialMedicoServiceTest {
    
    private static EntityManagerFactory emf;
    private static HistorialMedicoService historialService;
    private static PacienteService pacienteService;
    private EntityManager em;
    
    // Datos de prueba
    private static Paciente pacientePrueba;
    private static final String NOMBRE_PACIENTE = "Juan Pérez";
    private static final String DPI_PACIENTE = "1234567890123";
    private static final LocalDate FECHA_NACIMIENTO = LocalDate.of(1990, 5, 15);
    private static final String TELEFONO_PACIENTE = "12345678";
    private static final String EMAIL_PACIENTE = "juan.perez@email.com";
    
    private static final String ALERGIAS_PRUEBA = "Penicilina, Polen";
    private static final String ANTECEDENTES_PRUEBA = "Hipertensión, Diabetes tipo 2";
    private static final String OBSERVACIONES_PRUEBA = "Paciente con control regular";
    
    @BeforeAll
    static void setUpClass() {
        emf = Persistence.createEntityManagerFactory("HospitalSystemPU");
        historialService = new HistorialMedicoService(emf);
        pacienteService = new PacienteService(emf);
        
        // Crear paciente de prueba
        pacientePrueba = pacienteService.registrarPaciente(
            NOMBRE_PACIENTE, DPI_PACIENTE, FECHA_NACIMIENTO, TELEFONO_PACIENTE, EMAIL_PACIENTE
        );
    }
    
    @AfterAll
    static void tearDownClass() {
        if (historialService != null) {
            historialService.close();
        }
        if (pacienteService != null) {
            pacienteService.close();
        }
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
    
    @BeforeEach
    void setUp() {
        em = emf.createEntityManager();
    }
    
    @AfterEach
    void tearDown() {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
    
    @Test
    @Order(1)
    @DisplayName("Crear historial médico - Caso exitoso")
    void testCrearHistorial_Exitoso() {
        // Arrange
        Long pacienteId = pacientePrueba.getId();
        
        // Act
        HistorialMedico historial = historialService.crearHistorial(
            pacienteId, ALERGIAS_PRUEBA, ANTECEDENTES_PRUEBA, OBSERVACIONES_PRUEBA
        );
        
        // Assert
        assertNotNull(historial);
        assertNotNull(historial.getId());
        assertEquals(pacienteId, historial.getId()); // @MapsId hace que el ID sea el mismo
        assertEquals(ALERGIAS_PRUEBA, historial.getAlergias());
        assertEquals(ANTECEDENTES_PRUEBA, historial.getAntecedentes());
        assertEquals(OBSERVACIONES_PRUEBA, historial.getObservaciones());
        assertNotNull(historial.getPaciente());
        assertEquals(pacientePrueba.getId(), historial.getPaciente().getId());
    }
    
    @Test
    @Order(2)
    @DisplayName("Crear historial médico - Paciente nulo")
    void testCrearHistorial_PacienteNulo() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> historialService.crearHistorial(null, ALERGIAS_PRUEBA, ANTECEDENTES_PRUEBA, OBSERVACIONES_PRUEBA)
        );
        
        assertEquals("El ID del paciente no puede ser nulo", exception.getMessage());
    }
    
    @Test
    @Order(3)
    @DisplayName("Crear historial médico - Paciente inexistente")
    void testCrearHistorial_PacienteInexistente() {
        // Arrange
        Long pacienteIdInexistente = 99999L;
        
        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> historialService.crearHistorial(
                pacienteIdInexistente, ALERGIAS_PRUEBA, ANTECEDENTES_PRUEBA, OBSERVACIONES_PRUEBA
            )
        );
        
        assertTrue(exception.getMessage().contains("No se encontró el paciente con ID: " + pacienteIdInexistente));
    }
    
    @Test
    @Order(4)
    @DisplayName("Crear historial médico - Ya existe historial")
    void testCrearHistorial_YaExisteHistorial() {
        // Arrange
        Long pacienteId = pacientePrueba.getId();
        
        // Act & Assert - Intentar crear un segundo historial para el mismo paciente
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> historialService.crearHistorial(
                pacienteId, "Nuevas alergias", "Nuevos antecedentes", "Nuevas observaciones"
            )
        );
        
        assertTrue(exception.getMessage().contains("Ya existe un historial médico para el paciente con ID: " + pacienteId));
    }
    
    @Test
    @Order(5)
    @DisplayName("Consultar historial médico - Caso exitoso")
    void testConsultarHistorial_Exitoso() {
        // Arrange
        Long pacienteId = pacientePrueba.getId();
        
        // Act
        Optional<HistorialMedico> historialOpt = historialService.consultarHistorial(pacienteId);
        
        // Assert
        assertTrue(historialOpt.isPresent());
        HistorialMedico historial = historialOpt.get();
        assertEquals(pacienteId, historial.getId());
        assertEquals(ALERGIAS_PRUEBA, historial.getAlergias());
        assertEquals(ANTECEDENTES_PRUEBA, historial.getAntecedentes());
        assertEquals(OBSERVACIONES_PRUEBA, historial.getObservaciones());
        assertNotNull(historial.getPaciente());
    }
    
    @Test
    @Order(6)
    @DisplayName("Consultar historial médico - Paciente nulo")
    void testConsultarHistorial_PacienteNulo() {
        // Act
        Optional<HistorialMedico> historialOpt = historialService.consultarHistorial(null);
        
        // Assert
        assertTrue(historialOpt.isEmpty());
    }
    
    @Test
    @Order(7)
    @DisplayName("Consultar historial médico - Paciente inexistente")
    void testConsultarHistorial_PacienteInexistente() {
        // Arrange
        Long pacienteIdInexistente = 99999L;
        
        // Act
        Optional<HistorialMedico> historialOpt = historialService.consultarHistorial(pacienteIdInexistente);
        
        // Assert
        assertTrue(historialOpt.isEmpty());
    }
    
    @Test
    @Order(8)
    @DisplayName("Consultar historial por DPI - Caso exitoso")
    void testConsultarHistorialPorDpi_Exitoso() {
        // Arrange
        String dpi = pacientePrueba.getDpi();
        
        // Act
        Optional<HistorialMedico> historialOpt = historialService.consultarHistorialPorDpi(dpi);
        
        // Assert
        assertTrue(historialOpt.isPresent());
        HistorialMedico historial = historialOpt.get();
        assertEquals(pacientePrueba.getId(), historial.getId());
        assertEquals(dpi, historial.getPaciente().getDpi());
    }
    
    @Test
    @Order(9)
    @DisplayName("Consultar historial por DPI - DPI nulo o vacío")
    void testConsultarHistorialPorDpi_DpiNuloOVacio() {
        // Act & Assert
        assertTrue(historialService.consultarHistorialPorDpi(null).isEmpty());
        assertTrue(historialService.consultarHistorialPorDpi("").isEmpty());
        assertTrue(historialService.consultarHistorialPorDpi("   ").isEmpty());
    }
    
    @Test
    @Order(10)
    @DisplayName("Actualizar historial médico - Caso exitoso")
    void testActualizarHistorial_Exitoso() {
        // Arrange
        Long pacienteId = pacientePrueba.getId();
        String nuevasAlergias = "Penicilina, Polen, Mariscos";
        String nuevosAntecedentes = "Hipertensión, Diabetes tipo 2, Colesterol alto";
        String nuevasObservaciones = "Paciente requiere seguimiento mensual";
        
        // Act
        HistorialMedico historialActualizado = historialService.actualizarHistorial(
            pacienteId, nuevasAlergias, nuevosAntecedentes, nuevasObservaciones
        );
        
        // Assert
        assertNotNull(historialActualizado);
        assertEquals(pacienteId, historialActualizado.getId());
        assertEquals(nuevasAlergias, historialActualizado.getAlergias());
        assertEquals(nuevosAntecedentes, historialActualizado.getAntecedentes());
        assertEquals(nuevasObservaciones, historialActualizado.getObservaciones());
        
        // Verificar que los cambios se persistieron
        Optional<HistorialMedico> historialConsultado = historialService.consultarHistorial(pacienteId);
        assertTrue(historialConsultado.isPresent());
        assertEquals(nuevasAlergias, historialConsultado.get().getAlergias());
        assertEquals(nuevosAntecedentes, historialConsultado.get().getAntecedentes());
        assertEquals(nuevasObservaciones, historialConsultado.get().getObservaciones());
    }
    
    @Test
    @Order(11)
    @DisplayName("Actualizar historial médico - Paciente nulo")
    void testActualizarHistorial_PacienteNulo() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> historialService.actualizarHistorial(null, "Alergias", "Antecedentes", "Observaciones")
        );
        
        assertEquals("El ID del paciente no puede ser nulo", exception.getMessage());
    }
    
    @Test
    @Order(12)
    @DisplayName("Actualizar historial médico - Historial inexistente")
    void testActualizarHistorial_HistorialInexistente() {
        // Arrange - Crear un nuevo paciente sin historial
        Paciente nuevoPaciente = pacienteService.registrarPaciente(
            "María García", "9876543210987", LocalDate.of(1985, 3, 20), "87654321", "maria.garcia@email.com"
        );
        
        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> historialService.actualizarHistorial(
                nuevoPaciente.getId(), "Alergias", "Antecedentes", "Observaciones"
            )
        );
        
        assertTrue(exception.getMessage().contains("No se encontró historial médico para el paciente con ID: " + nuevoPaciente.getId()));
    }
    
    @Test
    @Order(13)
    @DisplayName("Existe historial - Caso exitoso")
    void testExisteHistorial_Exitoso() {
        // Arrange
        Long pacienteId = pacientePrueba.getId();
        
        // Act & Assert
        assertTrue(historialService.existeHistorial(pacienteId));
    }
    
    @Test
    @Order(14)
    @DisplayName("Existe historial - Paciente nulo")
    void testExisteHistorial_PacienteNulo() {
        // Act & Assert
        assertFalse(historialService.existeHistorial(null));
    }
    
    @Test
    @Order(15)
    @DisplayName("Existe historial - Paciente sin historial")
    void testExisteHistorial_PacienteSinHistorial() {
        // Arrange - Crear un nuevo paciente sin historial
        Paciente pacienteSinHistorial = pacienteService.registrarPaciente(
            "Carlos López", "5555555555555", LocalDate.of(1992, 8, 10), "55555555", "carlos.lopez@email.com"
        );
        
        // Act & Assert
        assertFalse(historialService.existeHistorial(pacienteSinHistorial.getId()));
    }
    
    @Test
    @Order(16)
    @DisplayName("Crear o actualizar historial - Crear nuevo")
    void testCrearOActualizarHistorial_CrearNuevo() {
        // Arrange - Crear un nuevo paciente sin historial
        Paciente nuevoPaciente = pacienteService.registrarPaciente(
            "Ana Rodríguez", "1111111111111", LocalDate.of(1988, 12, 5), "11111111", "ana.rodriguez@email.com"
        );
        
        // Act
        HistorialMedico historial = historialService.crearOActualizarHistorial(
            nuevoPaciente.getId(), "Sin alergias", "Sin antecedentes", "Paciente sano"
        );
        
        // Assert
        assertNotNull(historial);
        assertEquals(nuevoPaciente.getId(), historial.getId());
        assertEquals("Sin alergias", historial.getAlergias());
        assertEquals("Sin antecedentes", historial.getAntecedentes());
        assertEquals("Paciente sano", historial.getObservaciones());
    }
    
    @Test
    @Order(17)
    @DisplayName("Crear o actualizar historial - Actualizar existente")
    void testCrearOActualizarHistorial_ActualizarExistente() {
        // Arrange
        Long pacienteId = pacientePrueba.getId();
        String nuevasAlergias = "Actualización de alergias";
        
        // Act
        HistorialMedico historial = historialService.crearOActualizarHistorial(
            pacienteId, nuevasAlergias, "Antecedentes actualizados", "Observaciones actualizadas"
        );
        
        // Assert
        assertNotNull(historial);
        assertEquals(pacienteId, historial.getId());
        assertEquals(nuevasAlergias, historial.getAlergias());
        assertEquals("Antecedentes actualizados", historial.getAntecedentes());
        assertEquals("Observaciones actualizadas", historial.getObservaciones());
    }
    
    @Test
    @Order(18)
    @DisplayName("Eliminar historial médico - Caso exitoso")
    void testEliminarHistorial_Exitoso() {
        // Arrange - Crear un paciente con historial para eliminar
        Paciente pacienteParaEliminar = pacienteService.registrarPaciente(
            "Pedro Martínez", "2222222222222", LocalDate.of(1975, 4, 25), "22222222", "pedro.martinez@email.com"
        );
        historialService.crearHistorial(
            pacienteParaEliminar.getId(), "Alergias test", "Antecedentes test", "Observaciones test"
        );
        
        // Verificar que existe
        assertTrue(historialService.existeHistorial(pacienteParaEliminar.getId()));
        
        // Act
        boolean eliminado = historialService.eliminarHistorial(pacienteParaEliminar.getId());
        
        // Assert
        assertTrue(eliminado);
        assertFalse(historialService.existeHistorial(pacienteParaEliminar.getId()));
    }
    
    @Test
    @Order(19)
    @DisplayName("Eliminar historial médico - Paciente nulo")
    void testEliminarHistorial_PacienteNulo() {
        // Act & Assert
        assertFalse(historialService.eliminarHistorial(null));
    }
    
    @Test
    @Order(20)
    @DisplayName("Eliminar historial médico - Historial inexistente")
    void testEliminarHistorial_HistorialInexistente() {
        // Arrange
        Long pacienteIdInexistente = 99999L;
        
        // Act & Assert
        assertFalse(historialService.eliminarHistorial(pacienteIdInexistente));
    }
    
    @Test
    @Order(21)
    @DisplayName("Validación de longitud de campos")
    void testValidacionLongitudCampos() {
        // Arrange - Crear un nuevo paciente
        Paciente pacienteValidacion = pacienteService.registrarPaciente(
            "Validación Test", "3333333333333", LocalDate.of(1980, 6, 15), "33333333", "validacion@email.com"
        );
        
        // Crear strings que excedan los límites
        String alergiasLargas = "A".repeat(501); // Excede 500 caracteres
        String antecedentesLargos = "B".repeat(1001); // Excede 1000 caracteres
        String observacionesLargas = "C".repeat(1001); // Excede 1000 caracteres
        
        // Act & Assert - Alergias largas
        RuntimeException exception1 = assertThrows(
            RuntimeException.class,
            () -> historialService.crearHistorial(pacienteValidacion.getId(), alergiasLargas, "Antecedentes", "Observaciones")
        );
        assertTrue(exception1.getMessage().contains("Las alergias no pueden tener más de 500 caracteres"));
        
        // Act & Assert - Antecedentes largos
        RuntimeException exception2 = assertThrows(
            RuntimeException.class,
            () -> historialService.crearHistorial(pacienteValidacion.getId(), "Alergias", antecedentesLargos, "Observaciones")
        );
        assertTrue(exception2.getMessage().contains("Los antecedentes no pueden tener más de 1000 caracteres"));
        
        // Act & Assert - Observaciones largas
        RuntimeException exception3 = assertThrows(
            RuntimeException.class,
            () -> historialService.crearHistorial(pacienteValidacion.getId(), "Alergias", "Antecedentes", observacionesLargas)
        );
        assertTrue(exception3.getMessage().contains("Las observaciones no pueden tener más de 1000 caracteres"));
    }
}