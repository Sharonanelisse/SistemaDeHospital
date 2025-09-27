package com.darwinruiz.hospital.services;

import com.darwinruiz.hospital.enums.EstadoCita;
import com.darwinruiz.hospital.exceptions.CitaConflictoHorarioException;
import com.darwinruiz.hospital.exceptions.FechaInvalidaException;
import com.darwinruiz.hospital.models.Cita;
import com.darwinruiz.hospital.models.Medico;
import com.darwinruiz.hospital.models.Paciente;
import com.darwinruiz.hospital.repositories.CitaRepository;
import com.darwinruiz.hospital.repositories.MedicoRepository;
import com.darwinruiz.hospital.repositories.PacienteRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public class CitaService {
    
    private final CitaRepository citaRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;
    private final EntityManagerFactory emf;
    
    public CitaService() {
        this.emf = Persistence.createEntityManagerFactory("HospitalSystemPU");
        this.citaRepository = new CitaRepository();
        this.pacienteRepository = new PacienteRepository();
        this.medicoRepository = new MedicoRepository();
    }
    
    public CitaService(EntityManagerFactory emf) {
        this.emf = emf;
        this.citaRepository = new CitaRepository();
        this.pacienteRepository = new PacienteRepository();
        this.medicoRepository = new MedicoRepository();
    }

    public Cita agendarCita(Long pacienteId, Long medicoId, LocalDateTime fechaHora, String motivo) {
        validarDatosBasicos(pacienteId, medicoId, fechaHora);

        validarFechaFutura(fechaHora);
        
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            citaRepository.setEntityManager(em);
            pacienteRepository.setEntityManager(em);
            medicoRepository.setEntityManager(em);

            Paciente paciente = pacienteRepository.findById(pacienteId);
            if (paciente == null) {
                throw new RuntimeException("No se encontró el paciente con ID: " + pacienteId);
            }
            
            Medico medico = medicoRepository.findById(medicoId);
            if (medico == null) {
                throw new RuntimeException("No se encontró el médico con ID: " + medicoId);
            }

            validarConflictoHorarios(medicoId, fechaHora, null);

            Cita cita = new Cita(fechaHora, motivo, paciente, medico);

            citaRepository.persist(cita);
            
            em.getTransaction().commit();
            return cita;
            
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Error al agendar la cita: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public Cita cambiarEstadoCita(Long citaId, EstadoCita nuevoEstado) {
        if (citaId == null) {
            throw new IllegalArgumentException("El ID de la cita no puede ser nulo");
        }
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("El nuevo estado no puede ser nulo");
        }
        
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            
            citaRepository.setEntityManager(em);
            Cita cita = citaRepository.findById(citaId);
            
            if (cita == null) {
                throw new RuntimeException("No se encontró la cita con ID: " + citaId);
            }

            cita.cambiarEstado(nuevoEstado);
            
            Cita citaActualizada = citaRepository.merge(cita);
            em.getTransaction().commit();
            
            return citaActualizada;
            
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Error al cambiar el estado de la cita: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public List<Cita> listarCitasPorPaciente(Long pacienteId) {
        if (pacienteId == null) {
            return List.of();
        }
        
        EntityManager em = emf.createEntityManager();
        try {
            citaRepository.setEntityManager(em);
            return citaRepository.findByPacienteId(pacienteId);
        } finally {
            em.close();
        }
    }

    public List<Cita> listarProximasCitasPorMedico(Long medicoId) {
        if (medicoId == null) {
            return List.of();
        }
        
        EntityManager em = emf.createEntityManager();
        try {
            citaRepository.setEntityManager(em);
            return citaRepository.findProximasCitasByMedicoId(medicoId);
        } finally {
            em.close();
        }
    }

    public List<Cita> buscarCitasPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            return List.of();
        }
        
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        
        EntityManager em = emf.createEntityManager();
        try {
            citaRepository.setEntityManager(em);
            return citaRepository.findByRangoFechas(fechaInicio, fechaFin);
        } finally {
            em.close();
        }
    }

    public boolean eliminarCita(Long citaId) {
        if (citaId == null) {
            return false;
        }
        
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            
            citaRepository.setEntityManager(em);
            Cita cita = citaRepository.findById(citaId);
            
            if (cita != null) {
                citaRepository.remove(cita);
                em.getTransaction().commit();
                return true;
            }
            
            em.getTransaction().rollback();
            return false;
            
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Error al eliminar la cita: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public Optional<Cita> buscarPorId(Long citaId) {
        if (citaId == null) {
            return Optional.empty();
        }
        
        EntityManager em = emf.createEntityManager();
        try {
            citaRepository.setEntityManager(em);
            return Optional.ofNullable(citaRepository.findById(citaId));
        } finally {
            em.close();
        }
    }

    public List<Cita> listarTodasLasCitas() {
        EntityManager em = emf.createEntityManager();
        try {
            citaRepository.setEntityManager(em);
            return citaRepository.findAllWithDetails();
        } finally {
            em.close();
        }
    }

    public List<Cita> listarCitasPorEstado(EstadoCita estado) {
        if (estado == null) {
            return List.of();
        }
        
        EntityManager em = emf.createEntityManager();
        try {
            citaRepository.setEntityManager(em);
            return citaRepository.findByEstado(estado);
        } finally {
            em.close();
        }
    }

    public Cita actualizarCita(Long citaId, LocalDateTime nuevaFechaHora, String nuevoMotivo) {
        if (citaId == null) {
            throw new IllegalArgumentException("El ID de la cita no puede ser nulo");
        }
        
        if (nuevaFechaHora != null) {
            validarFechaFutura(nuevaFechaHora);
        }
        
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            
            citaRepository.setEntityManager(em);
            Cita cita = citaRepository.findById(citaId);
            
            if (cita == null) {
                throw new RuntimeException("No se encontró la cita con ID: " + citaId);
            }

            if (cita.getEstado() != EstadoCita.PROGRAMADA) {
                throw new RuntimeException("Solo se pueden actualizar citas en estado PROGRAMADA");
            }

            if (nuevaFechaHora != null && !nuevaFechaHora.equals(cita.getFechaHora())) {
                validarConflictoHorarios(cita.getMedico().getId(), nuevaFechaHora, citaId);
                cita.setFechaHora(nuevaFechaHora);
            }
            
            if (nuevoMotivo != null) {
                cita.setMotivo(nuevoMotivo);
            }
            
            Cita citaActualizada = citaRepository.merge(cita);
            em.getTransaction().commit();
            
            return citaActualizada;
            
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Error al actualizar la cita: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    private void validarDatosBasicos(Long pacienteId, Long medicoId, LocalDateTime fechaHora) {
        if (pacienteId == null) {
            throw new IllegalArgumentException("El ID del paciente no puede ser nulo");
        }
        if (medicoId == null) {
            throw new IllegalArgumentException("El ID del médico no puede ser nulo");
        }
        if (fechaHora == null) {
            throw new IllegalArgumentException("La fecha y hora de la cita no puede ser nula");
        }
    }

    private void validarFechaFutura(LocalDateTime fechaHora) {
        if (fechaHora.isBefore(LocalDateTime.now())) {
            throw new FechaInvalidaException(fechaHora);
        }
    }

    private void validarConflictoHorarios(Long medicoId, LocalDateTime fechaHora, Long citaIdExcluir) {

        boolean existeConflicto = citaRepository.existeConflictoHorario(medicoId, fechaHora, citaIdExcluir);
        
        if (existeConflicto) {
            Medico medico = medicoRepository.findById(medicoId);
            String nombreMedico = medico != null ? medico.getNombre() : "Desconocido";
            throw new CitaConflictoHorarioException(nombreMedico, fechaHora);
        }
    }

    public void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}