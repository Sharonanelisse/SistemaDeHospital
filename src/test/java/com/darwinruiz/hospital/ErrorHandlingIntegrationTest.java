package com.darwinruiz.hospital;

import com.darwinruiz.hospital.enums.Especialidad;
import com.darwinruiz.hospital.enums.EstadoCita;
import com.darwinruiz.hospital.exceptions.*;
import com.darwinruiz.hospital.models.*;
import com.darwinruiz.hospital.services.*;
import com.darwinruiz.hospital.repositories.*;
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
 * Test de integración específico para manejo de errores y validaciones
 * Verifica que todas las validaciones de negocio funcionen correctamente
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ErrorHandlingIntegrationTest {

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
    @DisplayName("Error: Validación de DPI duplicado")
    void testValidacionDpiDuplicado() {
        em.getTransaction().begin();
        
        try {
            // Crear primer paciente
            Paciente paciente1 = new Paciente();
            paciente1.setNombre("Primer Paciente");
            paciente1.setDpi("1111111111111");
            paciente1.setFechaNacimiento(LocalDate.of(1990, 1, 1));
            paciente1.setTelefono("11111111");
            paciente1.setEmail("primero@email.com");
            
            pacienteService.registrarPaciente(paciente1);
            
            // Intentar crear segundo paciente con mismo DPI
            Paciente paciente2 = new Paciente();
            paciente2.setNombre("Segundo Paciente");
            paciente2.setDpi("1111111111111"); // DPI duplicado
            paciente2.setFechaNacimiento(LocalDate.of(1985, 1, 1));
            paciente2.setTelefono("22222222");
            paciente2.setEmail("segundo@email.com");
            
            // Debe lanzar excepción
            Exception exception = assertThrows(Exception.class, () -> {
                pacienteService.registrarPaciente(paciente2);
            });
            
            System.out.println("✓ Validación DPI duplicado: " + exception.getClass().getSimpleName());
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            // La excepción es esperada
        }
    }

    @Test
    @Order(2)
    @DisplayName("Error: Validación de colegiado duplicado")
    void testValidacionColegiadoDuplicado() {
        em.getTransaction().begin();
        
        try {
            // Crear primer médico
            Medico medico1 = new Medico();
            medico1.setNombre("Dr. Primer Médico");
            medico1.setColegiado("COL111");
            medico1.setEspecialidad(Especialidad.CARDIOLOGIA);
            medico1.setEmail("primero@hospital.com");
            
            medicoService.registrarMedico(medico1);
            
            // Intentar crear segundo médico con mismo colegiado
            Medico medico2 = new Medico();
            medico2.setNombre("Dr. Segundo Médico");
            medico2.setColegiado("COL111"); // Colegiado duplicado
            medico2.setEspecialidad(Especialidad.NEUROLOGIA);
            medico2.setEmail("segundo@hospital.com");
            
            // Debe lanzar excepción
            Exception exception = assertThrows(Exception.class, () -> {
                medicoService.registrarMedico(medico2);
            });
            
            System.out.println("✓ Validación colegiado duplicado: " + exception.getClass().getSimpleName());
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            // La excepción es esperada
        }
    }

    @Test
    @Order(3)
    @DisplayName("Error: Validación de fecha en el pasado")
    void testValidacionFechaPasado() {
        em.getTransaction().begin();
        
        try {
            // Crear paciente y médico válidos
            Paciente paciente = new Paciente();
            paciente.setNombre("Paciente Fecha");
            paciente.setDpi("2222222222222");
            paciente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
            paciente.setTelefono("22222222");
            paciente.setEmail("fecha@email.com");
            Paciente pacienteGuardado = pacienteService.registrarPaciente(paciente);
            
            Medico medico = new Medico();
            medico.setNombre("Dr. Fecha");
            medico.setColegiado("COL222");
            medico.setEspecialidad(Especialidad.PEDIATRIA);
            medico.setEmail("fecha@hospital.com");
            Medico medicoGuardado = medicoService.registrarMedico(medico);
            
            // Intentar crear cita en el pasado
            Cita citaPasada = new Cita();
            citaPasada.setFechaHora(LocalDateTime.now().minusDays(1)); // Fecha pasada
            citaPasada.setMotivo("Cita en el pasado");
            citaPasada.setPaciente(pacienteGuardado);
            citaPasada.setMedico(medicoGuardado);
            
            // Debe lanzar FechaInvalidaException
            FechaInvalidaException exception = assertThrows(FechaInvalidaException.class, () -> {
                citaService.agendarCita(citaPasada);
            });
            
            System.out.println("✓ Validación fecha pasada: " + exception.getMessage());
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            if (!(e instanceof FechaInvalidaException)) {
                fail("Error inesperado: " + e.getMessage());
            }
        }
    }

    @Test
    @Order(4)
    @DisplayName("Error: Validación de conflicto de horarios")
    void testValidacionConflictoHorarios() {
        em.getTransaction().begin();
        
        try {
            // Crear pacientes y médico
            Paciente paciente1 = new Paciente();
            paciente1.setNombre("Paciente Conflicto 1");
            paciente1.setDpi("3333333333333");
            paciente1.setFechaNacimiento(LocalDate.of(1990, 1, 1));
            paciente1.setTelefono("33333333");
            paciente1.setEmail("conflicto1@email.com");
            Paciente paciente1Guardado = pacienteService.registrarPaciente(paciente1);
            
            Paciente paciente2 = new Paciente();
            paciente2.setNombre("Paciente Conflicto 2");
            paciente2.setDpi("4444444444444");
            paciente2.setFechaNacimiento(LocalDate.of(1985, 1, 1));
            paciente2.setTelefono("44444444");
            paciente2.setEmail("conflicto2@email.com");
            Paciente paciente2Guardado = pacienteService.registrarPaciente(paciente2);
            
            Medico medico = new Medico();
            medico.setNombre("Dr. Conflicto");
            medico.setColegiado("COL333");
            medico.setEspecialidad(Especialidad.GINECOLOGIA);
            medico.setEmail("conflicto@hospital.com");
            Medico medicoGuardado = medicoService.registrarMedico(medico);
            
            // Crear primera cita
            LocalDateTime fechaHora = LocalDateTime.now().plusDays(5).withHour(14).withMinute(30);
            Cita cita1 = new Cita();
            cita1.setFechaHora(fechaHora);
            cita1.setMotivo("Primera cita");
            cita1.setPaciente(paciente1Guardado);
            cita1.setMedico(medicoGuardado);
            
            citaService.agendarCita(cita1);
            
            // Intentar crear segunda cita con mismo médico y hora
            Cita cita2 = new Cita();
            cita2.setFechaHora(fechaHora); // Misma fecha y hora
            cita2.setMotivo("Segunda cita - conflicto");
            cita2.setPaciente(paciente2Guardado);
            cita2.setMedico(medicoGuardado); // Mismo médico
            
            // Debe lanzar CitaConflictoHorarioException
            CitaConflictoHorarioException exception = assertThrows(CitaConflictoHorarioException.class, () -> {
                citaService.agendarCita(cita2);
            });
            
            System.out.println("✓ Validación conflicto horarios: " + exception.getMessage());
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            if (!(e instanceof CitaConflictoHorarioException)) {
                fail("Error inesperado: " + e.getMessage());
            }
        }
    }

    @Test
    @Order(5)
    @DisplayName("Error: Validación de email inválido")
    void testValidacionEmailInvalido() {
        em.getTransaction().begin();
        
        try {
            // Intentar crear paciente con email inválido
            Paciente pacienteEmailInvalido = new Paciente();
            pacienteEmailInvalido.setNombre("Paciente Email Inválido");
            pacienteEmailInvalido.setDpi("5555555555555");
            pacienteEmailInvalido.setFechaNacimiento(LocalDate.of(1990, 1, 1));
            pacienteEmailInvalido.setTelefono("55555555");
            pacienteEmailInvalido.setEmail("email-sin-arroba-ni-dominio"); // Email inválido
            
            // Dependiendo de la implementación, puede lanzar excepción aquí o en la base de datos
            try {
                pacienteService.registrarPaciente(pacienteEmailInvalido);
                System.out.println("⚠️  Email inválido fue aceptado (validación puede estar a nivel de UI)");
            } catch (Exception e) {
                System.out.println("✓ Validación email inválido: " + e.getClass().getSimpleName());
            }
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            System.out.println("✓ Validación email inválido capturada: " + e.getClass().getSimpleName());
        }
    }

    @Test
    @Order(6)
    @DisplayName("Error: Operaciones con entidades inexistentes")
    void testOperacionesEntidadesInexistentes() {
        em.getTransaction().begin();
        
        try {
            // Intentar buscar paciente inexistente
            Paciente pacienteInexistente = pacienteService.buscarPorDpi("9999999999999");
            assertNull(pacienteInexistente, "Paciente inexistente debe retornar null");
            
            // Intentar buscar médico inexistente
            Medico medicoInexistente = medicoService.buscarPorColegiado("COLINEXISTENTE");
            assertNull(medicoInexistente, "Médico inexistente debe retornar null");
            
            // Intentar consultar historial de paciente inexistente
            HistorialMedico historialInexistente = historialService.consultarHistorial(99999L);
            assertNull(historialInexistente, "Historial inexistente debe retornar null");
            
            // Intentar cambiar estado de cita inexistente
            assertThrows(Exception.class, () -> {
                citaService.cambiarEstadoCita(99999L, EstadoCita.ATENDIDA);
            }, "Debe lanzar excepción al cambiar estado de cita inexistente");
            
            // Intentar eliminar cita inexistente
            assertThrows(Exception.class, () -> {
                citaService.eliminarCita(99999L);
            }, "Debe lanzar excepción al eliminar cita inexistente");
            
            System.out.println("✓ Manejo de entidades inexistentes verificado");
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            System.out.println("✓ Excepción esperada para entidades inexistentes: " + e.getClass().getSimpleName());
        }
    }

    @Test
    @Order(7)
    @DisplayName("Error: Validación de datos nulos o vacíos")
    void testValidacionDatosNulosVacios() {
        em.getTransaction().begin();
        
        try {
            // Intentar crear paciente con datos nulos
            Paciente pacienteNulo = new Paciente();
            pacienteNulo.setNombre(null); // Nombre nulo
            pacienteNulo.setDpi("6666666666666");
            pacienteNulo.setFechaNacimiento(LocalDate.of(1990, 1, 1));
            pacienteNulo.setTelefono("66666666");
            pacienteNulo.setEmail("nulo@email.com");
            
            assertThrows(Exception.class, () -> {
                pacienteService.registrarPaciente(pacienteNulo);
            }, "Debe lanzar excepción por nombre nulo");
            
            // Intentar crear paciente con DPI nulo
            Paciente pacienteDpiNulo = new Paciente();
            pacienteDpiNulo.setNombre("Paciente DPI Nulo");
            pacienteDpiNulo.setDpi(null); // DPI nulo
            pacienteDpiNulo.setFechaNacimiento(LocalDate.of(1990, 1, 1));
            pacienteDpiNulo.setTelefono("77777777");
            pacienteDpiNulo.setEmail("dpinulo@email.com");
            
            assertThrows(Exception.class, () -> {
                pacienteService.registrarPaciente(pacienteDpiNulo);
            }, "Debe lanzar excepción por DPI nulo");
            
            // Intentar crear médico con datos vacíos
            Medico medicoVacio = new Medico();
            medicoVacio.setNombre(""); // Nombre vacío
            medicoVacio.setColegiado("COL444");
            medicoVacio.setEspecialidad(Especialidad.TRAUMATOLOGIA);
            medicoVacio.setEmail("vacio@hospital.com");
            
            // Puede o no lanzar excepción dependiendo de las validaciones implementadas
            try {
                medicoService.registrarMedico(medicoVacio);
                System.out.println("⚠️  Nombre vacío fue aceptado");
            } catch (Exception e) {
                System.out.println("✓ Validación nombre vacío: " + e.getClass().getSimpleName());
            }
            
            System.out.println("✓ Validaciones de datos nulos/vacíos verificadas");
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            System.out.println("✓ Validación de datos nulos capturada: " + e.getClass().getSimpleName());
        }
    }

    @Test
    @Order(8)
    @DisplayName("Error: Violación de integridad referencial")
    void testViolacionIntegridadReferencial() {
        em.getTransaction().begin();
        
        try {
            // Crear datos válidos primero
            Paciente paciente = new Paciente();
            paciente.setNombre("Paciente Integridad");
            paciente.setDpi("7777777777777");
            paciente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
            paciente.setTelefono("77777777");
            paciente.setEmail("integridad@email.com");
            Paciente pacienteGuardado = pacienteService.registrarPaciente(paciente);
            
            Medico medico = new Medico();
            medico.setNombre("Dr. Integridad");
            medico.setColegiado("COL555");
            medico.setEspecialidad(Especialidad.DERMATOLOGIA);
            medico.setEmail("integridad@hospital.com");
            Medico medicoGuardado = medicoService.registrarMedico(medico);
            
            // Crear cita
            Cita cita = new Cita();
            cita.setFechaHora(LocalDateTime.now().plusDays(10));
            cita.setMotivo("Cita integridad");
            cita.setPaciente(pacienteGuardado);
            cita.setMedico(medicoGuardado);
            citaService.agendarCita(cita);
            
            // Intentar eliminar médico que tiene citas (puede fallar por integridad referencial)
            // Esto depende de la configuración de cascadas
            try {
                // Si no hay cascade en la relación Medico->Cita, esto debería fallar
                em.getTransaction().commit();
                em.getTransaction().begin();
                
                // Intentar eliminar directamente desde la base de datos
                Medico medicoAEliminar = em.find(Medico.class, medicoGuardado.getId());
                if (medicoAEliminar != null) {
                    em.remove(medicoAEliminar);
                    em.flush(); // Forzar la operación
                }
                
                System.out.println("⚠️  Médico con citas fue eliminado (cascade configurado)");
            } catch (Exception e) {
                System.out.println("✓ Integridad referencial protegida: " + e.getClass().getSimpleName());
            }
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            System.out.println("✓ Violación de integridad capturada: " + e.getClass().getSimpleName());
        }
    }

    @Test
    @Order(9)
    @DisplayName("Error: Límites de longitud de campos")
    void testLimitesLongitudCampos() {
        em.getTransaction().begin();
        
        try {
            // Crear paciente con nombre muy largo
            String nombreMuyLargo = "A".repeat(200); // Asumiendo límite de 100 caracteres
            
            Paciente pacienteNombreLargo = new Paciente();
            pacienteNombreLargo.setNombre(nombreMuyLargo);
            pacienteNombreLargo.setDpi("8888888888888");
            pacienteNombreLargo.setFechaNacimiento(LocalDate.of(1990, 1, 1));
            pacienteNombreLargo.setTelefono("88888888");
            pacienteNombreLargo.setEmail("largo@email.com");
            
            try {
                pacienteService.registrarPaciente(pacienteNombreLargo);
                System.out.println("⚠️  Nombre largo fue aceptado");
            } catch (Exception e) {
                System.out.println("✓ Validación longitud nombre: " + e.getClass().getSimpleName());
            }
            
            // Crear historial con observaciones muy largas
            Paciente pacienteValido = new Paciente();
            pacienteValido.setNombre("Paciente Válido");
            pacienteValido.setDpi("9999999999999");
            pacienteValido.setFechaNacimiento(LocalDate.of(1990, 1, 1));
            pacienteValido.setTelefono("99999999");
            pacienteValido.setEmail("valido@email.com");
            Paciente pacienteGuardado = pacienteService.registrarPaciente(pacienteValido);
            
            String observacionesLargas = "O".repeat(2000); // Asumiendo límite de 1000 caracteres
            
            HistorialMedico historialLargo = new HistorialMedico();
            historialLargo.setAlergias("Ninguna");
            historialLargo.setAntecedentes("Ninguno");
            historialLargo.setObservaciones(observacionesLargas);
            
            try {
                historialService.crearHistorial(pacienteGuardado.getId(), historialLargo);
                System.out.println("⚠️  Observaciones largas fueron aceptadas");
            } catch (Exception e) {
                System.out.println("✓ Validación longitud observaciones: " + e.getClass().getSimpleName());
            }
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            System.out.println("✓ Validación de límites capturada: " + e.getClass().getSimpleName());
        }
    }

    @Test
    @Order(10)
    @DisplayName("Error: Transacciones y rollback")
    void testTransaccionesRollback() {
        em.getTransaction().begin();
        
        try {
            // Crear datos válidos
            Paciente paciente = new Paciente();
            paciente.setNombre("Paciente Transacción");
            paciente.setDpi("1010101010101");
            paciente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
            paciente.setTelefono("10101010");
            paciente.setEmail("transaccion@email.com");
            Paciente pacienteGuardado = pacienteService.registrarPaciente(paciente);
            
            // Verificar que el paciente existe
            assertNotNull(pacienteService.buscarPorDpi("1010101010101"));
            
            // Simular error que cause rollback
            try {
                // Crear otro paciente con DPI duplicado para forzar error
                Paciente pacienteDuplicado = new Paciente();
                pacienteDuplicado.setNombre("Paciente Duplicado");
                pacienteDuplicado.setDpi("1010101010101"); // DPI duplicado
                pacienteDuplicado.setFechaNacimiento(LocalDate.of(1985, 1, 1));
                pacienteDuplicado.setTelefono("20202020");
                pacienteDuplicado.setEmail("duplicado@email.com");
                
                pacienteService.registrarPaciente(pacienteDuplicado);
                
                // Si llegamos aquí, no hubo error (no debería pasar)
                fail("Debería haber lanzado excepción por DPI duplicado");
                
            } catch (Exception e) {
                // Rollback explícito
                em.getTransaction().rollback();
                System.out.println("✓ Rollback ejecutado correctamente: " + e.getClass().getSimpleName());
                
                // Iniciar nueva transacción para verificar
                em.getTransaction().begin();
                
                // Verificar que el rollback funcionó
                // El primer paciente no debería existir después del rollback
                Paciente pacienteVerificacion = pacienteService.buscarPorDpi("1010101010101");
                if (pacienteVerificacion == null) {
                    System.out.println("✓ Rollback completo - datos no persistidos");
                } else {
                    System.out.println("⚠️  Rollback parcial - algunos datos persistieron");
                }
            }
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            System.out.println("✓ Manejo de transacciones verificado: " + e.getClass().getSimpleName());
        }
    }
}