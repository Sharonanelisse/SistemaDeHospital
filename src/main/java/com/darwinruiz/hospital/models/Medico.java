package com.darwinruiz.hospital.models;

import com.darwinruiz.hospital.enums.Especialidad;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "medico",
       uniqueConstraints = @UniqueConstraint(name = "uk_medico_colegiado", columnNames = "colegiado"))
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del médico es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El número de colegiado es obligatorio")
    @Size(max = 20, message = "El número de colegiado no puede exceder 20 caracteres")
    @Column(nullable = false, unique = true, length = 20)
    private String colegiado;

    @NotNull(message = "La especialidad es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Especialidad especialidad;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Column(nullable = false, length = 100)
    private String email;

    // Relación OneToMany con Cita
    @OneToMany(mappedBy = "medico", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<Cita> citas = new ArrayList<>();

    // Constructores
    public Medico() {}

    public Medico(String nombre, String colegiado, Especialidad especialidad, String email) {
        this.nombre = nombre;
        this.colegiado = colegiado;
        this.especialidad = especialidad;
        this.email = email;
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
    }

    public String getColegiado() {
        return colegiado;
    }

    public void setColegiado(String colegiado) {
        this.colegiado = colegiado;
    }

    public Especialidad getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(Especialidad especialidad) {
        this.especialidad = especialidad;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Cita> getCitas() {
        return citas;
    }

    public void setCitas(List<Cita> citas) {
        this.citas = citas;
    }

    public void addCita(Cita cita) {
        citas.add(cita);
        cita.setMedico(this);
    }

    public void removeCita(Cita cita) {
        citas.remove(cita);
        cita.setMedico(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Medico medico = (Medico) o;
        return Objects.equals(colegiado, medico.colegiado);
    }

    @Override
    public int hashCode() {
        return Objects.hash(colegiado);
    }

    @Override
    public String toString() {
        return "Medico{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", colegiado='" + colegiado + '\'' +
                ", especialidad=" + especialidad +
                ", email='" + email + '\'' +
                '}';
    }
}