package com.darwinruiz.hospital.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Entity
@Table(name = "paciente", 
       uniqueConstraints = @UniqueConstraint(name = "uk_paciente_dpi", columnNames = "dpi"))
public class Paciente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String nombre;
    
    @Column(nullable = false, unique = true, length = 20)
    private String dpi;
    
    @Column(nullable = false)
    private LocalDate fechaNacimiento;
    
    @Column(length = 15)
    private String telefono;
    
    @Column(nullable = false, length = 100)
    private String email;

    @OneToOne(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    private HistorialMedico historialMedico;

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cita> citas = new ArrayList<>();

    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Paciente() {}
    
    public Paciente(String nombre, String dpi, LocalDate fechaNacimiento, String telefono, String email) {
        this.nombre = nombre;
        this.dpi = dpi;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.email = email;
        validarDatos();
    }

    public void validarDpi() {
        if (dpi == null || dpi.trim().isEmpty()) {
            throw new IllegalArgumentException("El DPI no puede ser nulo o vacío");
        }
        if (dpi.length() > 20) {
            throw new IllegalArgumentException("El DPI no puede tener más de 20 caracteres");
        }
    }

    public void validarEmail() {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El email no puede ser nulo o vacío");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("El formato del email no es válido");
        }
        if (email.length() > 100) {
            throw new IllegalArgumentException("El email no puede tener más de 100 caracteres");
        }
    }

    public void validarDatos() {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede ser nulo o vacío");
        }
        if (nombre.length() > 100) {
            throw new IllegalArgumentException("El nombre no puede tener más de 100 caracteres");
        }
        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser nula");
        }
        if (fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura");
        }
        if (telefono != null && telefono.length() > 15) {
            throw new IllegalArgumentException("El teléfono no puede tener más de 15 caracteres");
        }
        
        validarDpi();
        validarEmail();
    }

    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
        if (nombre != null) {
            if (nombre.trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre no puede ser vacío");
            }
            if (nombre.length() > 100) {
                throw new IllegalArgumentException("El nombre no puede tener más de 100 caracteres");
            }
        }
    }
    
    public String getDpi() {
        return dpi;
    }
    
    public void setDpi(String dpi) {
        this.dpi = dpi;
        if (dpi != null) {
            validarDpi();
        }
    }
    
    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }
    
    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
        if (fechaNacimiento != null && fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura");
        }
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        if (telefono != null && telefono.length() > 15) {
            throw new IllegalArgumentException("El teléfono no puede tener más de 15 caracteres");
        }
        this.telefono = telefono;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
        if (email != null) {
            validarEmail();
        }
    }
    
    public HistorialMedico getHistorialMedico() {
        return historialMedico;
    }
    
    public void setHistorialMedico(HistorialMedico historialMedico) {
        this.historialMedico = historialMedico;
    }
    
    public List<Cita> getCitas() {
        return citas;
    }
    
    public void setCitas(List<Cita> citas) {
        this.citas = citas != null ? citas : new ArrayList<>();
    }

    public void agregarCita(Cita cita) {
        if (cita != null) {
            citas.add(cita);
            cita.setPaciente(this);
        }
    }

    public void removerCita(Cita cita) {
        if (cita != null) {
            citas.remove(cita);
            cita.setPaciente(null);
        }
    }
    
    @Override
    public String toString() {
        return "Paciente{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", dpi='" + dpi + '\'' +
                ", fechaNacimiento=" + fechaNacimiento +
                ", telefono='" + telefono + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Paciente paciente = (Paciente) o;
        
        return dpi != null ? dpi.equals(paciente.dpi) : paciente.dpi == null;
    }
    
    @Override
    public int hashCode() {
        return dpi != null ? dpi.hashCode() : 0;
    }
}