package com.darwinruiz.hospital.repositories;

import com.darwinruiz.hospital.enums.Especialidad;
import com.darwinruiz.hospital.enums.EstadoCita;
import com.darwinruiz.hospital.models.Cita;
import com.darwinruiz.hospital.models.Medico;
import com.darwinruiz.hospital.models.Paciente;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para CitaRepository.
 * Verifica consultas por fechas, relaciones y validaciones de conflictos de horario.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CitaRepositoryTest {
    
    private static CitaRepository citaRepository;
    private static PacienteRepository pacienteRepository;
    private static MedicoRepository medicoRepository;
    
    private static Paciente paciente1;
    private static Paciente paciente2;
    private static Medico medico1;
    private static Medico medico2;
    private static Cita cita1;
    private static Cita cita2;
    private static Cita cita3;
    
    @BeforeAll
    static void setUp() {
        citaRepository = new CitaRepository();
        pacienteRepository = new PacienteRepository();
        medicoRepository = new MedicoRepository();
        
        // Crear datos de prueba
        paciente1 = new Paciente(
            "Pedro Ramírez",
            "4444444444444",
            LocalDate.of(1975, 6, 20),
            "44444444",
            "pedro.ramirez@test.com"
        );
        
        paciente2 = new Paciente(
            "Laura Sánchez",
            "5555555555555",
            LocalDate.of(1988, 12, 5),
            "55555555",
            "laura.sanchez@test.com"
        );
        
        medico1 = new Medico(
            "Dr. Roberto Silva",
            "COL333333",
            Especialidad.CARDIOLOGIA,
            "dr.silva@test.com"
        );
        
        medico2 = new Medico(
            "Dra. Carmen Ruiz",
            "COL444444",
            Especialidad.DERMATOLOGIA,
            "dra.ruiz@test.com"
        );
        
        // Persistir entidades base
        pacienteRepository.persist(paciente1);
        pacienteRepository.persist(paciente2);
        medicoRepository.persist(medico1);
        medicoRepository.persist(medico2);
        
        // Crear citas de prueba
        LocalDateTime manana = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime pasadoManana = LocalDateTime.now().plusDays(2).withHour(14).withMinute(30).withSecond(0).withNano(0);
        LocalDateTime proximaSemana = LocalDateTime.now().plusDays(7).withHour(9).withMinute(0).withSecond(0).withNano(0);
        
        cita1 = new Cita(manana, "Consulta cardiológica", paciente1, medico1);
        cita2 = new Cita(pasadoManana, "Control dermatológico", paciente2, medico2);
        cita3 = new Cita(proximaSemana, EstadoCita.PROGRAMADA, "Seguimiento", paciente1, medico1);
        
        citaRepository.persist(cita1);
        citaRepository.persist(cita2);
        citaRepository.persist(cita3);
    }
    
    @AfterAll
    static void tearDown() {
        // Limpiar datos de prueba
        try {
            citaRepository.remove(cita1);
            citaRepository.remove(cita2);
            citaRepository.remove(cita3);
            
            pacienteRepository.remove(paciente1);
            pacienteRepository.remove(paciente2);
            medicoRepository.remove(medico1);
            medicoRepository.remove(medico2);
        } catch (Exception e) {
            // Ignorar errores de limpieza
        }
        BaseRepository.closeEntityManagerFactory();
    }
    
    @Test
    @Order(1)
    @DisplayName("Debe buscar citas por paciente ID")
    void testFindByPacienteId() {
        // When
        List<Cita> citas = citaRepository.findByPacienteId(paciente1.getId());
        
        // Then
        assertNotNull(citas);
        assertFalse(citas.isEmpty());
        assertTrue(citas.size() >= 2); // paciente1 tiene al menos 2 citas
        
        // Verificar que todas las citas pertenecen al paciente correcto
        assertTrue(citas.stream().allMatch(c -> c.getPaciente().getId().equals(paciente1.getId())));
        
        // Verificar que están ordenadas por fecha descendente
        for (int i = 0; i < citas.size() - 1; i++) {
            assertTrue(citas.get(i).getFechaHora().isAfter(citas.get(i + 1).getFechaHora()) ||
                      citas.get(i).getFechaHora().equals(citas.get(i + 1).getFechaHora()));
        }
    }
    
    @Test
    @Order(2)
    @DisplayName("Debe buscar próximas citas por médico ID")
    void testFindProximasCitasByMedicoId() {
        // When
        List<Cita> citas = citaRepository.findProximasCitasByMedicoId(medico1.getId());
        
        // Then
        assertNotNull(citas);
        assertFalse(citas.isEmpty());
        
        // Verificar que todas las citas pertenecen al médico correcto
        assertTrue(citas.stream().allMatch(c -> c.getMedico().getId().equals(medico1.getId())));
        
        // Verificar que todas las citas son futuras
        LocalDateTime ahora = LocalDateTime.now();
        assertTrue(citas.stream().allMatch(c -> c.getFechaHora().isAfter(ahora)));
        
        // Verificar que están ordenadas por fecha ascendente
        for (int i = 0; i < citas.size() - 1; i++) {
            assertTrue(citas.get(i).getFechaHora().isBefore(citas.get(i + 1).getFechaHora()) ||
                      citas.get(i).getFechaHora().equals(citas.get(i + 1).getFechaHora()));
        }
    }
    
    @Test
    @Order(3)
    @DisplayName("Debe buscar citas por rango de fechas usando BETWEEN")
    void testFindByRangoFechas() {
        // Given
        LocalDate hoy = LocalDate.now();
        LocalDate enUnaSemana = hoy.plusDays(7);
        
        // When
        List<Cita> citas = citaRepository.findByRangoFechas(hoy, enUnaSemana);
        
        // Then
        assertNotNull(citas);
        assertFalse(citas.isEmpty());
        
        // Verificar que todas las citas están en el rango
        LocalDateTime inicioRango = hoy.atStartOfDay();
        LocalDateTime finRango = enUnaSemana.atTime(23, 59, 59);
        
        for (Cita cita : citas) {
            assertTrue(cita.getFechaHora().isAfter(inicioRango) || cita.getFechaHora().equals(inicioRango));
            assertTrue(cita.getFechaHora().isBefore(finRango) || cita.getFechaHora().equals(finRango));
        }
        
        // Verificar que están ordenadas por fecha ascendente
        for (int i = 0; i < citas.size() - 1; i++) {
            assertTrue(citas.get(i).getFechaHora().isBefore(citas.get(i + 1).getFechaHora()) ||
                      citas.get(i).getFechaHora().equals(citas.get(i + 1).getFechaHora()));
        }
    }
    
    @Test
    @Order(4)
    @DisplayName("Debe buscar citas por estado")
    void testFindByEstado() {
        // When
        List<Cita> citasProgramadas = citaRepository.findByEstado(EstadoCita.PROGRAMADA);
        
        // Then
        assertNotNull(citasProgramadas);
        assertFalse(citasProgramadas.isEmpty());
        
        // Verificar que todas tienen el estado correcto
        assertTrue(citasProgramadas.stream().allMatch(c -> c.getEstado() == EstadoCita.PROGRAMADA));
        
        // Incluir nuestras citas de prueba
        assertTrue(citasProgramadas.stream().anyMatch(c -> c.getId().equals(cita1.getId())));
        assertTrue(citasProgramadas.stream().anyMatch(c -> c.getId().equals(cita2.getId())));
    }
    
    @Test
    @Order(5)
    @DisplayName("Debe detectar conflictos de horario correctamente")
    void testExisteConflictoHorario() {
        // Given - Usar la misma fecha/hora de cita1
        LocalDateTime fechaConflicto = cita1.getFechaHora();
        Long medicoId = medico1.getId();
        
        // When & Then
        // Debe detectar conflicto con cita existente
        assertTrue(citaRepository.existeConflictoHorario(medicoId, fechaConflicto));
        
        // No debe detectar conflicto si excluimos la cita actual
        assertFalse(citaRepository.existeConflictoHorario(medicoId, fechaConflicto, cita1.getId()));
        
        // No debe detectar conflicto en horario libre
        LocalDateTime horarioLibre = LocalDateTime.now().plusDays(20).withHour(15).withMinute(0);
        assertFalse(citaRepository.existeConflictoHorario(medicoId, horarioLibre));
        
        // No debe detectar conflicto para otro médico en el mismo horario
        assertFalse(citaRepository.existeConflictoHorario(medico2.getId(), fechaConflicto));
    }
    
    @Test
    @Order(6)
    @DisplayName("Debe obtener todas las citas con detalles completos")
    void testFindAllWithDetails() {
        // When
        List<Cita> citas = citaRepository.findAllWithDetails();
        
        // Then
        assertNotNull(citas);
        assertFalse(citas.isEmpty());
        
        // Verificar que las relaciones están cargadas
        for (Cita cita : citas) {
            assertNotNull(cita.getPaciente());
            assertNotNull(cita.getMedico());
            assertNotNull(cita.getPaciente().getNombre());
            assertNotNull(cita.getMedico().getNombre());
        }
        
        // Verificar que incluye nuestras citas de prueba
        assertTrue(citas.stream().anyMatch(c -> c.getId().equals(cita1.getId())));
        assertTrue(citas.stream().anyMatch(c -> c.getId().equals(cita2.getId())));
    }
    
    @Test
    @Order(7)
    @DisplayName("Debe buscar citas por médico y estado")
    void testFindByMedicoIdAndEstado() {
        // When
        List<Cita> citas = citaRepository.findByMedicoIdAndEstado(medico1.getId(), EstadoCita.PROGRAMADA);
        
        // Then
        assertNotNull(citas);
        assertFalse(citas.isEmpty());
        
        // Verificar filtros
        assertTrue(citas.stream().allMatch(c -> 
            c.getMedico().getId().equals(medico1.getId()) && 
            c.getEstado() == EstadoCita.PROGRAMADA
        ));
    }
    
    @Test
    @Order(8)
    @DisplayName("Debe buscar citas por paciente y estado")
    void testFindByPacienteIdAndEstado() {
        // When
        List<Cita> citas = citaRepository.findByPacienteIdAndEstado(paciente1.getId(), EstadoCita.PROGRAMADA);
        
        // Then
        assertNotNull(citas);
        assertFalse(citas.isEmpty());
        
        // Verificar filtros
        assertTrue(citas.stream().allMatch(c -> 
            c.getPaciente().getId().equals(paciente1.getId()) && 
            c.getEstado() == EstadoCita.PROGRAMADA
        ));
    }
    
    @Test
    @Order(9)
    @DisplayName("Debe contar citas por estado")
    void testCountByEstado() {
        // When
        long programadas = citaRepository.countByEstado(EstadoCita.PROGRAMADA);
        long atendidas = citaRepository.countByEstado(EstadoCita.ATENDIDA);
        long canceladas = citaRepository.countByEstado(EstadoCita.CANCELADA);
        
        // Then
        assertTrue(programadas >= 3); // Al menos nuestras 3 citas de prueba
        assertTrue(atendidas >= 0);
        assertTrue(canceladas >= 0);
    }
    
    @Test
    @Order(10)
    @DisplayName("Debe obtener cita por ID con detalles completos")
    void testFindByIdWithDetails() {
        // When
        Optional<Cita> resultado = citaRepository.findByIdWithDetails(cita1.getId());
        
        // Then
        assertTrue(resultado.isPresent());
        Cita cita = resultado.get();
        
        assertNotNull(cita.getPaciente());
        assertNotNull(cita.getMedico());
        assertEquals("Pedro Ramírez", cita.getPaciente().getNombre());
        assertEquals("Dr. Roberto Silva", cita.getMedico().getNombre());
    }
    
    @Test
    @Order(11)
    @DisplayName("Debe cambiar estado de cita y verificar conflictos")
    void testCambiarEstadoYVerificarConflictos() {
        // Given - Cambiar estado de una cita
        cita2.setEstado(EstadoCita.ATENDIDA);
        citaRepository.merge(cita2);
        
        // When - Verificar que ya no hay conflicto para esa fecha/hora
        boolean hayConflicto = citaRepository.existeConflictoHorario(
            medico2.getId(), 
            cita2.getFechaHora()
        );
        
        // Then - No debería haber conflicto porque la cita está ATENDIDA, no PROGRAMADA
        assertFalse(hayConflicto);
        
        // Restaurar estado para otros tests
        cita2.setEstado(EstadoCita.PROGRAMADA);
        citaRepository.merge(cita2);
    }
    
    @Test
    @Order(12)
    @DisplayName("Debe manejar búsquedas con parámetros nulos o vacíos")
    void testBusquedasConParametrosInvalidos() {
        // When & Then
        assertTrue(citaRepository.findByPacienteId(null).isEmpty());
        assertTrue(citaRepository.findProximasCitasByMedicoId(null).isEmpty());
        
        assertTrue(citaRepository.findByRangoFechas(null, LocalDate.now()).isEmpty());
        assertTrue(citaRepository.findByRangoFechas(LocalDate.now(), null).isEmpty());
        
        assertTrue(citaRepository.findByEstado(null).isEmpty());
        
        assertFalse(citaRepository.existeConflictoHorario(null, LocalDateTime.now()));
        assertFalse(citaRepository.existeConflictoHorario(medico1.getId(), null));
        
        assertTrue(citaRepository.findByMedicoIdAndEstado(null, EstadoCita.PROGRAMADA).isEmpty());
        assertTrue(citaRepository.findByMedicoIdAndEstado(medico1.getId(), null).isEmpty());
        
        assertTrue(citaRepository.findByPacienteIdAndEstado(null, EstadoCita.PROGRAMADA).isEmpty());
        assertTrue(citaRepository.findByPacienteIdAndEstado(paciente1.getId(), null).isEmpty());
        
        assertEquals(0, citaRepository.countByEstado(null));
        
        assertFalse(citaRepository.findByIdWithDetails(null).isPresent());
    }
}