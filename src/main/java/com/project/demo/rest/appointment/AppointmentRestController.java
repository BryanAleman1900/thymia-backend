package com.project.demo.rest.appointment;

import com.project.demo.logic.entity.appointment.Appointment;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.appointment.AppointmentRepository;
import com.project.demo.logic.entity.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentRestController {

    @Autowired
    private AppointmentRepository appointmentRepo;

    @Autowired
    private UserRepository userRepo;

    // Crear cita básica
    @PostMapping
    public ResponseEntity<Appointment> createAppointment(
            @RequestParam String title,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String description,
            @RequestParam Long patientId,
            @RequestParam Set<Long> guestIds,
            @AuthenticationPrincipal User doctor) {

        Appointment appointment = new Appointment();
        appointment.setTitle(title);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setDescription(description);
        appointment.setDoctor(doctor);
        appointment.setPatient(userRepo.findById(patientId).orElseThrow());

        Set<User> guests = userRepo.findAllById(guestIds);
        appointment.setGuests(guests);

        return ResponseEntity.ok(appointmentRepo.save(appointment));
    }

    // Actualizar cita
    @PutMapping("/{id}")
    public ResponseEntity<Appointment> updateAppointment(
            @PathVariable Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String description) {

        Appointment appointment = appointmentRepo.findById(id).orElseThrow();

        if (title != null) appointment.setTitle(title);
        if (startTime != null) appointment.setStartTime(startTime);
        if (endTime != null) appointment.setEndTime(endTime);
        if (description != null) appointment.setDescription(description);

        return ResponseEntity.ok(appointmentRepo.save(appointment));
    }

    // Eliminar cita
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        appointmentRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Obtener citas entre fechas
    @GetMapping
    public ResponseEntity<List<Appointment>> getAppointments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        return ResponseEntity.ok(appointmentRepo.findByStartTimeBetween(start, end));
    }
    //Obteer citas por invitados
    @GetMapping("/by-guests")
    public ResponseEntity<List<Appointment>> getAppointmentsByGuests(
            @RequestParam Set<Long> userIds) {
        return ResponseEntity.ok(appointmentRepo.findAllByGuestIds(userIds));
    }
}