package com.darwinruiz.hospital.repositories;

import com.darwinruiz.hospital.models.Paciente;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para BaseRepository usando Paciente como entidad de prueba.
 * Verifica las operaciones CRUD básicas del repositorio base.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BaseRepositoryTest {
    
    private static PacienteRepository repository;
    private static Paciente pacientePrueba;
    
    @BeforeAll
    static void setUp() {
        repository = new PacienteRepository();
        pacientePrueba = new Paciente(
            "Juan Pérez Test",
            "1234567890123",
            LocalDate.of(1990, 5, 15),
            "12345678",
            "juan.test@email.com"
        );
    }
    
    @AfterAll
    static void tearDown() {
        // Limpiar datos de prueba
        try {
            Optional<Paciente> paciente = repository.findByDpi("1234567890123");
            if (paciente.isPresent()) {
                repository.remove(paciente.get());
            }
        } catch (Exception e) {
            // Ignorar errores de limpieza
        }
        BaseRepository.closeEntityManagerFactory();
    }
    
    @Test
    @Order(1)
    @DisplayName("Debe persistir una nueva entidad correctamente")
    void testPersist() {
        // When
        assertDoesNotThrow(() -> repository.persist(pacientePrueba));
        
        // Then
        assertNotNull(pacientePrueba.getId());
        assertTrue(pacientePrueba.getId() > 0);
    }
    
    @Test
    @Order(2)
    @DisplayName("Debe encontrar una entidad por ID")
    void testFindById() {
        // Given
        Long id = pacientePrueba.getId();
        
        // When
        Optional<Paciente> resultado = repository.findById(id);
        
        // Then
        assertTrue(resultado.isPresent());
        assertEquals("Juan Pérez Test", resultado.get().getNombre());
        assertEquals("1234567890123", resultado.get().getDpi());
    }
    
    @Test
    @Order(3)
    @DisplayName("Debe verificar si existe una entidad por ID")
    void testExistsById() {
        // Given
        Long id = pacientePrueba.getId();
        
        // When & Then
        assertTrue(repository.existsById(id));
        assertFalse(repository.existsById(99999L));
    }
    
    @Test
    @Order(4)
    @DisplayName("Debe obtener todas las entidades")
    void testFindAll() {
        // When
        List<Paciente> pacientes = repository.findAll();
        
        // Then
        assertNotNull(pacientes);
        assertFalse(pacientes.isEmpty());
        assertTrue(pacientes.stream().anyMatch(p -> "1234567890123".equals(p.getDpi())));
    }
    
    @Test
    @Order(5)
    @DisplayName("Debe contar el número de entidades")
    void testCount() {
        // When
        long count = repository.count();
        
        // Then
        assertTrue(count > 0);
    }
    
    @Test
    @Order(6)
    @DisplayName("Debe actualizar una entidad existente")
    void testMerge() {
        // Given
        pacientePrueba.setTelefono("87654321");
        
        // When
        Paciente actualizado = repository.merge(pacientePrueba);
        
        // Then
        assertNotNull(actualizado);
        assertEquals("87654321", actualizado.getTelefono());
        
        // Verificar en base de datos
        Optional<Paciente> verificacion = repository.findById(pacientePrueba.getId());
        assertTrue(verificacion.isPresent());
        assertEquals("87654321", verificacion.get().getTelefono());
    }
    
    @Test
    @Order(7)
    @DisplayName("Debe manejar búsqueda de entidad inexistente")
    void testFindByIdInexistente() {
        // When
        Optional<Paciente> resultado = repository.findById(99999L);
        
        // Then
        assertFalse(resultado.isPresent());
    }
    
    @Test
    @Order(8)
    @DisplayName("Debe eliminar una entidad por ID")
    void testRemoveById() {
        // Given
        Long id = pacientePrueba.getId();
        
        // When
        assertDoesNotThrow(() -> repository.removeById(id));
        
        // Then
        assertFalse(repository.existsById(id));
        Optional<Paciente> verificacion = repository.findById(id);
        assertFalse(verificacion.isPresent());
    }
    
    @Test
    @Order(9)
    @DisplayName("Debe manejar eliminación de entidad inexistente")
    void testRemoveByIdInexistente() {
        // When & Then
        assertDoesNotThrow(() -> repository.removeById(99999L));
    }
}