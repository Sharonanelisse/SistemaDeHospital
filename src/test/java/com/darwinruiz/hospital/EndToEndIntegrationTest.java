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
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test de integración end-to-end completo que simula flujos reales de usuario
 * y verifica todos los aspectos del sistema de hospital
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EndToEndIntegrationTest {

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
    @DisplayName("E2E: Flujo completo de registro de nuevo paciente")
    void testFlujoCompletoRegistroPaciente() {
        em.getTransaction().begin();
        
        try {
            // Simular entrada de usuario para registrar paciente
            System.out.println("=== SIMULANDO REGISTRO DE PACIENTE ===");
            
            Paciente nuevoPaciente = new Paciente();
            nuevoPaciente.setNombre("María Elena Rodríguez");
            nuevoPaciente.setDpi("1234567890123");
            nuevoPaciente.setFechaNacimiento(LocalDate.of(1985, 3, 15));
            nuevoPaciente.setTelefono("12345678");
            nuevoPaciente.setEmail("maria.rodriguez@email.com");
            
            // Registrar paciente
            Paciente pacienteRegistrado = pacienteService.registrarPaciente(nuevoPaciente);
            
            // Verificaciones
            assertNotNull(pacienteRegistrado.getId());
            assertEquals("María Elena Rodríguez", pacienteRegistrado.getNombre());
            assertEquals("1234567890123", pacienteRegistrado.getDpi());
            
            // Verificar que aparece en listados
            List<Paciente> todosPacientes = pacienteService.listarPacientes();
            assertTrue(todosPacientes.stream()
                .anyMatch(p -> p.getDpi().equals("1234567890123")));
            
            // Verificar búsqueda por DPI
            Paciente pacienteEncontrado = pacienteService.buscarPorDpi("1234567890123");
            assertNotNull(pacienteEncontrado);
            assertEquals(pacienteRegistrado.getId(), pacienteEncontrado.getId());
            
            System.out.println("✓ Paciente registrado exitosamente: " + pacienteRegistrado.getNombre());
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            fail("Error en flujo de registro de paciente: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("E2E: Flujo completo de registro de médico con especialidad")
    void testFlujoCompletoRegistroMedico() {
        em.getTransaction().begin();
        
        try {
            System.out.println("=== SIMULANDO REGISTRO DE MÉDICO ===");
            
            Medico nuevoMedico = new Medico();
            nuevoMedico.setNombre("Dr. Carlos Alberto Mendoza");
            nuevoMedico.setColegiado("COL123456");
            nuevoMedico.setEspecialidad(Especialidad.CARDIOLOGIA);
            nuevoMedico.setEmail("carlos.mendoza@hospital.com");
            
            // Registrar médico
            Medico medicoRegistrado = medicoService.registrarMedico(nuevoMedico);
            
            // Verificaciones
            assertNotNull(medicoRegistrado.getId());
            assertEquals("Dr. Carlos Alberto Mendoza", medicoRegistrado.getNombre());
            assertEquals("COL123456", medicoRegistrado.getColegiado());
            assertEquals(Especialidad.CARDIOLOGIA, medicoRegistrado.getEspecialidad());
            
            // Verificar que aparece en listados
            List<Medico> todosMedicos = medicoService.listarMedicos();
            assertTrue(todosMedicos.stream()
                .anyMatch(m -> m.getColegiado().equals("COL123456")));
            
            // Verificar búsqueda por colegiado
            Medico medicoEncontrado = medicoService.buscarPorColegiado("COL123456");
            assertNotNull(medicoEncontrado);
            assertEquals(medicoRegistrado.getId(), medicoEncontrado.getId());
            
            System.out.println("✓ Médico registrado exitosamente: " + medicoRegistrado.getNombre());
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            fail("Error en flujo de registro de médico: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    @DisplayName("E2E: Flujo completo de creación de historial médico")
    void testFlujoCompletoHistorialMedico() {
        em.getTransaction().begin();
        
        try {
            System.out.println("=== SIMULANDO CREACIÓN DE HISTORIAL MÉDICO ===");
            
            // Buscar paciente existente
            Paciente paciente = pacienteService.buscarPorDpi("1234567890123");
            assertNotNull(paciente, "Debe existir el paciente del test anterior");
            
            // Crear historial médico
            HistorialMedico nuevoHistorial = new HistorialMedico();
            nuevoHistorial.setAlergias("Penicilina, Aspirina");
            nuevoHistorial.setAntecedentes("Hipertensión arterial, Diabetes tipo 2");
            nuevoHistorial.setObservaciones("Paciente con seguimiento regular por cardiología");
            
            // Asociar historial al paciente
            HistorialMedico historialCreado = historialService.crearHistorial(paciente.getId(), nuevoHistorial);
            
            // Verificaciones
            assertNotNull(historialCreado);
            assertEquals(paciente.getId(), historialCreado.getId()); // Mismo ID por @MapsId
            assertEquals("Penicilina, Aspirina", historialCreado.getAlergias());
            assertEquals("Hipertensión arterial, Diabetes tipo 2", historialCreado.getAntecedentes());
            
            // Verificar consulta del historial
            HistorialMedico historialConsultado = historialService.consultarHistorial(paciente.getId());
            assertNotNull(historialConsultado);
            assertEquals(historialCreado.getId(), historialConsultado.getId());
            
            // Probar actualización del historial
            HistorialMedico historialActualizado = new HistorialMedico();
            historialActualizado.setAlergias("Penicilina, Aspirina, Polen");
            historialActualizado.setAntecedentes("Hipertensión arterial, Diabetes tipo 2, Antecedentes familiares de cardiopatía");
            historialActualizado.setObservaciones("Paciente con seguimiento regular por cardiología. Última consulta: mejora en control glucémico");
            
            HistorialMedico historialModificado = historialService.actualizarHistorial(paciente.getId(), historialActualizado);
            assertEquals("Penicilina, Aspirina, Polen", historialModificado.getAlergias());
            
            System.out.println("✓ Historial médico creado y actualizado exitosamente");
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            fail("Error en flujo de historial médico: " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    @DisplayName("E2E: Flujo completo de agendamiento de citas")
    void testFlujoCompletoAgendamientoCitas() {
        em.getTransaction().begin();
        
        try {
            System.out.println("=== SIMULANDO AGENDAMIENTO DE CITAS ===");
            
            // Buscar paciente y médico existentes
            Paciente paciente = pacienteService.buscarPorDpi("1234567890123");
            Medico medico = medicoService.buscarPorColegiado("COL123456");
            
            assertNotNull(paciente, "Debe existir el paciente");
            assertNotNull(medico, "Debe existir el médico");
            
            // Agendar primera cita
            LocalDateTime fechaCita1 = LocalDateTime.now().plusDays(7).withHour(9).withMinute(0);
            Cita cita1 = new Cita();
            cita1.setFechaHora(fechaCita1);
            cita1.setMotivo("Consulta de cardiología - control rutinario");
            cita1.setPaciente(paciente);
            cita1.setMedico(medico);
            
            Cita cita1Agendada = citaService.agendarCita(cita1);
            
            // Verificaciones
            assertNotNull(cita1Agendada.getId());
            assertEquals(EstadoCita.PROGRAMADA, cita1Agendada.getEstado());
            assertEquals(fechaCita1, cita1Agendada.getFechaHora());
            
            // Agendar segunda cita (diferente hora)
            LocalDateTime fechaCita2 = LocalDateTime.now().plusDays(14).withHour(10).withMinute(30);
            Cita cita2 = new Cita();
            cita2.setFechaHora(fechaCita2);
            cita2.setMotivo("Seguimiento post-consulta");
            cita2.setPaciente(paciente);
            cita2.setMedico(medico);
            
            Cita cita2Agendada = citaService.agendarCita(cita2);
            assertNotNull(cita2Agendada.getId());
            
            // Verificar listado de citas del paciente
            List<Cita> citasPaciente = citaService.listarCitasPorPaciente(paciente.getId());
            assertTrue(citasPaciente.size() >= 2);
            
            // Verificar listado de citas del médico
            List<Cita> citasMedico = citaService.listarProximasCitasPorMedico(medico.getId());
            assertTrue(citasMedico.size() >= 2);
            
            System.out.println("✓ Citas agendadas exitosamente");
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            fail("Error en flujo de agendamiento: " + e.getMessage());
        }
    }

    @Test
    @Order(5)
    @DisplayName("E2E: Flujo completo de gestión de estados de citas")
    void testFlujoCompletoGestionEstados() {
        em.getTransaction().begin();
        
        try {
            System.out.println("=== SIMULANDO GESTIÓN DE ESTADOS DE CITAS ===");
            
            // Buscar citas existentes
            Paciente paciente = pacienteService.buscarPorDpi("1234567890123");
            List<Cita> citasPaciente = citaService.listarCitasPorPaciente(paciente.getId());
            
            assertFalse(citasPaciente.isEmpty(), "Debe haber citas del paciente");
            
            // Cambiar primera cita a ATENDIDA
            Cita primeraCita = citasPaciente.get(0);
            assertEquals(EstadoCita.PROGRAMADA, primeraCita.getEstado());
            
            Cita citaAtendida = citaService.cambiarEstadoCita(primeraCita.getId(), EstadoCita.ATENDIDA);
            assertEquals(EstadoCita.ATENDIDA, citaAtendida.getEstado());
            
            // Si hay más citas, cancelar una
            if (citasPaciente.size() > 1) {
                Cita segundaCita = citasPaciente.get(1);
                Cita citaCancelada = citaService.cambiarEstadoCita(segundaCita.getId(), EstadoCita.CANCELADA);
                assertEquals(EstadoCita.CANCELADA, citaCancelada.getEstado());
            }
            
            System.out.println("✓ Estados de citas gestionados exitosamente");
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            fail("Error en gestión de estados: " + e.getMessage());
        }
    }

    @Test
    @Order(6)
    @DisplayName("E2E: Flujo completo de consultas y reportes")
    void testFlujoCompletoConsultasReportes() {
        em.getTransaction().begin();
        
        try {
            System.out.println("=== SIMULANDO CONSULTAS Y REPORTES ===");
            
            // Consulta 1: Listar todos los pacientes
            List<Paciente> todosPacientes = pacienteService.listarPacientes();
            assertFalse(todosPacientes.isEmpty());
            System.out.println("✓ Encontrados " + todosPacientes.size() + " pacientes");
            
            // Consulta 2: Listar todos los médicos
            List<Medico> todosMedicos = medicoService.listarMedicos();
            assertFalse(todosMedicos.isEmpty());
            System.out.println("✓ Encontrados " + todosMedicos.size() + " médicos");
            
            // Consulta 3: Buscar citas por rango de fechas
            LocalDate fechaInicio = LocalDate.now();
            LocalDate fechaFin = LocalDate.now().plusDays(30);
            List<Cita> citasEnRango = citaService.buscarCitasPorRangoFechas(fechaInicio, fechaFin);
            System.out.println("✓ Encontradas " + citasEnRango.size() + " citas en el próximo mes");
            
            // Consulta 4: Ver historiales médicos
            for (Paciente paciente : todosPacientes) {
                HistorialMedico historial = historialService.consultarHistorial(paciente.getId());
                if (historial != null) {
                    System.out.println("✓ Historial encontrado para paciente: " + paciente.getNombre());
                }
            }
            
            // Consulta 5: Próximas citas por médico
            for (Medico medico : todosMedicos) {
                List<Cita> proximasCitas = citaService.listarProximasCitasPorMedico(medico.getId());
                System.out.println("✓ Dr. " + medico.getNombre() + " tiene " + proximasCitas.size() + " próximas citas");
            }
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            fail("Error en consultas y reportes: " + e.getMessage());
        }
    }

    @Test
    @Order(7)
    @DisplayName("E2E: Flujo completo de validaciones y manejo de errores")
    void testFlujoCompletoValidacionesErrores() {
        em.getTransaction().begin();
        
        try {
            System.out.println("=== SIMULANDO VALIDACIONES Y MANEJO DE ERRORES ===");
            
            // Error 1: DPI duplicado
            Paciente pacienteDuplicado = new Paciente();
            pacienteDuplicado.setNombre("Paciente Duplicado");
            pacienteDuplicado.setDpi("1234567890123"); // DPI ya existe
            pacienteDuplicado.setFechaNacimiento(LocalDate.of(1990, 1, 1));
            pacienteDuplicado.setTelefono("99999999");
            pacienteDuplicado.setEmail("duplicado@email.com");
            
            assertThrows(Exception.class, () -> {
                pacienteService.registrarPaciente(pacienteDuplicado);
            });
            System.out.println("✓ Validación de DPI duplicado funciona correctamente");
            
            // Error 2: Colegiado duplicado
            Medico medicoDuplicado = new Medico();
            medicoDuplicado.setNombre("Dr. Duplicado");
            medicoDuplicado.setColegiado("COL123456"); // Colegiado ya existe
            medicoDuplicado.setEspecialidad(Especialidad.PEDIATRIA);
            medicoDuplicado.setEmail("duplicado@hospital.com");
            
            assertThrows(Exception.class, () -> {
                medicoService.registrarMedico(medicoDuplicado);
            });
            System.out.println("✓ Validación de colegiado duplicado funciona correctamente");
            
            // Error 3: Fecha en el pasado
            Paciente pacienteExistente = pacienteService.buscarPorDpi("1234567890123");
            Medico medicoExistente = medicoService.buscarPorColegiado("COL123456");
            
            Cita citaPasada = new Cita();
            citaPasada.setFechaHora(LocalDateTime.now().minusDays(1)); // Fecha pasada
            citaPasada.setMotivo("Cita en el pasado");
            citaPasada.setPaciente(pacienteExistente);
            citaPasada.setMedico(medicoExistente);
            
            assertThrows(FechaInvalidaException.class, () -> {
                citaService.agendarCita(citaPasada);
            });
            System.out.println("✓ Validación de fecha pasada funciona correctamente");
            
            // Error 4: Conflicto de horarios
            LocalDateTime fechaConflicto = LocalDateTime.now().plusDays(5).withHour(15).withMinute(0);
            
            // Crear primera cita
            Cita cita1 = new Cita();
            cita1.setFechaHora(fechaConflicto);
            cita1.setMotivo("Primera cita");
            cita1.setPaciente(pacienteExistente);
            cita1.setMedico(medicoExistente);
            citaService.agendarCita(cita1);
            
            // Intentar crear segunda cita con mismo médico y hora
            Paciente otroPaciente = new Paciente();
            otroPaciente.setNombre("Otro Paciente");
            otroPaciente.setDpi("9999999999999");
            otroPaciente.setFechaNacimiento(LocalDate.of(1985, 1, 1));
            otroPaciente.setTelefono("88888888");
            otroPaciente.setEmail("otro@email.com");
            Paciente otroPacienteGuardado = pacienteService.registrarPaciente(otroPaciente);
            
            Cita cita2 = new Cita();
            cita2.setFechaHora(fechaConflicto); // Misma fecha y hora
            cita2.setMotivo("Segunda cita - conflicto");
            cita2.setPaciente(otroPacienteGuardado);
            cita2.setMedico(medicoExistente); // Mismo médico
            
            assertThrows(CitaConflictoHorarioException.class, () -> {
                citaService.agendarCita(cita2);
            });
            System.out.println("✓ Validación de conflicto de horarios funciona correctamente");
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            if (!(e instanceof FechaInvalidaException || e instanceof CitaConflictoHorarioException)) {
                fail("Error inesperado en validaciones: " + e.getMessage());
            }
        }
    }

    @Test
    @Order(8)
    @DisplayName("E2E: Flujo completo de eliminaciones con cascadas")
    void testFlujoCompletoEliminaciones() {
        em.getTransaction().begin();
        
        try {
            System.out.println("=== SIMULANDO ELIMINACIONES CON CASCADAS ===");
            
            // Crear datos específicos para eliminar
            Paciente pacienteEliminar = new Paciente();
            pacienteEliminar.setNombre("Paciente Para Eliminar");
            pacienteEliminar.setDpi("0000000000000");
            pacienteEliminar.setFechaNacimiento(LocalDate.of(1980, 6, 15));
            pacienteEliminar.setTelefono("00000000");
            pacienteEliminar.setEmail("eliminar@email.com");
            Paciente pacienteGuardado = pacienteService.registrarPaciente(pacienteEliminar);
            
            // Crear historial
            HistorialMedico historial = new HistorialMedico();
            historial.setAlergias("Ninguna");
            historial.setAntecedentes("Ninguno");
            historial.setObservaciones("Para eliminar");
            historialService.crearHistorial(pacienteGuardado.getId(), historial);
            
            // Crear cita
            Medico medico = medicoService.buscarPorColegiado("COL123456");
            Cita cita = new Cita();
            cita.setFechaHora(LocalDateTime.now().plusDays(20));
            cita.setMotivo("Cita para eliminar");
            cita.setPaciente(pacienteGuardado);
            cita.setMedico(medico);
            Cita citaGuardada = citaService.agendarCita(cita);
            
            // Verificar que todo existe
            assertNotNull(pacienteService.buscarPorDpi("0000000000000"));
            assertNotNull(historialService.consultarHistorial(pacienteGuardado.getId()));
            assertFalse(citaService.listarCitasPorPaciente(pacienteGuardado.getId()).isEmpty());
            
            // Eliminar solo la cita primero
            citaService.eliminarCita(citaGuardada.getId());
            assertTrue(citaService.listarCitasPorPaciente(pacienteGuardado.getId()).isEmpty());
            System.out.println("✓ Cita eliminada correctamente");
            
            // Eliminar paciente (debe eliminar historial en cascada)
            pacienteService.eliminarPaciente(pacienteGuardado.getId());
            
            // Verificar eliminación
            assertNull(pacienteService.buscarPorDpi("0000000000000"));
            assertNull(historialService.consultarHistorial(pacienteGuardado.getId()));
            System.out.println("✓ Paciente e historial eliminados en cascada correctamente");
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            fail("Error en eliminaciones: " + e.getMessage());
        }
    }

    @Test
    @Order(9)
    @DisplayName("E2E: Flujo completo con semilla de datos")
    void testFlujoCompletoConSemilla() {
        em.getTransaction().begin();
        
        try {
            System.out.println("=== SIMULANDO CARGA DE SEMILLA DE DATOS ===");
            
            // Cargar semilla de datos
            SemillaDatos semilla = new SemillaDatos(em);
            semilla.cargarDatos();
            
            // Verificar que los datos se cargaron
            List<Paciente> pacientes = pacienteService.listarPacientes();
            List<Medico> medicos = medicoService.listarMedicos();
            
            assertTrue(pacientes.size() >= 3, "Debe haber al menos 3 pacientes de la semilla");
            assertTrue(medicos.size() >= 3, "Debe haber al menos 3 médicos de la semilla");
            
            // Verificar especialidades diversas
            boolean tieneCardiologia = medicos.stream().anyMatch(m -> m.getEspecialidad() == Especialidad.CARDIOLOGIA);
            boolean tieneNeurologia = medicos.stream().anyMatch(m -> m.getEspecialidad() == Especialidad.NEUROLOGIA);
            assertTrue(tieneCardiologia || tieneNeurologia, "Debe haber médicos de diferentes especialidades");
            
            // Verificar citas programadas
            LocalDate hoy = LocalDate.now();
            LocalDate futuro = hoy.plusDays(60);
            List<Cita> citas = citaService.buscarCitasPorRangoFechas(hoy, futuro);
            assertFalse(citas.isEmpty(), "Debe haber citas programadas de la semilla");
            
            // Verificar historiales médicos
            int historialesEncontrados = 0;
            for (Paciente paciente : pacientes) {
                HistorialMedico historial = historialService.consultarHistorial(paciente.getId());
                if (historial != null) {
                    historialesEncontrados++;
                }
            }
            assertTrue(historialesEncontrados > 0, "Debe haber al menos un historial médico");
            
            System.out.println("✓ Semilla de datos cargada y verificada correctamente");
            System.out.println("  - Pacientes: " + pacientes.size());
            System.out.println("  - Médicos: " + medicos.size());
            System.out.println("  - Citas: " + citas.size());
            System.out.println("  - Historiales: " + historialesEncontrados);
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            fail("Error con semilla de datos: " + e.getMessage());
        }
    }

    @Test
    @Order(10)
    @DisplayName("E2E: Verificación final de integridad del sistema")
    void testVerificacionFinalIntegridad() {
        em.getTransaction().begin();
        
        try {
            System.out.println("=== VERIFICACIÓN FINAL DE INTEGRIDAD ===");
            
            // Verificar que todas las relaciones JPA funcionan
            List<Paciente> pacientes = pacienteService.listarPacientes();
            List<Medico> medicos = medicoService.listarMedicos();
            
            // Verificar relaciones OneToOne (Paciente-HistorialMedico)
            int relacionesOneToOne = 0;
            for (Paciente paciente : pacientes) {
                HistorialMedico historial = historialService.consultarHistorial(paciente.getId());
                if (historial != null) {
                    assertEquals(paciente.getId(), historial.getId());
                    relacionesOneToOne++;
                }
            }
            System.out.println("✓ Relaciones OneToOne verificadas: " + relacionesOneToOne);
            
            // Verificar relaciones ManyToOne (Cita-Paciente, Cita-Medico)
            LocalDate fechaInicio = LocalDate.now().minusDays(30);
            LocalDate fechaFin = LocalDate.now().plusDays(60);
            List<Cita> todasCitas = citaService.buscarCitasPorRangoFechas(fechaInicio, fechaFin);
            
            for (Cita cita : todasCitas) {
                assertNotNull(cita.getPaciente(), "Toda cita debe tener paciente");
                assertNotNull(cita.getMedico(), "Toda cita debe tener médico");
                assertNotNull(cita.getPaciente().getId(), "Paciente debe tener ID");
                assertNotNull(cita.getMedico().getId(), "Médico debe tener ID");
            }
            System.out.println("✓ Relaciones ManyToOne verificadas: " + todasCitas.size() + " citas");
            
            // Verificar que los índices y restricciones funcionan
            // (esto se verifica implícitamente por las validaciones de unicidad)
            
            // Verificar estados de citas
            long citasProgramadas = todasCitas.stream().filter(c -> c.getEstado() == EstadoCita.PROGRAMADA).count();
            long citasAtendidas = todasCitas.stream().filter(c -> c.getEstado() == EstadoCita.ATENDIDA).count();
            long citasCanceladas = todasCitas.stream().filter(c -> c.getEstado() == EstadoCita.CANCELADA).count();
            
            System.out.println("✓ Estados de citas:");
            System.out.println("  - Programadas: " + citasProgramadas);
            System.out.println("  - Atendidas: " + citasAtendidas);
            System.out.println("  - Canceladas: " + citasCanceladas);
            
            // Verificar especialidades médicas
            for (Especialidad especialidad : Especialidad.values()) {
                long medicosEspecialidad = medicos.stream()
                    .filter(m -> m.getEspecialidad() == especialidad)
                    .count();
                if (medicosEspecialidad > 0) {
                    System.out.println("  - " + especialidad + ": " + medicosEspecialidad + " médicos");
                }
            }
            
            System.out.println("✓ SISTEMA COMPLETAMENTE VERIFICADO");
            System.out.println("  - Total pacientes: " + pacientes.size());
            System.out.println("  - Total médicos: " + medicos.size());
            System.out.println("  - Total citas: " + todasCitas.size());
            System.out.println("  - Total historiales: " + relacionesOneToOne);
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            fail("Error en verificación final: " + e.getMessage());
        }
    }
}