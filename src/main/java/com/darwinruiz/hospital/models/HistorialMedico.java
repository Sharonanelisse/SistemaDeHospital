package com.darwinruiz.hospital.models;

import jakarta.persistence.*;

@Entity
@Table(name = "historial_medico")
public class HistorialMedico {
    
    @Id
    private Long id;
    
    @Column(length = 500)
    private String alergias;
    
    @Column(length = 1000)
    private String antecedentes;
    
    @Column(length = 1000)
    private String observaciones;

    @OneToOne
    @MapsId
    @JoinColumn(name = "paciente_id", foreignKey = @ForeignKey(name = "fk_historial_paciente"))
    private Paciente paciente;

    public HistorialMedico() {}
    
    public HistorialMedico(Paciente paciente) {
        this.paciente = paciente;
        validarDatos();
    }
    
    public HistorialMedico(Paciente paciente, String alergias, String antecedentes, String observaciones) {
        this.paciente = paciente;
        this.alergias = alergias;
        this.antecedentes = antecedentes;
        this.observaciones = observaciones;
        validarDatos();
    }

    public void validarPaciente() {
        if (paciente == null) {
            throw new IllegalArgumentException("El paciente no puede ser nulo");
        }
    }

    public void validarLongitudCampos() {
        if (alergias != null && alergias.length() > 500) {
            throw new IllegalArgumentException("Las alergias no pueden tener más de 500 caracteres");
        }
        if (antecedentes != null && antecedentes.length() > 1000) {
            throw new IllegalArgumentException("Los antecedentes no pueden tener más de 1000 caracteres");
        }
        if (observaciones != null && observaciones.length() > 1000) {
            throw new IllegalArgumentException("Las observaciones no pueden tener más de 1000 caracteres");
        }
    }

    public void validarDatos() {
        validarPaciente();
        validarLongitudCampos();
    }

    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Paciente getPaciente() {
        return paciente;
    }
    
    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
        if (paciente != null) {
            validarPaciente();
        }
    }
    
    public String getAlergias() {
        return alergias;
    }
    
    public void setAlergias(String alergias) {
        if (alergias != null && alergias.length() > 500) {
            throw new IllegalArgumentException("Las alergias no pueden tener más de 500 caracteres");
        }
        this.alergias = alergias;
    }
    
    public String getAntecedentes() {
        return antecedentes;
    }
    
    public void setAntecedentes(String antecedentes) {
        if (antecedentes != null && antecedentes.length() > 1000) {
            throw new IllegalArgumentException("Los antecedentes no pueden tener más de 1000 caracteres");
        }
        this.antecedentes = antecedentes;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        if (observaciones != null && observaciones.length() > 1000) {
            throw new IllegalArgumentException("Las observaciones no pueden tener más de 1000 caracteres");
        }
        this.observaciones = observaciones;
    }

    public boolean tieneInformacion() {
        return (alergias != null && !alergias.trim().isEmpty()) ||
               (antecedentes != null && !antecedentes.trim().isEmpty()) ||
               (observaciones != null && !observaciones.trim().isEmpty());
    }

    public void actualizarInformacion(String alergias, String antecedentes, String observaciones) {
        setAlergias(alergias);
        setAntecedentes(antecedentes);
        setObservaciones(observaciones);
    }
    
    @Override
    public String toString() {
        return "HistorialMedico{" +
                "id=" + id +
                ", alergias='" + alergias + '\'' +
                ", antecedentes='" + antecedentes + '\'' +
                ", observaciones='" + observaciones + '\'' +
                ", paciente=" + (paciente != null ? paciente.getNombre() : "null") +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        HistorialMedico that = (HistorialMedico) o;
        
        return id != null ? id.equals(that.id) : that.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}