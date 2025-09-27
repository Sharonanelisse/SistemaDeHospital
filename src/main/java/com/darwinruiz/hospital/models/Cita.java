package com.darwinruiz.hospital.models;

import com.darwinruiz.hospital.enums.EstadoCita;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "cita",
       indexes = {
           @Index(name = "ix_cita_paciente", columnList = "paciente_id"),
           @Index(name = "ix_cita_medico", columnList = "medico_id"),
           @Index(name = "ix_cita_fecha_hora", columnList = "fecha_hora")
       })
public class Cita {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "La fecha y hora de la cita es obligatoria")
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;
    
    @NotNull(message = "El estado de la cita es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCita estado = EstadoCita.PROGRAMADA;
    
    @Size(max = 200, message = "El motivo no puede exceder 200 caracteres")
    @Column(length = 200)
    private String motivo;

    @NotNull(message = "El paciente es obligatorio para la cita")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", foreignKey = @ForeignKey(name = "fk_cita_paciente"))
    private Paciente paciente;

    @NotNull(message = "El médico es obligatorio para la cita")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medico_id", foreignKey = @ForeignKey(name = "fk_cita_medico"))
    private Medico medico;

    public Cita() {}
    
    public Cita(LocalDateTime fechaHora, String motivo, Paciente paciente, Medico medico) {
        this.fechaHora = fechaHora;
        this.motivo = motivo;
        this.paciente = paciente;
        this.medico = medico;
        this.estado = EstadoCita.PROGRAMADA;
        validarDatos();
    }
    
    public Cita(LocalDateTime fechaHora, EstadoCita estado, String motivo, Paciente paciente, Medico medico) {
        this.fechaHora = fechaHora;
        this.estado = estado != null ? estado : EstadoCita.PROGRAMADA;
        this.motivo = motivo;
        this.paciente = paciente;
        this.medico = medico;
        validarDatos();
    }

    public void validarFechaFutura() {
        if (fechaHora != null && fechaHora.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha y hora de la cita no puede estar en el pasado");
        }
    }

    public void validarMotivo() {
        if (motivo != null && motivo.length() > 200) {
            throw new IllegalArgumentException("El motivo no puede exceder 200 caracteres");
        }
    }

    public void validarDatos() {
        if (fechaHora == null) {
            throw new IllegalArgumentException("La fecha y hora de la cita es obligatoria");
        }
        if (paciente == null) {
            throw new IllegalArgumentException("El paciente es obligatorio para la cita");
        }
        if (medico == null) {
            throw new IllegalArgumentException("El médico es obligatorio para la cita");
        }
        if (estado == null) {
            throw new IllegalArgumentException("El estado de la cita es obligatorio");
        }
        
        validarFechaFutura();
        validarMotivo();
    }

    public boolean puedeSerCancelada() {
        return estado == EstadoCita.PROGRAMADA;
    }

    public boolean puedeSerAtendida() {
        return estado == EstadoCita.PROGRAMADA;
    }

    public boolean esEnElFuturo() {
        return fechaHora != null && fechaHora.isAfter(LocalDateTime.now());
    }

    public boolean conflictaConHorario(Cita otraCita) {
        if (otraCita == null || otraCita.getFechaHora() == null || this.fechaHora == null) {
            return false;
        }
        return this.fechaHora.equals(otraCita.getFechaHora()) && 
               this.medico != null && 
               this.medico.equals(otraCita.getMedico());
    }

    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
    
    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
        if (fechaHora != null) {
            validarFechaFutura();
        }
    }
    
    public EstadoCita getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoCita estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado de la cita no puede ser nulo");
        }
        this.estado = estado;
    }
    
    public String getMotivo() {
        return motivo;
    }
    
    public void setMotivo(String motivo) {
        this.motivo = motivo;
        if (motivo != null) {
            validarMotivo();
        }
    }
    
    public Paciente getPaciente() {
        return paciente;
    }
    
    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }
    
    public Medico getMedico() {
        return medico;
    }
    
    public void setMedico(Medico medico) {
        this.medico = medico;
    }

    public void cambiarEstado(EstadoCita nuevoEstado) {
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("El nuevo estado no puede ser nulo");
        }

        switch (this.estado) {
            case PROGRAMADA:

                if (nuevoEstado != EstadoCita.ATENDIDA && nuevoEstado != EstadoCita.CANCELADA) {
                    throw new IllegalStateException("Desde PROGRAMADA solo se puede cambiar a ATENDIDA o CANCELADA");
                }
                break;
            case ATENDIDA:

                throw new IllegalStateException("Una cita ATENDIDA no puede cambiar de estado");
            case CANCELADA:

                throw new IllegalStateException("Una cita CANCELADA no puede cambiar de estado");
        }
        
        this.estado = nuevoEstado;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Cita cita = (Cita) o;
        
        return Objects.equals(fechaHora, cita.fechaHora) &&
               Objects.equals(paciente, cita.paciente) &&
               Objects.equals(medico, cita.medico);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(fechaHora, paciente, medico);
    }
    
    @Override
    public String toString() {
        return "Cita{" +
                "id=" + id +
                ", fechaHora=" + fechaHora +
                ", estado=" + estado +
                ", motivo='" + motivo + '\'' +
                ", paciente=" + (paciente != null ? paciente.getNombre() : "null") +
                ", medico=" + (medico != null ? medico.getNombre() : "null") +
                '}';
    }
}