package com.darwinruiz.hospital.repositories;

import com.darwinruiz.hospital.models.HistorialMedico;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class HistorialMedicoRepository extends BaseRepository<HistorialMedico> {
    
    public HistorialMedicoRepository() {
        super(HistorialMedico.class);
    }

    public Optional<HistorialMedico> findByPacienteId(Long pacienteId) {
        if (pacienteId == null) {
            return Optional.empty();
        }
        
        EntityManager em = getEntityManager();

        boolean shouldClose = (entityManager == null);
        
        try {
            String jpql = "SELECT h FROM HistorialMedico h " +
                         "JOIN FETCH h.paciente p " +
                         "WHERE p.id = ?1";
            TypedQuery<HistorialMedico> query = em.createQuery(jpql, HistorialMedico.class);
            query.setParameter(1, pacienteId);
            List<HistorialMedico> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            if (shouldClose) {
                em.close();
            }
        }
    }

    public boolean existsByPacienteId(Long pacienteId) {
        if (pacienteId == null) {
            return false;
        }
        
        EntityManager em = getEntityManager();

        boolean shouldClose = (entityManager == null);
        
        try {
            String jpql = "SELECT COUNT(h) FROM HistorialMedico h WHERE h.paciente.id = ?1";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter(1, pacienteId);
            return query.getSingleResult() > 0;
        } finally {
            if (shouldClose) {
                em.close();
            }
        }
    }

    public List<HistorialMedico> findAllWithPaciente() {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT h FROM HistorialMedico h " +
                         "JOIN FETCH h.paciente p " +
                         "ORDER BY p.nombre";
            TypedQuery<HistorialMedico> query = em.createQuery(jpql, HistorialMedico.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<HistorialMedico> findByAlergiasContaining(String alergia) {
        if (alergia == null || alergia.trim().isEmpty()) {
            return List.of();
        }
        
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT h FROM HistorialMedico h " +
                         "JOIN FETCH h.paciente p " +
                         "WHERE LOWER(h.alergias) LIKE LOWER(?1) " +
                         "ORDER BY p.nombre";
            TypedQuery<HistorialMedico> query = em.createQuery(jpql, HistorialMedico.class);
            query.setParameter(1, "%" + alergia.trim() + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<HistorialMedico> findByAntecedentesContaining(String antecedente) {
        if (antecedente == null || antecedente.trim().isEmpty()) {
            return List.of();
        }
        
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT h FROM HistorialMedico h " +
                         "JOIN FETCH h.paciente p " +
                         "WHERE LOWER(h.antecedentes) LIKE LOWER(?1) " +
                         "ORDER BY p.nombre";
            TypedQuery<HistorialMedico> query = em.createQuery(jpql, HistorialMedico.class);
            query.setParameter(1, "%" + antecedente.trim() + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<HistorialMedico> findByObservacionesContaining(String observacion) {
        if (observacion == null || observacion.trim().isEmpty()) {
            return List.of();
        }
        
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT h FROM HistorialMedico h " +
                         "JOIN FETCH h.paciente p " +
                         "WHERE LOWER(h.observaciones) LIKE LOWER(?1) " +
                         "ORDER BY p.nombre";
            TypedQuery<HistorialMedico> query = em.createQuery(jpql, HistorialMedico.class);
            query.setParameter(1, "%" + observacion.trim() + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<HistorialMedico> findHistorialesConInformacion() {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT h FROM HistorialMedico h " +
                         "JOIN FETCH h.paciente p " +
                         "WHERE (h.alergias IS NOT NULL AND TRIM(h.alergias) != '') " +
                         "OR (h.antecedentes IS NOT NULL AND TRIM(h.antecedentes) != '') " +
                         "OR (h.observaciones IS NOT NULL AND TRIM(h.observaciones) != '') " +
                         "ORDER BY p.nombre";
            TypedQuery<HistorialMedico> query = em.createQuery(jpql, HistorialMedico.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<HistorialMedico> findHistorialesVacios() {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT h FROM HistorialMedico h " +
                         "JOIN FETCH h.paciente p " +
                         "WHERE (h.alergias IS NULL OR TRIM(h.alergias) = '') " +
                         "AND (h.antecedentes IS NULL OR TRIM(h.antecedentes) = '') " +
                         "AND (h.observaciones IS NULL OR TRIM(h.observaciones) = '') " +
                         "ORDER BY p.nombre";
            TypedQuery<HistorialMedico> query = em.createQuery(jpql, HistorialMedico.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public long countHistorialesConInformacion() {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT COUNT(h) FROM HistorialMedico h " +
                         "WHERE (h.alergias IS NOT NULL AND TRIM(h.alergias) != '') " +
                         "OR (h.antecedentes IS NOT NULL AND TRIM(h.antecedentes) != '') " +
                         "OR (h.observaciones IS NOT NULL AND TRIM(h.observaciones) != '')";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    public Optional<HistorialMedico> findByIdWithPaciente(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT h FROM HistorialMedico h " +
                         "JOIN FETCH h.paciente p " +
                         "WHERE h.id = ?1";
            TypedQuery<HistorialMedico> query = em.createQuery(jpql, HistorialMedico.class);
            query.setParameter(1, id);
            List<HistorialMedico> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    public Optional<HistorialMedico> findByPacienteDpi(String dpi) {
        if (dpi == null || dpi.trim().isEmpty()) {
            return Optional.empty();
        }
        
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT h FROM HistorialMedico h " +
                         "JOIN FETCH h.paciente p " +
                         "WHERE p.dpi = ?1";
            TypedQuery<HistorialMedico> query = em.createQuery(jpql, HistorialMedico.class);
            query.setParameter(1, dpi.trim());
            List<HistorialMedico> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }
}