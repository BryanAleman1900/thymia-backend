// AppointmentController.java (en `rest/appointment/`)
package com.project.demo.rest.appointment;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.project.demo.logic.entity.appointment.Appointment;
import com.project.demo.logic.entity.appointment.AppointmentRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    @Autowired
    private AppointmentRepository appointmentRepo;
    @Autowired
    private UserRepository userRepo;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Appointment> createAppointment(
            @RequestBody AppointmentRequest request,
            @RequestHeader("Authorization") String accessToken
    ) throws Exception {
        // obtener un paciente y un medico
        User patient = userRepo.findById(request.getPatientId()).orElseThrow();
        User doctor = userRepo.findByEmail(request.getDoctorEmail()).orElseThrow();

        // crear evento
        Calendar service = new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                req -> req.setInterceptor(chain -> {
                    chain.getRequest().getHeaders().setAuthorization("Bearer " + accessToken);
                    return chain.proceed(chain.getRequest());
                })
        ).setApplicationName("Thymia-Calendar").build();

        Event event = new Event()
                .setSummary("Sesión: " + request.getDescription())
                .setStart(new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(request.getStartTime().toString())))
                .setEnd(new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(request.getEndTime().toString())));

        Event createdEvent = service.events().insert("primary", event).execute();

        // 3. Guardar en base de datos
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setStartTime(request.getStartTime());
        appointment.setEndTime(request.getEndTime());
        appointment.setDescription(request.getDescription());
        appointment.setGoogleEventId(createdEvent.getId());

        return ResponseEntity.ok(appointmentRepo.save(appointment));
    }
}