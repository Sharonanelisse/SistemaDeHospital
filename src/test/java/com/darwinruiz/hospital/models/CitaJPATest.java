package com.darwinruiz.hospital.models;

import com.darwinruiz.hospital.enums.EstadoCita;
import com.darwinruiz.hospital.enums.Especialidad;
import jakarta.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para verificar las anotaciones JPA y configuración de la entidad Cita.
 * Verifica índices, claves foráneas, y configuración de columnas.
 */
@DisplayName("Cita - Tests de Anotaciones JPA")
class CitaJPATest {

    private Class<Cita> citaClass;

    @BeforeEach
    void setUp() {
        citaClass = Cita.class;
    }

    @Test
    @DisplayName("Debe tener anotación @Entity")
    void debeTenerAnotacionEntity() {
        assertTrue(citaClass.isAnnotationPresent(Entity.class));
    }

    @Test
    @DisplayName("Debe tener configuración correcta de @Table")
    void debeTenerConfiguracionCorrectaDeTable() {
        assertTrue(citaClass.isAnnotationPresent(Table.class));
        
        Table tableAnnotation = citaClass.getAnnotation(Table.class);
        assertEquals("cita", tableAnnotation.name());
        
        // Verificar índices
        Index[] indexes = tableAnnotation.indexes();
        assertEquals(3, indexes.length);
        
        // Verificar índice por paciente_id
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
    @DisplayName("Debe tener campo id con anotaciones correctas")
    void debeTenerCampoIdConAnotacionesCorrectas() throws NoSuchFieldException {
        Field idField = citaClass.getDeclaredField("id");
        
        assertTrue(idField.isAnnotationPresent(Id.class));
        assertTrue(idField.isAnnotationPresent(GeneratedValue.class));
        
        GeneratedValue generatedValue = idField.getAnnotation(GeneratedValue.class);
        assertEquals(GenerationType.IDENTITY, generatedValue.strategy());
    }

    @Test
    @DisplayName("Debe tener campo fechaHora con anotaciones correctas")
    void debeTenerCampoFechaHoraConAnotacionesCorrectas() throws NoSuchFieldException {
        Field fechaHoraField = citaClass.getDeclaredField("fechaHora");
        
        assertTrue(fechaHoraField.isAnnotationPresent(Column.class));
        
        Column columnAnnotation = fechaHoraField.getAnnotation(Column.class);
        assertEquals("fecha_hora", columnAnnotation.name());
        assertFalse(columnAnnotation.nullable());
    }

    @Test
    @DisplayName("Debe tener campo estado con anotación @Enumerated")
    void debeTenerCampoEstadoConAnotacionEnumerated() throws NoSuchFieldException {
        Field estadoField = citaClass.getDeclaredField("estado");
        
        assertTrue(estadoField.isAnnotationPresent(Enumerated.class));
        assertTrue(estadoField.isAnnotationPresent(Column.class));
        
        Enumerated enumerated = estadoField.getAnnotation(Enumerated.class);
        assertEquals(EnumType.STRING, enumerated.value());
        
        Column columnAnnotation = estadoField.getAnnotation(Column.class);
        assertFalse(columnAnnotation.nullable());
    }

    @Test
    @DisplayName("Debe tener campo motivo con longitud correcta")
    void debeTenerCampoMotivoConLongitudCorrecta() throws NoSuchFieldException {
        Field motivoField = citaClass.getDeclaredField("motivo");
        
        assertTrue(motivoField.isAnnotationPresent(Column.class));
        
        Column columnAnnotation = motivoField.getAnnotation(Column.class);
        assertEquals(200, columnAnnotation.length());
    }

    @Test
    @DisplayName("Debe tener relación ManyToOne correcta con Paciente")
    void debeTenerRelacionManyToOneCorrectaConPaciente() throws NoSuchFieldException {
        Field pacienteField = citaClass.getDeclaredField("paciente");
        
        assertTrue(pacienteField.isAnnotationPresent(ManyToOne.class));
        assertTrue(pacienteField.isAnnotationPresent(JoinColumn.class));
        
        ManyToOne manyToOne = pacienteField.getAnnotation(ManyToOne.class);
        assertEquals(FetchType.LAZY, manyToOne.fetch());
        assertFalse(manyToOne.optional());
        
        JoinColumn joinColumn = pacienteField.getAnnotation(JoinColumn.class);
        assertEquals("paciente_id", joinColumn.name());
        
        ForeignKey foreignKey = joinColumn.foreignKey();
        assertEquals("fk_cita_paciente", foreignKey.name());
    }

    @Test
    @DisplayName("Debe tener relación ManyToOne correcta con Medico")
    void debeTenerRelacionManyToOneCorrectaConMedico() throws NoSuchFieldException {
        Field medicoField = citaClass.getDeclaredField("medico");
        
        assertTrue(medicoField.isAnnotationPresent(ManyToOne.class));
        assertTrue(medicoField.isAnnotationPresent(JoinColumn.class));
        
        ManyToOne manyToOne = medicoField.getAnnotation(ManyToOne.class);
        assertEquals(FetchType.LAZY, manyToOne.fetch());
        assertFalse(manyToOne.optional());
        
        JoinColumn joinColumn = medicoField.getAnnotation(JoinColumn.class);
        assertEquals("medico_id", joinColumn.name());
        
        ForeignKey foreignKey = joinColumn.foreignKey();
        assertEquals("fk_cita_medico", foreignKey.name());
    }

    @Test
    @DisplayName("Debe tener validaciones Bean Validation correctas")
    void debeTenerValidacionesBeanValidationCorrectas() throws NoSuchFieldException {
        // Verificar @NotNull en fechaHora
        Field fechaHoraField = citaClass.getDeclaredField("fechaHora");
        assertTrue(fechaHoraField.isAnnotationPresent(jakarta.validation.constraints.NotNull.class));
        
        // Verificar @NotNull en estado
        Field estadoField = citaClass.getDeclaredField("estado");
        assertTrue(estadoField.isAnnotationPresent(jakarta.validation.constraints.NotNull.class));
        
        // Verificar @Size en motivo
        Field motivoField = citaClass.getDeclaredField("motivo");
        assertTrue(motivoField.isAnnotationPresent(jakarta.validation.constraints.Size.class));
        
        jakarta.validation.constraints.Size sizeAnnotation = 
            motivoField.getAnnotation(jakarta.validation.constraints.Size.class);
        assertEquals(200, sizeAnnotation.max());
        
        // Verificar @NotNull en paciente
        Field pacienteField = citaClass.getDeclaredField("paciente");
        assertTrue(pacienteField.isAnnotationPresent(jakarta.validation.constraints.NotNull.class));
        
        // Verificar @NotNull en medico
        Field medicoField = citaClass.getDeclaredField("medico");
        assertTrue(medicoField.isAnnotationPresent(jakarta.validation.constraints.NotNull.class));
    }

    @Test
    @DisplayName("Debe crear instancia con valores por defecto correctos")
    void debeCrearInstanciaConValoresPorDefectoCorrectos() {
        Cita cita = new Cita();
        
        assertNull(cita.getId());
        assertNull(cita.getFechaHora());
        assertEquals(EstadoCita.PROGRAMADA, cita.getEstado());
        assertNull(cita.getMotivo());
        assertNull(cita.getPaciente());
        assertNull(cita.getMedico());
    }

    @Test
    @DisplayName("Debe permitir persistencia con datos válidos")
    void debePermitirPersistenciaConDatosValidos() {
        // Crear entidades relacionadas
        Paciente paciente = new Paciente(
            "Juan Pérez",
            "1234567890123",
            LocalDate.of(1990, 1, 1),
            "12345678",
            "juan@email.com"
        );
        
        Medico medico = new Medico(
            "Dr. García",
            "COL123",
            Especialidad.CARDIOLOGIA,
            "garcia@hospital.com"
        );
        
        // Crear cita
        LocalDateTime fechaFutura = LocalDateTime.now().plusDays(1);
        Cita cita = new Cita(fechaFutura, "Consulta general", paciente, medico);
        
        // Verificar que se puede crear sin errores
        assertNotNull(cita);
        assertEquals(fechaFutura, cita.getFechaHora());
        assertEquals(EstadoCita.PROGRAMADA, cita.getEstado());
        assertEquals("Consulta general", cita.getMotivo());
        assertEquals(paciente, cita.getPaciente());
        assertEquals(medico, cita.getMedico());
    }

    @Test
    @DisplayName("Debe validar tipos de datos correctos")
    void debeValidarTiposDeDatosCorrectos() throws NoSuchFieldException {
        // Verificar tipo de id
        Field idField = citaClass.getDeclaredField("id");
        assertEquals(Long.class, idField.getType());
        
        // Verificar tipo de fechaHora
        Field fechaHoraField = citaClass.getDeclaredField("fechaHora");
        assertEquals(LocalDateTime.class, fechaHoraField.getType());
        
        // Verificar tipo de estado
        Field estadoField = citaClass.getDeclaredField("estado");
        assertEquals(EstadoCita.class, estadoField.getType());
        
        // Verificar tipo de motivo
        Field motivoField = citaClass.getDeclaredField("motivo");
        assertEquals(String.class, motivoField.getType());
        
        // Verificar tipo de paciente
        Field pacienteField = citaClass.getDeclaredField("paciente");
        assertEquals(Paciente.class, pacienteField.getType());
        
        // Verificar tipo de medico
        Field medicoField = citaClass.getDeclaredField("medico");
        assertEquals(Medico.class, medicoField.getType());
    }
}