package com.darwinruiz.hospital.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para la entidad HistorialMedico y su relación OneToOne con Paciente
 */
class HistorialMedicoTest {

    private Paciente paciente;
    private HistorialMedico historialMedico;

    @BeforeEach
    void setUp() {
        paciente = new Paciente(
            "Juan Pérez",
            "1234567890123",
            LocalDate.of(1990, 5, 15),
            "12345678",
            "juan.perez@email.com"
        );
    }

    @Nested
    @DisplayName("Construcción de HistorialMedico")
    class ConstruccionHistorialMedico {

        @Test
        @DisplayName("Debe crear historial médico vacío correctamente")
        void debeCrearHistorialMedicoVacio() {
            historialMedico = new HistorialMedico();
            
            assertNotNull(historialMedico);
            assertNull(historialMedico.getId());
            assertNull(historialMedico.getPaciente());
            assertNull(historialMedico.getAlergias());
            assertNull(historialMedico.getAntecedentes());
            assertNull(historialMedico.getObservaciones());
        }

        @Test
        @DisplayName("Debe crear historial médico con paciente")
        void debeCrearHistorialMedicoConPaciente() {
            historialMedico = new HistorialMedico(paciente);
            
            assertNotNull(historialMedico);
            assertEquals(paciente, historialMedico.getPaciente());
            assertNull(historialMedico.getAlergias());
            assertNull(historialMedico.getAntecedentes());
            assertNull(historialMedico.getObservaciones());
        }

        @Test
        @DisplayName("Debe crear historial médico completo")
        void debeCrearHistorialMedicoCompleto() {
            String alergias = "Penicilina, Polen";
            String antecedentes = "Hipertensión familiar";
            String observaciones = "Paciente colaborador";
            
            historialMedico = new HistorialMedico(paciente, alergias, antecedentes, observaciones);
            
            assertNotNull(historialMedico);
            assertEquals(paciente, historialMedico.getPaciente());
            assertEquals(alergias, historialMedico.getAlergias());
            assertEquals(antecedentes, historialMedico.getAntecedentes());
            assertEquals(observaciones, historialMedico.getObservaciones());
        }

        @Test
        @DisplayName("Debe fallar al crear historial con paciente nulo")
        void debeFallarAlCrearHistorialConPacienteNulo() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new HistorialMedico(null)
            );
            
            assertEquals("El paciente no puede ser nulo", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Validaciones de campos")
    class ValidacionesCampos {

        @BeforeEach
        void setUp() {
            historialMedico = new HistorialMedico(paciente);
        }

        @Test
        @DisplayName("Debe validar longitud máxima de alergias")
        void debeValidarLongitudMaximaAlergias() {
            String alergiasLargas = "A".repeat(501);
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> historialMedico.setAlergias(alergiasLargas)
            );
            
            assertEquals("Las alergias no pueden tener más de 500 caracteres", exception.getMessage());
        }

        @Test
        @DisplayName("Debe aceptar alergias de longitud válida")
        void debeAceptarAlergiasLongitudValida() {
            String alergias = "A".repeat(500);
            
            assertDoesNotThrow(() -> historialMedico.setAlergias(alergias));
            assertEquals(alergias, historialMedico.getAlergias());
        }

        @Test
        @DisplayName("Debe validar longitud máxima de antecedentes")
        void debeValidarLongitudMaximaAntecedentes() {
            String antecedentesLargos = "A".repeat(1001);
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> historialMedico.setAntecedentes(antecedentesLargos)
            );
            
            assertEquals("Los antecedentes no pueden tener más de 1000 caracteres", exception.getMessage());
        }

        @Test
        @DisplayName("Debe aceptar antecedentes de longitud válida")
        void debeAceptarAntecedentesLongitudValida() {
            String antecedentes = "A".repeat(1000);
            
            assertDoesNotThrow(() -> historialMedico.setAntecedentes(antecedentes));
            assertEquals(antecedentes, historialMedico.getAntecedentes());
        }

        @Test
        @DisplayName("Debe validar longitud máxima de observaciones")
        void debeValidarLongitudMaximaObservaciones() {
            String observacionesLargas = "A".repeat(1001);
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> historialMedico.setObservaciones(observacionesLargas)
            );
            
