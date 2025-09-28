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
 * Tests de integración para MedicoRepository.
 * Verifica métodos específicos de búsqueda por colegiado, especialidad y consultas con citas.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MedicoRepositoryTest {
    
    private static MedicoRepository medicoRepository;
    private static PacienteRepository pacienteRepository;
    private static CitaRepository citaRepository;
    
    private static Medico medico1;
    private static Medico medico2;
    private static Paciente paciente;
    
    @BeforeAll
    static void setUp() {
        medicoRepository = new MedicoRepository();
        pacienteRepository = new PacienteRepository();
        citaRepository = new CitaRepository();
        
        // Crear datos de prueba
        medico1 = new Medico(
            "Dr. Juan Pérez",
            "COL111111",
            Especialidad.CARDIOLOGIA,
            "dr.perez@test.com"
        );
        
        medico2 = new Medico(
            "Dra. María González",
            "COL222222",
            Especialidad.PEDIATRIA,
            "dra.gonzalez@test.com"
        );
        
        paciente = new Paciente(
            "Paciente Test",
            "3333333333333",
            LocalDate.of(1980, 1, 1),
            "33333333",
            "paciente.test@email.com"
        );
        
        // Persistir datos
        medicoRepository.persist(medico1);
        medicoRepository.persist(medico2);
        pacienteRepository.persist(paciente);
    }
    
    @AfterAll
    static void tearDown() {
        // Limpiar datos de prueba
        try {
            // Eliminar citas primero
            List<Cita> citas = citaRepository.findAll();
            for (Cita cita : citas) {
                if (cita.getMedico().getColegiado().startsWith("COL111") || 
                    cita.getMedico().getColegiado().startsWith("COL222")) {
                    citaRepository.remove(cita);
                }
            }
            
            // Eliminar médicos y paciente
            medicoRepository.remove(medico1);
            medicoRepository.remove(medico2);
            pacienteRepository.remove(paciente);
        } catch (Exception e) {
            // Ignorar errores de limpieza
        }
        BaseRepository.closeEntityManagerFactory();
    }
    
    @Test
    @Order(1)
    @DisplayName("Debe buscar médico por colegiado correctamente")
    void testFindByColegiado() {
        // When
        Optional<Medico> resultado = medicoRepository.findByColegiado("COL111111");
        
        // Then
        assertTrue(resultado.isPresent());
        assertEquals("Dr. Juan Pérez", resultado.get().getNombre());
        assertEquals(Especialidad.CARDIOLOGIA, resultado.get().getEspecialidad());
    }
    
    @Test
    @Order(2)
    @DisplayName("Debe verificar existencia por colegiado")
    void testExistsByColegiado() {
        // When & Then
        assertTrue(medicoRepository.existsByColegiado("COL111111"));
        assertTrue(medicoRepository.existsByColegiado("COL222222"));
        assertFalse(medicoRepository.existsByColegiado("COL999999"));
        assertFalse(medicoRepository.existsByColegiado(null));
        assertFalse(medicoRepository.existsByColegiado(""));
    }
    
    @Test
    @Order(3)
    @DisplayName("Debe buscar médicos por especialidad")
    void testFindByEspecialidad() {
        // When
        List<Medico> cardiologos = medicoRepository.findByEspecialidad(Especialidad.CARDIOLOGIA);
        List<Medico> pediatras = medicoRepository.findByEspecialidad(Especialidad.PEDIATRIA);
        
        // Then
        assertFalse(cardiologos.isEmpty());
        assertTrue(cardiologos.stream().allMatch(m -> m.getEspecialidad() == Especialidad.CARDIOLOGIA));
        
        assertFalse(pediatras.isEmpty());
        assertTrue(pediatras.stream().allMatch(m -> m.getEspecialidad() == Especialidad.PEDIATRIA));
        
        // Especialidad inexistente
        List<Medico> neurologos = medicoRepository.findByEspecialidad(Especialidad.NEUROLOGIA);
        assertTrue(neurologos.isEmpty());
    }
    
    @Test
    @Order(4)
    @DisplayName("Debe buscar médicos por nombre parcial")
    void testFindByNombreContaining() {
        // When
        List<Medico> resultados = medicoRepository.findByNombreContaining("Juan");
        
        // Then
        assertFalse(resultados.isEmpty());
        assertTrue(resultados.stream().anyMatch(m -> m.getNombre().contains("Juan")));
        
        // Búsqueda case-insensitive
        List<Medico> resultados2 = medicoRepository.findByNombreContaining("maría");
        assertFalse(resultados2.isEmpty());
        assertTrue(resultados2.stream().anyMatch(m -> m.getNombre().toLowerCase().contains("maría")));
    }
    
    @Test
    @Order(5)
    @DisplayName("Debe buscar médico por email")
    void testFindByEmail() {
        // When
        Optional<Medico> resultado = medicoRepository.findByEmail("dra.gonzalez@test.com");
        
        // Then
        assertTrue(resultado.isPresent());
        assertEquals("Dra. María González", resultado.get().getNombre());
        assertEquals("COL222222", resultado.get().getColegiado());
    }
    
    @Test
    @Order(6)
    @DisplayName("Debe crear citas y obtener médicos con próximas citas")
    void testFindAllWithProximasCitas() {
        // Given - Crear citas futuras
        LocalDateTime manana = LocalDateTime.now().plusDays(1);
        LocalDateTime pasadoManana = LocalDateTime.now().plusDays(2);
        
        Cita cita1 = new Cita(manana, "Consulta cardiológica", paciente, medico1);
        Cita cita2 = new Cita(pasadoManana, "Control pediátrico", paciente, medico2);
        
        citaRepository.persist(cita1);
        citaRepository.persist(cita2);
        
        // When
        List<Medico> medicos = medicoRepository.findAllWithProximasCitas();
        
        // Then
        assertNotNull(medicos);
        assertFalse(medicos.isEmpty());
        
        // Verificar que incluye nuestros médicos de prueba
        assertTrue(medicos.stream().anyMatch(m -> "COL111111".equals(m.getColegiado())));
        assertTrue(medicos.stream().anyMatch(m -> "COL222222".equals(m.getColegiado())));
    }
    
    @Test
    @Order(7)
    @DisplayName("Debe obtener médico con todas sus citas")
    void testFindByIdWithCitas() {
        // When
        Optional<Medico> resultado = medicoRepository.findByIdWithCitas(medico1.getId());
        
        // Then
        assertTrue(resultado.isPresent());
        Medico medico = resultado.get();
        assertNotNull(medico.getCitas());
        // Debería tener al menos una cita de las creadas anteriormente
        assertFalse(medico.getCitas().isEmpty());
    }
    
    @Test
    @Order(8)
    @DisplayName("Debe encontrar médicos con citas en fecha específica")
    void testFindMedicosWithCitasEnFecha() {
        // Given
        LocalDateTime inicioHoy = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime finHoy = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        
        // When
        List<Medico> medicos = medicoRepository.findMedicosWithCitasEnFecha(inicioHoy, finHoy.plusDays(2));
        
        // Then
        assertNotNull(medicos);
        // Debería incluir médicos que tienen citas en el rango (las creadas en test anterior)
        assertTrue(medicos.stream().anyMatch(m -> "COL111111".equals(m.getColegiado())));
    }
    
    @Test
    @Order(9)
    @DisplayName("Debe encontrar médicos disponibles en fecha/hora específica")
    void testFindMedicosDisponiblesEnFechaHora() {
        // Given - Una fecha/hora donde no hay citas programadas
        LocalDateTime fechaLibre = LocalDateTime.now().plusDays(10).withHour(10).withMinute(0);
        
        // When
        List<Medico> disponibles = medicoRepository.findMedicosDisponiblesEnFechaHora(fechaLibre);
        
        // Then
        assertNotNull(disponibles);
        // Todos los médicos deberían estar disponibles en esa fecha
        assertTrue(disponibles.stream().anyMatch(m -> "COL111111".equals(m.getColegiado())));
        assertTrue(disponibles.stream().anyMatch(m -> "COL222222".equals(m.getColegiado())));
    }
    
    @Test
    @Order(10)
    @DisplayName("Debe contar médicos por especialidad")
    void testCountByEspecialidad() {
        // When
        long cardiologos = medicoRepository.countByEspecialidad(Especialidad.CARDIOLOGIA);
        long pediatras = medicoRepository.countByEspecialidad(Especialidad.PEDIATRIA);
        long neurologos = medicoRepository.countByEspecialidad(Especialidad.NEUROLOGIA);
        
        // Then
        assertTrue(cardiologos >= 1);
        assertTrue(pediatras >= 1);
        assertEquals(0, neurologos);
    }
    
    @Test
    @Order(11)
    @DisplayName("Debe obtener médicos ordenados por citas programadas")
    void testFindMedicosOrderByCitasProgramadas() {
        // When
        List<Medico> medicos = medicoRepository.findMedicosOrderByCitasProgramadas();
        
        // Then
        assertNotNull(medicos);
        assertFalse(medicos.isEmpty());
        
        // Verificar que incluye nuestros médicos
        assertTrue(medicos.stream().anyMatch(m -> "COL111111".equals(m.getColegiado())));
        assertTrue(medicos.stream().anyMatch(m -> "COL222222".equals(m.getColegiado())));
    }
    
    @Test
    @Order(12)
    @DisplayName("Debe manejar búsquedas con parámetros nulos o vacíos")
    void testBusquedasConParametrosInvalidos() {
        // When & Then
        assertFalse(medicoRepository.findByColegiado(null).isPresent());
        assertFalse(medicoRepository.findByColegiado("").isPresent());
        assertFalse(medicoRepository.findByColegiado("   ").isPresent());
        
        assertTrue(medicoRepository.findByEspecialidad(null).isEmpty());
        assertTrue(medicoRepository.findByNombreContaining(null).isEmpty());
        assertTrue(medicoRepository.findByNombreContaining("").isEmpty());
        
        assertFalse(medicoRepository.findByEmail(null).isPresent());
        assertFalse(medicoRepository.findByEmail("").isPresent());
        
        assertFalse(medicoRepository.findByIdWithCitas(null).isPresent());
        
        assertTrue(medicoRepository.findMedicosWithCitasEnFecha(null, LocalDateTime.now()).isEmpty());
        assertTrue(medicoRepository.findMedicosWithCitasEnFecha(LocalDateTime.now(), null).isEmpty());
        
        assertTrue(medicoRepository.findMedicosDisponiblesEnFechaHora(null).isEmpty());
        
        assertEquals(0, medicoRepository.countByEspecialidad(null));
    }
}