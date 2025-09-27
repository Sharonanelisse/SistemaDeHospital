package com.darwinruiz.hospital.console;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.darwinruiz.hospital.enums.Especialidad;
import com.darwinruiz.hospital.enums.EstadoCita;
import com.darwinruiz.hospital.models.Cita;
import com.darwinruiz.hospital.models.HistorialMedico;
import com.darwinruiz.hospital.models.Medico;
import com.darwinruiz.hospital.models.Paciente;
import com.darwinruiz.hospital.services.CitaService;
import com.darwinruiz.hospital.services.HistorialMedicoService;
import com.darwinruiz.hospital.services.MedicoService;
import com.darwinruiz.hospital.services.PacienteService;
import com.darwinruiz.hospital.utils.ConsoleUtils;
import com.darwinruiz.hospital.utils.SemillaDatos;
import com.darwinruiz.hospital.utils.TableFormatter;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class HospitalConsoleApp {
    
    private final PacienteService pacienteService;
    private final MedicoService medicoService;
    private final CitaService citaService;
    private final HistorialMedicoService historialMedicoService;
    private final EntityManagerFactory emf;
    private boolean ejecutando = true;
    
    public HospitalConsoleApp() {
        this.emf = Persistence.createEntityManagerFactory("HospitalSystemPU");
        this.pacienteService = new PacienteService(emf);
        this.medicoService = new MedicoService(emf);
        this.citaService = new CitaService(emf);
        this.historialMedicoService = new HistorialMedicoService(emf);
    }

    public void iniciar() {
        mostrarBienvenida();
        
        while (ejecutando) {
            try {
                mostrarMenuPrincipal();
                int opcion = ConsoleUtils.leerEnteroEnRango("Seleccione una opción", 1, 9);
                procesarOpcionPrincipal(opcion);
            } catch (Exception e) {
                ConsoleUtils.mostrarError("Error inesperado: " + e.getMessage());
                ConsoleUtils.pausar();
            }
        }
        
        cerrarAplicacion();
    }

    private void mostrarBienvenida() {
        ConsoleUtils.limpiarPantalla();
        System.out.println("║                    SISTEMA DE HOSPITAL                      ║");
        System.out.println();
        ConsoleUtils.mostrarInfo("Bienvenido");
        System.out.println();
    }

    private void mostrarMenuPrincipal() {
        System.out.println("===============================================================");
        System.out.println("                    MENU PRINCIPAL");
        System.out.println("===============================================================");
        System.out.println("1. Registrar paciente");
        System.out.println("2. Crear/editar historial medico de un paciente");
        System.out.println("3. Registrar medico");
        System.out.println("4. Agendar cita (eligiendo paciente y medico)");
        System.out.println("5. Cambiar estado de una cita (PROGRAMADA <-> ATENDIDA/CANCELADA)");
        System.out.println("6. Consultas");
        System.out.println("7. Eliminar");
        System.out.println("8. Semilla de datos");
        System.out.println("9. Salir");
        System.out.println("===============================================================");
    }

    private void procesarOpcionPrincipal(int opcion) {
        System.out.println();
        
        try {
            switch (opcion) {
                case 1:
                    registrarPaciente();
                    break;
                case 2:
                    gestionarHistorialMedico();
                    break;
                case 3:
                    registrarMedico();
                    break;
                case 4:
                    agendarCita();
                    break;
                case 5:
                    cambiarEstadoCita();
                    break;
                case 6:
                    mostrarMenuConsultas();
                    break;
                case 7:
                    mostrarMenuEliminacion();
                    break;
                case 8:
                    cargarSemillaDatos();
                    break;
                case 9:
                    salir();
                    break;
                default:
                    ConsoleUtils.mostrarError("Opción no válida");
            }
        } catch (Exception e) {
            ConsoleUtils.mostrarError("Error al procesar la opción: " + e.getMessage());
        }
        
        if (ejecutando && opcion != 6 && opcion != 7) {
            ConsoleUtils.pausar();
        }
    }

    private void mostrarMenuConsultas() {
        boolean enSubMenu = true;
        
        while (enSubMenu && ejecutando) {
            try {
                System.out.println();
                System.out.println("═══════════════════════════════════════════════════════════════");
                System.out.println("                      CONSULTAS");
                System.out.println("═══════════════════════════════════════════════════════════════");
                System.out.println("1. Listar pacientes con sus citas");
                System.out.println("2. Listar medicos con proximas citas");
                System.out.println("3. Buscar citas por rango de fechas");
                System.out.println("4. Ver historial medico de un paciente");
                System.out.println("5. Volver al menú principal");
                System.out.println("═══════════════════════════════════════════════════════════════");
                
                int opcion = ConsoleUtils.leerEnteroEnRango("Seleccione una opción", 1, 5);
                enSubMenu = procesarOpcionConsultas(opcion);
                
            } catch (Exception e) {
                ConsoleUtils.mostrarError("Error en el menú de consultas: " + e.getMessage());
                ConsoleUtils.pausar();
            }
        }
    }

    private boolean procesarOpcionConsultas(int opcion) {
        System.out.println();
        
        try {
            switch (opcion) {
                case 1:
                    listarPacientesConSusCitas();
                    break;
                case 2:
                    listarMedicosConProximasCitas();
                    break;
                case 3:
                    buscarCitasPorRangoFechas();
                    break;
                case 4:
                    verHistorialMedicoPaciente();
                    break;
                case 5:
                    return false;
                default:
                    ConsoleUtils.mostrarError("Opción no válida");
            }
        } catch (Exception e) {
            ConsoleUtils.mostrarError("Error al procesar la consulta: " + e.getMessage());
        }
        
        if (opcion != 5) {
            ConsoleUtils.pausar();
        }
        return true;
    }

    private void mostrarMenuEliminacion() {
        boolean enSubMenu = true;
        
        while (enSubMenu && ejecutando) {
            try {
                System.out.println();
                System.out.println("═══════════════════════════════════════════════════════════════");
                System.out.println("                      ELIMINAR");
                System.out.println("═══════════════════════════════════════════════════════════════");
                System.out.println("1. Eliminar cita");
                System.out.println("2. Eliminar paciente (explicar la decisión sobre cascadas y restricciones)");
                System.out.println("3. Volver al menú principal");
                System.out.println("═══════════════════════════════════════════════════════════════");
                
                int opcion = ConsoleUtils.leerEnteroEnRango("Seleccione una opción", 1, 3);
                enSubMenu = procesarOpcionEliminacion(opcion);
                
            } catch (Exception e) {
                ConsoleUtils.mostrarError("Error en el menú de eliminación: " + e.getMessage());
                ConsoleUtils.pausar();
            }
        }
    }

    private boolean procesarOpcionEliminacion(int opcion) {
        System.out.println();
        
        try {
            switch (opcion) {
                case 1:
                    eliminarCita();
                    break;
                case 2:
                    eliminarPaciente();
                    break;
                case 3:
                    return false;
                default:
                    ConsoleUtils.mostrarError("Opción no válida");
            }
        } catch (Exception e) {
            ConsoleUtils.mostrarError("Error al procesar la eliminación: " + e.getMessage());
        }
        
        if (opcion != 3) {
            ConsoleUtils.pausar();
        }
        return true;
    }

    private void salir() {
        System.out.println();
        if (ConsoleUtils.confirmar("¿Está seguro que desea salir del sistema?")) {
            ejecutando = false;
            ConsoleUtils.mostrarInfo("Gracias por usar el Sistema de Hospital");
        } else {
            ConsoleUtils.mostrarInfo("Continuando en el sistema...");
        }
    }

    private void cerrarAplicacion() {
        try {
            if (emf != null && emf.isOpen()) {
                emf.close();
            }
            System.out.println();
            System.out.println("╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║                    ¡HASTA LUEGO!                           ║");
            System.out.println("║              Sistema de Hospital cerrado                    ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
        } catch (Exception e) {
            ConsoleUtils.mostrarError("Error al cerrar la aplicación: " + e.getMessage());
        }
    }

    private void registrarPaciente() {
        try {
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("                   REGISTRAR PACIENTE");
            System.out.println("═══════════════════════════════════════════════════════════════");

            String nombre = ConsoleUtils.leerTextoNoVacio("Ingrese el nombre completo del paciente");
            String dpi = ConsoleUtils.leerTextoNoVacio("Ingrese el DPI del paciente");
            LocalDate fechaNacimiento = ConsoleUtils.leerFecha("Ingrese la fecha de nacimiento");
            String telefono = ConsoleUtils.leerTexto("Ingrese el teléfono (opcional)");
            String email = ConsoleUtils.leerEmail("Ingrese el email del paciente");

            System.out.println();
            System.out.println("───────────────────────────────────────────────────────────────");
            System.out.println("                    RESUMEN DEL PACIENTE");
            System.out.println("───────────────────────────────────────────────────────────────");
            System.out.println("Nombre: " + nombre);
            System.out.println("DPI: " + dpi);
            System.out.println("Fecha de Nacimiento: " + ConsoleUtils.formatearFecha(fechaNacimiento));
            System.out.println("Teléfono: " + (telefono.isEmpty() ? "No especificado" : telefono));
            System.out.println("Email: " + email);
            System.out.println("───────────────────────────────────────────────────────────────");

            if (ConsoleUtils.confirmar("¿Desea registrar este paciente?")) {
                // Registrar el paciente usando el servicio
                Paciente pacienteRegistrado = pacienteService.registrarPaciente(
                    nombre, dpi, fechaNacimiento, 
                    telefono.isEmpty() ? null : telefono, 
                    email
                );

                System.out.println();
                ConsoleUtils.mostrarExito("Paciente registrado exitosamente");
                System.out.println("ID asignado: " + pacienteRegistrado.getId());
                System.out.println("Nombre: " + pacienteRegistrado.getNombre());
                System.out.println("DPI: " + pacienteRegistrado.getDpi());
                
            } else {
                ConsoleUtils.mostrarInfo("Registro de paciente cancelado");
            }
            
        } catch (Exception e) {
            ConsoleUtils.mostrarError("Error al registrar el paciente: " + e.getMessage());
            System.out.println();
            ConsoleUtils.mostrarInfo("Verifique que:");
            System.out.println("• El DPI no esté registrado previamente");
            System.out.println("• El formato del email sea válido");
            System.out.println("• Todos los campos requeridos estén completos");
        }
    }

    private void registrarMedico() {
        try {
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("                    REGISTRAR MEDICO");
            System.out.println("═══════════════════════════════════════════════════════════════");

            String nombre = ConsoleUtils.leerTextoNoVacio("Ingrese el nombre completo del médico");
            String colegiado = ConsoleUtils.leerTextoNoVacio("Ingrese el número de colegiado");

            Especialidad especialidad = seleccionarEspecialidad();
            
            String email = ConsoleUtils.leerEmail("Ingrese el email del médico");

            System.out.println();
            System.out.println("───────────────────────────────────────────────────────────────");
            System.out.println("                     RESUMEN DEL MÉDICO");
            System.out.println("───────────────────────────────────────────────────────────────");
            System.out.println("Nombre: " + nombre);
            System.out.println("Colegiado: " + colegiado);
            System.out.println("Especialidad: " + especialidad.name());
            System.out.println("Email: " + email);
            System.out.println("───────────────────────────────────────────────────────────────");

            if (ConsoleUtils.confirmar("¿Desea registrar este médico?")) {

                Medico medicoRegistrado = medicoService.registrarMedico(
                    nombre, colegiado, especialidad, email
                );

                System.out.println();
                ConsoleUtils.mostrarExito("Médico registrado exitosamente");
                System.out.println("ID asignado: " + medicoRegistrado.getId());
                System.out.println("Nombre: " + medicoRegistrado.getNombre());
                System.out.println("Colegiado: " + medicoRegistrado.getColegiado());
                System.out.println("Especialidad: " + medicoRegistrado.getEspecialidad().name());
                
            } else {
                ConsoleUtils.mostrarInfo("Registro de médico cancelado");
            }
            
        } catch (Exception e) {
            ConsoleUtils.mostrarError("Error al registrar el médico: " + e.getMessage());
            System.out.println();
            ConsoleUtils.mostrarInfo("Verifique que:");
            System.out.println("• El número de colegiado no esté registrado previamente");
            System.out.println("• El formato del email sea válido");
            System.out.println("• Todos los campos requeridos estén completos");
        }
    }

    private Especialidad seleccionarEspecialidad() {
        System.out.println();
        System.out.println("Especialidades disponibles:");
        System.out.println("───────────────────────────────────────────────────────────────");
        
        Especialidad[] especialidades = Especialidad.values();
        for (int i = 0; i < especialidades.length; i++) {
            System.out.printf("%d. %s%n", i + 1, especialidades[i].name());
        }
        System.out.println("───────────────────────────────────────────────────────────────");
        
        int opcion = ConsoleUtils.leerEnteroEnRango(
            "Seleccione la especialidad", 1, especialidades.length
        );
        
        return especialidades[opcion - 1];
    }

    private void gestionarHistorialMedico() {
        try {
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("              CREAR/EDITAR HISTORIAL MEDICO");
            System.out.println("═══════════════════════════════════════════════════════════════");

            Paciente pacienteSeleccionado = seleccionarPacienteParaHistorial();
            if (pacienteSeleccionado == null) {
                ConsoleUtils.mostrarInfo("Operación cancelada");
                return;
            }

            boolean existeHistorial = historialMedicoService.existeHistorial(pacienteSeleccionado.getId());
            
            if (existeHistorial) {
                System.out.println();
                ConsoleUtils.mostrarInfo("El paciente ya tiene un historial médico registrado");

                mostrarHistorialActual(pacienteSeleccionado.getId());
                
                if (!ConsoleUtils.confirmar("¿Desea actualizar el historial existente?")) {
                    ConsoleUtils.mostrarInfo("Operación cancelada");
                    return;
                }
            } else {
                System.out.println();
                ConsoleUtils.mostrarInfo("Creando nuevo historial médico para: " + pacienteSeleccionado.getNombre());
            }

            String alergias = solicitarDatosHistorial("alergias", 500, existeHistorial ? 
                obtenerDatoActual(pacienteSeleccionado.getId(), "alergias") : null);
            
            String antecedentes = solicitarDatosHistorial("antecedentes médicos", 1000, existeHistorial ? 
                obtenerDatoActual(pacienteSeleccionado.getId(), "antecedentes") : null);
            
            String observaciones = solicitarDatosHistorial("observaciones", 1000, existeHistorial ? 
                obtenerDatoActual(pacienteSeleccionado.getId(), "observaciones") : null);

            mostrarResumenHistorial(pacienteSeleccionado, alergias, antecedentes, observaciones, existeHistorial);

            String accion = existeHistorial ? "actualizar" : "crear";
            if (ConsoleUtils.confirmar("¿Desea " + accion + " este historial médico?")) {

                HistorialMedico historialGuardado = historialMedicoService.crearOActualizarHistorial(
                    pacienteSeleccionado.getId(), alergias, antecedentes, observaciones
                );

                System.out.println();
                String mensaje = existeHistorial ? "actualizado" : "creado";
                ConsoleUtils.mostrarExito("Historial médico " + mensaje + " exitosamente");
                System.out.println("Paciente: " + pacienteSeleccionado.getNombre() + " (DPI: " + pacienteSeleccionado.getDpi() + ")");
                System.out.println("ID del historial: " + historialGuardado.getId());
                
            } else {
                ConsoleUtils.mostrarInfo("Operación cancelada");
            }
            
        } catch (Exception e) {
            ConsoleUtils.mostrarError("Error al gestionar el historial médico: " + e.getMessage());
            System.out.println();
            ConsoleUtils.mostrarInfo("Verifique que:");
            System.out.println("• El paciente seleccionado sea válido");
            System.out.println("• Los datos ingresados no excedan los límites de caracteres");
            System.out.println("• La conexión a la base de datos esté disponible");
        }
    }

    private Paciente seleccionarPacienteParaHistorial() {
        try {
            List<Paciente> pacientes = pacienteService.listarPacientes();
            
            if (pacientes.isEmpty()) {
                ConsoleUtils.mostrarError("No hay pacientes registrados en el sistema");
                System.out.println();
                ConsoleUtils.mostrarInfo("Debe registrar al menos un paciente antes de crear un historial médico");
                return null;
            }
            
            System.out.println();
            System.out.println("───────────────────────────────────────────────────────────────");
            System.out.println("                    SELECCIONAR PACIENTE");
            System.out.println("───────────────────────────────────────────────────────────────");

            for (int i = 0; i < pacientes.size(); i++) {
                Paciente p = pacientes.get(i);
                System.out.printf("%d. %s (DPI: %s) - %s%n", 
                    i + 1, p.getNombre(), p.getDpi(), 
                    ConsoleUtils.formatearFecha(p.getFechaNacimiento()));
            }
            System.out.println((pacientes.size() + 1) + ". Cancelar operación");
            System.out.println("───────────────────────────────────────────────────────────────");

            int opcion = ConsoleUtils.leerEnteroEnRango(
                "Seleccione el paciente", 1, pacientes.size() + 1
            );
            
            if (opcion == pacientes.size() + 1) {
                return null;
            }
            
            return pacientes.get(opcion - 1);
            
        } catch (Exception e) {
            ConsoleUtils.mostrarError("Error al obtener la lista de pacientes: " + e.getMessage());
            return null;
        }
    }

    private String solicitarDatosHistorial(String campo, int maxCaracteres, String valorActual) {
        System.out.println();
        System.out.println("───────────────────────────────────────────────────────────────");
        System.out.printf("                    %s (máx. %d caracteres)%n", 
            campo.toUpperCase(), maxCaracteres);
        System.out.println("───────────────────────────────────────────────────────────────");
        
        if (valorActual != null && !valorActual.trim().isEmpty()) {
            System.out.println("Valor actual: " + valorActual);
            System.out.println();
        }
        
        String valor;
        do {
            valor = ConsoleUtils.leerTexto("Ingrese " + campo + " (Enter para vacío)");
            
            if (valor.length() > maxCaracteres) {
                ConsoleUtils.mostrarError("El texto no puede exceder " + maxCaracteres + " caracteres");
                System.out.println("Caracteres ingresados: " + valor.length());
            }
        } while (valor.length() > maxCaracteres);
        
        return valor.trim().isEmpty() ? null : valor.trim();
    }

    private void mostrarHistorialActual(Long pacienteId) {
        try {
            var historialOpt = historialMedicoService.consultarHistorial(pacienteId);
            if (historialOpt.isPresent()) {
                HistorialMedico historial = historialOpt.get();
                System.out.println();
                System.out.println("───────────────────────────────────────────────────────────────");
                System.out.println("                    HISTORIAL ACTUAL");
                System.out.println("───────────────────────────────────────────────────────────────");
                System.out.println("Alergias: " + (historial.getAlergias() != null ? historial.getAlergias() : "No especificadas"));
                System.out.println("Antecedentes: " + (historial.getAntecedentes() != null ? historial.getAntecedentes() : "No especificados"));
                System.out.println("Observaciones: " + (historial.getObservaciones() != null ? historial.getObservaciones() : "No especificadas"));
                System.out.println("───────────────────────────────────────────────────────────────");
            }
        } catch (Exception e) {
            ConsoleUtils.mostrarError("Error al mostrar el historial actual: " + e.getMessage());
        }
    }

    private String obtenerDatoActual(Long pacienteId, String campo) {
        try {
            var historialOpt = historialMedicoService.consultarHistorial(pacienteId);
            if (historialOpt.isPresent()) {
                HistorialMedico historial = historialOpt.get();
                switch (campo) {
                    case "alergias":
                        return historial.getAlergias();
                    case "antecedentes":
                        return historial.getAntecedentes();
                    case "observaciones":
                        return historial.getObservaciones();
                }
            }
        } catch (Exception e) {

        }
        return null;
    }

    private void mostrarResumenHistorial(Paciente paciente, String alergias, String antecedentes, 
                                       String observaciones, boolean esActualizacion) {
        System.out.println();
        System.out.println("───────────────────────────────────────────────────────────────");
        String titulo = esActualizacion ? "RESUMEN DE ACTUALIZACIÓN" : "RESUMEN DEL NUEVO HISTORIAL";
        System.out.println("                    " + titulo);
        System.out.println("───────────────────────────────────────────────────────────────");
        System.out.println("Paciente: " + paciente.getNombre());
        System.out.println("DPI: " + paciente.getDpi());
        System.out.println();
        System.out.println("Alergias: " + (alergias != null ? alergias : "No especificadas"));
        System.out.println("Antecedentes: " + (antecedentes != null ? antecedentes : "No especificados"));
        System.out.println("Observaciones: " + (observaciones != null ? observaciones : "No especificadas"));
        System.out.println("───────────────────────────────────────────────────────────────");
    }

    private void listarPacientesConSusCitas() {
        try {
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("                PACIENTES CON SUS CITAS");
            System.out.println("═══════════════════════════════════════════════════════════════");
            
            List<Paciente> pacientes = pacienteService.listarPacientes();
            
            if (pacientes.isEmpty()) {
                TableFormatter.printNoDataMessage("No hay pacientes registrados en el sistema");
                return;
            }
            
            TableFormatter formatter = new TableFormatter();
            formatter.setHeaders("ID", "Nombre", "DPI", "Fecha Nac.", "Email", "Citas Programadas");
            
            for (Paciente paciente : pacientes) {

                List<Cita> citas = citaService.listarCitasPorPaciente(paciente.getId());
                long citasProgramadas = citas.stream()
                    .filter(c -> c.getEstado() == EstadoCita.PROGRAMADA)
                    .count();
                
                formatter.addRow(
                    paciente.getId().toString(),
                    paciente.getNombre(),
                    paciente.getDpi(),
                    ConsoleUtils.formatearFecha(paciente.getFechaNacimiento()),
                    paciente.getEmail(),
                    String.valueOf(citasProgramadas)
                );
            }
            
            formatter.print();

            System.out.println();
            if (ConsoleUtils.confirmar("¿Desea ver el detalle de citas de algún paciente?")) {
                mostrarDetalleCitasPaciente(pacientes);
            }
            
        } catch (Exception e) {
            ConsoleUtils.mostrarError("Error al listar pacientes: " + e.getMessage());
        }
    }

    private void mostrarDetalleCitasPaciente(List<Paciente> pacientes) {
        try {
            System.out.println();
            System.out.println("───────────────────────────────────────────────────────────────");
            System.out.println("                SELECCIONAR PACIENTE");
            System.out.println("───────────────────────────────────────────────────────────────");
            
            for (int i = 0; i < pacientes.size(); i++) {
                Paciente p = pacientes.get(i);
                System.out.printf("%d. %s (DPI: %s)%n", i + 1, p.getNombre(), p.getDpi());
            }
            System.out.println((pacientes.size() + 1) + ". Cancelar");
            
            int opcion = ConsoleUtils.leerEnteroEnRango(
                "Seleccione el paciente", 1, pacientes.size() + 1
            );
            
            if (opcion == pacientes.size() + 1) {
                return;
            }
            
            Paciente pacienteSeleccionado = pacientes.get(opcion - 1);
            List<Cita> citas = citaService.listarCitasPorPaciente(pacienteSeleccionado.getId());
            
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("           CITAS DE " + pacienteSeleccionado.getNombre().toUpperCase());
            System.out.println("═══════════════════════════════════════════════════════════════");
            
            if (citas.isEmpty()) {
                TableFormatter.printNoDataMessage("Este paciente no tiene citas registradas");
                return;
            }
            
            TableFormatter citasFormatter = new TableFormatter();
            citasFormatter.setHeaders("ID", "Fecha y Hora", "Médico", "Especialidad", "Estado", "Motivo");
            
            for (Cita cita : citas) {
                citasFormatter.addRow(
                    cita.getId().toString(),
                    ConsoleUtils.formatearFechaHora(cita.getFechaHora()),
                    cita.getMedico().getNombre(),
                    cita.getMedico().getEspecialidad().name(),
                    cita.getEstado().name(),
                    cita.getMotivo() != null ? cita.getMotivo() : "Sin especificar"
                );
            }
            
            citasFormatter.print();
            
        } catch (Exception e) {
            ConsoleUtils.mostrarError("Error al mostrar detalle de citas: " + e.getMessage());
        }
    }

    private void listarMedicosConProximasCitas() {
        try {
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("              MÉDICOS CON PRÓXIMAS CITAS");
            System.out.println("═══════════════════════════════════════════════════════════════");
            
            List<Medico> medicos = medicoService.listarMedicosConProximasCitas();
            
            if (medicos.isEmpty()) {
                TableFormatter.printNoDataMessage("No hay médicos registrados en el sistema");
                return;
            }
            
            TableFormatter formatter = new TableFormatter();
            formatter.setHeaders("ID", "Nombre", "Colegiado", "Especialidad", "Email", "Próximas Citas");
            
            for (Medico medico : medicos) {

                List<Cita> proximasCitas = citaService.listarProximasCitasPorMedico(medico.getId());
                long citasProgramadas = proximasCitas.stream()
                    .filter(c -> c.getEstado() == EstadoCita.PROGRAMADA)
                    .count();
                
                formatter.addRow(
                    medico.getId().toString(),
                    medico.getNombre(),
                    medico.getColegiado(),
                    medico.getEspecialidad().name(),
                    medico.getEmail(),
                    String.valueOf(citasProgramadas)
                );
            }
            
            formatter.print();

            System.out.println();
            if (ConsoleUtils.confirmar("¿Desea ver el detalle de próximas citas de algún médico?")) {
                mostrarDetalleCitasMedico(medicos);
            }
            
        } catch (Exception e) {
            ConsoleUtils.mostrarError("Error al listar médicos: " + e.getMessage());
        }
    }

    private void mostrarDetalleCitasMedico(List<Medico> medicos) {
        try {
            System.out.println();
            System.out.println("───────────────────────────────────────────────────────────────");
            System.out.println("                SELECCIONAR MÉDICO");
            System.out.println("───────────────────────────────────────────────────────────────");
            
            for (int i = 0; i < medicos.size(); i++) {
                Medico m = medicos.get(i);
                System.out.printf("%d. Dr. %s (%s) - %s%n", 
                    i + 1, m.getNombre(), m.getColegiado(), m.getEspecialidad().name());
            }
            System.out.println((medicos.size() + 1) + ". Cancelar");
            
            int opcion = ConsoleUtils.leerEnteroEnRango(
                "Seleccione el médico", 1, medicos.size() + 1
            );
            
            if (opcion == medicos.size() + 1) {
                return; // Cancelar
            }
            
            Medico medicoSeleccionado = medicos.get(opcion - 1);
            List<Cita> proximasCitas = citaService.listarProximasCitasPorMedico(medicoSeleccionado.getId());
            
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("        PRÓXIMAS CITAS DE DR. " + medicoSeleccionado.getNombre().toUpperCase());
            System.out.println("═══════════════════════════════════════════════════════════════");
            
            if (proximasCitas.isEmpty()) {
                TableFormatter.printNoDataMessage("Este médico no tiene próximas citas programadas");
                return;
            }
            
            TableFormatter citasFormatter = new TableFormatter();
            citasFormatter.setHeaders("ID", "Fecha y Hora", "Paciente", "DPI", "Estado", "Motivo");
            
            for (Cita cita : proximasCitas) {
                citasFormatter.addRow(
                    cita.getId().toString(),
                    ConsoleUtils.formatearFechaHora(cita.getFechaHora()),
                    cita.getPaciente().getNombre(),
                    cita.getPaciente().getDpi(),
                    cita.getEstado().name(),
                    cita.getMotivo() != null ? cita.getMotivo() : "Sin especificar"
                );
            }
            
            citasFormatter.print();
            
        } catch (Exception e) {
            ConsoleUtils.mostrarError("Error al mostrar detalle de citas del médico: " + e.getMessage());
        }
    }

    private void buscarCitasPorRangoFechas() {
        try {
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("              BUSCAR CITAS POR RANGO DE FECHAS");
            System.out.println("═══════════════════════════════════════════════════════════════");

            LocalDate fechaInicio = ConsoleUtils.leerFecha("Ingrese la fecha de inicio (dd/MM/yyyy)");
            LocalDate fechaFin = ConsoleUtils.leerFecha("Ingrese la fecha de fin (dd/MM/yyyy)");

            if (fechaInicio.isAfter(fechaFin)) {
                ConsoleUtils.mostrarError("La fecha de inicio no puede ser posterior a la fecha de fin");
                return;
            }

            List<Cita> citasEnRango = citaService.buscarCitasPorRangoFechas(fechaInicio, fechaFin);
            
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.printf("    CITAS ENTRE %s Y %s%n", 
                ConsoleUtils.formatearFecha(fechaInicio), 
                ConsoleUtils.formatearFecha(fechaFin));
            System.out.println("═══════════════════════════════════════════════════════════════");
            
            if (citasEnRango.isEmpty()) {
                TableFormatter.printNoDataMessage("No se encontraron citas en el rango de fechas especificado");
                return;
            }
            
            TableFormatter formatter = new TableFormatter();
            formatter.setHeaders("ID", "Fecha y Hora", "Paciente", "Médico", "Especialidad", "Estado", "Motivo");
            
            for (Cita cita : citasEnRango) {
                formatter.addRow(
                    cita.getId().toString(),
                    ConsoleUtils.formatearFechaHora(cita.getFechaHora()),
                    cita.getPaciente().getNombre(),
                    cita.getMedico().getNombre(),
                    cita.getMedico().getEspecialidad().name(),
                    cita.getEstado().name(),
                    cita.getMotivo() != null ? cita.getMotivo() : "Sin especificar"
                );
            }
            
            formatter.print();

            System.out.println();
            System.out.println("───────────────────────────────────────────────────────────────");
            System.out.println("                      ESTADÍSTICAS");
            System.out.println("───────────────────────────────────────────────────────────────");
            System.out.println("Total de citas encontradas: " + citasEnRango.size());
            
            long programadas = citasEnRango.stream().filter(c -> c.getEstado() == EstadoCita.PROGRAMADA).count();
            long atendidas = citasEnRango.stream().filter(c -> c.getEstado() == EstadoCita.ATENDIDA).count();
            long canceladas = citasEnRango.stream().filter(c -> c.getEstado() == EstadoCita.CANCELADA).count();
            
            System.out.println("Programadas: " + programadas);
            System.out.println("Atendidas: " + atendidas);
            System.out.println("Canceladas: " + canceladas);
            System.out.println("───────────────────────────────────────────────────────────────");
            
        } catch (Exception e) {
            ConsoleUtils.mostrarError("Error al buscar citas por rango de fechas: " + e.getMessage());
        }
    }

    private void verHistorialMedicoPaciente() {
        try {
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("              VER HISTORIAL MÉDICO DE PACIENTE");
            System.out.println("═══════════════════════════════════════════════════════════════");

            List<Paciente> pacientes = pacienteService.listarPacientes();
            
            if (pacientes.isEmpty()) {
                TableFormatter.printNoDataMessage("No hay pacientes registrados en el sistema");
                return;
            }

            System.out.println();
            System.out.println("───────────────────────────────────────────────────────────────");
            System.out.println("                SELECCIONAR PACIENTE");
            System.out.println("───────────────────────────────────────────────────────────────");
            
            for (int i = 0; i < pacientes.size(); i++) {
                Paciente p = pacientes.get(i);
                System.out.printf("%d. %s (DPI: %s) - %s%n", 
                    i + 1, p.getNombre(), p.getDpi(), 
                    ConsoleUtils.formatearFecha(p.getFechaNacimiento()));
            }
            System.out.println((pacientes.size() + 1) + ". Cancelar");
            System.out.println("───────────────────────────────────────────────────────────────");
            
            int opcion = ConsoleUtils.leerEnteroEnRango(
                "Seleccione el paciente", 1, pacientes.size() + 1
            );
            
            if (opcion == pacientes.size() + 1) {
                ConsoleUtils.mostrarInfo("Operación cancelada");
                return;
            }
            
            Paciente pacienteSeleccionado = pacientes.get(opcion - 1);

            Optional<HistorialMedico> historialOpt = historialMedicoService.consultarHistorial(pacienteSeleccionado.getId());
            
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("        HISTORIAL MÉDICO DE " + pacienteSeleccionado.getNombre().toUpperCase());
            System.out.println("═══════════════════════════════════════════════════════════════");
            
            if (historialOpt.isEmpty()) {
                TableFormatter.printNoDataMessage("Este paciente no tiene historial médico registrado");
                System.out.println();
                ConsoleUtils.mostrarInfo("Puede crear un historial médico desde el menú principal (opción 2)");
                return;
            }
            
            HistorialMedico historial = historialOpt.get();

            System.out.println("INFORMACIÓN DEL PACIENTE:");
            System.out.println("───────────────────────────────────────────────────────────────");
            System.out.println("Nombre: " + pacienteSeleccionado.getNombre());
            System.out.println("DPI: " + pacienteSeleccionado.getDpi());
            System.out.println("Fecha de Nacimiento: " + ConsoleUtils.formatearFecha(pacienteSeleccionado.getFechaNacimiento()));
            System.out.println("Email: " + pacienteSeleccionado.getEmail());
            if (pacienteSeleccionado.getTelefono() != null) {
                System.out.println("Teléfono: " + pacienteSeleccionado.getTelefono());
            }
            
            System.out.println();
            System.out.println("HISTORIAL MÉDICO:");
            System.out.println("───────────────────────────────────────────────────────────────");

            System.out.println("ALERGIAS:");
            if (historial.getAlergias() != null && !historial.getAlergias().trim().isEmpty()) {
                System.out.println(historial.getAlergias());
            } else {
                System.out.println("No se han registrado alergias");
            }
            
            System.out.println();
            System.out.println("ANTECEDENTES MÉDICOS:");
            if (historial.getAntecedentes() != null && !historial.getAntecedentes().trim().isEmpty()) {
                System.out.println(historial.getAntecedentes());
            } else {
                System.out.println("No se han registrado antecedentes médicos");
            }
            
            System.out.println();
            System.out.println("OBSERVACIONES:");
            if (historial.getObservaciones() != null && !historial.getObservaciones().trim().isEmpty()) {
                System.out.println(historial.getObservaciones());
            } else {
                System.out.println("No se han registrado observaciones");
            }
            
            System.out.println("───────────────────────────────────────────────────────────────");

            List<Cita> citasPaciente = citaService.listarCitasPorPaciente(pacienteSeleccionado.getId());
            if (!citasPaciente.isEmpty()) {
                System.out.println();
                System.out.println("CITAS REGISTRADAS:");
                System.out.println("───────────────────────────────────────────────────────────────");
                
                TableFormatter citasFormatter = new TableFormatter();
                citasFormatter.setHeaders("Fecha y Hora", "Médico", "Especialidad", "Estado");
                
                for (Cita cita : citasPaciente) {
                    citasFormatter.addRow(
                        ConsoleUtils.formatearFechaHora(cita.getFechaHora()),
                        cita.getMedico().getNombre(),
                        cita.getMedico().getEspecialidad().name(),
                        cita.getEstado().name()
                    );
                }
                
                citasFormatter.print();
            }
            
        } catch (Exception e) {
            ConsoleUtils.mostrarError("Error al consultar historial médico: " + e.getMessage());
        }
    }

    private void eliminarCita() {
        try {
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("                      ELIMINAR CITA");
            System.out.println("═══════════════════════════════════════════════════════════════");

            List<Cita> todasLasCitas = citaService.listarTodasLasCitas();
            
            if (todasLasCitas.isEmpty()) {
                TableFormatter.printNoDataMessage("No hay citas registradas en el sistema");
                return;
            }

            System.out.println();
            System.out.println("───────────────────────────────────────────────────────────────");
            System.out.println("                    SELECCIONAR CITA");
            System.out.println("───────────────────────────────────────────────────────────────");
            
            TableFormatter formatter = new TableFormatter();
            formatter.setHeaders("Opción", "ID", "Fecha y Hora", "Paciente", "Médico", "Estado");
            
            for (int i = 0; i < todasLasCitas.size(); i++) {
                Cita cita = todasLasCitas.get(i);
                formatter.addRow(
                    String.valueOf(i + 1),
                    cita.getId().toString(),
                    ConsoleUtils.formatearFechaHora(cita.getFechaHora()),
                    cita.getPaciente().getNombre(),
                    cita.getMedico().getNombre(),
                    cita.getEstado().name()
                );
            }
            
            formatter.print();
            
            System.out.println((todasLasCitas.size() + 1) + ". Cancelar operación");
            System.out.println("───────────────────────────────────────────────────────────────");

            int opcion = ConsoleUtils.leerEnteroEnRango(
                "Seleccione la cita a eliminar", 1, todasLasCitas.size() + 1
            );
            
            if (opcion == todasLasCitas.size() + 1) {
                ConsoleUtils.mostrarInfo("Operación cancelada");
                return;
            }
            
            Cita citaSeleccionada = todasLasCitas.get(opcion - 1);

            System.out.println();
            System.out.println("───────────────────────────────────────────────────────────────");
            System.out.println("                   DETALLES DE LA CITA");
            System.out.println("───────────────────────────────────────────────────────────────");
            System.out.println("ID: " + citaSeleccionada.getId());
            System.out.println("Fecha y Hora: " + ConsoleUtils.formatearFechaHora(citaSeleccionada.getFechaHora()));
            System.out.println("Paciente: " + citaSeleccionada.getPaciente().getNombre() + " (DPI: " + citaSeleccionada.getPaciente().getDpi() + ")");
            System.out.println("Médico: Dr. " + citaSeleccionada.getMedico().getNombre() + " (" + citaSeleccionada.getMedico().getEspecialidad().name() + ")");
            System.out.println("Estado: " + citaSeleccionada.getEstado().name());
            if (citaSeleccionada.getMotivo() != null) {
                System.out.println("Motivo: " + citaSeleccionada.getMotivo());
            }
            System.out.println("───────────────────────────────────────────────────────────────");

            System.out.println();
            ConsoleUtils.mostrarAdvertencia("⚠️  ADVERTENCIA: Esta acción no se puede deshacer");
            System.out.println();
            ConsoleUtils.mostrarInfo("Al eliminar esta cita:");
            System.out.println("• Se eliminará permanentemente del sistema");
            System.out.println("• NO afectará al paciente ni al médico");
            System.out.println("• El horario quedará disponible para nuevas citas");
            
            System.out.println();
            if (!ConsoleUtils.confirmar("¿Está COMPLETAMENTE SEGURO que desea eliminar esta cita?")) {
                ConsoleUtils.mostrarInfo("Eliminación cancelada");
                return;
            }

            if (!ConsoleUtils.confirmar("Confirme nuevamente: ¿Eliminar la cita del " + 
                ConsoleUtils.formatearFechaHora(citaSeleccionada.getFechaHora()) + "?")) {
                ConsoleUtils.mostrarInfo("Eliminación cancelada");
                return;
            }

            boolean eliminada = citaService.eliminarCita(citaSeleccionada.getId());
            
            if (eliminada) {
                System.out.println();
                ConsoleUtils.mostrarExito("✅ Cita eliminada exitosamente");
                System.out.println("ID eliminado: " + citaSeleccionada.getId());
                System.out.println("Fecha y hora liberada: " + ConsoleUtils.formatearFechaHora(citaSeleccionada.getFechaHora()));
                System.out.println("El horario está ahora disponible para nuevas citas");
            } else {
                ConsoleUtils.mostrarError("No se pudo eliminar la cita");
            }
            
        } catch (Exception e) {
            ConsoleUtils.mostrarError("Error al eliminar la cita: " + e.getMessage());
            System.out.println();
            ConsoleUtils.mostrarInfo("Posibles causas:");
            System.out.println("• La cita ya fue eliminada por otro usuario");
            System.out.println("• Error de conexión a la base de datos");
            System.out.println("• Restricciones de integridad de datos");
        }
    }

    private void eliminarPaciente() {
        try {
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("                    ELIMINAR PACIENTE");
            System.out.println("═══════════════════════════════════════════════════════════════");

            List<Paciente> pacientes = pacienteService.listarPacientes();
            
            if (pacientes.isEmpty()) {
                TableFormatter.printNoDataMessage("No hay pacientes registrados en el sistema");
                return;
            }

            System.out.println();
            System.out.println("───────────────────────────────────────────────────────────────");
            System.out.println("                  SELECCIONAR PACIENTE");
            System.out.println("───────────────────────────────────────────────────────────────");
            
            TableFormatter formatter = new TableFormatter();
            formatter.setHeaders("Opción", "ID", "Nombre", "DPI", "Email", "Citas");
            
            for (int i = 0; i < pacientes.size(); i++) {
                Paciente paciente = pacientes.get(i);
                List<Cita> citas = citaService.listarCitasPorPaciente(paciente.getId());
                
                formatter.addRow(
                    String.valueOf(i + 1),
                    paciente.getId().toString(),
                    paciente.getNombre(),
                    paciente.getDpi(),
                    paciente.getEmail(),
                    String.valueOf(citas.size())
                );
            }
            
            formatter.print();
            
            System.out.println((pacientes.size() + 1) + ". Cancelar operación");
            System.out.println("───────────────────────────────────────────────────────────────");

            int opcion = ConsoleUtils.leerEnteroEnRango(
                "Seleccione el paciente a eliminar", 1, pacientes.size() + 1
            );
            
            if (opcion == pacientes.size() + 1) {
                ConsoleUtils.mostrarInfo("Operación cancelada");
                return;
            }
            
            Paciente pacienteSeleccionado = pacientes.get(opcion - 1);

            List<Cita> citasPaciente = citaService.listarCitasPorPaciente(pacienteSeleccionado.getId());
            boolean tieneHistorial = historialMedicoService.existeHistorial(pacienteSeleccionado.getId());

            System.out.println();
            System.out.println("───────────────────────────────────────────────────────────────");
            System.out.println("                 DETALLES DEL PACIENTE");
            System.out.println("───────────────────────────────────────────────────────────────");
            System.out.println("ID: " + pacienteSeleccionado.getId());
            System.out.println("Nombre: " + pacienteSeleccionado.getNombre());
            System.out.println("DPI: " + pacienteSeleccionado.getDpi());
            System.out.println("Fecha de Nacimiento: " + ConsoleUtils.formatearFecha(pacienteSeleccionado.getFechaNacimiento()));
            System.out.println("Email: " + pacienteSeleccionado.getEmail());
            if (pacienteSeleccionado.getTelefono() != null) {
                System.out.println("Teléfono: " + pacienteSeleccionado.getTelefono());
            }
            System.out.println("───────────────────────────────────────────────────────────────");

            System.out.println();
            System.out.println("ANÁLISIS DE IMPACTO DE LA ELIMINACIÓN:");
            System.out.println("═══════════════════════════════════════════════════════════════");
            
            System.out.println("DECISIÓN SOBRE CASCADAS Y RESTRICCIONES:");
            System.out.println("───────────────────────────────────────────────────────────────");
            System.out.println("El sistema está configurado con CascadeType.ALL y orphanRemoval=true");
            System.out.println("para las relaciones OneToOne y OneToMany desde Paciente.");
            System.out.println();
            
            System.out.println("DATOS QUE SERÁN ELIMINADOS AUTOMÁTICAMENTE:");
            System.out.println("───────────────────────────────────────────────────────────────");

            if (tieneHistorial) {
                System.out.println("HISTORIAL MÉDICO: SÍ será eliminado (relación OneToOne con cascade)");
                System.out.println("   • Se eliminará toda la información médica del paciente");
                System.out.println("   • Alergias, antecedentes y observaciones se perderán");
            } else {
                System.out.println("HISTORIAL MÉDICO: No existe historial para este paciente");
            }
            
            System.out.println();

            if (!citasPaciente.isEmpty()) {
                System.out.println("CITAS MÉDICAS: SÍ serán eliminadas (relación OneToMany con cascade)");
                System.out.println("   • Total de citas a eliminar: " + citasPaciente.size());
                
                long programadas = citasPaciente.stream().filter(c -> c.getEstado() == EstadoCita.PROGRAMADA).count();
                long atendidas = citasPaciente.stream().filter(c -> c.getEstado() == EstadoCita.ATENDIDA).count();
                long canceladas = citasPaciente.stream().filter(c -> c.getEstado() == EstadoCita.CANCELADA).count();
                
                System.out.println("   • Citas programadas: " + programadas + " (se liberarán los horarios)");
                System.out.println("   • Citas atendidas: " + atendidas + " (se perderá el historial de consultas)");
                System.out.println("   • Citas canceladas: " + canceladas);
                
                if (programadas > 0) {
                    System.out.println();
                    ConsoleUtils.mostrarAdvertencia("⚠️  ATENCIÓN: Hay " + programadas + " cita(s) programada(s) que se cancelarán");
                }
            } else {
                System.out.println("CITAS MÉDICAS: No hay citas registradas para este paciente");
            }
            
            System.out.println();
            System.out.println("DATOS QUE NO SERÁN AFECTADOS:");
            System.out.println("───────────────────────────────────────────────────────────────");
            System.out.println("MÉDICOS: No serán eliminados ni modificados");
            System.out.println("OTROS PACIENTES: No serán afectados");
            System.out.println("INTEGRIDAD DEL SISTEMA: Se mantendrá completamente");
            
            System.out.println();
            System.out.println("JUSTIFICACIÓN TÉCNICA:");
            System.out.println("───────────────────────────────────────────────────────────────");
            System.out.println("• Historial médico: Relación OneToOne con @MapsId - eliminación automática");
            System.out.println("• Citas: Relación OneToMany con CascadeType.ALL - eliminación en cascada");
            System.out.println("• Médicos: Relación ManyToOne desde Cita - no se ven afectados");
            System.out.println("• Integridad referencial: Garantizada por JPA/Hibernate");

            System.out.println();
            ConsoleUtils.mostrarAdvertencia("ADVERTENCIA CRÍTICA: Esta acción es IRREVERSIBLE");
            
            System.out.println();
            if (!ConsoleUtils.confirmar("¿Ha leído y entendido completamente el impacto de esta eliminación?")) {
                ConsoleUtils.mostrarInfo("Eliminación cancelada - Es importante entender el impacto antes de proceder");
                return;
            }
            
            if (!ConsoleUtils.confirmar("¿Está COMPLETAMENTE SEGURO que desea eliminar al paciente " + 
                pacienteSeleccionado.getNombre() + "?")) {
                ConsoleUtils.mostrarInfo("Eliminación cancelada");
                return;
            }

            String confirmacionFinal = String.format("CONFIRME ESCRIBIENDO 'ELIMINAR': Eliminar paciente %s (DPI: %s) y TODOS sus datos asociados", 
                pacienteSeleccionado.getNombre(), pacienteSeleccionado.getDpi());
            
            String confirmacion = ConsoleUtils.leerTextoNoVacio(confirmacionFinal);
            
            if (!"ELIMINAR".equals(confirmacion.toUpperCase())) {
                ConsoleUtils.mostrarInfo("Eliminación cancelada - Confirmación incorrecta");
                return;
            }

            boolean eliminado = pacienteService.eliminarPaciente(pacienteSeleccionado.getId());
            
            if (eliminado) {
                System.out.println();
                ConsoleUtils.mostrarExito("PACIENTE ELIMINADO EXITOSAMENTE");
                System.out.println("═══════════════════════════════════════════════════════════════");
                System.out.println("Paciente eliminado: " + pacienteSeleccionado.getNombre());
                System.out.println("DPI: " + pacienteSeleccionado.getDpi());
                
                if (tieneHistorial) {
                    System.out.println("Historial médico eliminado automáticamente");
                }
                
                if (!citasPaciente.isEmpty()) {
                    System.out.println(" " + citasPaciente.size() + " cita(s) eliminada(s) automáticamente");
                }
                
                System.out.println("Integridad del sistema mantenida");
                System.out.println("═══════════════════════════════════════════════════════════════");
                
            } else {
                ConsoleUtils.mostrarError("No se pudo eliminar el paciente");
            }
            
        } catch (Exception e) {
            ConsoleUtils.mostrarError("Error al eliminar el paciente: " + e.getMessage());
            System.out.println();
            ConsoleUtils.mostrarInfo("Posibles causas:");
            System.out.println("• El paciente ya fue eliminado por otro usuario");
            System.out.println("• Error de conexión a la base de datos");
            System.out.println("• Restricciones de integridad no contempladas");
            System.out.println("• Transacción interrumpida durante el proceso");
        }
    }

    private void agendarCita() {
        try {
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("                      AGENDAR CITA");
            System.out.println("═══════════════════════════════════════════════════════════════");

            Paciente pacienteSeleccionado = seleccionarPacienteParaCita();
            if (pacienteSeleccionado == null) {
                ConsoleUtils.mostrarInfo("Operación cancelada");
                return;
            }

            Medico medicoSeleccionado = seleccionarMedicoParaCita();
            if (medicoSeleccionado == null) {
                ConsoleUtils.mostrarInfo("Operación cancelada");
                return;
            }

            LocalDateTime fechaHora = ConsoleUtils.leerFechaHora("Ingrese la fecha y hora de la cita (dd/MM/yyyy HH:mm)");

            String motivo = ConsoleUtils.leerTexto("Ingrese el motivo de la cita (opcional)");

            System.out.println();
            System.out.println("───────────────────────────────────────────────────────────────");
            System.out.println("                    RESUMEN DE LA CITA");
            System.out.println("───────────────────────────────────────────────────────────────");
            System.out.println("Paciente: " + pacienteSeleccionado.getNombre() + " (DPI: " + pacienteSeleccionado.getDpi() + ")");
            System.out.println("Médico: " + medicoSeleccionado.getNombre() + " (" + medicoSeleccionado.getEspecialidad() + ")");
            System.out.println("Fecha y hora: " + ConsoleUtils.formatearFechaHora(fechaHora));
            System.out.println("Motivo: " + (motivo.isEmpty() ? "No especificado" : motivo));
            System.out.println("───────────────────────────────────────────────────────────────");

            if (ConsoleUtils.confirmar("¿Desea agendar esta cita?")) {
                Cita citaAgendada = citaService.agendarCita(
                    pacienteSeleccionado.getId(),
                    medicoSeleccionado.getId(),
                    fechaHora,
                    motivo.isEmpty() ? null : motivo
                );
                
                System.out.println();
                ConsoleUtils.mostrarExito("Cita agendada exitosamente");
                System.out.println("ID de la cita: " + citaAgendada.getId());
                System.out.println("Estado: " + citaAgendada.getEstado());
                
            } else {
                ConsoleUtils.mostrarInfo("Agendamiento de cita cancelado");
            }
            
        } catch (Exception e) {
            ConsoleUtils.mostrarError("Error al agendar la cita: " + e.getMessage());
            System.out.println();
            ConsoleUtils.mostrarInfo("Verifique que:");
            System.out.println("• La fecha y hora no esté en el pasado");
            System.out.println("• El médico no tenga otra cita a la misma hora");
            System.out.println("• Los datos ingresados sean válidos");
        }
    }

    private Paciente seleccionarPacienteParaCita() {
        try {
            List<Paciente> pacientes = pacienteService.listarPacientes();
            
            if (pacientes.isEmpty()) {
                ConsoleUtils.mostrarError("No hay pacientes registrados en el sistema");
                System.out.println();
                ConsoleUtils.mostrarInfo("Debe registrar al menos un paciente antes de agendar una cita");
                return null;
            }
            
            System.out.println();
            System.out.println("───────────────────────────────────────────────────────────────");
            System.out.println("                    SELECCIONAR PACIENTE");
            System.out.println("───────────────────────────────────────────────────────────────");
            
            for (int i = 0; i < pacientes.size(); i++) {
                Paciente p = pacientes.get(i);
                System.out.printf("%d. %s (DPI: %s)%n", i + 1, p.getNombre(), p.getDpi());
            }
            System.out.println((pacientes.size() + 1) + ". Cancelar");
            System.out.println("───────────────────────────────────────────────────────────────");
            
            int opcion = ConsoleUtils.leerEnteroEnRango("Seleccione el paciente", 1, pacientes.size() + 1);
            
            if (opcion == pacientes.size() + 1) {
                return null;
            }
            
            return pacientes.get(opcion - 1);
            
        } catch (Exception e) {
            ConsoleUtils.mostrarError("Error al obtener la lista de pacientes: " + e.getMessage());
            return null;
        }
    }

    private Medico seleccionarMedicoParaCita() {
        try {
            List<Medico> medicos = medicoService.listarMedicos();
            
            if (medicos.isEmpty()) {
                ConsoleUtils.mostrarError("No hay médicos registrados en el sistema");
                System.out.println();
                ConsoleUtils.mostrarInfo("Debe registrar al menos un médico antes de agendar una cita");
                return null;
            }
            
            System.out.println();
            System.out.println("───────────────────────────────────────────────────────────────");
            System.out.println("                     SELECCIONAR MÉDICO");
            System.out.println("───────────────────────────────────────────────────────────────");
            
            for (int i = 0; i < medicos.size(); i++) {
                Medico m = medicos.get(i);
                System.out.printf("%d. Dr. %s (%s) - Colegiado: %s%n", 
                    i + 1, m.getNombre(), m.getEspecialidad(), m.getColegiado());
            }
            System.out.println((medicos.size() + 1) + ". Cancelar");
            System.out.println("───────────────────────────────────────────────────────────────");
            
            int opcion = ConsoleUtils.leerEnteroEnRango("Seleccione el médico", 1, medicos.size() + 1);
            
            if (opcion == medicos.size() + 1) {
                return null;
            }
            
            return medicos.get(opcion - 1);
            
        } catch (Exception e) {
            ConsoleUtils.mostrarError("Error al obtener la lista de médicos: " + e.getMessage());
            return null;
        }
    }

    private void cambiarEstadoCita() {
        try {
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("                   CAMBIAR ESTADO DE CITA");
            System.out.println("═══════════════════════════════════════════════════════════════");

            List<Cita> citasProgramadas = citaService.listarCitasPorEstado(EstadoCita.PROGRAMADA);
            
            if (citasProgramadas.isEmpty()) {
                ConsoleUtils.mostrarError("No hay citas programadas en el sistema");
                System.out.println();
                ConsoleUtils.mostrarInfo("Solo se pueden cambiar estados de citas PROGRAMADAS");
                return;
            }
            
            System.out.println();
            System.out.println("───────────────────────────────────────────────────────────────");
            System.out.println("                    CITAS PROGRAMADAS");
            System.out.println("───────────────────────────────────────────────────────────────");
            
            for (int i = 0; i < citasProgramadas.size(); i++) {
                Cita c = citasProgramadas.get(i);
                System.out.printf("%d. %s - Dr. %s (%s) - %s%n", 
                    i + 1,
                    c.getPaciente().getNombre(),
                    c.getMedico().getNombre(),
                    c.getMedico().getEspecialidad(),
                    ConsoleUtils.formatearFechaHora(c.getFechaHora()));
            }
            System.out.println((citasProgramadas.size() + 1) + ". Cancelar");
            System.out.println("───────────────────────────────────────────────────────────────");
            
            int opcionCita = ConsoleUtils.leerEnteroEnRango("Seleccione la cita", 1, citasProgramadas.size() + 1);
            
            if (opcionCita == citasProgramadas.size() + 1) {
                ConsoleUtils.mostrarInfo("Operación cancelada");
                return;
            }
            
            Cita citaSeleccionada = citasProgramadas.get(opcionCita - 1);

            System.out.println();
            System.out.println("───────────────────────────────────────────────────────────────");
            System.out.println("                    NUEVO ESTADO");
            System.out.println("───────────────────────────────────────────────────────────────");
            System.out.println("1. ATENDIDA");
            System.out.println("2. CANCELADA");
            System.out.println("3. Cancelar operación");
            System.out.println("───────────────────────────────────────────────────────────────");
            
            int opcionEstado = ConsoleUtils.leerEnteroEnRango("Seleccione el nuevo estado", 1, 3);
            
            if (opcionEstado == 3) {
                ConsoleUtils.mostrarInfo("Operación cancelada");
                return;
            }
            
            EstadoCita nuevoEstado = (opcionEstado == 1) ? EstadoCita.ATENDIDA : EstadoCita.CANCELADA;

            System.out.println();
            System.out.println("───────────────────────────────────────────────────────────────");
            System.out.println("                    RESUMEN DEL CAMBIO");
            System.out.println("───────────────────────────────────────────────────────────────");
            System.out.println("Cita ID: " + citaSeleccionada.getId());
            System.out.println("Paciente: " + citaSeleccionada.getPaciente().getNombre());
            System.out.println("Médico: Dr. " + citaSeleccionada.getMedico().getNombre());
            System.out.println("Fecha: " + ConsoleUtils.formatearFechaHora(citaSeleccionada.getFechaHora()));
            System.out.println("Estado actual: " + citaSeleccionada.getEstado());
            System.out.println("Nuevo estado: " + nuevoEstado);
            System.out.println("───────────────────────────────────────────────────────────────");

            if (ConsoleUtils.confirmar("¿Desea cambiar el estado de esta cita?")) {
                Cita citaActualizada = citaService.cambiarEstadoCita(citaSeleccionada.getId(), nuevoEstado);
                
                System.out.println();
                ConsoleUtils.mostrarExito("Estado de la cita cambiado exitosamente");
                System.out.println("Nuevo estado: " + citaActualizada.getEstado());
                
            } else {
                ConsoleUtils.mostrarInfo("Cambio de estado cancelado");
            }
            
        } catch (Exception e) {
            ConsoleUtils.mostrarError("Error al cambiar el estado de la cita: " + e.getMessage());
            System.out.println();
            ConsoleUtils.mostrarInfo("Verifique que:");
            System.out.println("• La cita seleccionada esté en estado PROGRAMADA");
            System.out.println("• No haya problemas de conexión a la base de datos");
            System.out.println("• La cita no haya sido modificada por otro usuario");
        }
    }

    private void cargarSemillaDatos() {
        try {
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("                    SEMILLA DE DATOS");
            System.out.println("═══════════════════════════════════════════════════════════════");
            
            // Crear instancia de SemillaDatos
            SemillaDatos semillaDatos = new SemillaDatos(emf);
            
            // Verificar si ya existen datos
            if (semillaDatos.existenDatos()) {
                System.out.println();
                ConsoleUtils.mostrarInfo("El sistema ya contiene datos:");
                System.out.println("Registros actuales: " + semillaDatos.obtenerConteoActual());
                System.out.println();
                
                if (!ConsoleUtils.confirmar("¿Desea cargar datos adicionales de prueba?")) {
                    ConsoleUtils.mostrarInfo("Operación cancelada");
                    return;
                }
            } else {
                System.out.println();
                ConsoleUtils.mostrarInfo("El sistema está vacío. Se cargarán datos de prueba completos.");
                System.out.println();
                
                if (!ConsoleUtils.confirmar("¿Desea proceder con la carga de datos de prueba?")) {
                    ConsoleUtils.mostrarInfo("Operación cancelada");
                    return;
                }
            }
            
            System.out.println();
            ConsoleUtils.mostrarInfo("Iniciando carga de datos de prueba...");
            System.out.println("Esto puede tomar unos momentos...");
            System.out.println();

            semillaDatos.cargarDatosDePrueba();
            
            System.out.println();
            ConsoleUtils.mostrarExito("¡Datos de prueba cargados exitosamente!");
            System.out.println();
            ConsoleUtils.mostrarInfo("Ahora puede:");
            System.out.println("• Explorar las consultas para ver los datos cargados");
            System.out.println("• Probar las funcionalidades con datos reales");
            System.out.println("• Agendar nuevas citas con los pacientes y médicos creados");
            System.out.println("• Verificar los historiales médicos generados");
            
        } catch (Exception e) {
            ConsoleUtils.mostrarError("Error al cargar los datos de prueba: " + e.getMessage());
            System.out.println();
            ConsoleUtils.mostrarInfo("Posibles causas:");
            System.out.println("• Error de conexión a la base de datos");
            System.out.println("• Datos duplicados (DPI o colegiado ya existentes)");
            System.out.println("• Problemas de configuración de JPA/Hibernate");
            System.out.println("• Restricciones de integridad en la base de datos");
            System.out.println();
            ConsoleUtils.mostrarInfo("Verifique la configuración y vuelva a intentar");
        }
    }

    public static void main(String[] args) {
        HospitalConsoleApp app = new HospitalConsoleApp();
        app.iniciar();
    }
}