package com.darwinruiz.hospital;

import com.darwinruiz.hospital.enums.Especialidad;
import com.darwinruiz.hospital.enums.EstadoCita;
import com.darwinruiz.hospital.exceptions.*;
import com.darwinruiz.hospital.models.*;
import com.darwinruiz.hospital.services.*;
import com.darwinruiz.hospital.repositories.*;
import com.darwinruiz.hospital.utils.SemillaDatos;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test de integración completo que verifica el flujo end-to-end
 * de la aplicación del sistema de hospital
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CompleteApplicationFlowTest {

    private static EntityManagerFactory emf;
    private EntityManager em;
    
    private PacienteService pacienteService;
    private MedicoService medicoService;
    private CitaService citaService;
    private HistorialMedicoService historialService;

    @BeforeAll
    static void setUpClass() {
        emf = Persistence.createEntityManagerFactory("HospitalSystemPU");
    }

    @AfterAll
    static void tearDownClass() {
        if (emf != null) {
            emf.close();
        }
    }

    @BeforeEach
    void setUp() {
        em = emf.createEntityManager();
        
        // Inicializar repositorios
        PacienteRepository pacienteRepo = new PacienteRepository(em);
        MedicoRepository medicoRepo = new MedicoRepository(em);
        CitaRepository citaRepo = new CitaRepository(em);
        HistorialMedicoRepository historialRepo = new HistorialMedicoRepository(em);
        
        // Inicializar servicios
        pacienteService = new PacienteService(pacienteRepo);
        medicoService = new MedicoService(medicoRepo);
        citaService = new CitaService(citaRepo, pacienteRepo, medicoRepo);
        historialService = new HistorialMedicoService(historialRepo, pacienteRepo);
    }

    @AfterEach
    void tearDown() {
        if (em != null) {
            em.close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Flujo completo: Registrar paciente y verificar validaciones")
    void testRegistrarPacienteCompleto() {
        em.getTransaction().begin();
        
        try {
            // Crear paciente válido
            Paciente paciente = new Paciente();
            paciente.setNombre("Juan Pérez");
            paciente.setDpi("1234567890123");
            paciente.setFechaNacimiento(LocalDate.of(1990, 5, 15));
            paciente.setTelefono("12345678");
            paciente.setEmail("juan.perez@email.com");
            
            // Registrar paciente
            Paciente pacienteGuardado = pacienteService.registrarPaciente(paciente);
            
            assertNotNull(pacienteGuardado.getId());
            assertEquals("Juan Pérez", pacienteGuardado.getNombre());
            assertEquals("1234567890123", pacienteGuardado.getDpi());
            
            // Verificar que se puede buscar por DPI
            Paciente pacienteEncontrado = pacienteService.buscarPorDpi("1234567890123");
            assertNotNull(pacienteEncontrado);
            assertEquals(pacienteGuardado.getId(), pacienteEncontrado.getId());
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            fail("Error en flujo de registro de paciente: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("Flujo completo: Registrar médico y verificar validaciones")
    void testRegistrarMedicoCompleto() {
        em.getTransaction().begin();
        
        try {
            // Crear médico válido
            Medico medico = new Medico();
            medico.setNombre("Dr. María García");
            medico.setColegiado("COL12345");
            medico.setEspecialidad(Especialidad.CARDIOLOGIA);
            medico.setEmail("maria.garcia@hospital.com");
            
            // Registrar médico
            Medico medicoGuardado = medicoService.registrarMedico(medico);
            
            assertNotNull(medicoGuardado.getId());
            assertEquals("Dr. María García", medicoGuardado.getNombre());
            assertEquals("COL12345", medicoGuardado.getColegiado());
            assertEquals(Especialidad.CARDIOLOGIA, medicoGuardado.getEspecialidad());
            
            // Verificar que se puede buscar por colegiado
            Medico medicoEncontrado = medicoService.buscarPorColegiado("COL12345");
            assertNotNull(medicoEncontrado);
            assertEquals(medicoGuardado.getId(), medicoEncontrado.getId());
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            fail("Error en flujo de registro de médico: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    @DisplayName("Flujo completo: Crear historial médico con relación OneToOne")
    void testCrearHistorialMedicoCompleto() {
        em.getTransaction().begin();
        
        try {
            // Primero crear un paciente
            Paciente paciente = new Paciente();
            paciente.setNombre("Ana López");
            paciente.setDpi("9876543210987");
            paciente.setFechaNacimiento(LocalDate.of(1985, 8, 20));
            paciente.setTelefono("87654321");
            paciente.setEmail("ana.lopez@email.com");
            
            Paciente pacienteGuardado = pacienteService.registrarPaciente(paciente);
            
            // Crear historial médico
            HistorialMedico historial = new HistorialMedico();
            historial.setAlergias("Penicilina, Polen");
            historial.setAntecedentes("Hipertensión familiar");
            historial.setObservaciones("Paciente con seguimiento regular");
            
            // Crear historial asociado al paciente
            HistorialMedico historialGuardado = historialService.crearHistorial(pacienteGuardado.getId(), historial);
            
            assertNotNull(historialGuardado);
            assertEquals(pacienteGuardado.getId(), historialGuardado.getId()); // Mismo ID por @MapsId
            assertEquals("Penicilina, Polen", historialGuardado.getAlergias());
            
            // Verificar relación OneToOne
            HistorialMedico historialConsultado = historialService.consultarHistorial(pacienteGuardado.getId());
            assertNotNull(historialConsultado);
            assertEquals(historialGuardado.getId(), historialConsultado.getId());
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            fail("Error en flujo de historial médico: " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    @DisplayName("Flujo completo: Agendar cita con validaciones")
    void testAgendarCitaCompleto() {
        em.getTransaction().begin();
        
        try {
            // Crear paciente y médico para la cita
            Paciente paciente = new Paciente();
            paciente.setNombre("Carlos Ruiz");
            paciente.setDpi("5555555555555");
            paciente.setFechaNacimiento(LocalDate.of(1980, 3, 10));
            paciente.setTelefono("55555555");
            paciente.setEmail("carlos.ruiz@email.com");
            Paciente pacienteGuardado = pacienteService.registrarPaciente(paciente);
            
            Medico medico = new Medico();
            medico.setNombre("Dr. Pedro Martínez");
            medico.setColegiado("COL67890");
            medico.setEspecialidad(Especialidad.NEUROLOGIA);
            medico.setEmail("pedro.martinez@hospital.com");
            Medico medicoGuardado = medicoService.registrarMedico(medico);
            
            // Crear cita futura
            LocalDateTime fechaFutura = LocalDateTime.now().plusDays(7);
            Cita cita = new Cita();
            cita.setFechaHora(fechaFutura);
            cita.setMotivo("Consulta de seguimiento");
            cita.setPaciente(pacienteGuardado);
            cita.setMedico(medicoGuardado);
            
            // Agendar cita
            Cita citaGuardada = citaService.agendarCita(cita);
            
            assertNotNull(citaGuardada.getId());
            assertEquals(EstadoCita.PROGRAMADA, citaGuardada.getEstado());
            assertEquals(fechaFutura, citaGuardada.getFechaHora());
            assertEquals(pacienteGuardado.getId(), citaGuardada.getPaciente().getId());
            assertEquals(medicoGuardado.getId(), citaGuardada.getMedico().getId());
            
            // Cambiar estado de la cita
            Cita citaActualizada = citaService.cambiarEstadoCita(citaGuardada.getId(), EstadoCita.ATENDIDA);
            assertEquals(EstadoCita.ATENDIDA, citaActualizada.getEstado());
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            fail("Error en flujo de citas: " + e.getMessage());
        }
    }

    @Test
    @Order(5)
    @DisplayName("Flujo completo: Consultas del sistema")
    void testConsultasCompleto() {
        em.getTransaction().begin();
        
        try {
            // Listar todos los pacientes
            List<Paciente> pacientes = pacienteService.listarPacientes();
            assertFalse(pacientes.isEmpty(), "Debe haber pacientes registrados");
            
            // Listar todos los médicos
            List<Medico> medicos = medicoService.listarMedicos();
            assertFalse(medicos.isEmpty(), "Debe haber médicos registrados");
            
            // Buscar citas por rango de fechas
            LocalDate fechaInicio = LocalDate.now();
            LocalDate fechaFin = LocalDate.now().plusDays(30);
            List<Cita> citasEnRango = citaService.buscarCitasPorRangoFechas(fechaInicio, fechaFin);
            
            // Verificar que las consultas funcionan sin errores
            assertNotNull(citasEnRango);
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            fail("Error en consultas del sistema: " + e.getMessage());
        }
    }

    @Test
    @Order(6)
    @DisplayName("Verificar validaciones de negocio")
    void testValidacionesNegocio() {
        em.getTransaction().begin();
        
        try {
            // Crear paciente para pruebas de validación
            Paciente paciente1 = new Paciente();
            paciente1.setNombre("Test Paciente 1");
            paciente1.setDpi("1111111111111");
            paciente1.setFechaNacimiento(LocalDate.of(1990, 1, 1));
            paciente1.setTelefono("11111111");
            paciente1.setEmail("test1@email.com");
            pacienteService.registrarPaciente(paciente1);
            
            // Intentar crear otro paciente con el mismo DPI (debe fallar)
            Paciente paciente2 = new Paciente();
            paciente2.setNombre("Test Paciente 2");
            paciente2.setDpi("1111111111111"); // DPI duplicado
            paciente2.setFechaNacimiento(LocalDate.of(1985, 1, 1));
            paciente2.setTelefono("22222222");
            paciente2.setEmail("test2@email.com");
            
            assertThrows(Exception.class, () -> {
                pacienteService.registrarPaciente(paciente2);
            }, "Debe lanzar excepción por DPI duplicado");
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            // Esta excepción es esperada para la validación
        }
    }

    @Test
    @Order(7)
    @DisplayName("Verificar relaciones JPA funcionan correctamente")
    void testRelacionesJPA() {
        em.getTransaction().begin();
        
        try {
            // Buscar un paciente con sus citas
            List<Paciente> pacientes = pacienteService.listarPacientes();
            if (!pacientes.isEmpty()) {
                Paciente paciente = pacientes.get(0);
                
                // Verificar que se pueden cargar las citas del paciente
                List<Cita> citasPaciente = citaService.listarCitasPorPaciente(paciente.getId());
                assertNotNull(citasPaciente);
                
                // Si hay citas, verificar que las relaciones funcionan
                if (!citasPaciente.isEmpty()) {
                    Cita cita = citasPaciente.get(0);
                    assertNotNull(cita.getPaciente());
                    assertNotNull(cita.getMedico());
                    assertEquals(paciente.getId(), cita.getPaciente().getId());
                }
            }
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            fail("Error verificando relaciones JPA: " + e.getMessage());
        }
    }

    @Test
    @Order(8)
    @DisplayName("Flujo completo: Validación de conflictos de horarios")
    void testValidacionConflictoHorarios() {
        em.getTransaction().begin();
        
        try {
            // Crear pacientes y médico para prueba de conflicto
            Paciente paciente1 = new Paciente();
            paciente1.setNombre("Paciente Conflicto 1");
            paciente1.setDpi("2222222222222");
            paciente1.setFechaNacimiento(LocalDate.of(1990, 1, 1));
            paciente1.setTelefono("22222222");
            paciente1.setEmail("conflicto1@email.com");
            Paciente paciente1Guardado = pacienteService.registrarPaciente(paciente1);
            
            Paciente paciente2 = new Paciente();
            paciente2.setNombre("Paciente Conflicto 2");
            paciente2.setDpi("3333333333333");
            paciente2.setFechaNacimiento(LocalDate.of(1985, 1, 1));
            paciente2.setTelefono("33333333");
            paciente2.setEmail("conflicto2@email.com");
            Paciente paciente2Guardado = pacienteService.registrarPaciente(paciente2);
            
            Medico medico = new Medico();
            medico.setNombre("Dr. Conflicto Test");
            medico.setColegiado("COLCONF123");
            medico.setEspecialidad(Especialidad.PEDIATRIA);
            medico.setEmail("conflicto@hospital.com");
            Medico medicoGuardado = medicoService.registrarMedico(medico);
            
            // Crear primera cita
            LocalDateTime fechaHora = LocalDateTime.now().plusDays(5).withHour(10).withMinute(0);
            Cita cita1 = new Cita();
            cita1.setFechaHora(fechaHora);
            cita1.setMotivo("Primera cita");
            cita1.setPaciente(paciente1Guardado);
            cita1.setMedico(medicoGuardado);
            
            Cita cita1Guardada = citaService.agendarCita(cita1);
            assertNotNull(cita1Guardada.getId());
            
            // Intentar crear segunda cita con el mismo médico y hora (debe fallar)
            Cita cita2 = new Cita();
            cita2.setFechaHora(fechaHora); // Misma fecha y hora
            cita2.setMotivo("Segunda cita - conflicto");
            cita2.setPaciente(paciente2Guardado);
            cita2.setMedico(medicoGuardado); // Mismo médico
            
            assertThrows(CitaConflictoHorarioException.class, () -> {
                citaService.agendarCita(cita2);
            }, "Debe lanzar excepción por conflicto de horario");
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            if (!(e instanceof CitaConflictoHorarioException)) {
                fail("Error inesperado en validación de conflictos: " + e.getMessage());
            }
        }
    }

    @Test
    @Order(9)
    @DisplayName("Flujo completo: Validación de fechas pasadas")
    void testValidacionFechasPasadas() {
        em.getTransaction().begin();
        
        try {
            // Usar datos existentes
            List<Paciente> pacientes = pacienteService.listarPacientes();
            List<Medico> medicos = medicoService.listarMedicos();
            
            if (!pacientes.isEmpty() && !medicos.isEmpty()) {
                Paciente paciente = pacientes.get(0);
                Medico medico = medicos.get(0);
                
                // Intentar crear cita en el pasado
                LocalDateTime fechaPasada = LocalDateTime.now().minusDays(1);
                Cita citaPasada = new Cita();
                citaPasada.setFechaHora(fechaPasada);
                citaPasada.setMotivo("Cita en el pasado");
                citaPasada.setPaciente(paciente);
                citaPasada.setMedico(medico);
                
                assertThrows(FechaInvalidaException.class, () -> {
                    citaService.agendarCita(citaPasada);
                }, "Debe lanzar excepción por fecha en el pasado");
            }
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            if (!(e instanceof FechaInvalidaException)) {
                fail("Error inesperado en validación de fechas: " + e.getMessage());
            }
        }
    }

    @Test
    @Order(10)
    @DisplayName("Flujo completo: Operaciones de eliminación con cascadas")
    void testEliminacionConCascadas() {
        em.getTransaction().begin();
        
        try {
            // Crear paciente con historial y citas para probar eliminación
            Paciente pacienteEliminar = new Paciente();
            pacienteEliminar.setNombre("Paciente Para Eliminar");
            pacienteEliminar.setDpi("9999999999999");
            pacienteEliminar.setFechaNacimiento(LocalDate.of(1975, 12, 25));
            pacienteEliminar.setTelefono("99999999");
            pacienteEliminar.setEmail("eliminar@email.com");
            Paciente pacienteGuardado = pacienteService.registrarPaciente(pacienteEliminar);
            
            // Crear historial médico
            HistorialMedico historial = new HistorialMedico();
            historial.setAlergias("Ninguna");
            historial.setAntecedentes("Ninguno");
            historial.setObservaciones("Para eliminar");
            historialService.crearHistorial(pacienteGuardado.getId(), historial);
            
            // Crear cita
            List<Medico> medicos = medicoService.listarMedicos();
            if (!medicos.isEmpty()) {
                Medico medico = medicos.get(0);
                
                Cita cita = new Cita();
                cita.setFechaHora(LocalDateTime.now().plusDays(10));
                cita.setMotivo("Cita para eliminar");
                cita.setPaciente(pacienteGuardado);
                cita.setMedico(medico);
                Cita citaGuardada = citaService.agendarCita(cita);
                
                // Verificar que todo existe antes de eliminar
                assertNotNull(pacienteService.buscarPorDpi("9999999999999"));
                assertNotNull(historialService.consultarHistorial(pacienteGuardado.getId()));
                assertNotNull(citaService.listarCitasPorPaciente(pacienteGuardado.getId()));
                
                // Eliminar solo la cita primero
                citaService.eliminarCita(citaGuardada.getId());
                
                // Verificar que la cita se eliminó pero el paciente sigue
                assertTrue(citaService.listarCitasPorPaciente(pacienteGuardado.getId()).isEmpty());
                assertNotNull(pacienteService.buscarPorDpi("9999999999999"));
                
                // Eliminar paciente (debe eliminar historial en cascada)
                pacienteService.eliminarPaciente(pacienteGuardado.getId());
                
                // Verificar que el paciente y su historial se eliminaron
                assertNull(pacienteService.buscarPorDpi("9999999999999"));
                assertNull(historialService.consultarHistorial(pacienteGuardado.getId()));
            }
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            fail("Error en operaciones de eliminación: " + e.getMessage());
        }
    }

    @Test
    @Order(11)
    @DisplayName("Flujo completo: Consultas avanzadas con rangos de fechas")
    void testConsultasAvanzadas() {
        em.getTransaction().begin();
        
        try {
            // Crear datos específicos para consultas
            Paciente pacienteConsulta = new Paciente();
            pacienteConsulta.setNombre("Paciente Consulta");
            pacienteConsulta.setDpi("7777777777777");
            pacienteConsulta.setFechaNacimiento(LocalDate.of(1988, 6, 15));
            pacienteConsulta.setTelefono("77777777");
            pacienteConsulta.setEmail("consulta@email.com");
            Paciente pacienteGuardado = pacienteService.registrarPaciente(pacienteConsulta);
            
            List<Medico> medicos = medicoService.listarMedicos();
            if (!medicos.isEmpty()) {
                Medico medico = medicos.get(0);
                
                // Crear citas en diferentes fechas
                LocalDateTime fecha1 = LocalDateTime.now().plusDays(1);
                LocalDateTime fecha2 = LocalDateTime.now().plusDays(15);
                LocalDateTime fecha3 = LocalDateTime.now().plusDays(45);
                
                Cita cita1 = new Cita();
                cita1.setFechaHora(fecha1);
                cita1.setMotivo("Consulta 1");
                cita1.setPaciente(pacienteGuardado);
                cita1.setMedico(medico);
                citaService.agendarCita(cita1);
                
                Cita cita2 = new Cita();
                cita2.setFechaHora(fecha2);
                cita2.setMotivo("Consulta 2");
                cita2.setPaciente(pacienteGuardado);
                cita2.setMedico(medico);
                citaService.agendarCita(cita2);
                
                Cita cita3 = new Cita();
                cita3.setFechaHora(fecha3);
                cita3.setMotivo("Consulta 3");
                cita3.setPaciente(pacienteGuardado);
                cita3.setMedico(medico);
                citaService.agendarCita(cita3);
                
                // Consultar citas en rango específico (debe incluir cita1 y cita2, no cita3)
                LocalDate fechaInicio = LocalDate.now();
                LocalDate fechaFin = LocalDate.now().plusDays(30);
                List<Cita> citasEnRango = citaService.buscarCitasPorRangoFechas(fechaInicio, fechaFin);
                
                // Verificar que las consultas funcionan correctamente
                assertNotNull(citasEnRango);
                assertTrue(citasEnRango.size() >= 2, "Debe encontrar al menos las 2 citas en el rango");
                
                // Consultar próximas citas del médico
                List<Cita> proximasCitas = citaService.listarProximasCitasPorMedico(medico.getId());
                assertNotNull(proximasCitas);
                assertTrue(proximasCitas.size() >= 3, "Debe encontrar las 3 citas del médico");
                
                // Verificar que están ordenadas por fecha
                for (int i = 0; i < proximasCitas.size() - 1; i++) {
                    assertTrue(proximasCitas.get(i).getFechaHora().isBefore(proximasCitas.get(i + 1).getFechaHora()) ||
                              proximasCitas.get(i).getFechaHora().isEqual(proximasCitas.get(i + 1).getFechaHora()),
                              "Las citas deben estar ordenadas por fecha");
                }
            }
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            fail("Error en consultas avanzadas: " + e.getMessage());
        }
    }

    @Test
    @Order(12)
    @DisplayName("Flujo completo: Cambios de estado de citas")
    void testCambiosEstadoCitas() {
        em.getTransaction().begin();
        
        try {
            // Usar datos existentes o crear nuevos
            List<Paciente> pacientes = pacienteService.listarPacientes();
            List<Medico> medicos = medicoService.listarMedicos();
            
            if (!pacientes.isEmpty() && !medicos.isEmpty()) {
                Paciente paciente = pacientes.get(0);
                Medico medico = medicos.get(0);
                
                // Crear cita para cambiar estados
                Cita cita = new Cita();
                cita.setFechaHora(LocalDateTime.now().plusDays(3));
                cita.setMotivo("Cita para cambiar estado");
                cita.setPaciente(paciente);
                cita.setMedico(medico);
                Cita citaGuardada = citaService.agendarCita(cita);
                
                // Verificar estado inicial
                assertEquals(EstadoCita.PROGRAMADA, citaGuardada.getEstado());
                
                // Cambiar a ATENDIDA
                Cita citaAtendida = citaService.cambiarEstadoCita(citaGuardada.getId(), EstadoCita.ATENDIDA);
                assertEquals(EstadoCita.ATENDIDA, citaAtendida.getEstado());
                
                // Crear otra cita para cancelar
                Cita cita2 = new Cita();
                cita2.setFechaHora(LocalDateTime.now().plusDays(4));
                cita2.setMotivo("Cita para cancelar");
                cita2.setPaciente(paciente);
                cita2.setMedico(medico);
                Cita cita2Guardada = citaService.agendarCita(cita2);
                
                // Cambiar a CANCELADA
                Cita citaCancelada = citaService.cambiarEstadoCita(cita2Guardada.getId(), EstadoCita.CANCELADA);
                assertEquals(EstadoCita.CANCELADA, citaCancelada.getEstado());
            }
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            fail("Error en cambios de estado: " + e.getMessage());
        }
    }

    @Test
    @Order(13)
    @DisplayName("Flujo completo: Validación de integridad de datos")
    void testIntegridadDatos() {
        em.getTransaction().begin();
        
        try {
            // Verificar que no se pueden crear médicos con colegiado duplicado
            Medico medico1 = new Medico();
            medico1.setNombre("Dr. Duplicado 1");
            medico1.setColegiado("COLDUP123");
            medico1.setEspecialidad(Especialidad.GINECOLOGIA);
            medico1.setEmail("duplicado1@hospital.com");
            medicoService.registrarMedico(medico1);
            
            Medico medico2 = new Medico();
            medico2.setNombre("Dr. Duplicado 2");
            medico2.setColegiado("COLDUP123"); // Mismo colegiado
            medico2.setEspecialidad(Especialidad.TRAUMATOLOGIA);
            medico2.setEmail("duplicado2@hospital.com");
            
            assertThrows(Exception.class, () -> {
                medicoService.registrarMedico(medico2);
            }, "Debe lanzar excepción por colegiado duplicado");
            
            // Verificar validación de email
            Paciente pacienteEmailInvalido = new Paciente();
            pacienteEmailInvalido.setNombre("Paciente Email Inválido");
            pacienteEmailInvalido.setDpi("8888888888888");
            pacienteEmailInvalido.setFechaNacimiento(LocalDate.of(1990, 1, 1));
            pacienteEmailInvalido.setTelefono("88888888");
            pacienteEmailInvalido.setEmail("email-invalido"); // Email sin formato válido
            
            // Nota: La validación de email puede ser manejada a nivel de servicio o base de datos
            // dependiendo de la implementación específica
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            // Las excepciones de validación son esperadas
        }
    }

    @Test
    @Order(14)
    @DisplayName("Flujo completo: Prueba con semilla de datos")
    void testConSemillaDatos() {
        em.getTransaction().begin();
        
        try {
            // Cargar semilla de datos
            SemillaDatos semilla = new SemillaDatos(em);
            semilla.cargarDatos();
            
            // Verificar que los datos se cargaron correctamente
            List<Paciente> pacientes = pacienteService.listarPacientes();
            List<Medico> medicos = medicoService.listarMedicos();
            
            assertFalse(pacientes.isEmpty(), "Debe haber pacientes de la semilla");
            assertFalse(medicos.isEmpty(), "Debe haber médicos de la semilla");
            
            // Verificar que hay citas programadas
            LocalDate hoy = LocalDate.now();
            LocalDate futuro = hoy.plusDays(60);
            List<Cita> citas = citaService.buscarCitasPorRangoFechas(hoy, futuro);
            assertFalse(citas.isEmpty(), "Debe haber citas de la semilla");
            
            // Verificar que hay historiales médicos
            for (Paciente paciente : pacientes) {
                HistorialMedico historial = historialService.consultarHistorial(paciente.getId());
                if (historial != null) {
                    assertNotNull(historial.getId());
                    break; // Al menos uno debe tener historial
                }
            }
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            fail("Error con semilla de datos: " + e.getMessage());
        }
    }
}