package com.darwinruiz.hospital.repositories;

import com.darwinruiz.hospital.enums.Especialidad;
import com.darwinruiz.hospital.models.Medico;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class MedicoRepository extends BaseRepository<Medico> {
    
    public MedicoRepository() {
        super(Medico.class);
    }

    public Optional<Medico> findByColegiado(String colegiado) {
        if (colegiado == null || colegiado.trim().isEmpty()) {
            return Optional.empty();
        }
        
        String jpql = "SELECT m FROM Medico m WHERE m.colegiado = ?1";
        return executeSingleResultQuery(jpql, colegiado.trim());
    }

    public boolean existsByColegiado(String colegiado) {
        if (colegiado == null || colegiado.trim().isEmpty()) {
            return false;
        }
        
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT COUNT(m) FROM Medico m WHERE m.colegiado = ?1";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter(1, colegiado.trim());
            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    public List<Medico> findByEspecialidad(Especialidad especialidad) {
        if (especialidad == null) {
            return List.of();
        }
        
        String jpql = "SELECT m FROM Medico m WHERE m.especialidad = ?1 ORDER BY m.nombre";
        return executeQuery(jpql, especialidad);
    }

    public List<Medico> findByNombreContaining(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return List.of();
        }
        
        String jpql = "SELECT m FROM Medico m WHERE LOWER(m.nombre) LIKE LOWER(?1) ORDER BY m.nombre";
        return executeQuery(jpql, "%" + nombre.trim() + "%");
    }

    public Optional<Medico> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        
        String jpql = "SELECT m FROM Medico m WHERE m.email = ?1";
        return executeSingleResultQuery(jpql, email.trim());
    }

    public List<Medico> findAllWithProximasCitas() {
        EntityManager em = getEntityManager();
        try {
            LocalDateTime ahora = LocalDateTime.now();
            String jpql = "SELECT DISTINCT m FROM Medico m " +
                         "LEFT JOIN FETCH m.citas c " +
                         "WHERE c.fechaHora >= ?1 OR c IS NULL " +
                         "ORDER BY m.nombre";
            TypedQuery<Medico> query = em.createQuery(jpql, Medico.class);
            query.setParameter(1, ahora);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<Medico> findByIdWithCitas(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT m FROM Medico m LEFT JOIN FETCH m.citas WHERE m.id = ?1";
            TypedQuery<Medico> query = em.createQuery(jpql, Medico.class);
            query.setParameter(1, id);
            List<Medico> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    public List<Medico> findMedicosWithCitasEnFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            return List.of();
        }
        
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT DISTINCT m FROM Medico m " +
                         "JOIN m.citas c " +
                         "WHERE c.fechaHora BETWEEN ?1 AND ?2 " +
                         "ORDER BY m.nombre";
            TypedQuery<Medico> query = em.createQuery(jpql, Medico.class);
            query.setParameter(1, fechaInicio);
            query.setParameter(2, fechaFin);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Medico> findMedicosDisponiblesEnFechaHora(LocalDateTime fechaHora) {
        if (fechaHora == null) {
            return List.of();
        }
        
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT m FROM Medico m " +
                         "WHERE m.id NOT IN (" +
                         "  SELECT c.medico.id FROM Cita c " +
                         "  WHERE c.fechaHora = ?1 AND c.estado = 'PROGRAMADA'" +
                         ") ORDER BY m.nombre";
            TypedQuery<Medico> query = em.createQuery(jpql, Medico.class);
            query.setParameter(1, fechaHora);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public long countByEspecialidad(Especialidad especialidad) {
        if (especialidad == null) {
            return 0;
        }
        
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT COUNT(m) FROM Medico m WHERE m.especialidad = ?1";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter(1, especialidad);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    public List<Medico> findMedicosOrderByCitasProgramadas() {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT m FROM Medico m " +
                         "LEFT JOIN m.citas c " +
                         "WHERE c.estado = 'PROGRAMADA' OR c IS NULL " +
                         "GROUP BY m.id, m.nombre, m.colegiado, m.especialidad, m.email " +
                         "ORDER BY COUNT(c) DESC, m.nombre";
            TypedQuery<Medico> query = em.createQuery(jpql, Medico.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}