package com.project.demo.rest.appointment;

import com.project.demo.logic.entity.appointment.Appointment;
import com.project.demo.logic.entity.appointment.AppointmentRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.client.util.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentRestController {

    private final AppointmentRepository appointmentRepo;
    private final UserRepository userRepo;
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Autowired
    public AppointmentRestController(AppointmentRepository appointmentRepo, UserRepository userRepo) {
        this.appointmentRepo = appointmentRepo;
        this.userRepo = userRepo;
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Appointment> createAppointment(
            @RequestParam Long patientId,
            @RequestParam Long doctorId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime,
            @RequestParam String title,
            @RequestParam String description,
            @RequestHeader("Authorization") String accessToken) throws Exception {

        User patient = userRepo.findById(patientId).orElseThrow(() ->
                new IllegalArgumentException("Paciente no encontrado"));
        User doctor = userRepo.findById(doctorId).orElseThrow(() ->
                new IllegalArgumentException("Doctor no encontrado"));


        Calendar calendarService = new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                (HttpRequest request) -> {
                    request.getHeaders().setAuthorization("Bearer " + accessToken);
                    return request;
                }
        )
                .setApplicationName("Thymia")
                .build();

        Event event = new Event()
                .setSummary(title)
                .setDescription(description)
                .setStart(new EventDateTime().setDateTime(new DateTime(startTime.toString())))
                .setEnd(new EventDateTime().setDateTime(new DateTime(endTime.toString())));


        Event createdEvent = calendarService.events()
                .insert("primary", event)
                .execute();


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
    public List<Appointment> getAppointmentsByPatient(@PathVariable Long patientId) {
        return appointmentRepo.findByPatientId(patientId);
    }
}