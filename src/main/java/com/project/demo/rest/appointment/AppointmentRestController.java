package com.project.demo.rest.appointment;

import com.project.demo.logic.entity.appointment.Appointment;
import com.project.demo.logic.entity.appointment.AppointmentRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentRestController {

    private final AppointmentRepository appointmentRepo;
    private final UserRepository userRepo;

    @Autowired
    public AppointmentRestController(AppointmentRepository appointmentRepo, UserRepository userRepo) {
        this.appointmentRepo = appointmentRepo;
        this.userRepo = userRepo;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Appointment> getCalendarAppointments(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        return appointmentRepo.findByDateRange(start, end);
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Appointment> createAppointmentWithGuests(
            @RequestBody CreateAppointmentRequest request) {

        User doctor = userRepo.findById(request.doctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor no encontrado"));

        User patient = userRepo.findById(request.patientId())
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));

        Set<User> guests = new HashSet<>(userRepo.findAllById(request.guestIds()));

        Appointment appointment = new Appointment();
        appointment.setTitle(request.title());
        appointment.setStartTime(request.startTime());
        appointment.setEndTime(request.endTime());
        appointment.setDescription(request.description());
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setGuests(guests);

        return ResponseEntity.ok(appointmentRepo.save(appointment));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Appointment> updateAppointment(
            @PathVariable Long id,
            @RequestBody UpdateAppointmentRequest request) {

        Appointment appointment = appointmentRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        if (request.title() != null) appointment.setTitle(request.title());
        if (request.startTime() != null) appointment.setStartTime(request.startTime());
        if (request.endTime() != null) appointment.setEndTime(request.endTime());
        if (request.description() != null) appointment.setDescription(request.description());
        if (request.guestIds() != null) {appointment.setGuests(new HashSet<>(userRepo.findAllById(request.guestIds())));
        }

        return ResponseEntity.ok(appointmentRepo.save(appointment));
    }

    public record CreateAppointmentRequest(
            String title,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String description,
            Long doctorId,
            Long patientId,
            Set<Long> guestIds
    ) {}

    public record UpdateAppointmentRequest(
            String title,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String description,
            Set<Long> guestIds
    ) {}
}