package com.project.demo.rest.appointment;

import com.project.demo.logic.entity.appointment.Appointment;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.appointment.AppointmentRepository;
import com.project.demo.logic.entity.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentRestController {

    private final AppointmentRepository appointmentRepo;
    private final UserRepository userRepo;

    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@RequestBody AppointmentRequest request) {
        if (request.getPatientId() == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "patientId is required");
        }
        if (request.getDoctorId() == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "doctorId is required");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "startTime and endTime are required");
        }
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "endTime must be after startTime");
        }

        User patient = userRepo.findById(request.getPatientId()).orElseThrow(() ->
                new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Patient not found"));
        User doctor = userRepo.findById(request.getDoctorId()).orElseThrow(() ->
                new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Doctor not found"));

        Appointment appointment = new Appointment();
        appointment.setTitle(request.getTitle());
        appointment.setStartTime(request.getStartTime());
        appointment.setEndTime(request.getEndTime());
        appointment.setDescription(request.getDescription());
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);

        if (request.getGuestIds() != null && !request.getGuestIds().isEmpty()) {
            java.util.Set<User> guests = new java.util.HashSet<>();
            userRepo.findAllById(request.getGuestIds()).forEach(guests::add);
            appointment.setGuests(guests);
        }

        Appointment saved = appointmentRepo.save(appointment);

        if (saved.getDoctor() != null) saved.getDoctor().getId();
        if (saved.getPatient() != null) saved.getPatient().getId();
        if (saved.getGuests() != null) saved.getGuests().size();

        return ResponseEntity.ok(saved);
    }




    @PutMapping("/{id}")
    public ResponseEntity<Appointment> updateAppointment(
            @PathVariable Long id,
            @RequestBody AppointmentRequest request) {

        Appointment appointment = appointmentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (request.getTitle() != null) appointment.setTitle(request.getTitle());
        if (request.getStartTime() != null) appointment.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) appointment.setEndTime(request.getEndTime());
        if (request.getDescription() != null) appointment.setDescription(request.getDescription());

        if (request.getDoctorId() != null) {
            appointment.setDoctor(userRepo.findById(request.getDoctorId()).orElseThrow());
        }

        return ResponseEntity.ok(appointmentRepo.save(appointment));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getById(@PathVariable Long id) {
        Appointment a = appointmentRepo.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Appointment not found"));

        if (a.getDoctor() != null) a.getDoctor().getId();
        if (a.getPatient() != null) a.getPatient().getId();
        if (a.getGuests() != null) a.getGuests().size();

        return ResponseEntity.ok(a);
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        if (!appointmentRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        appointmentRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Appointment>> getAppointments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(appointmentRepo.findByStartTimeBetween(start, end));
    }

    @GetMapping("/by-guests")
    public ResponseEntity<List<Appointment>> getAppointmentsByGuests(
            @RequestParam Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(appointmentRepo.findAllByGuestIds(userIds));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<Appointment> joinAppointment(@PathVariable Long id) {
        Appointment appointment = appointmentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        return ResponseEntity.ok(appointment);
    }

    public static class AppointmentRequest {
        private String title;

        @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private java.time.LocalDateTime startTime;

        @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private java.time.LocalDateTime endTime;

        private String description;
        private Long patientId;
        private Long doctorId;
        private java.util.Set<Long> guestIds;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public java.time.LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(java.time.LocalDateTime startTime) { this.startTime = startTime; }

        public java.time.LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(java.time.LocalDateTime endTime) { this.endTime = endTime; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Long getPatientId() { return patientId; }
        public void setPatientId(Long patientId) { this.patientId = patientId; }

        public Long getDoctorId() { return doctorId; }
        public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

        public java.util.Set<Long> getGuestIds() { return guestIds; }
        public void setGuestIds(java.util.Set<Long> guestIds) { this.guestIds = guestIds; }
    }

}
