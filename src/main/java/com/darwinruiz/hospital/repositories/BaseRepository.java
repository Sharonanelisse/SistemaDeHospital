package com.darwinruiz.hospital.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;


public abstract class BaseRepository<T> {
    
    protected static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("HospitalSystemPU");
    protected final Class<T> entityClass;
    protected EntityManager entityManager;
    
    public BaseRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    protected EntityManager getEntityManager() {
        return entityManager != null ? entityManager : emf.createEntityManager();
    }

    public void persist(T entity) {
        if (entityManager != null) {

            entityManager.persist(entity);
        } else {

            EntityManager em = getEntityManager();
            try {
                em.getTransaction().begin();
                em.persist(entity);
                em.getTransaction().commit();
            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new RuntimeException("Error al persistir la entidad: " + e.getMessage(), e);
            } finally {
                em.close();
            }
        }
    }

    public T merge(T entity) {
        if (entityManager != null) {

            return entityManager.merge(entity);
        } else {

            EntityManager em = getEntityManager();
            try {
                em.getTransaction().begin();
                T mergedEntity = em.merge(entity);
                em.getTransaction().commit();
                return mergedEntity;
            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new RuntimeException("Error al actualizar la entidad: " + e.getMessage(), e);
            } finally {
                em.close();
            }
        }
    }

    public void remove(T entity) {
        if (entityManager != null) {

            T managedEntity = entityManager.merge(entity);
            entityManager.remove(managedEntity);
        } else {

            EntityManager em = getEntityManager();
            try {
                em.getTransaction().begin();
                T managedEntity = em.merge(entity);
                em.remove(managedEntity);
                em.getTransaction().commit();
            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new RuntimeException("Error al eliminar la entidad: " + e.getMessage(), e);
            } finally {
                em.close();
            }
        }
    }

    public void removeById(Long id) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            T entity = em.find(entityClass, id);
            if (entity != null) {
                em.remove(entity);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error al eliminar la entidad por ID: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public T findById(Long id) {
        EntityManager em = getEntityManager();
        if (entityManager != null) {

            return em.find(entityClass, id);
        } else {

            try {
                return em.find(entityClass, id);
            } finally {
                em.close();
            }
        }
    }

    public Optional<T> findByIdOptional(Long id) {
        return Optional.ofNullable(findById(id));
    }

    public List<T> findAll() {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
            TypedQuery<T> query = em.createQuery(jpql, entityClass);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public long count() {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    public boolean existsById(Long id) {
        return findByIdOptional(id).isPresent();
    }

    protected List<T> executeQuery(String jpql, Object... parameters) {
        EntityManager em = getEntityManager();
        if (entityManager != null) {
            // Usar EntityManager externo
            TypedQuery<T> query = em.createQuery(jpql, entityClass);
            for (int i = 0; i < parameters.length; i++) {
                query.setParameter(i + 1, parameters[i]);
            }
            return query.getResultList();
        } else {
            // Crear EntityManager propio
            try {
                TypedQuery<T> query = em.createQuery(jpql, entityClass);
                for (int i = 0; i < parameters.length; i++) {
                    query.setParameter(i + 1, parameters[i]);
                }
                return query.getResultList();
            } finally {
                em.close();
            }
        }
    }

    protected Optional<T> executeSingleResultQuery(String jpql, Object... parameters) {
        EntityManager em = getEntityManager();
        if (entityManager != null) {

            TypedQuery<T> query = em.createQuery(jpql, entityClass);
            for (int i = 0; i < parameters.length; i++) {
                query.setParameter(i + 1, parameters[i]);
            }
            List<T> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } else {
            try {
                TypedQuery<T> query = em.createQuery(jpql, entityClass);
                for (int i = 0; i < parameters.length; i++) {
                    query.setParameter(i + 1, parameters[i]);
                }
                List<T> results = query.getResultList();
                return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
            } finally {
                em.close();
            }
        }
    }

    protected <R> List<R> executeTypedQuery(String jpql, Class<R> resultClass, Object... parameters) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<R> query = em.createQuery(jpql, resultClass);
            for (int i = 0; i < parameters.length; i++) {
                query.setParameter(i + 1, parameters[i]);
            }
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public static void closeEntityManagerFactory() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}