            assertEquals("Las observaciones no pueden tener más de 1000 caracteres", exception.getMessage());
        }

        @Test
        @DisplayName("Debe aceptar observaciones de longitud válida")
        void debeAceptarObservacionesLongitudValida() {
            String observaciones = "A".repeat(1000);
            
            assertDoesNotThrow(() -> historialMedico.setObservaciones(observaciones));
            assertEquals(observaciones, historialMedico.getObservaciones());
        }

        @Test
        @DisplayName("Debe aceptar campos nulos")
        void debeAceptarCamposNulos() {
            assertDoesNotThrow(() -> {
                historialMedico.setAlergias(null);
                historialMedico.setAntecedentes(null);
                historialMedico.setObservaciones(null);
            });
            
            assertNull(historialMedico.getAlergias());
            assertNull(historialMedico.getAntecedentes());
            assertNull(historialMedico.getObservaciones());
        }
    }

    @Nested
    @DisplayName("Relación OneToOne con Paciente")
    class RelacionOneToOnePaciente {

        @Test
        @DisplayName("Debe establecer relación OneToOne correctamente")
        void debeEstablecerRelacionOneToOneCorrectamente() {
            historialMedico = new HistorialMedico(paciente);
            
            // Verificar que la relación se establece correctamente
            assertEquals(paciente, historialMedico.getPaciente());
            
            // Establecer la relación bidireccional
            paciente.setHistorialMedico(historialMedico);
            assertEquals(historialMedico, paciente.getHistorialMedico());
        }

        @Test
        @DisplayName("Debe usar @MapsId para compartir ID con Paciente")
        void debeUsarMapsIdParaCompartirIdConPaciente() {
            historialMedico = new HistorialMedico(paciente);
            
            // Simular que el paciente tiene ID (como si fuera persistido)
            paciente.setId(1L);
            
            // El historial debería poder usar el mismo ID
            historialMedico.setId(paciente.getId());
            assertEquals(paciente.getId(), historialMedico.getId());
        }

        @Test
        @DisplayName("Debe fallar al establecer paciente nulo")
        void debeFallarAlEstablecerPacienteNulo() {
            historialMedico = new HistorialMedico(paciente);
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> historialMedico.setPaciente(null)
            );
            
            assertEquals("El paciente no puede ser nulo", exception.getMessage());
        }

        @Test
        @DisplayName("Debe mantener integridad de relación bidireccional")
        void debeMantenerIntegridadRelacionBidireccional() {
            historialMedico = new HistorialMedico(paciente);
            paciente.setHistorialMedico(historialMedico);
            
            // Verificar relación bidireccional
            assertEquals(paciente, historialMedico.getPaciente());
            assertEquals(historialMedico, paciente.getHistorialMedico());
            
            // Cambiar paciente del historial
            Paciente nuevoPaciente = new Paciente(
                "María García",
                "9876543210987",
                LocalDate.of(1985, 8, 20),
                "87654321",
                "maria.garcia@email.com"
            );
            
            historialMedico.setPaciente(nuevoPaciente);
            assertEquals(nuevoPaciente, historialMedico.getPaciente());
        }
    }

    @Nested
    @DisplayName("Métodos de utilidad")
    class MetodosUtilidad {

        @BeforeEach
        void setUp() {
            historialMedico = new HistorialMedico(paciente);
        }

        @Test
        @DisplayName("Debe detectar cuando no tiene información")
        void debeDetectarCuandoNoTieneInformacion() {
            assertFalse(historialMedico.tieneInformacion());
            
            historialMedico.setAlergias("");
            historialMedico.setAntecedentes("   ");
            historialMedico.setObservaciones(null);
            assertFalse(historialMedico.tieneInformacion());
        }

        @Test
        @DisplayName("Debe detectar cuando tiene información en alergias")
        void debeDetectarCuandoTieneInformacionEnAlergias() {
            historialMedico.setAlergias("Penicilina");
            assertTrue(historialMedico.tieneInformacion());
        }

        @Test
        @DisplayName("Debe detectar cuando tiene información en antecedentes")
        void debeDetectarCuandoTieneInformacionEnAntecedentes() {
            historialMedico.setAntecedentes("Diabetes familiar");
            assertTrue(historialMedico.tieneInformacion());
        }

        @Test
        @DisplayName("Debe detectar cuando tiene información en observaciones")
        void debeDetectarCuandoTieneInformacionEnObservaciones() {
            historialMedico.setObservaciones("Paciente colaborador");
            assertTrue(historialMedico.tieneInformacion());
        }

        @Test
        @DisplayName("Debe actualizar toda la información correctamente")
        void debeActualizarTodaLaInformacionCorrectamente() {
            String alergias = "Polen, Ácaros";
            String antecedentes = "Asma infantil";
            String observaciones = "Requiere seguimiento";
            
            historialMedico.actualizarInformacion(alergias, antecedentes, observaciones);
            
            assertEquals(alergias, historialMedico.getAlergias());
            assertEquals(antecedentes, historialMedico.getAntecedentes());
            assertEquals(observaciones, historialMedico.getObservaciones());
        }

        @Test
        @DisplayName("Debe validar al actualizar información")
        void debeValidarAlActualizarInformacion() {
            String alergiasLargas = "A".repeat(501);
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> historialMedico.actualizarInformacion(alergiasLargas, "Antecedentes", "Observaciones")
            );
            
            assertEquals("Las alergias no pueden tener más de 500 caracteres", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Métodos equals y hashCode")
    class MetodosEqualsHashCode {

        @Test
        @DisplayName("Debe ser igual a sí mismo")
        void debeSerIgualASiMismo() {
            historialMedico = new HistorialMedico(paciente);
            historialMedico.setId(1L);
            
            assertEquals(historialMedico, historialMedico);
            assertEquals(historialMedico.hashCode(), historialMedico.hashCode());
        }

        @Test
        @DisplayName("Debe ser igual a otro historial con mismo ID")
        void debeSerIgualAOtroHistorialConMismoId() {
            historialMedico = new HistorialMedico(paciente);
            historialMedico.setId(1L);
            
            HistorialMedico otroHistorial = new HistorialMedico(paciente);
            otroHistorial.setId(1L);
            
            assertEquals(historialMedico, otroHistorial);
            assertEquals(historialMedico.hashCode(), otroHistorial.hashCode());
        }

        @Test
        @DisplayName("No debe ser igual a historial con ID diferente")
        void noDebeSerIgualAHistorialConIdDiferente() {
            historialMedico = new HistorialMedico(paciente);
            historialMedico.setId(1L);
            
            HistorialMedico otroHistorial = new HistorialMedico(paciente);
            otroHistorial.setId(2L);
            
            assertNotEquals(historialMedico, otroHistorial);
        }

        @Test
        @DisplayName("No debe ser igual a null")
        void noDebeSerIgualANull() {
            historialMedico = new HistorialMedico(paciente);
            historialMedico.setId(1L);
            
            assertNotEquals(historialMedico, null);
        }

        @Test
        @DisplayName("No debe ser igual a objeto de diferente clase")
        void noDebeSerIgualAObjetoDiferenteClase() {
            historialMedico = new HistorialMedico(paciente);
            historialMedico.setId(1L);
            
            assertNotEquals(historialMedico, "string");
        }
    }

    @Nested
    @DisplayName("Método toString")
    class MetodoToString {

        @Test
        @DisplayName("Debe generar toString correctamente")
        void debeGenerarToStringCorrectamente() {
            historialMedico = new HistorialMedico(paciente, "Penicilina", "Hipertensión", "Colaborador");
            historialMedico.setId(1L);
            
            String toString = historialMedico.toString();
            
            assertTrue(toString.contains("HistorialMedico{"));
            assertTrue(toString.contains("id=1"));
            assertTrue(toString.contains("alergias='Penicilina'"));
            assertTrue(toString.contains("antecedentes='Hipertensión'"));
            assertTrue(toString.contains("observaciones='Colaborador'"));
            assertTrue(toString.contains("paciente=Juan Pérez"));
        }

        @Test
        @DisplayName("Debe manejar paciente nulo en toString")
        void debeManejarPacienteNuloEnToString() {
            historialMedico = new HistorialMedico();
            historialMedico.setAlergias("Penicilina");
            
            String toString = historialMedico.toString();
            
            assertTrue(toString.contains("paciente=null"));
        }
    }
}