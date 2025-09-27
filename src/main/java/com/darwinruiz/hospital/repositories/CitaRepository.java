package com.darwinruiz.hospital.repositories;

import com.darwinruiz.hospital.enums.EstadoCita;
import com.darwinruiz.hospital.models.Cita;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class CitaRepository extends BaseRepository<Cita> {
    
    public CitaRepository() {
        super(Cita.class);
    }

    public List<Cita> findByPacienteId(Long pacienteId) {
        if (pacienteId == null) {
            return List.of();
        }
        
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT c FROM Cita c " +
                         "JOIN FETCH c.paciente p " +
                         "JOIN FETCH c.medico m " +
                         "WHERE p.id = ?1 " +
                         "ORDER BY c.fechaHora DESC";
            TypedQuery<Cita> query = em.createQuery(jpql, Cita.class);
            query.setParameter(1, pacienteId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Cita> findProximasCitasByMedicoId(Long medicoId) {
        if (medicoId == null) {
            return List.of();
        }
        
        EntityManager em = getEntityManager();
        try {
            LocalDateTime ahora = LocalDateTime.now();
            String jpql = "SELECT c FROM Cita c " +
                         "JOIN FETCH c.paciente p " +
                         "JOIN FETCH c.medico m " +
                         "WHERE m.id = ?1 AND c.fechaHora >= ?2 " +
                         "ORDER BY c.fechaHora ASC";
            TypedQuery<Cita> query = em.createQuery(jpql, Cita.class);
            query.setParameter(1, medicoId);
            query.setParameter(2, ahora);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Cita> findByRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            return List.of();
        }
        
        EntityManager em = getEntityManager();
        try {
            LocalDateTime inicioDateTime = fechaInicio.atStartOfDay();
            LocalDateTime finDateTime = fechaFin.atTime(23, 59, 59);
            
            String jpql = "SELECT c FROM Cita c " +
                         "JOIN FETCH c.paciente p " +
                         "JOIN FETCH c.medico m " +
                         "WHERE c.fechaHora BETWEEN ?1 AND ?2 " +
                         "ORDER BY c.fechaHora ASC";
            TypedQuery<Cita> query = em.createQuery(jpql, Cita.class);
            query.setParameter(1, inicioDateTime);
            query.setParameter(2, finDateTime);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Cita> findByEstado(EstadoCita estado) {
        if (estado == null) {
            return List.of();
        }
        
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT c FROM Cita c " +
                         "JOIN FETCH c.paciente p " +
                         "JOIN FETCH c.medico m " +
                         "WHERE c.estado = ?1 " +
                         "ORDER BY c.fechaHora ASC";
            TypedQuery<Cita> query = em.createQuery(jpql, Cita.class);
            query.setParameter(1, estado);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public boolean existeConflictoHorario(Long medicoId, LocalDateTime fechaHora, Long citaIdExcluir) {
        if (medicoId == null || fechaHora == null) {
            return false;
        }
        
        EntityManager em = getEntityManager();
        boolean shouldClose = (entityManager == null);
        
        try {
            String jpql = "SELECT COUNT(c) FROM Cita c " +
                         "WHERE c.medico.id = ?1 " +
                         "AND c.fechaHora = ?2 " +
                         "AND c.estado = 'PROGRAMADA'";
            
            if (citaIdExcluir != null) {
                jpql += " AND c.id != ?3";
            }
            
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter(1, medicoId);
            query.setParameter(2, fechaHora);
            
            if (citaIdExcluir != null) {
                query.setParameter(3, citaIdExcluir);
            }
            
            return query.getSingleResult() > 0;
        } finally {
            if (shouldClose) {
                em.close();
            }
        }
    }

    public boolean existeConflictoHorario(Long medicoId, LocalDateTime fechaHora) {
        return existeConflictoHorario(medicoId, fechaHora, null);
    }

    public List<Cita> findAllWithDetails() {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT c FROM Cita c " +
                         "JOIN FETCH c.paciente p " +
                         "JOIN FETCH c.medico m " +
                         "ORDER BY c.fechaHora DESC";
            TypedQuery<Cita> query = em.createQuery(jpql, Cita.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Cita> findByMedicoIdAndEstado(Long medicoId, EstadoCita estado) {
        if (medicoId == null || estado == null) {
            return List.of();
        }
        
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT c FROM Cita c " +
                         "JOIN FETCH c.paciente p " +
                         "JOIN FETCH c.medico m " +
                         "WHERE m.id = ?1 AND c.estado = ?2 " +
                         "ORDER BY c.fechaHora ASC";
            TypedQuery<Cita> query = em.createQuery(jpql, Cita.class);
            query.setParameter(1, medicoId);
            query.setParameter(2, estado);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Cita> findByPacienteIdAndEstado(Long pacienteId, EstadoCita estado) {
        if (pacienteId == null || estado == null) {
            return List.of();
        }
        
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT c FROM Cita c " +
                         "JOIN FETCH c.paciente p " +
                         "JOIN FETCH c.medico m " +
                         "WHERE p.id = ?1 AND c.estado = ?2 " +
                         "ORDER BY c.fechaHora DESC";
            TypedQuery<Cita> query = em.createQuery(jpql, Cita.class);
            query.setParameter(1, pacienteId);
            query.setParameter(2, estado);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Cita> findCitasHoy() {
        EntityManager em = getEntityManager();
        try {
            LocalDate hoy = LocalDate.now();
            LocalDateTime inicioHoy = hoy.atStartOfDay();
            LocalDateTime finHoy = hoy.atTime(23, 59, 59);
            
            String jpql = "SELECT c FROM Cita c " +
                         "JOIN FETCH c.paciente p " +
                         "JOIN FETCH c.medico m " +
                         "WHERE c.fechaHora BETWEEN ?1 AND ?2 " +
                         "AND c.estado = 'PROGRAMADA' " +
                         "ORDER BY c.fechaHora ASC";
            TypedQuery<Cita> query = em.createQuery(jpql, Cita.class);
            query.setParameter(1, inicioHoy);
            query.setParameter(2, finHoy);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public long countByEstado(EstadoCita estado) {
        if (estado == null) {
            return 0;
        }
        
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT COUNT(c) FROM Cita c WHERE c.estado = ?1";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter(1, estado);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    public Optional<Cita> findByIdWithDetails(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT c FROM Cita c " +
                         "JOIN FETCH c.paciente p " +
                         "JOIN FETCH c.medico m " +
                         "WHERE c.id = ?1";
            TypedQuery<Cita> query = em.createQuery(jpql, Cita.class);
            query.setParameter(1, id);
            List<Cita> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }
}