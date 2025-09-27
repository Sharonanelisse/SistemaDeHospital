package com.darwinruiz.hospital.repositories;

import com.darwinruiz.hospital.models.Paciente;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class PacienteRepository extends BaseRepository<Paciente> {
    
    public PacienteRepository() {
        super(Paciente.class);
    }

    public Optional<Paciente> findByDpi(String dpi) {
        if (dpi == null || dpi.trim().isEmpty()) {
            return Optional.empty();
        }
        
        String jpql = "SELECT p FROM Paciente p WHERE p.dpi = ?1";
        return executeSingleResultQuery(jpql, dpi.trim());
    }

    public boolean existsByDpi(String dpi) {
        if (dpi == null || dpi.trim().isEmpty()) {
            return false;
        }
        
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT COUNT(p) FROM Paciente p WHERE p.dpi = ?1";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter(1, dpi.trim());
            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    public List<Paciente> findByNombreContaining(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return List.of();
        }
        
        String jpql = "SELECT p FROM Paciente p WHERE LOWER(p.nombre) LIKE LOWER(?1)";
        return executeQuery(jpql, "%" + nombre.trim() + "%");
    }

    public Optional<Paciente> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        
        String jpql = "SELECT p FROM Paciente p WHERE p.email = ?1";
        return executeSingleResultQuery(jpql, email.trim());
    }

    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT COUNT(p) FROM Paciente p WHERE p.email = ?1";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter(1, email.trim());
            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    public List<Paciente> findAllWithCitas() {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT DISTINCT p FROM Paciente p LEFT JOIN FETCH p.citas c ORDER BY p.nombre";
            TypedQuery<Paciente> query = em.createQuery(jpql, Paciente.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<Paciente> findByIdWithHistorial(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT p FROM Paciente p LEFT JOIN FETCH p.historialMedico WHERE p.id = ?1";
            TypedQuery<Paciente> query = em.createQuery(jpql, Paciente.class);
            query.setParameter(1, id);
            List<Paciente> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    public Optional<Paciente> findByIdWithAllRelations(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT p FROM Paciente p " +
                         "LEFT JOIN FETCH p.citas c " +
                         "LEFT JOIN FETCH p.historialMedico " +
                         "WHERE p.id = ?1";
            TypedQuery<Paciente> query = em.createQuery(jpql, Paciente.class);
            query.setParameter(1, id);
            List<Paciente> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    public List<Paciente> findPacientesWithCitasProgramadas() {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT DISTINCT p FROM Paciente p " +
                         "JOIN p.citas c " +
                         "WHERE c.estado = 'PROGRAMADA' " +
                         "ORDER BY p.nombre";
            TypedQuery<Paciente> query = em.createQuery(jpql, Paciente.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public long countPacientesWithHistorial() {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT COUNT(p) FROM Paciente p WHERE p.historialMedico IS NOT NULL";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
}