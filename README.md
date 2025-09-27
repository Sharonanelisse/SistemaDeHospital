# Sistema de Hospital

Sistema de gestión hospitalaria JPA/Hibernate y PostgreSQL.

## Autores
- Sharon Anelisse Marroquín Hernandez
- Eddy Alexander Cheguen Garcia


## Estructura del Proyecto

```
src/main/java/com/darwinruiz/hospital/
├── models/          # Entidades JPA
├── services/        # Lógica de negocio
├── repositories/    # Acceso a datos
├── console/         # Interfaz de usuario
├── enums/          # Enumeraciones
├── exceptions/     # Excepciones personalizadas
└── utils/          # Utilidades
```

### Base de Datos
```bash
docker run --name postgres-jpql -e POSTGRES_PASSWORD=admin123 -e POSTGRES_USER=postgres -e POSTGRES_DB=sistema_hospital -p 5433:5432 -d postgres
```




## Funcionalidades

1. **Registrar paciente**
2. **Crear/editar historial médico**
3. **Registrar médico**
4. **Agendar cita**
5. **Cambiar estado de cita**
6. **Consultas avanzadas**
7. **Eliminar registros**
8. **Semilla de datos**
