package com.darwinruiz.hospital.console;

import com.darwinruiz.hospital.models.Paciente;
import com.darwinruiz.hospital.services.PacienteService;
import com.darwinruiz.hospital.services.HistorialMedicoService;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para verificar la funcionalidad de gestión de historial médico en consola
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HistorialMedicoConsoleTest {
    
    private static EntityManagerFactory emf;
    private static PacienteService pacienteService;
    private static HistorialMedicoService historialService;
    private static HospitalConsoleApp consoleApp;
    
    @BeforeAll
    static void setUp() {
        emf = Persistence.createEntityManagerFactory("HospitalSystemPU");
        pacienteService = new PacienteService(emf);
        historialService = new HistorialMedicoService(emf);
        consoleApp = new HospitalConsoleApp();
    }
    
    @AfterAll
    static void tearDown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
    
    @Test
    @Order(1)
    @DisplayName("Verificar que la aplicación de consola se inicializa correctamente")
    void testConsoleAppInitialization() {
        assertNotNull(consoleApp, "La aplicación de consola debe inicializarse correctamente");
    }
    
    @Test
    @Order(2)
    @DisplayName("Verificar que se pueden listar pacientes para selección")
    void testListarPacientesParaSeleccion() {
        // Crear algunos pacientes de prueba
        Paciente paciente1 = pacienteService.registrarPaciente(
            "María González", "1111111111111", LocalDate.of(1985, 3, 20),
            "555-0001", "maria@test.com"
        );
        
        Paciente paciente2 = pacienteService.registrarPaciente(
            "Carlos López", "2222222222222", LocalDate.of(1978, 8, 10),
            "555-0002", "carlos@test.com"
        );
        
        // Verificar que se pueden listar
        List<Paciente> pacientes = pacienteService.listarPacientes();
        assertFalse(pacientes.isEmpty(), "Debe haber pacientes registrados");
        assertTrue(pacientes.size() >= 2, "Debe haber al menos 2 pacientes");
        
        // Verificar que los pacientes creados están en la lista
        boolean mariaEncontrada = pacientes.stream()
            .anyMatch(p -> p.getNombre().equals("María González"));
        boolean carlosEncontrado = pacientes.stream()
            .anyMatch(p -> p.getNombre().equals("Carlos López"));
        
        assertTrue(mariaEncontrada, "María González debe estar en la lista");
        assertTrue(carlosEncontrado, "Carlos López debe estar en la lista");
    }
    
    @Test
    @Order(3)
    @DisplayName("Verificar creación de historial médico")
    void testCrearHistorialMedico() {
        // Obtener un paciente existente
        List<Paciente> pacientes = pacienteService.listarPacientes();
        assertFalse(pacientes.isEmpty(), "Debe haber pacientes para crear historial");
        
        Paciente paciente = pacientes.get(0);
        
        // Verificar que no existe historial previo
        assertFalse(historialService.existeHistorial(paciente.getId()), 
            "No debe existir historial previo");
        
        // Crear historial médico
        var historial = historialService.crearHistorial(
            paciente.getId(),
            "Alergia a polen",
            "Asma infantil",
            "Paciente activo"
        );
        
        assertNotNull(historial, "El historial debe crearse correctamente");
        assertEquals(paciente.getId(), historial.getId(), "El ID debe coincidir con el del paciente");
        assertEquals("Alergia a polen", historial.getAlergias());
        assertEquals("Asma infantil", historial.getAntecedentes());
        assertEquals("Paciente activo", historial.getObservaciones());
    }
    
    @Test
    @Order(4)
    @DisplayName("Verificar actualización de historial médico existente")
    void testActualizarHistorialMedico() {
        // Obtener un paciente con historial
        List<Paciente> pacientes = pacienteService.listarPacientes();
        Paciente paciente = pacientes.get(0);
        
        // Verificar que existe historial
        assertTrue(historialService.existeHistorial(paciente.getId()), 
            "Debe existir historial para actualizar");
        
        // Actualizar historial
        var historialActualizado = historialService.actualizarHistorial(
            paciente.getId(),
            "Alergia a polen y ácaros",
            "Asma infantil, rinitis alérgica",
            "Paciente activo, usa inhalador"
        );
        
        assertNotNull(historialActualizado, "El historial debe actualizarse correctamente");
        assertEquals("Alergia a polen y ácaros", historialActualizado.getAlergias());
        assertEquals("Asma infantil, rinitis alérgica", historialActualizado.getAntecedentes());
        assertEquals("Paciente activo, usa inhalador", historialActualizado.getObservaciones());
    }
    
    @Test
    @Order(5)
    @DisplayName("Verificar consulta de historial médico")
    void testConsultarHistorialMedico() {
        // Obtener un paciente con historial
        List<Paciente> pacientes = pacienteService.listarPacientes();
        Paciente paciente = pacientes.get(0);
        
        // Consultar historial
        var historialOpt = historialService.consultarHistorial(paciente.getId());
        
        assertTrue(historialOpt.isPresent(), "Debe encontrar el historial");
        
        var historial = historialOpt.get();
        assertNotNull(historial.getAlergias(), "Debe tener alergias registradas");
        assertNotNull(historial.getAntecedentes(), "Debe tener antecedentes registrados");
        assertNotNull(historial.getObservaciones(), "Debe tener observaciones registradas");
    }
    
    @Test
    @Order(6)
    @DisplayName("Verificar función crear o actualizar historial")
    void testCrearOActualizarHistorial() {
        // Crear un nuevo paciente para esta prueba
        Paciente nuevoPaciente = pacienteService.registrarPaciente(
            "Ana Martínez", "3333333333333", LocalDate.of(1992, 12, 5),
            "555-0003", "ana@test.com"
        );
        
        // Verificar que no existe historial
        assertFalse(historialService.existeHistorial(nuevoPaciente.getId()));
        
        // Usar crear o actualizar (debe crear)
        var historial1 = historialService.crearOActualizarHistorial(
            nuevoPaciente.getId(),
            "Sin alergias conocidas",
            "Saludable",
            "Primera consulta"
        );
        
        assertNotNull(historial1, "Debe crear el historial");
        assertEquals("Sin alergias conocidas", historial1.getAlergias());
        
        // Usar crear o actualizar nuevamente (debe actualizar)
        var historial2 = historialService.crearOActualizarHistorial(
            nuevoPaciente.getId(),
            "Alergia a medicamentos",
            "Saludable, antecedente familiar de diabetes",
            "Segunda consulta, seguimiento"
        );
        
        assertNotNull(historial2, "Debe actualizar el historial");
        assertEquals("Alergia a medicamentos", historial2.getAlergias());
        assertEquals(historial1.getId(), historial2.getId(), "Debe ser el mismo historial");
    }
    
    @Test
    @Order(7)
    @DisplayName("Verificar validaciones de entrada de datos")
    void testValidacionesDatos() {
        // Crear paciente para pruebas
        Paciente paciente = pacienteService.registrarPaciente(
            "Pedro Validation", "4444444444444", LocalDate.of(1980, 6, 15),
            "555-0004", "pedro@test.com"
        );
        
        // Probar con datos que exceden límites
        String alergiasLargas = "A".repeat(501); // Excede 500 caracteres
        String antecedentesLargos = "B".repeat(1001); // Excede 1000 caracteres
        String observacionesLargas = "C".repeat(1001); // Excede 1000 caracteres
        
        // Estas operaciones deben fallar por validaciones en el modelo
        assertThrows(Exception.class, () -> {
            historialService.crearHistorial(paciente.getId(), alergiasLargas, "Normal", "Normal");
        }, "Debe fallar por alergias muy largas");
        
        // Probar con datos válidos
        assertDoesNotThrow(() -> {
            historialService.crearHistorial(
                paciente.getId(),
                "A".repeat(500), // Exactamente 500 caracteres
                "B".repeat(1000), // Exactamente 1000 caracteres
                "C".repeat(1000)  // Exactamente 1000 caracteres
            );
        }, "Debe funcionar con datos en el límite");
    }
}