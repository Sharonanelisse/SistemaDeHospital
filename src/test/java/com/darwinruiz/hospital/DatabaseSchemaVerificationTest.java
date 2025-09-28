package com.darwinruiz.hospital;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test para verificar que todas las tablas, claves foráneas e índices
 * se generen correctamente según los requerimientos 8.1, 8.2, 8.3, 8.4, 8.5
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseSchemaVerificationTest {

    private static EntityManagerFactory emf;
    private EntityManager em;

    @BeforeAll
    static void setUpClass() {
        emf = Persistence.createEntityManagerFactory("HospitalSystemPU");
    }

    @AfterAll
    static void tearDownClass() {
        if (emf != null) {
            emf.close();
        }
    }

    @BeforeEach
    void setUp() {
        em = emf.createEntityManager();
    }

    @AfterEach
    void tearDown() {
        if (em != null) {
            em.close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Verificar que todas las tablas necesarias existen")
    void testTablesExist() throws SQLException {
        Connection connection = em.unwrap(Connection.class);
        DatabaseMetaData metaData = connection.getMetaData();

        // Verificar tablas principales
        String[] expectedTables = {"paciente", "medico", "historial_medico", "cita"};
        
        for (String tableName : expectedTables) {
            ResultSet tables = metaData.getTables(null, null, tableName, new String[]{"TABLE"});
            assertTrue(tables.next(), "La tabla " + tableName + " debe existir");
            tables.close();
        }
    }

    @Test
    @Order(2)
    @DisplayName("Verificar claves foráneas con nombres específicos")
    void testForeignKeysExist() throws SQLException {
        Connection connection = em.unwrap(Connection.class);
        DatabaseMetaData metaData = connection.getMetaData();

        // Verificar claves foráneas esperadas
        List<String> expectedForeignKeys = List.of(
            "fk_historial_paciente",
            "fk_cita_paciente", 
            "fk_cita_medico"
        );

        for (String fkName : expectedForeignKeys) {
            boolean found = false;
            
            // Buscar en todas las tablas
            String[] tables = {"historial_medico", "cita"};
            for (String table : tables) {
                ResultSet foreignKeys = metaData.getImportedKeys(null, null, table);
                while (foreignKeys.next()) {
                    String fkNameFromDb = foreignKeys.getString("FK_NAME");
                    if (fkName.equals(fkNameFromDb)) {
                        found = true;
                        break;
                    }
                }
                foreignKeys.close();
                if (found) break;
            }
            
            assertTrue(found, "La clave foránea " + fkName + " debe existir");
        }
    }

    @Test
    @Order(3)
    @DisplayName("Verificar índices específicos")
    void testIndexesExist() throws SQLException {
        Connection connection = em.unwrap(Connection.class);
        DatabaseMetaData metaData = connection.getMetaData();

        // Verificar índices en tabla cita
        List<String> expectedIndexes = List.of(
            "ix_cita_paciente",
            "ix_cita_medico", 
            "ix_cita_fecha_hora"
        );

        ResultSet indexes = metaData.getIndexInfo(null, null, "cita", false, false);
        List<String> foundIndexes = new ArrayList<>();
        
        while (indexes.next()) {
            String indexName = indexes.getString("INDEX_NAME");
            if (indexName != null && !indexName.startsWith("pk_")) {
                foundIndexes.add(indexName);
            }
        }
        indexes.close();

        for (String expectedIndex : expectedIndexes) {
            assertTrue(foundIndexes.stream().anyMatch(idx -> idx.contains(expectedIndex) || 
                      expectedIndex.contains(idx)), 
                      "El índice " + expectedIndex + " debe existir. Índices encontrados: " + foundIndexes);
        }
    }

    @Test
    @Order(4)
    @DisplayName("Verificar restricciones únicas")
    void testUniqueConstraints() throws SQLException {
        Connection connection = em.unwrap(Connection.class);
        DatabaseMetaData metaData = connection.getMetaData();

        // Verificar restricción única en paciente.dpi
        ResultSet indexes = metaData.getIndexInfo(null, null, "paciente", true, false);
        boolean dpiUniqueFound = false;
        
        while (indexes.next()) {
            String columnName = indexes.getString("COLUMN_NAME");
            if ("dpi".equals(columnName)) {
                dpiUniqueFound = true;
                break;
            }
        }
        indexes.close();
        
        assertTrue(dpiUniqueFound, "Debe existir restricción única en paciente.dpi");

        // Verificar restricción única en medico.colegiado
        indexes = metaData.getIndexInfo(null, null, "medico", true, false);
        boolean colegiadoUniqueFound = false;
        
        while (indexes.next()) {
            String columnName = indexes.getString("COLUMN_NAME");
            if ("colegiado".equals(columnName)) {
                colegiadoUniqueFound = true;
                break;
            }
        }
        indexes.close();
        
        assertTrue(colegiadoUniqueFound, "Debe existir restricción única en medico.colegiado");
    }

    @Test
    @Order(5)
    @DisplayName("Verificar configuración de enums como STRING")
    void testEnumConfiguration() {
        em.getTransaction().begin();
        
        try {
            // Verificar que los enums se almacenan como STRING
            Query query = em.createNativeQuery(
                "SELECT data_type FROM information_schema.columns " +
                "WHERE table_name = 'medico' AND column_name = 'especialidad'"
            );
            
            Object result = query.getSingleResult();
            String dataType = result.toString();
            
            // En PostgreSQL, los enums de JPA se almacenan como VARCHAR
            assertTrue(dataType.contains("character varying") || dataType.contains("varchar"), 
                      "La especialidad debe almacenarse como STRING, encontrado: " + dataType);
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            fail("Error verificando configuración de enums: " + e.getMessage());
        }
    }

    @Test
    @Order(6)
    @DisplayName("Verificar estructura de columnas de las tablas")
    void testTableStructure() throws SQLException {
        Connection connection = em.unwrap(Connection.class);
        DatabaseMetaData metaData = connection.getMetaData();

        // Verificar columnas de tabla paciente
        ResultSet columns = metaData.getColumns(null, null, "paciente", null);
        List<String> pacienteColumns = new ArrayList<>();
        while (columns.next()) {
            pacienteColumns.add(columns.getString("COLUMN_NAME"));
        }
        columns.close();

        List<String> expectedPacienteColumns = List.of("id", "nombre", "dpi", "fechanacimiento", "telefono", "email");
        for (String expectedCol : expectedPacienteColumns) {
            assertTrue(pacienteColumns.stream().anyMatch(col -> col.equalsIgnoreCase(expectedCol)), 
                      "La tabla paciente debe tener la columna " + expectedCol);
        }

        // Verificar columnas de tabla cita
        columns = metaData.getColumns(null, null, "cita", null);
        List<String> citaColumns = new ArrayList<>();
        while (columns.next()) {
            citaColumns.add(columns.getString("COLUMN_NAME"));
        }
        columns.close();

        List<String> expectedCitaColumns = List.of("id", "fechahora", "estado", "motivo", "paciente_id", "medico_id");
        for (String expectedCol : expectedCitaColumns) {
            assertTrue(citaColumns.stream().anyMatch(col -> col.equalsIgnoreCase(expectedCol)), 
                      "La tabla cita debe tener la columna " + expectedCol);
        }
    }
}