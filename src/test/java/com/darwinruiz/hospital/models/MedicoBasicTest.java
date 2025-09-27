package com.darwinruiz.hospital.models;

import com.darwinruiz.hospital.enums.Especialidad;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test básico para verificar que la entidad Medico compila y funciona correctamente.
 */
public class MedicoBasicTest {

    @Test
    public void testMedicoCreation() {
        // Crear un médico usando el constructor con parámetros
        Medico medico = new Medico("Dr. Juan Pérez", "COL12345", Especialidad.CARDIOLOGIA, "juan.perez@hospital.com");
        
        // Verificar que los valores se asignaron correctamente
        assertEquals("Dr. Juan Pérez", medico.getNombre());
        assertEquals("COL12345", medico.getColegiado());
        assertEquals(Especialidad.CARDIOLOGIA, medico.getEspecialidad());
        assertEquals("juan.perez@hospital.com", medico.getEmail());
        
        // Verificar que la lista de citas se inicializa
        assertNotNull(medico.getCitas());
        assertTrue(medico.getCitas().isEmpty());
    }

    @Test
    public void testMedicoDefaultConstructor() {
        // Crear un médico usando el constructor por defecto
        Medico medico = new Medico();
        
        // Verificar que la lista de citas se inicializa
        assertNotNull(medico.getCitas());
        assertTrue(medico.getCitas().isEmpty());
    }

    @Test
    public void testMedicoSettersAndGetters() {
        Medico medico = new Medico();
        
        // Probar setters
        medico.setNombre("Dra. María González");
        medico.setColegiado("COL67890");
        medico.setEspecialidad(Especialidad.PEDIATRIA);
        medico.setEmail("maria.gonzalez@hospital.com");
        
        // Verificar getters
        assertEquals("Dra. María González", medico.getNombre());
        assertEquals("COL67890", medico.getColegiado());
        assertEquals(Especialidad.PEDIATRIA, medico.getEspecialidad());
        assertEquals("maria.gonzalez@hospital.com", medico.getEmail());
    }

    @Test
    public void testMedicoEquals() {
        Medico medico1 = new Medico("Dr. A", "COL123", Especialidad.CARDIOLOGIA, "a@hospital.com");
        Medico medico2 = new Medico("Dr. B", "COL123", Especialidad.NEUROLOGIA, "b@hospital.com");
        Medico medico3 = new Medico("Dr. C", "COL456", Especialidad.CARDIOLOGIA, "c@hospital.com");
        
        // Médicos con mismo colegiado deben ser iguales
        assertEquals(medico1, medico2);
        
        // Médicos con diferente colegiado no deben ser iguales
        assertNotEquals(medico1, medico3);
    }

    @Test
    public void testMedicoHashCode() {
        Medico medico1 = new Medico("Dr. A", "COL123", Especialidad.CARDIOLOGIA, "a@hospital.com");
        Medico medico2 = new Medico("Dr. B", "COL123", Especialidad.NEUROLOGIA, "b@hospital.com");
        
        // HashCode debe ser igual para objetos iguales
        assertEquals(medico1.hashCode(), medico2.hashCode());
    }

    @Test
    public void testMedicoToString() {
        Medico medico = new Medico("Dr. Juan Pérez", "COL12345", Especialidad.CARDIOLOGIA, "juan.perez@hospital.com");
        String toString = medico.toString();
        
        // Verificar que toString contiene información básica
        assertTrue(toString.contains("Medico{"));
        assertTrue(toString.contains("nombre="));
        assertTrue(toString.contains("colegiado="));
        assertTrue(toString.contains("especialidad="));
        assertTrue(toString.contains("email="));
    }
}