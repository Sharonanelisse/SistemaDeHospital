package com.darwinruiz.hospital.models;

import com.darwinruiz.hospital.enums.Especialidad;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para la entidad Medico.
 * Verifica validaciones, constructores y métodos de utilidad.
 */
@DisplayName("Medico Entity Tests")
class MedicoTest {

    private Validator validator;
    private Medico medico;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        // Crear un médico válido para las pruebas
        medico = new Medico("Dr. Juan Pérez", "COL12345", Especialidad.CARDIOLOGIA, "juan.perez@hospital.com");
    }

    @Test
    @DisplayName("Debe crear médico válido sin violaciones")
    void debeCrearMedicoValidoSinViolaciones() {
        Set<ConstraintViolation<Medico>> violations = validator.validate(medico);
        assertTrue(violations.isEmpty(), "No debe haber violaciones para un médico válido");
    }

    @Test
    @DisplayName("Debe fallar cuando el nombre está vacío")
    void debeFallarCuandoNombreEstaVacio() {
        medico.setNombre("");
        
        Set<ConstraintViolation<Medico>> violations = validator.validate(medico);
        assertFalse(violations.isEmpty());
        
        boolean foundNombreViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("nombre"));
        assertTrue(foundNombreViolation, "Debe existir violación para nombre vacío");
    }

    @Test
    @DisplayName("Debe fallar cuando el nombre es null")
    void debeFallarCuandoNombreEsNull() {
        medico.setNombre(null);
        
        Set<ConstraintViolation<Medico>> violations = validator.validate(medico);
        assertFalse(violations.isEmpty());
        
        boolean foundNombreViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("nombre"));
        assertTrue(foundNombreViolation, "Debe existir violación para nombre null");
    }

    @Test
    @DisplayName("Debe fallar cuando el nombre excede 100 caracteres")
    void debeFallarCuandoNombreExcede100Caracteres() {
        String nombreLargo = "a".repeat(101);
        medico.setNombre(nombreLargo);
        
        Set<ConstraintViolation<Medico>> violations = validator.validate(medico);
        assertFalse(violations.isEmpty());
        
        boolean foundSizeViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("nombre") && 
                             v.getMessage().contains("100 caracteres"));
        assertTrue(foundSizeViolation, "Debe existir violación de tamaño para nombre");
    }

    @Test
    @DisplayName("Debe fallar cuando el colegiado está vacío")
    void debeFallarCuandoColegiadoEstaVacio() {
        medico.setColegiado("");
        
        Set<ConstraintViolation<Medico>> violations = validator.validate(medico);
        assertFalse(violations.isEmpty());
        
        boolean foundColegiadoViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("colegiado"));
        assertTrue(foundColegiadoViolation, "Debe existir violación para colegiado vacío");
    }

    @Test
    @DisplayName("Debe fallar cuando el colegiado es null")
    void debeFallarCuandoColegiadoEsNull() {
        medico.setColegiado(null);
        
        Set<ConstraintViolation<Medico>> violations = validator.validate(medico);
        assertFalse(violations.isEmpty());
        
        boolean foundColegiadoViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("colegiado"));
        assertTrue(foundColegiadoViolation, "Debe existir violación para colegiado null");
    }

    @Test
    @DisplayName("Debe fallar cuando el colegiado excede 20 caracteres")
    void debeFallarCuandoColegiadoExcede20Caracteres() {
        String colegiadoLargo = "COL" + "1".repeat(18);
        medico.setColegiado(colegiadoLargo);
        
        Set<ConstraintViolation<Medico>> violations = validator.validate(medico);
        assertFalse(violations.isEmpty());
        
        boolean foundSizeViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("colegiado") && 
                             v.getMessage().contains("20 caracteres"));
        assertTrue(foundSizeViolation, "Debe existir violación de tamaño para colegiado");
    }

    @Test
    @DisplayName("Debe fallar cuando la especialidad es null")
    void debeFallarCuandoEspecialidadEsNull() {
        medico.setEspecialidad(null);
        
        Set<ConstraintViolation<Medico>> violations = validator.validate(medico);
        assertFalse(violations.isEmpty());
        
        boolean foundEspecialidadViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("especialidad"));
        assertTrue(foundEspecialidadViolation, "Debe existir violación para especialidad null");
    }

    @Test
    @DisplayName("Debe aceptar todas las especialidades válidas")
    void debeAceptarTodasLasEspecialidadesValidas() {
        for (Especialidad especialidad : Especialidad.values()) {
            medico.setEspecialidad(especialidad);
            Set<ConstraintViolation<Medico>> violations = validator.validate(medico);
            
            boolean hasEspecialidadViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("especialidad"));
            assertFalse(hasEspecialidadViolation, 
                       "No debe haber violación para especialidad: " + especialidad);
        }
    }

    @Test
    @DisplayName("Debe fallar cuando el email está vacío")
    void debeFallarCuandoEmailEstaVacio() {
        medico.setEmail("");
        
        Set<ConstraintViolation<Medico>> violations = validator.validate(medico);
        assertFalse(violations.isEmpty());
        
        boolean foundEmailViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        assertTrue(foundEmailViolation, "Debe existir violación para email vacío");
    }

    @Test
    @DisplayName("Debe fallar cuando el email es null")
    void debeFallarCuandoEmailEsNull() {
        medico.setEmail(null);
        
        Set<ConstraintViolation<Medico>> violations = validator.validate(medico);
        assertFalse(violations.isEmpty());
        
        boolean foundEmailViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        assertTrue(foundEmailViolation, "Debe existir violación para email null");
    }

    @Test
    @DisplayName("Debe fallar cuando el formato del email es inválido")
    void debeFallarCuandoFormatoEmailEsInvalido() {
        String[] emailsInvalidos = {
            "email-invalido",
            "@hospital.com",
            "usuario@",
            "usuario.hospital.com",
            "usuario@@hospital.com"
        };
        
        for (String emailInvalido : emailsInvalidos) {
            medico.setEmail(emailInvalido);
            Set<ConstraintViolation<Medico>> violations = validator.validate(medico);
            
            boolean foundEmailViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("email") && 
                                 v.getMessage().contains("formato"));
            assertTrue(foundEmailViolation, 
                      "Debe existir violación de formato para email: " + emailInvalido);
        }
    }

    @Test
    @DisplayName("Debe aceptar emails con formato válido")
    void debeAceptarEmailsConFormatoValido() {
        String[] emailsValidos = {
            "doctor@hospital.com",
            "juan.perez@clinica.org",
            "medico123@centro-medico.net",
            "dr_martinez@hospital-nacional.gov.gt"
        };
        
        for (String emailValido : emailsValidos) {
            medico.setEmail(emailValido);
            Set<ConstraintViolation<Medico>> violations = validator.validate(medico);
            
            boolean hasEmailViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
            assertFalse(hasEmailViolation, 
                       "No debe haber violación para email válido: " + emailValido);
        }
    }

    @Test
    @DisplayName("Debe fallar cuando el email excede 100 caracteres")
    void debeFallarCuandoEmailExcede100Caracteres() {
        String emailLargo = "a".repeat(90) + "@hospital.com";
        medico.setEmail(emailLargo);
        
        Set<ConstraintViolation<Medico>> violations = validator.validate(medico);
        assertFalse(violations.isEmpty());
        
        boolean foundSizeViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email") && 
                             v.getMessage().contains("100 caracteres"));
        assertTrue(foundSizeViolation, "Debe existir violación de tamaño para email");
    }

    @Test
    @DisplayName("Debe inicializar lista de citas vacía")
    void debeInicializarListaCitasVacia() {
        Medico nuevoMedico = new Medico();
        assertNotNull(nuevoMedico.getCitas(), "La lista de citas no debe ser null");
        assertTrue(nuevoMedico.getCitas().isEmpty(), "La lista de citas debe estar vacía inicialmente");
    }

    @Test
    @DisplayName("Constructor con parámetros debe asignar valores correctamente")
    void constructorConParametrosDebeAsignarValoresCorrectamente() {
        String nombre = "Dra. María González";
        String colegiado = "COL67890";
        Especialidad especialidad = Especialidad.PEDIATRIA;
        String email = "maria.gonzalez@hospital.com";
        
        Medico nuevoMedico = new Medico(nombre, colegiado, especialidad, email);
        
        assertEquals(nombre, nuevoMedico.getNombre());
        assertEquals(colegiado, nuevoMedico.getColegiado());
        assertEquals(especialidad, nuevoMedico.getEspecialidad());
        assertEquals(email, nuevoMedico.getEmail());
        assertNotNull(nuevoMedico.getCitas());
        assertTrue(nuevoMedico.getCitas().isEmpty());
    }

    @Test
    @DisplayName("Equals debe comparar por colegiado")
    void equalsDebeCompararPorColegiado() {
        Medico medico1 = new Medico("Dr. A", "COL123", Especialidad.CARDIOLOGIA, "a@hospital.com");
        Medico medico2 = new Medico("Dr. B", "COL123", Especialidad.NEUROLOGIA, "b@hospital.com");
        Medico medico3 = new Medico("Dr. C", "COL456", Especialidad.CARDIOLOGIA, "c@hospital.com");
        
        assertEquals(medico1, medico2, "Médicos con mismo colegiado deben ser iguales");
        assertNotEquals(medico1, medico3, "Médicos con diferente colegiado no deben ser iguales");
    }

    @Test
    @DisplayName("HashCode debe ser consistente con equals")
    void hashCodeDebeSerConsistenteConEquals() {
        Medico medico1 = new Medico("Dr. A", "COL123", Especialidad.CARDIOLOGIA, "a@hospital.com");
        Medico medico2 = new Medico("Dr. B", "COL123", Especialidad.NEUROLOGIA, "b@hospital.com");
        
        assertEquals(medico1.hashCode(), medico2.hashCode(), 
                    "HashCode debe ser igual para objetos iguales");
    }

    @Test
    @DisplayName("ToString debe incluir información básica")
    void toStringDebeIncluirInformacionBasica() {
        String toString = medico.toString();
        
        assertTrue(toString.contains("Medico{"), "ToString debe incluir nombre de clase");
        assertTrue(toString.contains("nombre="), "ToString debe incluir nombre");
        assertTrue(toString.contains("colegiado="), "ToString debe incluir colegiado");
        assertTrue(toString.contains("especialidad="), "ToString debe incluir especialidad");
        assertTrue(toString.contains("email="), "ToString debe incluir email");
    }
}