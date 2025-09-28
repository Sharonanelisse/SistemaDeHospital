package com.darwinruiz.hospital.models;

import com.darwinruiz.hospital.enums.EstadoCita;
import com.darwinruiz.hospital.enums.Especialidad;
import jakarta.persistence.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests específicos para verificar las anotaciones JPA y Bean Validation de la entidad Cita.
 */
@DisplayName("Cita - Tests de Anotaciones y Validaciones")
class CitaAnnotationTest {

    private Validator validator;
    private Paciente paciente;
    private Medico medico;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        paciente = new Paciente(
            "Juan Pérez",
            "1234567890123",
            LocalDate.of(1990, 1, 1),
            "12345678",
            "juan@email.com"
        );

        medico = new Medico(
            "Dr. García",
            "COL123",
            Especialidad.CARDIOLOGIA,
            "garcia@hospital.com"
        );
    }

    @Test
    @DisplayName("Debe validar correctamente con Bean Validation - datos válidos")
    void debeValidarCorrectamenteConBeanValidationDatosValidos() {
        // Given
        LocalDateTime fechaFutura = LocalDateTime.now().plusDays(1);
        Cita cita = new Cita(fechaFutura, "Consulta general", paciente, medico);

        // When
        Set<ConstraintViolation<Cita>> violations = validator.validate(cita);

        // Then
        assertTrue(violations.isEmpty(), "No debe haber violaciones de validación");
    }

    @Test
    @DisplayName("Debe detectar violación cuando fechaHora es null")
    void debeDetectarViolacionCuandoFechaHoraEsNull() {
        // Given
        Cita cita = new Cita();
        cita.setFechaHora(null);
        cita.setEstado(EstadoCita.PROGRAMADA);
        cita.setMotivo("Consulta");
        cita.setPaciente(paciente);
        cita.setMedico(medico);

        // When
        Set<ConstraintViolation<Cita>> violations = validator.validate(cita);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("fecha y hora") || v.getMessage().contains("obligatoria")));
    }

    @Test
    @DisplayName("Debe detectar violación cuando estado es null")
    void debeDetectarViolacionCuandoEstadoEsNull() {
        // Given
        Cita cita = new Cita();
        cita.setFechaHora(LocalDateTime.now().plusDays(1));
        cita.setEstado(null);
        cita.setMotivo("Consulta");
        cita.setPaciente(paciente);
        cita.setMedico(medico);

        // When
        Set<ConstraintViolation<Cita>> violations = validator.validate(cita);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("estado") || v.getMessage().contains("obligatorio")));
    }

    @Test
    @DisplayName("Debe detectar violación cuando paciente es null")
    void debeDetectarViolacionCuandoPacienteEsNull() {
        // Given
        Cita cita = new Cita();
        cita.setFechaHora(LocalDateTime.now().plusDays(1));
        cita.setEstado(EstadoCita.PROGRAMADA);
        cita.setMotivo("Consulta");
        cita.setPaciente(null);
        cita.setMedico(medico);

        // When
        Set<ConstraintViolation<Cita>> violations = validator.validate(cita);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("paciente") || v.getMessage().contains("obligatorio")));
    }

    @Test
    @DisplayName("Debe detectar violación cuando médico es null")
    void debeDetectarViolacionCuandoMedicoEsNull() {
        // Given
        Cita cita = new Cita();
        cita.setFechaHora(LocalDateTime.now().plusDays(1));
        cita.setEstado(EstadoCita.PROGRAMADA);
        cita.setMotivo("Consulta");
        cita.setPaciente(paciente);
        cita.setMedico(null);

        // When
        Set<ConstraintViolation<Cita>> violations = validator.validate(cita);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("médico") || v.getMessage().contains("obligatorio")));
    }

    @Test
    @DisplayName("Debe detectar violación cuando motivo excede 200 caracteres")
    void debeDetectarViolacionCuandoMotivoExcede200Caracteres() {
        // Given
        String motivoLargo = "a".repeat(201);
        Cita cita = new Cita();
        cita.setFechaHora(LocalDateTime.now().plusDays(1));
        cita.setEstado(EstadoCita.PROGRAMADA);
        cita.setMotivo(motivoLargo);
        cita.setPaciente(paciente);
        cita.setMedico(medico);

        // When
        Set<ConstraintViolation<Cita>> violations = validator.validate(cita);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("200") || v.getMessage().contains("caracteres")));
    }

    @Test
    @DisplayName("Debe permitir motivo de exactamente 200 caracteres")
    void debePermitirMotivoDe200CaracteresExactos() {
        // Given
        String motivo200 = "a".repeat(200);
        LocalDateTime fechaFutura = LocalDateTime.now().plusDays(1);
        Cita cita = new Cita(fechaFutura, motivo200, paciente, medico);

        // When
        Set<ConstraintViolation<Cita>> violations = validator.validate(cita);

        // Then
        assertTrue(violations.isEmpty(), "Motivo de 200 caracteres debe ser válido");
    }

    @Test
    @DisplayName("Debe permitir motivo null")
    void debePermitirMotivoNull() {
        // Given
        LocalDateTime fechaFutura = LocalDateTime.now().plusDays(1);
        Cita cita = new Cita(fechaFutura, null, paciente, medico);

        // When
        Set<ConstraintViolation<Cita>> violations = validator.validate(cita);

        // Then
        assertTrue(violations.isEmpty(), "Motivo null debe ser válido");
    }

    @Test
    @DisplayName("Debe tener configuración correcta de enum EstadoCita")
    void debeTenerConfiguracionCorrectaDeEnumEstadoCita() throws NoSuchFieldException {
        // Given
        Field estadoField = Cita.class.getDeclaredField("estado");

        // When & Then
        assertTrue(estadoField.isAnnotationPresent(Enumerated.class));
        
        Enumerated enumerated = estadoField.getAnnotation(Enumerated.class);
        assertEquals(EnumType.STRING, enumerated.value());
    }

    @Test
    @DisplayName("Debe usar valores correctos del enum EstadoCita")
    void debeUsarValoresCorrectosDelEnumEstadoCita() {
        // Given
        LocalDateTime fechaFutura = LocalDateTime.now().plusDays(1);

        // When & Then - Probar todos los valores del enum
        Cita citaProgramada = new Cita(fechaFutura, EstadoCita.PROGRAMADA, "Motivo", paciente, medico);
        assertEquals(EstadoCita.PROGRAMADA, citaProgramada.getEstado());

        Cita citaAtendida = new Cita(fechaFutura, EstadoCita.ATENDIDA, "Motivo", paciente, medico);
        assertEquals(EstadoCita.ATENDIDA, citaAtendida.getEstado());

        Cita citaCancelada = new Cita(fechaFutura, EstadoCita.CANCELADA, "Motivo", paciente, medico);
        assertEquals(EstadoCita.CANCELADA, citaCancelada.getEstado());
    }

    @Test
    @DisplayName("Debe tener configuración correcta de índices en @Table")
    void debeTenerConfiguracionCorrectaDeIndicesEnTable() {
        // Given
        Class<Cita> citaClass = Cita.class;

        // When
        Table tableAnnotation = citaClass.getAnnotation(Table.class);

        // Then
        assertNotNull(tableAnnotation);
        assertEquals("cita", tableAnnotation.name());

        Index[] indexes = tableAnnotation.indexes();
        assertEquals(3, indexes.length);

        // Verificar que existen los índices esperados
        boolean tieneIndicePaciente = false;
        boolean tieneIndiceMedico = false;
        boolean tieneIndiceFechaHora = false;

        for (Index index : indexes) {
            switch (index.name()) {
                case "ix_cita_paciente":
                    assertEquals("paciente_id", index.columnList());
                    tieneIndicePaciente = true;
                    break;
                case "ix_cita_medico":
                    assertEquals("medico_id", index.columnList());
                    tieneIndiceMedico = true;
                    break;
                case "ix_cita_fecha_hora":
                    assertEquals("fecha_hora", index.columnList());
                    tieneIndiceFechaHora = true;
                    break;
            }
        }

        assertTrue(tieneIndicePaciente, "Debe tener índice por paciente_id");
        assertTrue(tieneIndiceMedico, "Debe tener índice por medico_id");
        assertTrue(tieneIndiceFechaHora, "Debe tener índice por fecha_hora");
    }

    @Test
    @DisplayName("Debe tener configuración correcta de claves foráneas")
    void debeTenerConfiguracionCorrectaDeClavesForaneas() throws NoSuchFieldException {
        // Verificar clave foránea de paciente
        Field pacienteField = Cita.class.getDeclaredField("paciente");
        JoinColumn pacienteJoinColumn = pacienteField.getAnnotation(JoinColumn.class);
        
        assertNotNull(pacienteJoinColumn);
        assertEquals("paciente_id", pacienteJoinColumn.name());
        assertEquals("fk_cita_paciente", pacienteJoinColumn.foreignKey().name());

        // Verificar clave foránea de médico
        Field medicoField = Cita.class.getDeclaredField("medico");
        JoinColumn medicoJoinColumn = medicoField.getAnnotation(JoinColumn.class);
        
        assertNotNull(medicoJoinColumn);
        assertEquals("medico_id", medicoJoinColumn.name());
        assertEquals("fk_cita_medico", medicoJoinColumn.foreignKey().name());
    }

    @Test
    @DisplayName("Debe tener configuración correcta de relaciones ManyToOne")
    void debeTenerConfiguracionCorrectaDeRelacionesManyToOne() throws NoSuchFieldException {
        // Verificar relación con Paciente
        Field pacienteField = Cita.class.getDeclaredField("paciente");
        ManyToOne pacienteManyToOne = pacienteField.getAnnotation(ManyToOne.class);
        
        assertNotNull(pacienteManyToOne);
        assertEquals(FetchType.LAZY, pacienteManyToOne.fetch());
        assertFalse(pacienteManyToOne.optional());

        // Verificar relación con Medico
        Field medicoField = Cita.class.getDeclaredField("medico");
        ManyToOne medicoManyToOne = medicoField.getAnnotation(ManyToOne.class);
        
        assertNotNull(medicoManyToOne);
        assertEquals(FetchType.LAZY, medicoManyToOne.fetch());
        assertFalse(medicoManyToOne.optional());
    }

    @Test
    @DisplayName("Debe tener configuración correcta de columna fecha_hora")
    void debeTenerConfiguracionCorrectaDeColumnaFechaHora() throws NoSuchFieldException {
        // Given
        Field fechaHoraField = Cita.class.getDeclaredField("fechaHora");

        // When & Then
        assertTrue(fechaHoraField.isAnnotationPresent(Column.class));
        
        Column columnAnnotation = fechaHoraField.getAnnotation(Column.class);
        assertEquals("fecha_hora", columnAnnotation.name());
        assertFalse(columnAnnotation.nullable());
    }
}