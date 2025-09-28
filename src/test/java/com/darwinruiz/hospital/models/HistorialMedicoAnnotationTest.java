package com.darwinruiz.hospital.models;

import jakarta.persistence.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para verificar que las anotaciones JPA están correctamente configuradas en HistorialMedico
 */
class HistorialMedicoAnnotationTest {

    @Test
    @DisplayName("Debe tener anotación @Entity")
    void debeTenerAnotacionEntity() {
        assertTrue(HistorialMedico.class.isAnnotationPresent(Entity.class));
    }

    @Test
    @DisplayName("Debe tener anotación @Table con nombre correcto")
    void debeTenerAnotacionTableConNombreCorrecto() {
        assertTrue(HistorialMedico.class.isAnnotationPresent(Table.class));
        
        Table tableAnnotation = HistorialMedico.class.getAnnotation(Table.class);
        assertEquals("historial_medico", tableAnnotation.name());
    }

    @Test
    @DisplayName("Campo id debe tener anotación @Id")
    void campoIdDebeTenerAnotacionId() throws NoSuchFieldException {
        Field idField = HistorialMedico.class.getDeclaredField("id");
        assertTrue(idField.isAnnotationPresent(Id.class));
    }

    @Test
    @DisplayName("Campo paciente debe tener configuración OneToOne correcta")
    void campoPacienteDebeTenerConfiguracionOneToOneCorrecta() throws NoSuchFieldException {
        Field pacienteField = HistorialMedico.class.getDeclaredField("paciente");
        
        // Verificar @OneToOne
        assertTrue(pacienteField.isAnnotationPresent(OneToOne.class));
        
        // Verificar @MapsId
        assertTrue(pacienteField.isAnnotationPresent(MapsId.class));
        
        // Verificar @JoinColumn
        assertTrue(pacienteField.isAnnotationPresent(JoinColumn.class));
        JoinColumn joinColumn = pacienteField.getAnnotation(JoinColumn.class);
        assertEquals("paciente_id", joinColumn.name());
        
        // Verificar @ForeignKey
        ForeignKey foreignKey = joinColumn.foreignKey();
        assertEquals("fk_historial_paciente", foreignKey.name());
    }

    @Test
    @DisplayName("Campo alergias debe tener longitud correcta")
    void campoAlergiasDebeTenerLongitudCorrecta() throws NoSuchFieldException {
        Field alergiasField = HistorialMedico.class.getDeclaredField("alergias");
        assertTrue(alergiasField.isAnnotationPresent(Column.class));
        
        Column columnAnnotation = alergiasField.getAnnotation(Column.class);
        assertEquals(500, columnAnnotation.length());
    }

    @Test
    @DisplayName("Campo antecedentes debe tener longitud correcta")
    void campoAntecedentesDebeTenerLongitudCorrecta() throws NoSuchFieldException {
        Field antecedentesField = HistorialMedico.class.getDeclaredField("antecedentes");
        assertTrue(antecedentesField.isAnnotationPresent(Column.class));
        
        Column columnAnnotation = antecedentesField.getAnnotation(Column.class);
        assertEquals(1000, columnAnnotation.length());
    }

    @Test
    @DisplayName("Campo observaciones debe tener longitud correcta")
    void campoObservacionesDebeTenerLongitudCorrecta() throws NoSuchFieldException {
        Field observacionesField = HistorialMedico.class.getDeclaredField("observaciones");
        assertTrue(observacionesField.isAnnotationPresent(Column.class));
        
        Column columnAnnotation = observacionesField.getAnnotation(Column.class);
        assertEquals(1000, columnAnnotation.length());
    }

    @Test
    @DisplayName("Debe tener todos los métodos getter y setter requeridos")
    void debeTenerTodosLosMetodosGetterSetterRequeridos() throws NoSuchMethodException {
        // Verificar getters
        assertNotNull(HistorialMedico.class.getMethod("getId"));
        assertNotNull(HistorialMedico.class.getMethod("getPaciente"));
        assertNotNull(HistorialMedico.class.getMethod("getAlergias"));
        assertNotNull(HistorialMedico.class.getMethod("getAntecedentes"));
        assertNotNull(HistorialMedico.class.getMethod("getObservaciones"));
        
        // Verificar setters
        assertNotNull(HistorialMedico.class.getMethod("setId", Long.class));
        assertNotNull(HistorialMedico.class.getMethod("setPaciente", Paciente.class));
        assertNotNull(HistorialMedico.class.getMethod("setAlergias", String.class));
        assertNotNull(HistorialMedico.class.getMethod("setAntecedentes", String.class));
        assertNotNull(HistorialMedico.class.getMethod("setObservaciones", String.class));
    }

    @Test
    @DisplayName("Debe tener métodos de utilidad requeridos")
    void debeTenerMetodosUtilidadRequeridos() throws NoSuchMethodException {
        assertNotNull(HistorialMedico.class.getMethod("tieneInformacion"));
        assertNotNull(HistorialMedico.class.getMethod("actualizarInformacion", String.class, String.class, String.class));
        assertNotNull(HistorialMedico.class.getMethod("validarDatos"));
    }

    @Test
    @DisplayName("Debe tener métodos equals, hashCode y toString")
    void debeTenerMetodosEqualsHashCodeToString() throws NoSuchMethodException {
        assertNotNull(HistorialMedico.class.getMethod("equals", Object.class));
        assertNotNull(HistorialMedico.class.getMethod("hashCode"));
        assertNotNull(HistorialMedico.class.getMethod("toString"));
    }
}