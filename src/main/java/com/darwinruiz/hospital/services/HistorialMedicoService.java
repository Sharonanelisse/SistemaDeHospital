package com.darwinruiz.hospital.services;

import com.darwinruiz.hospital.models.HistorialMedico;
import com.darwinruiz.hospital.models.Paciente;
import com.darwinruiz.hospital.repositories.HistorialMedicoRepository;
import com.darwinruiz.hospital.repositories.PacienteRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.Optional;

public class HistorialMedicoService {
    
    private final HistorialMedicoRepository historialRepository;
    private final PacienteRepository pacienteRepository;
    private final EntityManagerFactory emf;
    
    public HistorialMedicoService() {
        this.emf = Persistence.createEntityManagerFactory("HospitalSystemPU");
        this.historialRepository = new HistorialMedicoRepository();
        this.pacienteRepository = new PacienteRepository();
    }
    
    public HistorialMedicoService(EntityManagerFactory emf) {
        this.emf = emf;
        this.historialRepository = new HistorialMedicoRepository();
        this.pacienteRepository = new PacienteRepository();
    }

    public HistorialMedico crearHistorial(Long pacienteId, String alergias, String antecedentes, String observaciones) {
        if (pacienteId == null) {
            throw new IllegalArgumentException("El ID del paciente no puede ser nulo");
        }
        
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            pacienteRepository.setEntityManager(em);
            historialRepository.setEntityManager(em);

            Paciente paciente = pacienteRepository.findById(pacienteId);
            if (paciente == null) {
                throw new RuntimeException("No se encontró el paciente con ID: " + pacienteId);
            }

            if (historialRepository.existsByPacienteId(pacienteId)) {
                throw new RuntimeException("Ya existe un historial médico para el paciente con ID: " + pacienteId);
            }

            HistorialMedico historial = new HistorialMedico(paciente, alergias, antecedentes, observaciones);

            historialRepository.persist(historial);
            
            em.getTransaction().commit();
            return historial;
            
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Error al crear el historial médico: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public HistorialMedico actualizarHistorial(Long pacienteId, String alergias, String antecedentes, String observaciones) {
        if (pacienteId == null) {
            throw new IllegalArgumentException("El ID del paciente no puede ser nulo");
        }
        
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            historialRepository.setEntityManager(em);

            Optional<HistorialMedico> historialOpt = historialRepository.findByPacienteId(pacienteId);
            if (historialOpt.isEmpty()) {
                throw new RuntimeException("No se encontró historial médico para el paciente con ID: " + pacienteId);
            }
            
            HistorialMedico historial = historialOpt.get();

            historial.actualizarInformacion(alergias, antecedentes, observaciones);

            HistorialMedico historialActualizado = historialRepository.merge(historial);
            
            em.getTransaction().commit();
            return historialActualizado;
            
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Error al actualizar el historial médico: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public Optional<HistorialMedico> consultarHistorial(Long pacienteId) {
        if (pacienteId == null) {
            return Optional.empty();
        }
        
        EntityManager em = emf.createEntityManager();
        try {
            historialRepository.setEntityManager(em);
            return historialRepository.findByPacienteId(pacienteId);
        } finally {
            em.close();
        }
    }
    

    public Optional<HistorialMedico> consultarHistorialPorDpi(String dpi) {
        if (dpi == null || dpi.trim().isEmpty()) {
            return Optional.empty();
        }
        
        EntityManager em = emf.createEntityManager();
        try {
            historialRepository.setEntityManager(em);
            return historialRepository.findByPacienteDpi(dpi.trim());
        } finally {
            em.close();
        }
    }

    public boolean existeHistorial(Long pacienteId) {
        if (pacienteId == null) {
            return false;
        }
        
        EntityManager em = emf.createEntityManager();
        try {
            historialRepository.setEntityManager(em);
            return historialRepository.existsByPacienteId(pacienteId);
        } finally {
            em.close();
        }
    }

    public HistorialMedico crearOActualizarHistorial(Long pacienteId, String alergias, String antecedentes, String observaciones) {
        if (existeHistorial(pacienteId)) {
            return actualizarHistorial(pacienteId, alergias, antecedentes, observaciones);
        } else {
            return crearHistorial(pacienteId, alergias, antecedentes, observaciones);
        }
    }

    public boolean eliminarHistorial(Long pacienteId) {
        if (pacienteId == null) {
            return false;
        }
        
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            
            historialRepository.setEntityManager(em);
            Optional<HistorialMedico> historialOpt = historialRepository.findByPacienteId(pacienteId);
            
            if (historialOpt.isPresent()) {
                historialRepository.remove(historialOpt.get());
                em.getTransaction().commit();
                return true;
            }
            
            em.getTransaction().rollback();
            return false;
            
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Error al eliminar el historial médico: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}