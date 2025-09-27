package com.darwinruiz.hospital.repositories;

import com.darwinruiz.hospital.enums.Especialidad;
import com.darwinruiz.hospital.enums.EstadoCita;
import com.darwinruiz.hospital.models.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Clase para probar manualmente la funcionalidad de los repositorios.
 * Ejecuta operaciones básicas para verificar que todo funciona correctamente.
 */
public class RepositoryIntegrationTestRunner {
    
    public static void main(String[] args) {
        System.out.println("=== INICIANDO PRUEBAS DE REPOSITORIOS ===");
        
        try {
            testPacienteRepository();
            testMedicoRepository();
            testHistorialMedicoRepository();
            testCitaRepository();
            
            System.out.println("\n=== TODAS LAS PRUEBAS COMPLETADAS EXITOSAMENTE ===");
            
        } catch (Exception e) {
            System.err.println("Error durante las pruebas: " + e.getMessage());
            e.printStackTrace();
        } finally {
            BaseRepository.closeEntityManagerFactory();
        }
    }
    
    private static void testPacienteRepository() {
        System.out.println("\n--- Probando PacienteRepository ---");
        
        PacienteRepository repo = new PacienteRepository();
        
        // Crear paciente de prueba
        Paciente paciente = new Paciente(
            "Juan Test",
            "TEST123456789",
            LocalDate.of(1990, 1, 1),
            "12345678",
            "juan.test@email.com"
        );
        
        // Test persist
        repo.persist(paciente);
        System.out.println("✓ Paciente persistido con ID: " + paciente.getId());
        
        // Test findById
        Optional<Paciente> encontrado = repo.findById(paciente.getId());
        System.out.println("✓ Paciente encontrado por ID: " + encontrado.isPresent());
        
        // Test findByDpi
        Optional<Paciente> porDpi = repo.findByDpi("TEST123456789");
        System.out.println("✓ Paciente encontrado por DPI: " + porDpi.isPresent());
        
        // Test existsByDpi
        boolean existe = repo.existsByDpi("TEST123456789");
        System.out.println("✓ Existe por DPI: " + existe);
        
        // Test findAll
        List<Paciente> todos = repo.findAll();
        System.out.println("✓ Total pacientes: " + todos.size());
        
        // Limpiar
        repo.remove(paciente);
        System.out.println("✓ Paciente eliminado");
    }
    
    private static void testMedicoRepository() {
        System.out.println("\n--- Probando MedicoRepository ---");
        
        MedicoRepository repo = new MedicoRepository();
        
        // Crear médico de prueba
        Medico medico = new Medico(
            "Dr. Test",
            "COLTEST123",
            Especialidad.CARDIOLOGIA,
            "dr.test@email.com"
        );
        
        // Test persist
        repo.persist(medico);
        System.out.println("✓ Médico persistido con ID: " + medico.getId());
        
        // Test findByColegiado
        Optional<Medico> porColegiado = repo.findByColegiado("COLTEST123");
        System.out.println("✓ Médico encontrado por colegiado: " + porColegiado.isPresent());
        
        // Test existsByColegiado
        boolean existe = repo.existsByColegiado("COLTEST123");
        System.out.println("✓ Existe por colegiado: " + existe);
        
        // Test findByEspecialidad
        List<Medico> cardiologos = repo.findByEspecialidad(Especialidad.CARDIOLOGIA);
        System.out.println("✓ Cardiólogos encontrados: " + cardiologos.size());
        
        // Limpiar
        repo.remove(medico);
        System.out.println("✓ Médico eliminado");
    }
    
    private static void testHistorialMedicoRepository() {
        System.out.println("\n--- Probando HistorialMedicoRepository ---");
        
        PacienteRepository pacienteRepo = new PacienteRepository();
        HistorialMedicoRepository historialRepo = new HistorialMedicoRepository();
        
        // Crear paciente
        Paciente paciente = new Paciente(
            "Paciente Historial",
            "HIST123456789",
            LocalDate.of(1985, 5, 15),
            "87654321",
            "historial.test@email.com"
        );
        pacienteRepo.persist(paciente);
        
        // Crear historial
        HistorialMedico historial = new HistorialMedico(
            paciente,
            "Alergia a penicilina",
            "Hipertensión",
            "Paciente colaborador"
        );
        historialRepo.persist(historial);
        System.out.println("✓ Historial persistido con ID: " + historial.getId());
        
        // Test findByPacienteId
        Optional<HistorialMedico> porPaciente = historialRepo.findByPacienteId(paciente.getId());
        System.out.println("✓ Historial encontrado por paciente ID: " + porPaciente.isPresent());
        
        // Test existsByPacienteId
        boolean existe = historialRepo.existsByPacienteId(paciente.getId());
        System.out.println("✓ Existe historial para paciente: " + existe);
        
        // Test búsqueda por contenido
        List<HistorialMedico> conPenicilina = historialRepo.findByAlergiasContaining("penicilina");
        System.out.println("✓ Historiales con penicilina: " + conPenicilina.size());
        
        // Limpiar
        historialRepo.remove(historial);
        pacienteRepo.remove(paciente);
        System.out.println("✓ Historial y paciente eliminados");
    }
    
    private static void testCitaRepository() {
        System.out.println("\n--- Probando CitaRepository ---");
        
        PacienteRepository pacienteRepo = new PacienteRepository();
        MedicoRepository medicoRepo = new MedicoRepository();
        CitaRepository citaRepo = new CitaRepository();
        
        // Crear paciente y médico
        Paciente paciente = new Paciente(
            "Paciente Cita",
            "CITA123456789",
            LocalDate.of(1980, 3, 10),
            "11111111",
            "cita.test@email.com"
        );
        pacienteRepo.persist(paciente);
        
        Medico medico = new Medico(
            "Dr. Cita",
            "COLCITA123",
            Especialidad.PEDIATRIA,
            "dr.cita@email.com"
        );
        medicoRepo.persist(medico);
        
        // Crear cita
        LocalDateTime fechaCita = LocalDateTime.now().plusDays(1);
        Cita cita = new Cita(
            fechaCita,
            "Consulta de prueba",
            paciente,
            medico
        );
        citaRepo.persist(cita);
        System.out.println("✓ Cita persistida con ID: " + cita.getId());
        
        // Test findByPacienteId
        List<Cita> citasPaciente = citaRepo.findByPacienteId(paciente.getId());
        System.out.println("✓ Citas del paciente: " + citasPaciente.size());
        
        // Test findProximasCitasByMedicoId
        List<Cita> proximasCitas = citaRepo.findProximasCitasByMedicoId(medico.getId());
        System.out.println("✓ Próximas citas del médico: " + proximasCitas.size());
        
        // Test existeConflictoHorario
        boolean conflicto = citaRepo.existeConflictoHorario(medico.getId(), fechaCita);
        System.out.println("✓ Existe conflicto de horario: " + conflicto);
        
        // Test findByEstado
        List<Cita> programadas = citaRepo.findByEstado(EstadoCita.PROGRAMADA);
        System.out.println("✓ Citas programadas: " + programadas.size());
        
        // Limpiar
        citaRepo.remove(cita);
        pacienteRepo.remove(paciente);
        medicoRepo.remove(medico);
        System.out.println("✓ Cita, paciente y médico eliminados");
    }
}