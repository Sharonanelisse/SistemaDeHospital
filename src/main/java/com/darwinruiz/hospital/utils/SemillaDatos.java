package com.darwinruiz.hospital.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.darwinruiz.hospital.enums.Especialidad;
import com.darwinruiz.hospital.enums.EstadoCita;
import com.darwinruiz.hospital.models.Cita;
import com.darwinruiz.hospital.models.Medico;
import com.darwinruiz.hospital.models.Paciente;
import com.darwinruiz.hospital.services.CitaService;
import com.darwinruiz.hospital.services.HistorialMedicoService;
import com.darwinruiz.hospital.services.MedicoService;
import com.darwinruiz.hospital.services.PacienteService;

import jakarta.persistence.EntityManagerFactory;

public class SemillaDatos {

    private final PacienteService pacienteService;
    private final MedicoService medicoService;
    private final HistorialMedicoService historialMedicoService;
    private final CitaService citaService;

    private final List<Paciente> pacientesCreados = new ArrayList<>();
    private final List<Medico> medicosCreados = new ArrayList<>();

    public SemillaDatos(EntityManagerFactory emf) {
        this.pacienteService = new PacienteService(emf);
        this.medicoService = new MedicoService(emf);
        this.historialMedicoService = new HistorialMedicoService(emf);
        this.citaService = new CitaService(emf);
    }

    public void cargarDatosDePrueba() {
        try {
            ConsoleUtils.mostrarInfo("Iniciando carga de datos de prueba...");

            cargarPacientes();
            ConsoleUtils.mostrarExito("✓ Pacientes cargados: " + pacientesCreados.size());

            cargarMedicos();
            ConsoleUtils.mostrarExito("✓ Médicos cargados: " + medicosCreados.size());

            cargarHistoriales();
            ConsoleUtils.mostrarExito("✓ Historiales médicos cargados");

            cargarCitas();
            ConsoleUtils.mostrarExito("✓ Citas cargadas");

            System.out.println();
            ConsoleUtils.mostrarExito("¡Datos de prueba cargados exitosamente!");
            mostrarResumenDatos();

        } catch (Exception e) {
            ConsoleUtils.mostrarError("Error al cargar datos de prueba: " + e.getMessage());
            throw new RuntimeException("Fallo en la carga de datos de prueba", e);
        }
    }

    private void cargarPacientes() {
        Object[][] datosPacientes = {
            {"María Elena García López", "1234567890123", LocalDate.of(1985, 3, 15), "555-0101", "maria.garcia@email.com"},
            {"Carlos Roberto Mendoza", "2345678901234", LocalDate.of(1978, 7, 22), "555-0102", "carlos.mendoza@email.com"},
            {"Ana Sofía Rodríguez", "3456789012345", LocalDate.of(1992, 11, 8), "555-0103", "ana.rodriguez@email.com"},
            {"José Luis Hernández", "4567890123456", LocalDate.of(1965, 1, 30), "555-0104", "jose.hernandez@email.com"},
            {"Patricia Isabel Morales", "5678901234567", LocalDate.of(1988, 9, 12), "555-0105", "patricia.morales@email.com"},
            {"Roberto Carlos Jiménez", "6789012345678", LocalDate.of(1975, 4, 18), "555-0106", "roberto.jimenez@email.com"},
            {"Lucía Fernanda Castro", "7890123456789", LocalDate.of(1990, 6, 25), null, "lucia.castro@email.com"},
            {"Miguel Ángel Vargas", "8901234567890", LocalDate.of(1982, 12, 3), "555-0108", "miguel.vargas@email.com"}
        };

        for (Object[] datos : datosPacientes) {
            try {
                Paciente paciente = pacienteService.registrarPaciente(
                        (String) datos[0],
                        (String) datos[1],
                        (LocalDate) datos[2],
                        (String) datos[3],
                        (String) datos[4]
                );
                pacientesCreados.add(paciente);
            } catch (Exception e) {
                var pacienteExistente = pacienteService.buscarPorDpi((String) datos[1]);
                if (pacienteExistente.isPresent()) {
                    pacientesCreados.add(pacienteExistente.get());
                }
            }
        }
    }

