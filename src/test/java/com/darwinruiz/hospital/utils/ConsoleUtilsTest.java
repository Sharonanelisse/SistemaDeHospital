package com.darwinruiz.hospital.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Tests unitarios para ConsoleUtils
 */
class ConsoleUtilsTest {
    
    @Test
    @DisplayName("Debe validar emails correctos")
    void debeValidarEmailsCorrectos() {
        assertTrue(ConsoleUtils.esEmailValido("usuario@ejemplo.com"));
        assertTrue(ConsoleUtils.esEmailValido("test.email@dominio.org"));
        assertTrue(ConsoleUtils.esEmailValido("nombre123@empresa.co.uk"));
        assertTrue(ConsoleUtils.esEmailValido("user+tag@gmail.com"));
        assertTrue(ConsoleUtils.esEmailValido("a@b.co"));
    }
    
    @Test
    @DisplayName("Debe rechazar emails inválidos")
    void debeRechazarEmailsInvalidos() {
        assertFalse(ConsoleUtils.esEmailValido(null));
        assertFalse(ConsoleUtils.esEmailValido(""));
        assertFalse(ConsoleUtils.esEmailValido("email-sin-arroba.com"));
        assertFalse(ConsoleUtils.esEmailValido("@dominio.com"));
        assertFalse(ConsoleUtils.esEmailValido("usuario@"));
        assertFalse(ConsoleUtils.esEmailValido("usuario@dominio"));
        assertFalse(ConsoleUtils.esEmailValido("usuario.dominio.com"));
        assertFalse(ConsoleUtils.esEmailValido("usuario@@dominio.com"));
        assertFalse(ConsoleUtils.esEmailValido("usuario@dominio..com"));
    }
    
    @Test
    @DisplayName("Debe validar fechas futuras correctamente")
    void debeValidarFechasFuturas() {
        LocalDate hoy = LocalDate.now();
        LocalDate manana = hoy.plusDays(1);
        LocalDate proximoMes = hoy.plusMonths(1);
        LocalDate proximoAno = hoy.plusYears(1);
        LocalDate ayer = hoy.minusDays(1);
        
        assertTrue(ConsoleUtils.esFechaFutura(manana));
        assertTrue(ConsoleUtils.esFechaFutura(proximoMes));
        assertTrue(ConsoleUtils.esFechaFutura(proximoAno));
        
        assertFalse(ConsoleUtils.esFechaFutura(hoy));
        assertFalse(ConsoleUtils.esFechaFutura(ayer));
        assertFalse(ConsoleUtils.esFechaFutura(null));
    }
    
    @Test
    @DisplayName("Debe validar fechas y horas futuras correctamente")
    void debeValidarFechasHorasFuturas() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime enUnaHora = ahora.plusHours(1);
        LocalDateTime manana = ahora.plusDays(1);
        LocalDateTime haceUnaHora = ahora.minusHours(1);
        
        assertTrue(ConsoleUtils.esFechaHoraFutura(enUnaHora));
        assertTrue(ConsoleUtils.esFechaHoraFutura(manana));
        
        assertFalse(ConsoleUtils.esFechaHoraFutura(haceUnaHora));
        assertFalse(ConsoleUtils.esFechaHoraFutura(null));
    }
    
    @Test
    @DisplayName("Debe formatear fechas correctamente")
    void debeFormatearFechasCorrectamente() {
        LocalDate fecha = LocalDate.of(2024, 12, 25);
        assertEquals("25/12/2024", ConsoleUtils.formatearFecha(fecha));
        assertEquals("N/A", ConsoleUtils.formatearFecha(null));
    }
    
    @Test
    @DisplayName("Debe formatear fechas y horas correctamente")
    void debeFormatearFechasHorasCorrectamente() {
        LocalDateTime fechaHora = LocalDateTime.of(2024, 12, 25, 14, 30);
        assertEquals("25/12/2024 14:30", ConsoleUtils.formatearFechaHora(fechaHora));
        assertEquals("N/A", ConsoleUtils.formatearFechaHora(null));
    }
    
    @Test
    @DisplayName("Debe validar regex de email correctamente")
    void debeValidarRegexEmailCorrectamente() {
        // Casos válidos
        assertTrue(ConsoleUtils.esEmailValido("simple@example.com"));
        assertTrue(ConsoleUtils.esEmailValido("very.common@example.com"));
        assertTrue(ConsoleUtils.esEmailValido("disposable.style.email.with+symbol@example.com"));
        assertTrue(ConsoleUtils.esEmailValido("x@example.com"));
        assertTrue(ConsoleUtils.esEmailValido("example@s.example"));
        
        // Casos inválidos
        assertFalse(ConsoleUtils.esEmailValido("plainaddress"));
        assertFalse(ConsoleUtils.esEmailValido("@missingdomain.com"));
        assertFalse(ConsoleUtils.esEmailValido("missing@.com"));
        assertFalse(ConsoleUtils.esEmailValido("missing@domain"));
        assertFalse(ConsoleUtils.esEmailValido("spaces in@email.com"));
        assertFalse(ConsoleUtils.esEmailValido("email@domain@domain.com"));
    }
    
    @Test
    @DisplayName("Debe manejar casos límite en validación de fechas")
    void debeManejarCasosLimiteEnValidacionFechas() {
        LocalDate hoy = LocalDate.now();
        
        // Exactamente mañana debe ser válido
        assertTrue(ConsoleUtils.esFechaFutura(hoy.plusDays(1)));
        
        // Exactamente hoy debe ser inválido
        assertFalse(ConsoleUtils.esFechaFutura(hoy));
        
        // Fechas muy lejanas deben ser válidas
        assertTrue(ConsoleUtils.esFechaFutura(hoy.plusYears(100)));
        
        // Fechas muy pasadas deben ser inválidas
        assertFalse(ConsoleUtils.esFechaFutura(hoy.minusYears(100)));
    }
    
    @Test
    @DisplayName("Debe manejar casos límite en validación de fechas y horas")
    void debeManejarCasosLimiteEnValidacionFechasHoras() {
        LocalDateTime ahora = LocalDateTime.now();
        
        // Un minuto en el futuro debe ser válido
        assertTrue(ConsoleUtils.esFechaHoraFutura(ahora.plusMinutes(1)));
        
        // Un minuto en el pasado debe ser inválido
        assertFalse(ConsoleUtils.esFechaHoraFutura(ahora.minusMinutes(1)));
        
        // Fechas muy lejanas deben ser válidas
        assertTrue(ConsoleUtils.esFechaHoraFutura(ahora.plusYears(50)));
    }
}