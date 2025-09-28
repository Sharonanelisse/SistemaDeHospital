package com.darwinruiz.hospital.repositories;

import com.darwinruiz.hospital.models.HistorialMedico;
import com.darwinruiz.hospital.models.Paciente;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para HistorialMedicoRepository.
 * Verifica consultas por paciente y búsquedas en contenido médico.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HistorialMedicoRepositoryTest {
    
    private static HistorialMedicoRepository historialRepository;
    private static PacienteRepository pacienteRepository;
    
    private static Paciente paciente1;
    private static Paciente paciente2;
    private static Paciente paciente3;
    private static HistorialMedico historial1;
    private static HistorialMedico historial2;
    private static HistorialMedico historial3;
    
    @BeforeAll
    static void setUp() {
        historialRepository = new HistorialMedicoRepository();
        pacienteRepository = new PacienteRepository();
        
        // Crear pacientes de prueba
        paciente1 = new Paciente(
            "Elena Morales",
            "6666666666666",
            LocalDate.of(1982, 4, 15),
            "66666666",
            "elena.morales@test.com"
        );
        
        paciente2 = new Paciente(
            "Miguel Torres",
            "7777777777777",
            LocalDate.of(1975, 9, 30),
            "77777777",
            "miguel.torres@test.com"
        );
        
        paciente3 = new Paciente(
            "Sofia Herrera",
            "8888888888888",
            LocalDate.of(1990, 11, 8),
            "88888888",
            "sofia.herrera@test.com"
        );
        
        // Persistir pacientes
        pacienteRepository.persist(paciente1);
        pacienteRepository.persist(paciente2);
        pacienteRepository.persist(paciente3);
        
        // Crear historiales médicos
        historial1 = new HistorialMedico(
            paciente1,
            "Alergia a penicilina, mariscos",
            "Hipertensión arterial, diabetes tipo 2",
            "Paciente colaboradora, sigue tratamiento regularmente"
        );
        
        historial2 = new HistorialMedico(
            paciente2,
            "Alergia a aspirina",
            "Antecedentes de infarto miocardio en 2020",
            "Requiere seguimiento cardiológico estricto"
        );
        
        historial3 = new HistorialMedico(
            paciente3,
            "", // Sin alergias
            "", // Sin antecedentes
            ""  // Sin observaciones - historial vacío
        );
        
        // Persistir historiales
        historialRepository.persist(historial1);
        historialRepository.persist(historial2);
        historialRepository.persist(historial3);
    }
    
    @AfterAll
    static void tearDown() {
        // Limpiar datos de prueba
        try {
            historialRepository.remove(historial1);
            historialRepository.remove(historial2);
            historialRepository.remove(historial3);
            
            pacienteRepository.remove(paciente1);
            pacienteRepository.remove(paciente2);
            pacienteRepository.remove(paciente3);
        } catch (Exception e) {
            // Ignorar errores de limpieza
        }
        BaseRepository.closeEntityManagerFactory();
    }
    
    @Test
    @Order(1)
    @DisplayName("Debe buscar historial médico por ID del paciente")
    void testFindByPacienteId() {
        // When
        Optional<HistorialMedico> resultado = historialRepository.findByPacienteId(paciente1.getId());
        
        // Then
        assertTrue(resultado.isPresent());
        HistorialMedico historial = resultado.get();
        
        assertEquals(paciente1.getId(), historial.getPaciente().getId());
        assertEquals("Alergia a penicilina, mariscos", historial.getAlergias());
        assertEquals("Hipertensión arterial, diabetes tipo 2", historial.getAntecedentes());
        assertTrue(historial.getObservaciones().contains("colaboradora"));
    }
    
    @Test
    @Order(2)
    @DisplayName("Debe verificar existencia de historial por paciente ID")
    void testExistsByPacienteId() {
        // When & Then
        assertTrue(historialRepository.existsByPacienteId(paciente1.getId()));
        assertTrue(historialRepository.existsByPacienteId(paciente2.getId()));
        assertTrue(historialRepository.existsByPacienteId(paciente3.getId()));
        
        assertFalse(historialRepository.existsByPacienteId(99999L));
        assertFalse(historialRepository.existsByPacienteId(null));
    }
    
    @Test
    @Order(3)
    @DisplayName("Debe obtener todos los historiales con información del paciente")
    void testFindAllWithPaciente() {
        // When
        List<HistorialMedico> historiales = historialRepository.findAllWithPaciente();
        
        // Then
        assertNotNull(historiales);
        assertFalse(historiales.isEmpty());
        
        // Verificar que las relaciones están cargadas
        for (HistorialMedico historial : historiales) {
            assertNotNull(historial.getPaciente());
            assertNotNull(historial.getPaciente().getNombre());
        }
        
        // Verificar que incluye nuestros historiales de prueba
        assertTrue(historiales.stream().anyMatch(h -> 
            h.getPaciente().getDpi().equals("6666666666666")));
        assertTrue(historiales.stream().anyMatch(h -> 
            h.getPaciente().getDpi().equals("7777777777777")));
        
        // Verificar ordenamiento por nombre del paciente
        for (int i = 0; i < historiales.size() - 1; i++) {
            String nombre1 = historiales.get(i).getPaciente().getNombre();
            String nombre2 = historiales.get(i + 1).getPaciente().getNombre();
            assertTrue(nombre1.compareTo(nombre2) <= 0);
        }
    }
    
    @Test
    @Order(4)
    @DisplayName("Debe buscar historiales por contenido en alergias")
    void testFindByAlergiasContaining() {
        // When
        List<HistorialMedico> conPenicilina = historialRepository.findByAlergiasContaining("penicilina");
        List<HistorialMedico> conAspirina = historialRepository.findByAlergiasContaining("aspirina");
        List<HistorialMedico> conMariscos = historialRepository.findByAlergiasContaining("mariscos");
        
        // Then
        assertFalse(conPenicilina.isEmpty());
        assertTrue(conPenicilina.stream().anyMatch(h -> 
            h.getPaciente().getDpi().equals("6666666666666")));
        
        assertFalse(conAspirina.isEmpty());
        assertTrue(conAspirina.stream().anyMatch(h -> 
            h.getPaciente().getDpi().equals("7777777777777")));
        
        assertFalse(conMariscos.isEmpty());
        assertTrue(conMariscos.stream().anyMatch(h -> 
            h.getPaciente().getDpi().equals("6666666666666")));
        
        // Búsqueda case-insensitive
        List<HistorialMedico> conPenicilinaMayus = historialRepository.findByAlergiasContaining("PENICILINA");
        assertFalse(conPenicilinaMayus.isEmpty());
    }
    
    @Test
    @Order(5)
    @DisplayName("Debe buscar historiales por contenido en antecedentes")
    void testFindByAntecedentesContaining() {
        // When
        List<HistorialMedico> conHipertension = historialRepository.findByAntecedentesContaining("hipertensión");
        List<HistorialMedico> conDiabetes = historialRepository.findByAntecedentesContaining("diabetes");
        List<HistorialMedico> conInfarto = historialRepository.findByAntecedentesContaining("infarto");
        
        // Then
        assertFalse(conHipertension.isEmpty());
        assertTrue(conHipertension.stream().anyMatch(h -> 
            h.getPaciente().getDpi().equals("6666666666666")));
        
        assertFalse(conDiabetes.isEmpty());
        assertTrue(conDiabetes.stream().anyMatch(h -> 
            h.getPaciente().getDpi().equals("6666666666666")));
        
        assertFalse(conInfarto.isEmpty());
        assertTrue(conInfarto.stream().anyMatch(h -> 
            h.getPaciente().getDpi().equals("7777777777777")));
    }
    
    @Test
    @Order(6)
    @DisplayName("Debe buscar historiales por contenido en observaciones")
    void testFindByObservacionesContaining() {
        // When
        List<HistorialMedico> conColaboradora = historialRepository.findByObservacionesContaining("colaboradora");
        List<HistorialMedico> conSeguimiento = historialRepository.findByObservacionesContaining("seguimiento");
        List<HistorialMedico> conCardiologico = historialRepository.findByObservacionesContaining("cardiológico");
        
        // Then
        assertFalse(conColaboradora.isEmpty());
        assertTrue(conColaboradora.stream().anyMatch(h -> 
            h.getPaciente().getDpi().equals("6666666666666")));
        
        assertFalse(conSeguimiento.isEmpty());
        assertTrue(conSeguimiento.stream().anyMatch(h -> 
            h.getPaciente().getDpi().equals("7777777777777")));
        
        assertFalse(conCardiologico.isEmpty());
        assertTrue(conCardiologico.stream().anyMatch(h -> 
            h.getPaciente().getDpi().equals("7777777777777")));
    }
    
    @Test
    @Order(7)
    @DisplayName("Debe encontrar historiales con información médica")
    void testFindHistorialesConInformacion() {
        // When
        List<HistorialMedico> conInformacion = historialRepository.findHistorialesConInformacion();
        
        // Then
        assertNotNull(conInformacion);
        assertFalse(conInformacion.isEmpty());
        
        // Verificar que incluye historiales con información
        assertTrue(conInformacion.stream().anyMatch(h -> 
            h.getPaciente().getDpi().equals("6666666666666")));
        assertTrue(conInformacion.stream().anyMatch(h -> 
            h.getPaciente().getDpi().equals("7777777777777")));
        
        // Verificar que NO incluye el historial vacío
        assertFalse(conInformacion.stream().anyMatch(h -> 
            h.getPaciente().getDpi().equals("8888888888888")));
    }
    
    @Test
    @Order(8)
    @DisplayName("Debe encontrar historiales vacíos")
    void testFindHistorialesVacios() {
        // When
        List<HistorialMedico> vacios = historialRepository.findHistorialesVacios();
        
        // Then
        assertNotNull(vacios);
        assertFalse(vacios.isEmpty());
        
        // Verificar que incluye el historial vacío
        assertTrue(vacios.stream().anyMatch(h -> 
            h.getPaciente().getDpi().equals("8888888888888")));
        
        // Verificar que NO incluye historiales con información
        assertFalse(vacios.stream().anyMatch(h -> 
            h.getPaciente().getDpi().equals("6666666666666")));
        assertFalse(vacios.stream().anyMatch(h -> 
            h.getPaciente().getDpi().equals("7777777777777")));
    }
    
    @Test
    @Order(9)
    @DisplayName("Debe contar historiales con información")
    void testCountHistorialesConInformacion() {
        // When
        long count = historialRepository.countHistorialesConInformacion();
        
        // Then
        assertTrue(count >= 2); // Al menos historial1 e historial2 tienen información
    }
    
    @Test
    @Order(10)
    @DisplayName("Debe obtener historial por ID con información del paciente")
    void testFindByIdWithPaciente() {
        // When
        Optional<HistorialMedico> resultado = historialRepository.findByIdWithPaciente(historial1.getId());
        
        // Then
        assertTrue(resultado.isPresent());
        HistorialMedico historial = resultado.get();
        
        assertNotNull(historial.getPaciente());
        assertEquals("Elena Morales", historial.getPaciente().getNombre());
        assertEquals("6666666666666", historial.getPaciente().getDpi());
    }
    
    @Test
    @Order(11)
    @DisplayName("Debe buscar historial por DPI del paciente")
    void testFindByPacienteDpi() {
        // When
        Optional<HistorialMedico> resultado = historialRepository.findByPacienteDpi("7777777777777");
        
        // Then
        assertTrue(resultado.isPresent());
        HistorialMedico historial = resultado.get();
        
        assertEquals("Miguel Torres", historial.getPaciente().getNombre());
        assertTrue(historial.getAlergias().contains("aspirina"));
        assertTrue(historial.getAntecedentes().contains("infarto"));
    }
    
    @Test
    @Order(12)
    @DisplayName("Debe verificar método tieneInformacion de la entidad")
    void testTieneInformacionEntity() {
        // When
        Optional<HistorialMedico> historialConInfo = historialRepository.findByPacienteId(paciente1.getId());
        Optional<HistorialMedico> historialVacio = historialRepository.findByPacienteId(paciente3.getId());
        
        // Then
        assertTrue(historialConInfo.isPresent());
        assertTrue(historialConInfo.get().tieneInformacion());
        
        assertTrue(historialVacio.isPresent());
        assertFalse(historialVacio.get().tieneInformacion());
    }
    
    @Test
    @Order(13)
    @DisplayName("Debe manejar búsquedas con parámetros nulos o vacíos")
    void testBusquedasConParametrosInvalidos() {
        // When & Then
        assertFalse(historialRepository.findByPacienteId(null).isPresent());
        assertFalse(historialRepository.existsByPacienteId(null));
        
        assertTrue(historialRepository.findByAlergiasContaining(null).isEmpty());
        assertTrue(historialRepository.findByAlergiasContaining("").isEmpty());
        assertTrue(historialRepository.findByAlergiasContaining("   ").isEmpty());
        
        assertTrue(historialRepository.findByAntecedentesContaining(null).isEmpty());
        assertTrue(historialRepository.findByAntecedentesContaining("").isEmpty());
        
        assertTrue(historialRepository.findByObservacionesContaining(null).isEmpty());
        assertTrue(historialRepository.findByObservacionesContaining("").isEmpty());
        
        assertFalse(historialRepository.findByIdWithPaciente(null).isPresent());
        
        assertFalse(historialRepository.findByPacienteDpi(null).isPresent());
        assertFalse(historialRepository.findByPacienteDpi("").isPresent());
        assertFalse(historialRepository.findByPacienteDpi("   ").isPresent());
    }
}