    private void cargarMedicos() {
        Object[][] datosMedicos = {
            {"Dr. Fernando Alejandro Ruiz", "COL001", Especialidad.CARDIOLOGIA, "dr.ruiz@hospital.com"},
            {"Dra. Carmen Beatriz López", "COL002", Especialidad.NEUROLOGIA, "dra.lopez@hospital.com"},
            {"Dr. Eduardo Martín Sánchez", "COL003", Especialidad.PEDIATRIA, "dr.sanchez@hospital.com"},
            {"Dra. Gabriela Esperanza Torres", "COL004", Especialidad.GINECOLOGIA, "dra.torres@hospital.com"},
            {"Dr. Andrés Felipe Ramírez", "COL005", Especialidad.TRAUMATOLOGIA, "dr.ramirez@hospital.com"},
            {"Dra. Mónica Alejandra Vega", "COL006", Especialidad.DERMATOLOGIA, "dra.vega@hospital.com"},
            {"Dr. Ricardo Javier Moreno", "COL007", Especialidad.OFTALMOLOGIA, "dr.moreno@hospital.com"},
            {"Dra. Silvia Marcela Herrera", "COL008", Especialidad.PSIQUIATRIA, "dra.herrera@hospital.com"}
        };

        for (Object[] datos : datosMedicos) {
            try {
                Medico medico = medicoService.registrarMedico(
                        (String) datos[0],
                        (String) datos[1],
                        (Especialidad) datos[2],
                        (String) datos[3]
                );
                medicosCreados.add(medico);
            } catch (Exception e) {
                var medicoExistente = medicoService.buscarPorColegiado((String) datos[1]);
                if (medicoExistente.isPresent()) {
                    medicosCreados.add(medicoExistente.get());
                }
            }
        }
    }

    private void cargarHistoriales() {
        if (pacientesCreados.isEmpty()) {
            ConsoleUtils.mostrarError("No hay pacientes disponibles para crear historiales");
            return;
        }

        String[][] datosHistoriales = {
            {"Penicilina, Aspirina", "Hipertensión arterial desde 2018", "Paciente colaborador, sigue tratamiento regularmente"},
            {"Ninguna conocida", "Diabetes tipo 2, Colesterol alto", "Requiere control nutricional estricto"},
            {"Polen, Ácaros", "Asma bronquial desde la infancia", "Porta inhalador de rescate siempre"},
            {"Mariscos, Nueces", "Gastritis crónica", "Evitar alimentos irritantes, tomar medicación con alimentos"},
            {"Ibuprofeno", "Migrañas frecuentes", "Episodios de cefalea 2-3 veces por semana"},
            {"Ninguna conocida", "Artritis reumatoide", "En tratamiento con metotrexato, controles mensuales"},
            {"Látex", "Ninguno relevante", "Paciente joven, sin antecedentes significativos"},
            {"Sulfonamidas", "Hipertensión, Arritmia cardíaca", "Requiere monitoreo cardíaco regular"}
        };

        int cantidadHistoriales = Math.min(pacientesCreados.size(), datosHistoriales.length);

        for (int i = 0; i < cantidadHistoriales; i++) {
            try {
                Paciente paciente = pacientesCreados.get(i);
                String[] datosHistorial = datosHistoriales[i];

                historialMedicoService.crearOActualizarHistorial(
                        paciente.getId(),
                        datosHistorial[0],
                        datosHistorial[1],
                        datosHistorial[2]
                );
            } catch (Exception e) {
                ConsoleUtils.mostrarError("Error al crear historial para paciente " + (i + 1) + ": " + e.getMessage());
            }
        }
    }

