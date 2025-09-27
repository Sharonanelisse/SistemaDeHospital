package com.darwinruiz.hospital.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para la entidad Paciente.
 * Verifica las validaciones de DPI único, email y otros campos.
 */
@DisplayName("Paciente Entity Tests")
class PacienteTest {

    private Paciente paciente;
    private LocalDate fechaNacimientoValida;

    @BeforeEach
    void setUp() {
        fechaNacimientoValida = LocalDate.of(1990, 5, 15);
        paciente = new Paciente();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Debe crear paciente con datos válidos")
        void debeCrearPacienteConDatosValidos() {
            // Given
            String nombre = "Juan Pérez";
            String dpi = "1234567890123";
            String telefono = "12345678";
            String email = "juan@example.com";

            // When
            Paciente paciente = new Paciente(nombre, dpi, fechaNacimientoValida, telefono, email);

            // Then
            assertNotNull(paciente);
            assertEquals(nombre, paciente.getNombre());
            assertEquals(dpi, paciente.getDpi());
            assertEquals(fechaNacimientoValida, paciente.getFechaNacimiento());
            assertEquals(telefono, paciente.getTelefono());
            assertEquals(email, paciente.getEmail());
        }

        @Test
        @DisplayName("Debe lanzar excepción con datos inválidos en constructor")
        void debeLanzarExcepcionConDatosInvalidosEnConstructor() {
            // Given
            String nombre = "Juan Pérez";
            String dpi = "1234567890123";
            String telefono = "12345678";
            String emailInvalido = "email-invalido";

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                new Paciente(nombre, dpi, fechaNacimientoValida, telefono, emailInvalido);
            });
        }
    }

    @Nested
    @DisplayName("DPI Validation Tests")
    class DpiValidationTests {

        @Test
        @DisplayName("Debe validar DPI válido")
        void debeValidarDpiValido() {
            // Given
            paciente.setDpi("1234567890123");

            // When & Then
            assertDoesNotThrow(() -> paciente.validarDpi());
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando DPI es nulo")
        void debeLanzarExcepcionCuandoDpiEsNulo() {
            // Given
            paciente.setDpi(null);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> paciente.validarDpi());
            assertEquals("El DPI no puede ser nulo o vacío", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando DPI está vacío")
        void debeLanzarExcepcionCuandoDpiEstaVacio() {
            // Given
            paciente.setDpi("");

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> paciente.validarDpi());
            assertEquals("El DPI no puede ser nulo o vacío", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando DPI solo tiene espacios")
        void debeLanzarExcepcionCuandoDpiSoloTieneEspacios() {
            // Given
            paciente.setDpi("   ");

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> paciente.validarDpi());
            assertEquals("El DPI no puede ser nulo o vacío", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando DPI excede 20 caracteres")
        void debeLanzarExcepcionCuandoDpiExcede20Caracteres() {
            // Given
            String dpiLargo = "123456789012345678901"; // 21 caracteres
            paciente.setDpi(dpiLargo);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> paciente.validarDpi());
            assertEquals("El DPI no puede tener más de 20 caracteres", exception.getMessage());
        }

        @Test
        @DisplayName("Debe validar DPI con exactamente 20 caracteres")
        void debeValidarDpiConExactamente20Caracteres() {
            // Given
            String dpi20Chars = "12345678901234567890"; // 20 caracteres
            paciente.setDpi(dpi20Chars);

            // When & Then
            assertDoesNotThrow(() -> paciente.validarDpi());
        }
    }

    @Nested
    @DisplayName("Email Validation Tests")
    class EmailValidationTests {

        @Test
        @DisplayName("Debe validar email válido")
        void debeValidarEmailValido() {
            // Given
            paciente.setEmail("usuario@example.com");

            // When & Then
            assertDoesNotThrow(() -> paciente.validarEmail());
        }

        @Test
        @DisplayName("Debe validar email con subdominios")
        void debeValidarEmailConSubdominios() {
            // Given
            paciente.setEmail("usuario@mail.example.com");

            // When & Then
            assertDoesNotThrow(() -> paciente.validarEmail());
        }

        @Test
        @DisplayName("Debe validar email con números y caracteres especiales")
        void debeValidarEmailConNumerosYCaracteresEspeciales() {
            // Given
            paciente.setEmail("user123+test@example-site.co.uk");

            // When & Then
            assertDoesNotThrow(() -> paciente.validarEmail());
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando email es nulo")
        void debeLanzarExcepcionCuandoEmailEsNulo() {
            // Given
            paciente.setEmail(null);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> paciente.validarEmail());
            assertEquals("El email no puede ser nulo o vacío", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando email está vacío")
        void debeLanzarExcepcionCuandoEmailEstaVacio() {
            // Given
            paciente.setEmail("");

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> paciente.validarEmail());
            assertEquals("El email no puede ser nulo o vacío", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar excepción con formato de email inválido - sin @")
        void debeLanzarExcepcionConFormatoEmailInvalidoSinArroba() {
            // Given
            paciente.setEmail("usuarioexample.com");

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> paciente.validarEmail());
            assertEquals("El formato del email no es válido", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar excepción con formato de email inválido - sin dominio")
        void debeLanzarExcepcionConFormatoEmailInvalidoSinDominio() {
            // Given
            paciente.setEmail("usuario@");

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> paciente.validarEmail());
            assertEquals("El formato del email no es válido", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar excepción con formato de email inválido - sin extensión")
        void debeLanzarExcepcionConFormatoEmailInvalidoSinExtension() {
            // Given
            paciente.setEmail("usuario@example");

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> paciente.validarEmail());
            assertEquals("El formato del email no es válido", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando email excede 100 caracteres")
        void debeLanzarExcepcionCuandoEmailExcede100Caracteres() {
            // Given
            String emailLargo = "a".repeat(90) + "@example.com"; // Más de 100 caracteres
            paciente.setEmail(emailLargo);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> paciente.validarEmail());
            assertEquals("El email no puede tener más de 100 caracteres", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("General Data Validation Tests")
    class GeneralDataValidationTests {

        @Test
        @DisplayName("Debe validar todos los datos correctos")
        void debeValidarTodosLosDatosCorrectos() {
            // Given
            paciente.setNombre("Juan Pérez");
            paciente.setDpi("1234567890123");
            paciente.setFechaNacimiento(fechaNacimientoValida);
            paciente.setTelefono("12345678");
            paciente.setEmail("juan@example.com");

            // When & Then
            assertDoesNotThrow(() -> paciente.validarDatos());
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando nombre es nulo")
        void debeLanzarExcepcionCuandoNombreEsNulo() {
            // Given
            paciente.setNombre(null);
            paciente.setDpi("1234567890123");
            paciente.setFechaNacimiento(fechaNacimientoValida);
            paciente.setEmail("juan@example.com");

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> paciente.validarDatos());
            assertEquals("El nombre no puede ser nulo o vacío", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando nombre está vacío")
        void debeLanzarExcepcionCuandoNombreEstaVacio() {
            // Given
            paciente.setNombre("");
            paciente.setDpi("1234567890123");
            paciente.setFechaNacimiento(fechaNacimientoValida);
            paciente.setEmail("juan@example.com");

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> paciente.validarDatos());
            assertEquals("El nombre no puede ser nulo o vacío", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando nombre excede 100 caracteres")
        void debeLanzarExcepcionCuandoNombreExcede100Caracteres() {
            // Given
            String nombreLargo = "a".repeat(101);
            paciente.setNombre(nombreLargo);
            paciente.setDpi("1234567890123");
            paciente.setFechaNacimiento(fechaNacimientoValida);
            paciente.setEmail("juan@example.com");

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> paciente.validarDatos());
            assertEquals("El nombre no puede tener más de 100 caracteres", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando fecha de nacimiento es nula")
        void debeLanzarExcepcionCuandoFechaNacimientoEsNula() {
            // Given
            paciente.setNombre("Juan Pérez");
            paciente.setDpi("1234567890123");
            paciente.setFechaNacimiento(null);
            paciente.setEmail("juan@example.com");

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> paciente.validarDatos());
            assertEquals("La fecha de nacimiento no puede ser nula", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando fecha de nacimiento es futura")
        void debeLanzarExcepcionCuandoFechaNacimientoEsFutura() {
            // Given
            LocalDate fechaFutura = LocalDate.now().plusDays(1);
            paciente.setNombre("Juan Pérez");
            paciente.setDpi("1234567890123");
            paciente.setFechaNacimiento(fechaFutura);
            paciente.setEmail("juan@example.com");

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> paciente.validarDatos());
            assertEquals("La fecha de nacimiento no puede ser futura", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando teléfono excede 15 caracteres")
        void debeLanzarExcepcionCuandoTelefonoExcede15Caracteres() {
            // Given
            String telefonoLargo = "1234567890123456"; // 16 caracteres
            paciente.setNombre("Juan Pérez");
            paciente.setDpi("1234567890123");
            paciente.setFechaNacimiento(fechaNacimientoValida);
            paciente.setTelefono(telefonoLargo);
            paciente.setEmail("juan@example.com");

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> paciente.validarDatos());
            assertEquals("El teléfono no puede tener más de 15 caracteres", exception.getMessage());
        }

        @Test
        @DisplayName("Debe permitir teléfono nulo")
        void debePermitirTelefonoNulo() {
            // Given
            paciente.setNombre("Juan Pérez");
            paciente.setDpi("1234567890123");
            paciente.setFechaNacimiento(fechaNacimientoValida);
            paciente.setTelefono(null);
            paciente.setEmail("juan@example.com");

            // When & Then
            assertDoesNotThrow(() -> paciente.validarDatos());
        }
    }

    @Nested
    @DisplayName("Setter Validation Tests")
    class SetterValidationTests {

        @Test
        @DisplayName("Setter de DPI debe validar automáticamente")
        void setterDpiDebeValidarAutomaticamente() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                paciente.setDpi("");
            });
        }

        @Test
        @DisplayName("Setter de email debe validar automáticamente")
        void setterEmailDebeValidarAutomaticamente() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                paciente.setEmail("email-invalido");
            });
        }

        @Test
        @DisplayName("Setter de nombre debe validar automáticamente")
        void setterNombreDebeValidarAutomaticamente() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                paciente.setNombre("");
            });
        }

        @Test
        @DisplayName("Setter de fecha nacimiento debe validar automáticamente")
        void setterFechaNacimientoDebeValidarAutomaticamente() {
            // Given
            LocalDate fechaFutura = LocalDate.now().plusDays(1);

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                paciente.setFechaNacimiento(fechaFutura);
            });
        }

        @Test
        @DisplayName("Setter de teléfono debe validar automáticamente")
        void setterTelefonoDebeValidarAutomaticamente() {
            // Given
            String telefonoLargo = "1234567890123456"; // 16 caracteres

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                paciente.setTelefono(telefonoLargo);
            });
        }
    }

    @Nested
    @DisplayName("Utility Methods Tests")
    class UtilityMethodsTests {

        @Test
        @DisplayName("Debe agregar cita correctamente")
        void debeAgregarCitaCorrectamente() {
            // Given
            Cita cita = new Cita();

            // When
            paciente.agregarCita(cita);

            // Then
            assertTrue(paciente.getCitas().contains(cita));
            assertEquals(paciente, cita.getPaciente());
        }

        @Test
        @DisplayName("Debe remover cita correctamente")
        void debeRemoverCitaCorrectamente() {
            // Given
            Cita cita = new Cita();
            paciente.agregarCita(cita);

            // When
            paciente.removerCita(cita);

            // Then
            assertFalse(paciente.getCitas().contains(cita));
            assertNull(cita.getPaciente());
        }

        @Test
        @DisplayName("Debe manejar cita nula en agregar")
        void debeManejarCitaNulaEnAgregar() {
            // When & Then
            assertDoesNotThrow(() -> paciente.agregarCita(null));
        }

        @Test
        @DisplayName("Debe manejar cita nula en remover")
        void debeManejarCitaNulaEnRemover() {
            // When & Then
            assertDoesNotThrow(() -> paciente.removerCita(null));
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Debe ser igual cuando DPI es igual")
        void debeSerIgualCuandoDpiEsIgual() {
            // Given
            Paciente paciente1 = new Paciente();
            paciente1.setDpi("1234567890");
            
            Paciente paciente2 = new Paciente();
            paciente2.setDpi("1234567890");

            // When & Then
            assertEquals(paciente1, paciente2);
            assertEquals(paciente1.hashCode(), paciente2.hashCode());
        }

        @Test
        @DisplayName("No debe ser igual cuando DPI es diferente")
        void noDebeSerIgualCuandoDpiEsDiferente() {
            // Given
            Paciente paciente1 = new Paciente();
            paciente1.setDpi("1234567890");
            
            Paciente paciente2 = new Paciente();
            paciente2.setDpi("0987654321");

            // When & Then
            assertNotEquals(paciente1, paciente2);
        }

        @Test
        @DisplayName("Debe manejar DPI nulo en equals")
        void debeManejarDpiNuloEnEquals() {
            // Given
            Paciente paciente1 = new Paciente();
            paciente1.setDpi(null);
            
            Paciente paciente2 = new Paciente();
            paciente2.setDpi(null);

            // When & Then
            assertEquals(paciente1, paciente2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("ToString debe incluir campos principales")
        void toStringDebeIncluirCamposPrincipales() {
            // Given
            paciente.setId(1L);
            paciente.setNombre("Juan Pérez");
            paciente.setDpi("1234567890");
            paciente.setFechaNacimiento(fechaNacimientoValida);
            paciente.setTelefono("12345678");
            paciente.setEmail("juan@example.com");

            // When
            String resultado = paciente.toString();

            // Then
            assertTrue(resultado.contains("Juan Pérez"));
            assertTrue(resultado.contains("1234567890"));
            assertTrue(resultado.contains("juan@example.com"));
        }
    }
}