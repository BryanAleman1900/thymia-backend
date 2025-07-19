package com.project.demo.rest.appointment;

import com.project.demo.logic.entity.appointment.Appointment;
import com.project.demo.logic.entity.appointment.AppointmentRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentRestController {
    @Autowired
    private AppointmentRepository appointmentRepo;
    @Autowired
    private UserRepository userRepo;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Appointment> createAppointment(
            @RequestParam Long patientId,
            @RequestParam Long doctorId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime,
            @RequestParam String title,
            @RequestParam String description,
            @RequestHeader("Authorization") String accessToken  // Token de Google OAuth
    ) throws Exception {
        // 1. Validar paciente y doctor
        User patient = userRepo.findById(patientId).orElseThrow();
        User doctor = userRepo.findById(doctorId).orElseThrow();

        // 2. Crear evento en Google Calendar
        Calendar calendarService = new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                req -> req.getHeaders().setAuthorization("Bearer " + accessToken)
        ).setApplicationName("Thymia").build();

        Event event = new Event()
                .setSummary(title)
                .setDescription(description)
                .setStart(new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(startTime.toString())))
                .setEnd(new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(endTime.toString())));

        Event createdEvent = calendarService.events()
                .insert("primary", event)
                .execute();

        // 3. Guardar en base de datos
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setTitle(title);
        appointment.setDescription(description);
        appointment.setGoogleEventId(createdEvent.getId());

        return ResponseEntity.ok(appointmentRepo.save(appointment));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public List<Appointment> getByPatient(@PathVariable Long patientId) {
        return appointmentRepo.findByPatientId(patientId);
    }
}