    private void cargarCitas() {
        if (pacientesCreados.isEmpty() || medicosCreados.isEmpty()) {
            ConsoleUtils.mostrarError("No hay suficientes pacientes o médicos para crear citas");
            return;
        }

        LocalDateTime ahora = LocalDateTime.now();

        Object[][] citasProgramadas = {
            {0, 0, ahora.plusDays(1).withHour(9).withMinute(0), "Consulta de control cardiológico", EstadoCita.PROGRAMADA},
            {1, 1, ahora.plusDays(2).withHour(10).withMinute(30), "Evaluación neurológica", EstadoCita.PROGRAMADA},
            {2, 2, ahora.plusDays(3).withHour(14).withMinute(0), "Control pediátrico", EstadoCita.PROGRAMADA},
            {3, 3, ahora.plusDays(4).withHour(11).withMinute(15), "Consulta ginecológica", EstadoCita.PROGRAMADA},
            {4, 4, ahora.plusDays(5).withHour(15).withMinute(30), "Evaluación traumatológica", EstadoCita.PROGRAMADA}
        };

        Object[][] citasAtendidas = {
            {5, 5, ahora.minusDays(7).withHour(9).withMinute(0), "Consulta dermatológica", EstadoCita.ATENDIDA},
            {6, 6, ahora.minusDays(5).withHour(16).withMinute(0), "Examen oftalmológico", EstadoCita.ATENDIDA},
            {7, 7, ahora.minusDays(3).withHour(13).withMinute(30), "Consulta psiquiátrica", EstadoCita.ATENDIDA}
        };

        Object[][] citasCanceladas = {
            {0, 2, ahora.plusDays(10).withHour(8).withMinute(0), "Consulta pediátrica", EstadoCita.CANCELADA},
            {1, 4, ahora.plusDays(12).withHour(17).withMinute(0), "Evaluación traumatológica", EstadoCita.CANCELADA}
        };

        crearCitasDesdeArray(citasProgramadas);
        crearCitasDesdeArray(citasAtendidas);
        crearCitasDesdeArray(citasCanceladas);
    }

    private void crearCitasDesdeArray(Object[][] datosCitas) {
        for (Object[] datos : datosCitas) {
            try {
                int indicePaciente = (Integer) datos[0];
                int indiceMedico = (Integer) datos[1];

                if (indicePaciente < pacientesCreados.size() && indiceMedico < medicosCreados.size()) {
                    Paciente paciente = pacientesCreados.get(indicePaciente);
                    Medico medico = medicosCreados.get(indiceMedico);
                    LocalDateTime fechaHora = (LocalDateTime) datos[2];
                    String motivo = (String) datos[3];
                    EstadoCita estado = (EstadoCita) datos[4];

                    Cita cita = citaService.agendarCita(
                            paciente.getId(),
                            medico.getId(),
                            fechaHora,
                            motivo
                    );

                    if (estado != EstadoCita.PROGRAMADA) {
                        citaService.cambiarEstadoCita(cita.getId(), estado);
                    }
                }
            } catch (Exception e) {
                ConsoleUtils.mostrarError("Error al crear cita: " + e.getMessage());
            }
        }
    }

    private void mostrarResumenDatos() {
        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("                    RESUMEN DE DATOS CARGADOS");
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("Pacientes registrados: " + pacientesCreados.size());
        System.out.println("Médicos registrados: " + medicosCreados.size());
        System.out.println("Historiales médicos creados");
        System.out.println("Citas programadas (varios estados)");
        System.out.println();
        System.out.println("Los datos incluyen:");
        System.out.println("• Pacientes con información completa y DPIs únicos");
        System.out.println("• Médicos de diferentes especialidades");
        System.out.println("• Historiales médicos con alergias y antecedentes");
        System.out.println("• Citas en diferentes estados (programadas, atendidas, canceladas)");
        System.out.println("• Fechas variadas (pasadas y futuras) para pruebas completas");
        System.out.println("═══════════════════════════════════════════════════════════════");
    }

    public boolean existenDatos() {
        try {
            List<Paciente> pacientes = pacienteService.listarPacientes();
            List<Medico> medicos = medicoService.listarMedicos();
            return !pacientes.isEmpty() || !medicos.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public String obtenerConteoActual() {
        try {
            int pacientes = pacienteService.listarPacientes().size();
            int medicos = medicoService.listarMedicos().size();
            return String.format("Pacientes: %d, Médicos: %d", pacientes, medicos);
        } catch (Exception e) {
            return "No se pudo obtener el conteo actual";
        }
    }
}
