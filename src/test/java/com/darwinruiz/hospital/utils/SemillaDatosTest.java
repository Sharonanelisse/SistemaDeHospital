package com.darwinruiz.hospital.utils;

import com.darwinruiz.hospital.models.Cita;
import com.darwinruiz.hospital.models.HistorialMedico;
import com.darwinruiz.hospital.models.Medico;
import com.darwinruiz.hospital.models.Paciente;
import com.darwinruiz.hospital.services.CitaService;
import com.darwinruiz.hospital.services.HistorialMedicoService;
import com.darwinruiz.hospital.services.MedicoService;
import com.darwinruiz.hospital.services.PacienteService;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para la funcionalidad de semilla de datos.
 * Verifica que los datos se carguen correctamente y se persistan en la base de datos.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SemillaDatosTest {
    
    private static EntityManagerFactory emf;
    private static SemillaDatos semillaDatos;
    private static PacienteService pacienteService;
    private static MedicoService medicoService;
    private static HistorialMedicoService historialMedicoService;
    private static CitaService citaService;
    
    @BeforeAll
    static void setUp() {
        emf = Persistence.createEntityManagerFactory("HospitalSystemTestPU");
        semillaDatos = new SemillaDatos(emf);
        pacienteService = new PacienteService(emf);
        medicoService = new MedicoService(emf);
        historialMedicoService = new HistorialMedicoService(emf);
        citaService = new CitaService(emf);
    }
    
    @AfterAll
    static void tearDown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
    
    @Test
    @Order(1)
    @DisplayName("Debe verificar que el sistema esté inicialmente vacío")
    void debeVerificarSistemaVacio() {
        // Given - Sistema recién iniciado
        
        // When
        boolean existenDatos = semillaDatos.existenDatos();
        
        // Then
        assertFalse(existenDatos, "El sistema debe estar vacío inicialmente");
    }
    
    @Test
    @Order(2)
    @DisplayName("Debe cargar datos de prueba exitosamente")
    void debeCargarDatosDePruebaExitosamente() {
        // Given - Sistema vacío
        
        // When
        assertDoesNotThrow(() -> {
            semillaDatos.cargarDatosDePrueba();
        }, "La carga de datos no debe lanzar excepciones");
        
        // Then
        assertTrue(semillaDatos.existenDatos(), "Después de cargar, deben existir datos");
    }
    
    @Test
    @Order(3)
    @DisplayName("Debe verificar que se cargaron pacientes correctamente")
    void debeVerificarPacientesCargados() {
        // Given - Datos ya cargados
        
        // When
        List<Paciente> pacientes = pacienteService.listarPacientes();
        
        // Then
        assertFalse(pacientes.isEmpty(), "Debe haber pacientes cargados");
        assertTrue(pacientes.size() >= 5, "Debe haber al menos 5 pacientes");
        
        // Verificar que los pacientes tienen datos válidos
        for (Paciente paciente : pacientes) {
            assertNotNull(paciente.getNombre(), "El paciente debe tener nombre");
            assertNotNull(paciente.getDpi(), "El paciente debe tener DPI");
            assertNotNull(paciente.getEmail(), "El paciente debe tener email");
            assertNotNull(paciente.getFechaNacimiento(), "El paciente debe tener fecha de nacimiento");
        }
    }
    
    @Test
    @Order(4)
    @DisplayName("Debe verificar que se cargaron médicos correctamente")
    void debeVerificarMedicosCargados() {
        // Given - Datos ya cargados
        
        // When
        List<Medico> medicos = medicoService.listarMedicos();
        
        // Then
        assertFalse(medicos.isEmpty(), "Debe haber médicos cargados");
        assertTrue(medicos.size() >= 5, "Debe haber al menos 5 médicos");
        
        // Verificar que los médicos tienen datos válidos
        for (Medico medico : medicos) {
            assertNotNull(medico.getNombre(), "El médico debe tener nombre");
            assertNotNull(medico.getColegiado(), "El médico debe tener número de colegiado");
            assertNotNull(medico.getEspecialidad(), "El médico debe tener especialidad");
            assertNotNull(medico.getEmail(), "El médico debe tener email");
        }
    }
    
    @Test
    @Order(5)
    @DisplayName("Debe verificar que se crearon historiales médicos")
    void debeVerificarHistorialesMedicosCargados() {
        // Given - Datos ya cargados
        List<Paciente> pacientes = pacienteService.listarPacientes();
        
        // When & Then
        int historialesEncontrados = 0;
        for (Paciente paciente : pacientes) {
            var historialOpt = historialMedicoService.consultarHistorial(paciente.getId());
            if (historialOpt.isPresent()) {
                historialesEncontrados++;
                HistorialMedico historial = historialOpt.get();
                
                // Verificar que el historial tiene al menos algunos datos
                assertTrue(
                    historial.getAlergias() != null || 
                    historial.getAntecedentes() != null || 
                    historial.getObservaciones() != null,
                    "El historial debe tener al menos un campo con datos"
                );
            }
        }
        
        assertTrue(historialesEncontrados > 0, "Debe haber al menos un historial médico creado");
    }
    
    @Test
    @Order(6)
    @DisplayName("Debe verificar que se crearon citas con diferentes estados")
    void debeVerificarCitasCargadas() {
        // Given - Datos ya cargados
        List<Paciente> pacientes = pacienteService.listarPacientes();
        
        // When
        int citasEncontradas = 0;
        boolean hayProgramadas = false;
        boolean hayAtendidas = false;
        boolean hayCanceladas = false;
        
        for (Paciente paciente : pacientes) {
            List<Cita> citas = citaService.listarCitasPorPaciente(paciente.getId());
            citasEncontradas += citas.size();
            
            for (Cita cita : citas) {
                switch (cita.getEstado()) {
                    case PROGRAMADA:
                        hayProgramadas = true;
                        break;
                    case ATENDIDA:
                        hayAtendidas = true;
                        break;
                    case CANCELADA:
                        hayCanceladas = true;
                        break;
                }
                
                // Verificar que la cita tiene datos válidos
                assertNotNull(cita.getFechaHora(), "La cita debe tener fecha y hora");
                assertNotNull(cita.getEstado(), "La cita debe tener estado");
                assertNotNull(cita.getPaciente(), "La cita debe tener paciente");
                assertNotNull(cita.getMedico(), "La cita debe tener médico");
            }
        }
        
        // Then
        assertTrue(citasEncontradas > 0, "Debe haber al menos una cita creada");
        assertTrue(hayProgramadas, "Debe haber al menos una cita programada");
        assertTrue(hayAtendidas, "Debe haber al menos una cita atendida");
        assertTrue(hayCanceladas, "Debe haber al menos una cita cancelada");
    }
    
    @Test
    @Order(7)
    @DisplayName("Debe obtener conteo actual correctamente")
    void debeObtenerConteoActual() {
        // Given - Datos ya cargados
        
        // When
        String conteo = semillaDatos.obtenerConteoActual();
        
        // Then
        assertNotNull(conteo, "El conteo no debe ser null");
        assertTrue(conteo.contains("Pacientes:"), "El conteo debe incluir información de pacientes");
        assertTrue(conteo.contains("Médicos:"), "El conteo debe incluir información de médicos");
        assertFalse(conteo.contains("0"), "El conteo no debe mostrar ceros después de cargar datos");
    }
    
    @Test
    @Order(8)
    @DisplayName("Debe manejar carga múltiple sin errores")
    void debeManejarCargaMultipleSinErrores() {
        // Given - Datos ya cargados previamente
        
        // When & Then - Intentar cargar datos nuevamente
        assertDoesNotThrow(() -> {
            semillaDatos.cargarDatosDePrueba();
        }, "La carga múltiple no debe lanzar excepciones");
        
        // Verificar que los datos siguen siendo válidos
        assertTrue(semillaDatos.existenDatos(), "Los datos deben seguir existiendo");
    }
    
    @Test
    @Order(9)
    @DisplayName("Debe verificar integridad de relaciones JPA")
    void debeVerificarIntegridadRelacionesJPA() {
        // Given - Datos cargados
        List<Paciente> pacientes = pacienteService.listarPacientes();
        
        // When & Then
        for (Paciente paciente : pacientes) {
            // Verificar relación OneToMany con citas
            List<Cita> citas = citaService.listarCitasPorPaciente(paciente.getId());
            for (Cita cita : citas) {
                assertEquals(paciente.getId(), cita.getPaciente().getId(), 
                    "La cita debe estar correctamente asociada al paciente");
                assertNotNull(cita.getMedico(), "La cita debe tener un médico asociado");
            }
            
            // Verificar relación OneToOne con historial médico
            var historialOpt = historialMedicoService.consultarHistorial(paciente.getId());
            if (historialOpt.isPresent()) {
                HistorialMedico historial = historialOpt.get();
                assertEquals(paciente.getId(), historial.getId(), 
                    "El historial debe tener el mismo ID que el paciente");
            }
        }
    }
}