package com.project.demo.logic.service;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventAttendee;
import com.project.demo.logic.entity.appointment.Appointment;
import com.project.demo.logic.exceptions.HorarioOcupadoException;
import com.project.demo.logic.repository.AppointmentRepository;
import com.project.demo.logic.service.calendar.GoogleCalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository repository;

    @Autowired
    private GoogleCalendarService calendarService;

    public Appointment schedule(Appointment appointment) {
        boolean conflict = repository.existsByProfessionalAndStartLessThanEqualAndEndGreaterThanEqual(
                appointment.getProfessional(), appointment.getEnd(), appointment.getStart());

        if (conflict) {
            throw new HorarioOcupadoException("El profesional ya tiene una cita en ese horario.");
        }

        appointment.setStatus("pendiente");
        Appointment saved = repository.save(appointment);

        // Crear evento en Google Calendar
        crearEventoGoogleCalendar(saved);

        return saved;
    }

    public Appointment reschedule(Long id, LocalDateTime nuevoInicio, LocalDateTime nuevoFin) {
        Optional<Appointment> optional = repository.findById(id);
        if (optional.isEmpty()) {
            throw new RuntimeException("Cita no encontrada.");
        }

        Appointment cita = optional.get();

        if (cita.getStatus().equalsIgnoreCase("completada") ||
                cita.getStatus().equalsIgnoreCase("cancelada")) {
            throw new RuntimeException("No se puede modificar una cita completada o cancelada.");
        }

        boolean conflicto = repository.existsByProfessionalAndStartLessThanEqualAndEndGreaterThanEqual(
                cita.getProfessional(), nuevoFin, nuevoInicio);

        if (conflicto) {
            throw new HorarioOcupadoException("Ya existe una cita para este profesional en ese horario.");
        }

        cita.setStart(nuevoInicio);
        cita.setEnd(nuevoFin);
        cita.setStatus("pendiente");

        Appointment saved = repository.save(cita);

        // Crear nuevo evento (no actualiza, solo inserta)
        crearEventoGoogleCalendar(saved);

        return saved;
    }

    private void crearEventoGoogleCalendar(Appointment cita) {
        try {
            Event event = new Event()
                    .setSummary("Cita Thymia con " + cita.getProfessional().getName())
                    .setDescription("Sesión agendada desde Thymia")
                    .setStart(new EventDateTime().setDateTime(new DateTime(cita.getStart().toString())))
                    .setEnd(new EventDateTime().setDateTime(new DateTime(cita.getEnd().toString())))
                    .setAttendees(List.of(
                            new EventAttendee().setEmail(cita.getPatient().getEmail()),
                            new EventAttendee().setEmail(cita.getProfessional().getEmail())
                    ));


            Event createdEvent = calendarService.createEvent("primary", event);


            String meetLink = createdEvent.getHangoutLink();
            if (meetLink != null) {
                cita.setMeetUrl(meetLink);
                repository.save(cita); // Guardar el enlace en la base de datos
            }

        } catch (Exception e) {
            System.err.println("Error al sincronizar con Google Calendar: " + e.getMessage());
        }
    }
}
