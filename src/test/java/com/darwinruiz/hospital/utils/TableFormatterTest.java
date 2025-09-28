package com.darwinruiz.hospital.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

/**
 * Tests unitarios para TableFormatter
 */
class TableFormatterTest {
    
    private TableFormatter formatter;
    
    @BeforeEach
    void setUp() {
        formatter = new TableFormatter();
    }
    
    @Test
    @DisplayName("Debe crear tabla vacía correctamente")
    void debeCrearTablaVaciaCorrectamente() {
        String resultado = formatter.build();
        assertEquals("Tabla vacía", resultado);
        assertTrue(formatter.isEmpty());
        assertEquals(0, formatter.getRowCount());
    }
    
    @Test
    @DisplayName("Debe crear tabla con solo encabezados")
    void debeCrearTablaConSoloEncabezados() {
        formatter.setHeaders("ID", "Nombre", "Email");
        
        String resultado = formatter.build();
        
        assertNotNull(resultado);
        assertTrue(resultado.contains("ID"));
        assertTrue(resultado.contains("Nombre"));
        assertTrue(resultado.contains("Email"));
        assertTrue(resultado.contains("+"));
        assertTrue(resultado.contains("|"));
        assertTrue(resultado.contains("-"));
        
        assertTrue(formatter.isEmpty());
        assertEquals(0, formatter.getRowCount());
    }
    
    @Test
    @DisplayName("Debe crear tabla con datos correctamente")
    void debeCrearTablaConDatosCorrectamente() {
        formatter.setHeaders("ID", "Nombre", "Email")
                .addRow("1", "Juan Pérez", "juan@email.com")
                .addRow("2", "María García", "maria@email.com");
        
        String resultado = formatter.build();
        
        assertNotNull(resultado);
        assertTrue(resultado.contains("Juan Pérez"));
        assertTrue(resultado.contains("María García"));
        assertTrue(resultado.contains("juan@email.com"));
        assertTrue(resultado.contains("maria@email.com"));
        
        assertFalse(formatter.isEmpty());
        assertEquals(2, formatter.getRowCount());
    }
    
    @Test
    @DisplayName("Debe ajustar anchos de columna automáticamente")
    void debeAjustarAnchosColumnaAutomaticamente() {
        formatter.setHeaders("ID", "Nombre")
                .addRow("1", "Juan")
                .addRow("2", "María Fernanda González");
        
        String resultado = formatter.build();
        
        // La columna "Nombre" debe expandirse para acomodar "María Fernanda González"
        assertNotNull(resultado);
        assertTrue(resultado.contains("María Fernanda González"));
        
        // Verificar que las líneas tienen la misma longitud
        String[] lineas = resultado.split("\n");
        int longitudPrimeraLinea = lineas[0].length();
        for (String linea : lineas) {
            if (!linea.trim().isEmpty()) {
                assertEquals(longitudPrimeraLinea, linea.length(), 
                    "Todas las líneas deben tener la misma longitud");
            }
        }
    }
    
    @Test
    @DisplayName("Debe manejar valores nulos correctamente")
    void debeManejarValoresNulosCorrectamente() {
        formatter.setHeaders("ID", "Nombre", "Email")
                .addRow("1", null, "juan@email.com")
                .addRow("2", "María", null);
        
        String resultado = formatter.build();
        
        assertNotNull(resultado);
        assertFalse(resultado.contains("null"));
        assertTrue(resultado.contains("juan@email.com"));
        assertTrue(resultado.contains("María"));
    }
    
    @Test
    @DisplayName("Debe manejar filas con menos columnas")
    void debeManejarFilasConMenosColumnas() {
        formatter.setHeaders("ID", "Nombre", "Email", "Teléfono")
                .addRow("1", "Juan")  // Solo 2 valores para 4 columnas
                .addRow("2", "María", "maria@email.com");  // Solo 3 valores
        
        String resultado = formatter.build();
        
        assertNotNull(resultado);
        assertTrue(resultado.contains("Juan"));
        assertTrue(resultado.contains("María"));
        assertEquals(2, formatter.getRowCount());
    }
    
    @Test
    @DisplayName("Debe limpiar tabla correctamente")
    void debeLimpiarTablaCorrectamente() {
        formatter.setHeaders("ID", "Nombre")
                .addRow("1", "Juan")
                .addRow("2", "María");
        
        assertEquals(2, formatter.getRowCount());
        assertFalse(formatter.isEmpty());
        
        formatter.clear();
        
        assertEquals(0, formatter.getRowCount());
        assertTrue(formatter.isEmpty());
        
        // Los encabezados deben mantenerse
        String resultado = formatter.build();
        assertTrue(resultado.contains("ID"));
        assertTrue(resultado.contains("Nombre"));
    }
    
    @Test
    @DisplayName("Debe crear tabla simple estática correctamente")
    void debeCrearTablaSimpleEstaticaCorrectamente() {
        String[] headers = {"ID", "Nombre"};
        List<String[]> data = Arrays.asList(
            new String[]{"1", "Juan"},
            new String[]{"2", "María"}
        );
        
        // Este método no retorna nada, solo imprime
        // Verificamos que no lance excepciones
        assertDoesNotThrow(() -> {
            TableFormatter.printSimpleTable(headers, data);
        });
    }
    
    @Test
    @DisplayName("Debe mostrar mensaje de no datos correctamente")
    void debeMostrarMensajeNoDatosCorrectamente() {
        // Este método no retorna nada, solo imprime
        // Verificamos que no lance excepciones
        assertDoesNotThrow(() -> {
            TableFormatter.printNoDataMessage("No hay datos disponibles");
        });
    }
    
    @Test
    @DisplayName("Debe manejar encabezados largos correctamente")
    void debeManejarEncabezadosLargosCorrectamente() {
        formatter.setHeaders("ID", "Nombre Completo del Paciente", "Email")
                .addRow("1", "Juan", "juan@email.com");
        
        String resultado = formatter.build();
        
        assertNotNull(resultado);
        assertTrue(resultado.contains("Nombre Completo del Paciente"));
        assertTrue(resultado.contains("Juan"));
        
        // Verificar que el formato es consistente
        String[] lineas = resultado.split("\n");
        assertTrue(lineas.length > 0);
        
        // Todas las líneas de separación deben tener la misma longitud
        for (String linea : lineas) {
            if (linea.startsWith("+")) {
                assertEquals(lineas[0].length(), linea.length());
            }
        }
    }
    
    @Test
    @DisplayName("Debe formatear correctamente con una sola columna")
    void debeFormatearCorrectamenteConUnaSolaColumna() {
        formatter.setHeaders("Nombre")
                .addRow("Juan")
                .addRow("María");
        
        String resultado = formatter.build();
        
        assertNotNull(resultado);
        assertTrue(resultado.contains("Juan"));
        assertTrue(resultado.contains("María"));
        assertTrue(resultado.contains("+"));
        assertTrue(resultado.contains("|"));
        assertEquals(2, formatter.getRowCount());
    }
    
    @Test
    @DisplayName("Debe manejar cadenas vacías correctamente")
    void debeManejarCadenasVaciasCorrectamente() {
        formatter.setHeaders("ID", "Nombre", "Comentario")
                .addRow("1", "Juan", "")
                .addRow("2", "", "Sin nombre");
        
        String resultado = formatter.build();
        
        assertNotNull(resultado);
        assertTrue(resultado.contains("Juan"));
        assertTrue(resultado.contains("Sin nombre"));
        assertEquals(2, formatter.getRowCount());
    }
}