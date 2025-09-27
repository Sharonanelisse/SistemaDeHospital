package com.darwinruiz.hospital.services;

import com.darwinruiz.hospital.exceptions.EmailInvalidoException;
import com.darwinruiz.hospital.exceptions.PacienteYaExisteException;
import com.darwinruiz.hospital.models.Paciente;
import com.darwinruiz.hospital.repositories.PacienteRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class PacienteService {
    
    private final PacienteRepository pacienteRepository;
    private final EntityManagerFactory emf;

    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    public PacienteService() {
        this.emf = Persistence.createEntityManagerFactory("HospitalSystemPU");
        this.pacienteRepository = new PacienteRepository();
    }
    
    public PacienteService(EntityManagerFactory emf) {
        this.emf = emf;
        this.pacienteRepository = new PacienteRepository();
    }

    public Paciente registrarPaciente(String nombre, String dpi, LocalDate fechaNacimiento, 
                                    String telefono, String email) {
        validarDatosBasicos(nombre, dpi, fechaNacimiento, email);

        validarDpiUnico(dpi);

        validarFormatoEmail(email);
        
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Paciente paciente = new Paciente(nombre, dpi, fechaNacimiento, telefono, email);

            pacienteRepository.setEntityManager(em);
            pacienteRepository.persist(paciente);
            
            em.getTransaction().commit();
            return paciente;
            
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Error al registrar el paciente: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public List<Paciente> listarPacientes() {
        EntityManager em = emf.createEntityManager();
        try {
            pacienteRepository.setEntityManager(em);
            return pacienteRepository.findAll();
        } finally {
            em.close();
        }
    }

    public Optional<Paciente> buscarPorDpi(String dpi) {
        if (dpi == null || dpi.trim().isEmpty()) {
            return Optional.empty();
        }
        
        EntityManager em = emf.createEntityManager();
        try {
            pacienteRepository.setEntityManager(em);
            return pacienteRepository.findByDpi(dpi.trim());
        } finally {
            em.close();
        }
    }

    public Optional<Paciente> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        
        EntityManager em = emf.createEntityManager();
        try {
            pacienteRepository.setEntityManager(em);
            return Optional.ofNullable(pacienteRepository.findById(id));
        } finally {
            em.close();
        }
    }

    public boolean eliminarPaciente(Long id) {
        if (id == null) {
            return false;
        }
        
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            
            pacienteRepository.setEntityManager(em);
            Paciente paciente = pacienteRepository.findById(id);
            
            if (paciente != null) {
                pacienteRepository.remove(paciente);
                em.getTransaction().commit();
                return true;
            }
            
            em.getTransaction().rollback();
            return false;
            
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Error al eliminar el paciente: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public Paciente actualizarPaciente(Long id, String nombre, String dpi, LocalDate fechaNacimiento,
                                     String telefono, String email) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del paciente no puede ser nulo");
        }

        validarDatosBasicos(nombre, dpi, fechaNacimiento, email);
        
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            
            pacienteRepository.setEntityManager(em);
            Paciente paciente = pacienteRepository.findById(id);
            
            if (paciente == null) {
                throw new RuntimeException("No se encontró el paciente con ID: " + id);
            }

            if (!paciente.getDpi().equals(dpi)) {
                validarDpiUnico(dpi);
            }

            validarFormatoEmail(email);

            paciente.setNombre(nombre);
            paciente.setDpi(dpi);
            paciente.setFechaNacimiento(fechaNacimiento);
            paciente.setTelefono(telefono);
            paciente.setEmail(email);
            
            Paciente pacienteActualizado = pacienteRepository.merge(paciente);
            em.getTransaction().commit();
            
            return pacienteActualizado;
            
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Error al actualizar el paciente: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    private void validarDatosBasicos(String nombre, String dpi, LocalDate fechaNacimiento, String email) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del paciente es obligatorio");
        }
        if (nombre.length() > 100) {
            throw new IllegalArgumentException("El nombre no puede exceder 100 caracteres");
        }
        
        if (dpi == null || dpi.trim().isEmpty()) {
            throw new IllegalArgumentException("El DPI del paciente es obligatorio");
        }
        if (dpi.length() > 20) {
            throw new IllegalArgumentException("El DPI no puede exceder 20 caracteres");
        }
        
        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
        }
        if (fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura");
        }
        
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El email del paciente es obligatorio");
        }
        if (email.length() > 100) {
            throw new IllegalArgumentException("El email no puede exceder 100 caracteres");
        }
    }

    private void validarDpiUnico(String dpi) {
        EntityManager em = emf.createEntityManager();
        try {
            pacienteRepository.setEntityManager(em);
            if (pacienteRepository.existsByDpi(dpi)) {
                throw new PacienteYaExisteException(dpi);
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