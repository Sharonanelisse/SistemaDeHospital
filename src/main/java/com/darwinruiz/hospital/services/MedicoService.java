package com.darwinruiz.hospital.services;

import com.darwinruiz.hospital.enums.Especialidad;
import com.darwinruiz.hospital.exceptions.EmailInvalidoException;
import com.darwinruiz.hospital.exceptions.MedicoYaExisteException;
import com.darwinruiz.hospital.models.Medico;
import com.darwinruiz.hospital.repositories.MedicoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;


public class MedicoService {
    
    private final MedicoRepository medicoRepository;
    private final EntityManagerFactory emf;

    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    public MedicoService() {
        this.emf = Persistence.createEntityManagerFactory("HospitalSystemPU");
        this.medicoRepository = new MedicoRepository();
    }
    
    public MedicoService(EntityManagerFactory emf) {
        this.emf = emf;
        this.medicoRepository = new MedicoRepository();
    }

    public Medico registrarMedico(String nombre, String colegiado, Especialidad especialidad, String email) {

        validarDatosBasicos(nombre, colegiado, especialidad, email);

        validarColegiadoUnico(colegiado);

        validarFormatoEmail(email);
        
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Medico medico = new Medico(nombre, colegiado, especialidad, email);

            medicoRepository.setEntityManager(em);
            medicoRepository.persist(medico);
            
            em.getTransaction().commit();
            return medico;
            
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Error al registrar el médico: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public List<Medico> listarMedicos() {
        EntityManager em = emf.createEntityManager();
        try {
            medicoRepository.setEntityManager(em);
            return medicoRepository.findAll();
        } finally {
            em.close();
        }
    }

    public List<Medico> listarMedicosConProximasCitas() {
        EntityManager em = emf.createEntityManager();
        try {
            medicoRepository.setEntityManager(em);
            return medicoRepository.findAllWithProximasCitas();
        } finally {
            em.close();
        }
    }

    public Optional<Medico> buscarPorColegiado(String colegiado) {
        if (colegiado == null || colegiado.trim().isEmpty()) {
            return Optional.empty();
        }
        
        EntityManager em = emf.createEntityManager();
        try {
            medicoRepository.setEntityManager(em);
            return medicoRepository.findByColegiado(colegiado.trim());
        } finally {
            em.close();
        }
    }

    public Optional<Medico> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        
        EntityManager em = emf.createEntityManager();
        try {
            medicoRepository.setEntityManager(em);
            return Optional.ofNullable(medicoRepository.findById(id));
        } finally {
            em.close();
        }
    }

    public List<Medico> buscarPorEspecialidad(Especialidad especialidad) {
        if (especialidad == null) {
            return List.of();
        }
        
        EntityManager em = emf.createEntityManager();
        try {
            medicoRepository.setEntityManager(em);
            return medicoRepository.findByEspecialidad(especialidad);
        } finally {
            em.close();
        }
    }

    public Medico actualizarMedico(Long id, String nombre, String colegiado, 
                                 Especialidad especialidad, String email) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del médico no puede ser nulo");
        }

        validarDatosBasicos(nombre, colegiado, especialidad, email);
        
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            
            medicoRepository.setEntityManager(em);
            Medico medico = medicoRepository.findById(id);
            
            if (medico == null) {
                throw new RuntimeException("No se encontró el médico con ID: " + id);
            }

            if (!medico.getColegiado().equals(colegiado)) {
                validarColegiadoUnico(colegiado);
            }

            validarFormatoEmail(email);

            medico.setNombre(nombre);
            medico.setColegiado(colegiado);
            medico.setEspecialidad(especialidad);
            medico.setEmail(email);
            
            Medico medicoActualizado = medicoRepository.merge(medico);
            em.getTransaction().commit();
            
            return medicoActualizado;
            
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Error al actualizar el médico: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public boolean eliminarMedico(Long id) {
        if (id == null) {
            return false;
        }
        
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            
            medicoRepository.setEntityManager(em);
            Medico medico = medicoRepository.findById(id);
            
            if (medico != null) {
                if (medico.getCitas() != null && !medico.getCitas().isEmpty()) {
                    throw new RuntimeException("No se puede eliminar el médico porque tiene citas asociadas");
                }
                
                medicoRepository.remove(medico);
                em.getTransaction().commit();
                return true;
            }
            
            em.getTransaction().rollback();
            return false;
            
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Error al eliminar el médico: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    private void validarDatosBasicos(String nombre, String colegiado, Especialidad especialidad, String email) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del médico es obligatorio");
        }
        if (nombre.length() > 100) {
            throw new IllegalArgumentException("El nombre no puede exceder 100 caracteres");
        }
        
        if (colegiado == null || colegiado.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de colegiado es obligatorio");
        }
        if (colegiado.length() > 20) {
            throw new IllegalArgumentException("El número de colegiado no puede exceder 20 caracteres");
        }
        
        if (especialidad == null) {
            throw new IllegalArgumentException("La especialidad es obligatoria");
        }
        
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El email del médico es obligatorio");
        }
        if (email.length() > 100) {
            throw new IllegalArgumentException("El email no puede exceder 100 caracteres");
        }
    }

    private void validarColegiadoUnico(String colegiado) {
        EntityManager em = emf.createEntityManager();
        try {
            medicoRepository.setEntityManager(em);
            if (medicoRepository.existsByColegiado(colegiado)) {
                throw new MedicoYaExisteException(colegiado);
            }
        } finally {
            em.close();
        }
    }

    private void validarFormatoEmail(String email) {
        if (email != null && !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new EmailInvalidoException(email);
        }
    }

    public void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}