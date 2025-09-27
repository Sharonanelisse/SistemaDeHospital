package com.darwinruiz.hospital.services;

import com.darwinruiz.hospital.enums.Especialidad;
import com.darwinruiz.hospital.enums.EstadoCita;
import com.darwinruiz.hospital.exceptions.EmailInvalidoException;
import com.darwinruiz.hospital.exceptions.MedicoYaExisteException;
import com.darwinruiz.hospital.models.Cita;
import com.darwinruiz.hospital.models.Medico;
import com.darwinruiz.hospital.models.Paciente;
import com.darwinruiz.hospital.services.CitaService;
import com.darwinruiz.hospital.services.PacienteService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para MedicoService.
 * Verifica las validaciones de colegiado único y formato de email.
 * Requerimiento: 2.2 - Validación de colegiado único
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MedicoServiceTest {
    
    private static EntityManagerFactory emf;
    private static MedicoService medicoService;
    
    @BeforeAll
    static void setUpClass() {
        emf = Persistence.createEntityManagerFactory("HospitalSystemPU");
        medicoService = new MedicoService(emf);
        
        // Limpiar datos de prueba previos
        limpiarDatosPrueba();
    }
    
    @AfterAll
    static void tearDownClass() {
        limpiarDatosPrueba();
        if (medicoService != null) {
            medicoService.close();
        }
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
    
    private static void limpiarDatosPrueba() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Cita c WHERE c.medico.colegiado LIKE 'TEST%'").executeUpdate();
            em.createQuery("DELETE FROM Medico m WHERE m.colegiado LIKE 'TEST%'").executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
    
    @Test
    @Order(1)
    @DisplayName("Debe registrar un médico con datos válidos")
    void testRegistrarMedicoValido() {
        // Arrange
        String nombre = "Dr. Juan Pérez";
        String colegiado = "TEST123456";
        Especialidad especialidad = Especialidad.CARDIOLOGIA;
        String email = "dr.juan@hospital.com";
        
        // Act
        Medico medico = medicoService.registrarMedico(nombre, colegiado, especialidad, email);
        
        // Assert
        assertNotNull(medico);
        assertNotNull(medico.getId());
        assertEquals(nombre, medico.getNombre());
        assertEquals(colegiado, medico.getColegiado());
        assertEquals(especialidad, medico.getEspecialidad());
        assertEquals(email, medico.getEmail());
    }
    
    @Test
    @Order(2)
    @DisplayName("Debe lanzar excepción al registrar médico con colegiado duplicado")
    void testRegistrarMedicoColegiadoDuplicado() {
        // Arrange
        String colegiadoDuplicado = "TEST123456"; // Ya existe del test anterior
        
        // Act & Assert
        MedicoYaExisteException exception = assertThrows(
            MedicoYaExisteException.class,
            () -> medicoService.registrarMedico(
                "Dra. María García", 
                colegiadoDuplicado, 
                Especialidad.NEUROLOGIA, 
                "dra.maria@hospital.com"
            )
        );
        
        assertTrue(exception.getMessage().contains(colegiadoDuplicado));
    }
    
    @Test
    @Order(3)
    @DisplayName("Debe lanzar excepción con email inválido")
    void testRegistrarMedicoEmailInvalido() {
        // Arrange
        String emailInvalido = "email-invalido";
        
        // Act & Assert
        EmailInvalidoException exception = assertThrows(
            EmailInvalidoException.class,
            () -> medicoService.registrarMedico(
                "Dr. Carlos López", 
                "TEST987654", 
                Especialidad.PEDIATRIA, 
                emailInvalido
            )
        );
        
        assertTrue(exception.getMessage().contains(emailInvalido));
    }
    
    @Test
    @Order(4)
    @DisplayName("Debe lanzar excepción con datos nulos o vacíos")
    void testRegistrarMedicoDatosInvalidos() {
        // Nombre nulo
        assertThrows(IllegalArgumentException.class, 
            () -> medicoService.registrarMedico(null, "TEST111", Especialidad.CARDIOLOGIA, "test@email.com"));
        
        // Nombre vacío
        assertThrows(IllegalArgumentException.class, 
            () -> medicoService.registrarMedico("", "TEST222", Especialidad.CARDIOLOGIA, "test@email.com"));
        
        // Colegiado nulo
        assertThrows(IllegalArgumentException.class, 
            () -> medicoService.registrarMedico("Dr. Test", null, Especialidad.CARDIOLOGIA, "test@email.com"));
        
        // Colegiado vacío
        assertThrows(IllegalArgumentException.class, 
            () -> medicoService.registrarMedico("Dr. Test", "", Especialidad.CARDIOLOGIA, "test@email.com"));
        
        // Especialidad nula
        assertThrows(IllegalArgumentException.class, 
            () -> medicoService.registrarMedico("Dr. Test", "TEST333", null, "test@email.com"));
        
        // Email nulo
        assertThrows(IllegalArgumentException.class, 
            () -> medicoService.registrarMedico("Dr. Test", "TEST444", Especialidad.CARDIOLOGIA, null));
        
        // Email vacío
        assertThrows(IllegalArgumentException.class, 
            () -> medicoService.registrarMedico("Dr. Test", "TEST555", Especialidad.CARDIOLOGIA, ""));
    }
    
    @Test
    @Order(5)
    @DisplayName("Debe validar longitud máxima de campos")
    void testValidarLongitudCampos() {
        // Nombre muy largo (más de 100 caracteres)
        String nombreLargo = "Dr. " + "A".repeat(100);
        assertThrows(IllegalArgumentException.class, 
            () -> medicoService.registrarMedico(nombreLargo, "TEST666", Especialidad.CARDIOLOGIA, "test@email.com"));
        
        // Colegiado muy largo (más de 20 caracteres)
        String colegiadoLargo = "TEST" + "1".repeat(20);
        assertThrows(IllegalArgumentException.class, 
            () -> medicoService.registrarMedico("Dr. Test", colegiadoLargo, Especialidad.CARDIOLOGIA, "test@email.com"));
        
        // Email muy largo (más de 100 caracteres)
        String emailLargo = "test" + "a".repeat(100) + "@email.com";
        assertThrows(IllegalArgumentException.class, 
            () -> medicoService.registrarMedico("Dr. Test", "TEST777", Especialidad.CARDIOLOGIA, emailLargo));
    }
    
    @Test
    @Order(6)
    @DisplayName("Debe buscar médico por colegiado")
    void testBuscarPorColegiado() {
        // Arrange - Registrar un médico
        String colegiado = "TESTBUSCAR123";
        medicoService.registrarMedico("Dr. Test Buscar", colegiado, Especialidad.DERMATOLOGIA, "buscar@hospital.com");
        
        // Act
        Optional<Medico> medicoEncontrado = medicoService.buscarPorColegiado(colegiado);
        
        // Assert
        assertTrue(medicoEncontrado.isPresent());
        assertEquals(colegiado, medicoEncontrado.get().getColegiado());
        assertEquals("Dr. Test Buscar", medicoEncontrado.get().getNombre());
        assertEquals(Especialidad.DERMATOLOGIA, medicoEncontrado.get().getEspecialidad());
    }
    
    @Test
    @Order(7)
    @DisplayName("Debe retornar vacío al buscar colegiado inexistente")
    void testBuscarPorColegiadoInexistente() {
        // Act
        Optional<Medico> medico = medicoService.buscarPorColegiado("COLEGIADO_INEXISTENTE");
        
        // Assert
        assertFalse(medico.isPresent());
    }
    
    @Test
    @Order(8)
    @DisplayName("Debe buscar médicos por especialidad")
    void testBuscarPorEspecialidad() {
        // Arrange - Registrar médicos con diferentes especialidades
        medicoService.registrarMedico("Dr. Cardio 1", "TESTCARDIO1", Especialidad.CARDIOLOGIA, "cardio1@hospital.com");
        medicoService.registrarMedico("Dr. Cardio 2", "TESTCARDIO2", Especialidad.CARDIOLOGIA, "cardio2@hospital.com");
        medicoService.registrarMedico("Dr. Neuro", "TESTNEURO1", Especialidad.NEUROLOGIA, "neuro@hospital.com");
        
        // Act
        List<Medico> cardiologos = medicoService.buscarPorEspecialidad(Especialidad.CARDIOLOGIA);
        List<Medico> neurologos = medicoService.buscarPorEspecialidad(Especialidad.NEUROLOGIA);
        
        // Assert
        assertNotNull(cardiologos);
        assertNotNull(neurologos);
        
        // Verificar que hay al menos 2 cardiólogos de prueba
        long cardiologosTest = cardiologos.stream()
            .filter(m -> m.getColegiado().startsWith("TESTCARDIO"))
            .count();
        assertEquals(2, cardiologosTest);
        
        // Verificar que hay al menos 1 neurólogo de prueba
        long neurologosTest = neurologos.stream()
            .filter(m -> m.getColegiado().startsWith("TESTNEURO"))
            .count();
        assertEquals(1, neurologosTest);
    }
    
    @Test
    @Order(9)
    @DisplayName("Debe listar todos los médicos")
    void testListarMedicos() {
        // Act
        List<Medico> medicos = medicoService.listarMedicos();
        
        // Assert
        assertNotNull(medicos);
        assertFalse(medicos.isEmpty());
        
        // Verificar que contiene al menos los médicos de prueba
        boolean contieneMedicoTest = medicos.stream()
            .anyMatch(m -> m.getColegiado().startsWith("TEST"));
        assertTrue(contieneMedicoTest);
    }
    
    @Test
    @Order(10)
    @DisplayName("Debe actualizar médico existente")
    void testActualizarMedico() {
        // Arrange - Registrar un médico
        String colegiadoOriginal = "TESTACTUALIZAR";
        Medico medicoOriginal = medicoService.registrarMedico(
            "Dr. Original", colegiadoOriginal, Especialidad.GINECOLOGIA, "original@hospital.com");
        
        // Act - Actualizar datos
        String nuevoNombre = "Dr. Nombre Actualizado";
        String nuevoEmail = "actualizado@hospital.com";
        Especialidad nuevaEspecialidad = Especialidad.TRAUMATOLOGIA;
        
        Medico medicoActualizado = medicoService.actualizarMedico(
            medicoOriginal.getId(), nuevoNombre, colegiadoOriginal, nuevaEspecialidad, nuevoEmail);
        
        // Assert
        assertNotNull(medicoActualizado);
        assertEquals(nuevoNombre, medicoActualizado.getNombre());
        assertEquals(nuevoEmail, medicoActualizado.getEmail());
        assertEquals(nuevaEspecialidad, medicoActualizado.getEspecialidad());
        assertEquals(colegiadoOriginal, medicoActualizado.getColegiado()); // No debe cambiar
    }
    
    @Test
    @Order(11)
    @DisplayName("Debe eliminar médico sin citas")
    void testEliminarMedicoSinCitas() {
        // Arrange - Registrar un médico
        Medico medico = medicoService.registrarMedico(
            "Dr. Para Eliminar", "TESTELIMINAR", Especialidad.OFTALMOLOGIA, "eliminar@hospital.com");
        
        // Act
        boolean eliminado = medicoService.eliminarMedico(medico.getId());
        
        // Assert
        assertTrue(eliminado);
        
        // Verificar que ya no existe
        Optional<Medico> medicoBuscado = medicoService.buscarPorId(medico.getId());
        assertFalse(medicoBuscado.isPresent());
    }
    
    @Test
    @Order(12)
    @DisplayName("Debe retornar false al eliminar médico inexistente")
    void testEliminarMedicoInexistente() {
        // Act
        boolean eliminado = medicoService.eliminarMedico(99999L);
        
        // Assert
        assertFalse(eliminado);
    }
    
    @Test
    @Order(13)
    @DisplayName("Debe validar todas las especialidades disponibles")
    void testEspecialidadesDisponibles() {
        // Arrange & Act - Registrar médicos con todas las especialidades
        Especialidad[] especialidades = Especialidad.values();
        
        for (int i = 0; i < especialidades.length; i++) {
            Especialidad esp = especialidades[i];
            String colegiado = "TESTESP" + i;
            String nombre = "Dr. " + esp.name();
            String email = "dr." + esp.name().toLowerCase() + "@hospital.com";
            
            // Act
            Medico medico = medicoService.registrarMedico(nombre, colegiado, esp, email);
            
            // Assert
            assertNotNull(medico);
            assertEquals(esp, medico.getEspecialidad());
        }
        
        // Verificar que se pueden buscar por cada especialidad
        for (Especialidad esp : especialidades) {
            List<Medico> medicosEsp = medicoService.buscarPorEspecialidad(esp);
            assertNotNull(medicosEsp);
            
            // Debe haber al menos uno de prueba
            boolean tieneEspecialista = medicosEsp.stream()
                .anyMatch(m -> m.getEspecialidad() == esp && m.getColegiado().startsWith("TESTESP"));
            assertTrue(tieneEspecialista, "No se encontró médico de especialidad: " + esp);
        }
    }
    
    @Test
    @Order(14)
    @DisplayName("Test de integración - Debe listar médicos con próximas citas correctamente")
    void testIntegracionListarMedicosConProximasCitas() {
        // Arrange - Crear médicos y citas para probar la funcionalidad completa
        
        // Registrar médicos de prueba
        Medico medico1 = medicoService.registrarMedico(
            "Dr. Integración 1", "TESTINT001", Especialidad.CARDIOLOGIA, "int1@hospital.com");
        Medico medico2 = medicoService.registrarMedico(
            "Dr. Integración 2", "TESTINT002", Especialidad.NEUROLOGIA, "int2@hospital.com");
        
        // Act - Listar médicos con próximas citas
        List<Medico> medicosConCitas = medicoService.listarMedicosConProximasCitas();
        
        // Assert
        assertNotNull(medicosConCitas);
        assertFalse(medicosConCitas.isEmpty());
        
        // Verificar que los médicos de prueba están en la lista
        boolean contieneMedico1 = medicosConCitas.stream()
            .anyMatch(m -> m.getColegiado().equals("TESTINT001"));
        boolean contieneMedico2 = medicosConCitas.stream()
            .anyMatch(m -> m.getColegiado().equals("TESTINT002"));
        
        assertTrue(contieneMedico1, "Debe incluir médico de integración 1");
        assertTrue(contieneMedico2, "Debe incluir médico de integración 2");
        
        // Verificar que la consulta no falla con médicos sin citas
        assertTrue(medicosConCitas.size() >= 2, "Debe incluir al menos los médicos de prueba");
    }
    
    @Test
    @Order(15)
    @DisplayName("Test de integración - Persistencia y recuperación de datos")
    void testIntegracionPersistenciaYRecuperacion() {
        // Arrange - Datos de prueba
        String nombre = "Dr. Persistencia Test";
        String colegiado = "TESTPERS001";
        Especialidad especialidad = Especialidad.TRAUMATOLOGIA;
        String email = "persistencia@hospital.com";
        
        // Act 1 - Registrar médico
        Medico medicoRegistrado = medicoService.registrarMedico(nombre, colegiado, especialidad, email);
        
        // Assert 1 - Verificar registro
        assertNotNull(medicoRegistrado);
        assertNotNull(medicoRegistrado.getId());
        
        // Act 2 - Buscar por colegiado
        Optional<Medico> medicoEncontrado = medicoService.buscarPorColegiado(colegiado);
        
        // Assert 2 - Verificar búsqueda
        assertTrue(medicoEncontrado.isPresent());
        assertEquals(medicoRegistrado.getId(), medicoEncontrado.get().getId());
        assertEquals(nombre, medicoEncontrado.get().getNombre());
        assertEquals(especialidad, medicoEncontrado.get().getEspecialidad());
        assertEquals(email, medicoEncontrado.get().getEmail());
        
        // Act 3 - Buscar por ID
        Optional<Medico> medicoPorId = medicoService.buscarPorId(medicoRegistrado.getId());
        
        // Assert 3 - Verificar búsqueda por ID
        assertTrue(medicoPorId.isPresent());
        assertEquals(medicoRegistrado.getId(), medicoPorId.get().getId());
        
        // Act 4 - Listar todos los médicos
        List<Medico> todosMedicos = medicoService.listarMedicos();
        
        // Assert 4 - Verificar que está en la lista
        boolean estaEnLista = todosMedicos.stream()
            .anyMatch(m -> m.getId().equals(medicoRegistrado.getId()));
        assertTrue(estaEnLista, "El médico registrado debe aparecer en la lista completa");
    }
    
    @Test
    @Order(16)
    @DisplayName("Test de integración - Validaciones de negocio con base de datos")
    void testIntegracionValidacionesNegocio() {
        // Arrange - Registrar un médico inicial
        String colegiadoExistente = "TESTVALID001";
        medicoService.registrarMedico(
            "Dr. Validación Inicial", colegiadoExistente, Especialidad.PEDIATRIA, "inicial@hospital.com");
        
        // Test 1 - Validación de colegiado único con base de datos
        MedicoYaExisteException exception1 = assertThrows(
            MedicoYaExisteException.class,
            () -> medicoService.registrarMedico(
                "Dr. Duplicado", colegiadoExistente, Especialidad.GINECOLOGIA, "duplicado@hospital.com"
            )
        );
        assertTrue(exception1.getMessage().contains(colegiadoExistente));
        
        // Test 2 - Validación de email inválido
        EmailInvalidoException exception2 = assertThrows(
            EmailInvalidoException.class,
            () -> medicoService.registrarMedico(
                "Dr. Email Malo", "TESTVALID002", Especialidad.DERMATOLOGIA, "email-sin-formato-valido"
            )
        );
        assertTrue(exception2.getMessage().contains("email-sin-formato-valido"));
        
        // Test 3 - Actualización con colegiado existente de otro médico
        Medico otroMedico = medicoService.registrarMedico(
            "Dr. Otro", "TESTVALID003", Especialidad.OFTALMOLOGIA, "otro@hospital.com");
        
        MedicoYaExisteException exception3 = assertThrows(
            MedicoYaExisteException.class,
            () -> medicoService.actualizarMedico(
                otroMedico.getId(), "Dr. Otro Actualizado", colegiadoExistente, 
                Especialidad.PSIQUIATRIA, "otro.actualizado@hospital.com"
            )
        );
        assertTrue(exception3.getMessage().contains(colegiadoExistente));
    }
    
    @Test
    @Order(17)
    @DisplayName("Test de integración completa - Médicos con citas programadas")
    void testIntegracionCompletaMedicosConCitas() {
        // Arrange - Crear servicios necesarios
        PacienteService pacienteService = new PacienteService(emf);
        CitaService citaService = new CitaService(emf);
        
        try {
            // Crear paciente de prueba
            Paciente paciente = pacienteService.registrarPaciente(
                "Paciente Test Citas", "DPICITAS001", 
                LocalDate.of(1985, 5, 15), "555-0123", "paciente.citas@email.com");
            
            // Crear médicos de prueba
            Medico medicoConCitas = medicoService.registrarMedico(
                "Dr. Con Citas", "TESTCITAS001", Especialidad.CARDIOLOGIA, "concitas@hospital.com");
            Medico medicoSinCitas = medicoService.registrarMedico(
                "Dr. Sin Citas", "TESTCITAS002", Especialidad.NEUROLOGIA, "sincitas@hospital.com");
            
            // Crear citas futuras para el primer médico
            LocalDateTime citaFutura1 = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime citaFutura2 = LocalDateTime.now().plusDays(2).withHour(14).withMinute(30).withSecond(0).withNano(0);
            
            Cita cita1 = citaService.agendarCita(paciente.getId(), medicoConCitas.getId(), citaFutura1, "Consulta cardiológica");
            Cita cita2 = citaService.agendarCita(paciente.getId(), medicoConCitas.getId(), citaFutura2, "Seguimiento");
            
            // Act - Listar médicos con próximas citas
            List<Medico> medicosConProximasCitas = medicoService.listarMedicosConProximasCitas();
            
            // Assert - Verificar que ambos médicos aparecen (con y sin citas)
            assertNotNull(medicosConProximasCitas);
            
            boolean contieneMedicoConCitas = medicosConProximasCitas.stream()
                .anyMatch(m -> m.getId().equals(medicoConCitas.getId()));
            boolean contieneMedicoSinCitas = medicosConProximasCitas.stream()
                .anyMatch(m -> m.getId().equals(medicoSinCitas.getId()));
            
            assertTrue(contieneMedicoConCitas, "Debe incluir médico con citas programadas");
            assertTrue(contieneMedicoSinCitas, "Debe incluir médico sin citas");
            
            // Verificar que las citas fueron creadas correctamente
            assertNotNull(cita1);
            assertNotNull(cita2);
            assertEquals(EstadoCita.PROGRAMADA, cita1.getEstado());
            assertEquals(EstadoCita.PROGRAMADA, cita2.getEstado());
            
        } finally {
            // Cleanup
            pacienteService.close();
            citaService.close();
        }
    }
}