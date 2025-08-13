package com.project.demo.rest.appointment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.demo.logic.entity.appointment.Appointment;
import com.project.demo.logic.entity.appointment.AppointmentRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentRestController {

    @Autowired private AppointmentRepository appointmentRepo;
    @Autowired private UserRepository userRepo;

    private boolean hasRole(User u, String role) {
        if (u == null || u.getAuthorities() == null) return false;
        String target = "ROLE_" + role;
        return u.getAuthorities().stream().anyMatch(a -> target.equals(a.getAuthority()));
    }

    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@RequestBody AppointmentRequest request,
                                                         @AuthenticationPrincipal User current) {

        if (request.getPatientId() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "patientId is required");
        }
        if (request.getDoctorId() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "doctorId is required");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "startTime and endTime are required");
        }
        if (!hasRole(current, "USER")) {
            throw new ResponseStatusException(FORBIDDEN, "Only patients can create appointments");
        }
        if (!Objects.equals(request.getPatientId(), current.getId())) {
            throw new ResponseStatusException(FORBIDDEN, "You can only create appointments for yourself");
        }

        User patient = userRepo.findById(request.getPatientId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Patient not found"));
        User doctor  = userRepo.findById(request.getDoctorId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Doctor not found"));

        Appointment a = new Appointment();
        a.setTitle(request.getTitle());
        a.setStartTime(request.getStartTime());
        a.setEndTime(request.getEndTime());
        a.setDescription(request.getDescription());
        a.setPatient(patient);
        a.setDoctor(doctor);

        if (request.getGuestIds() != null && !request.getGuestIds().isEmpty()) {
            Set<User> guests = new HashSet<>();
            userRepo.findAllById(request.getGuestIds()).forEach(guests::add);
            a.setGuests(guests);
        }

        Appointment saved = appointmentRepo.save(a);
        if (saved.getDoctor() != null) saved.getDoctor().getId();
        if (saved.getPatient() != null) saved.getPatient().getId();
        if (saved.getGuests() != null) saved.getGuests().size();

        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Appointment> updateAppointment(@PathVariable Long id,
                                                         @RequestBody AppointmentRequest request,
                                                         @AuthenticationPrincipal User current) {

        Appointment a = appointmentRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Appointment not found"));

        if (!hasRole(current, "USER") || !Objects.equals(a.getPatient().getId(), current.getId())) {
            throw new ResponseStatusException(FORBIDDEN, "Only the owning patient can update this appointment");
        }

        if (request.getTitle() != null) a.setTitle(request.getTitle());
        if (request.getStartTime() != null) a.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) a.setEndTime(request.getEndTime());
        if (request.getDescription() != null) a.setDescription(request.getDescription());
        if (request.getDoctorId() != null) {
            User doctor = userRepo.findById(request.getDoctorId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Doctor not found"));
            a.setDoctor(doctor);
        }

        Appointment saved = appointmentRepo.save(a);
        if (saved.getDoctor() != null) saved.getDoctor().getId();
        if (saved.getPatient() != null) saved.getPatient().getId();
        if (saved.getGuests() != null) saved.getGuests().size();

        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id,
                                                  @AuthenticationPrincipal User current) {
        Appointment a = appointmentRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Appointment not found"));

        if (!hasRole(current, "USER") || !Objects.equals(a.getPatient().getId(), current.getId())) {
            throw new ResponseStatusException(FORBIDDEN, "Only the owning patient can delete this appointment");
        }

        appointmentRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getById(@PathVariable Long id,
                                               @AuthenticationPrincipal User current) {
        Appointment a = appointmentRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Appointment not found"));

        boolean isOwner = a.getPatient() != null && Objects.equals(a.getPatient().getId(), current.getId());
        boolean isDoctor = a.getDoctor() != null && Objects.equals(a.getDoctor().getId(), current.getId());
        boolean isAdmin = hasRole(current, "ADMIN") || hasRole(current, "SUPER_ADMIN");

        if (!(isOwner || isDoctor || isAdmin)) {
            throw new ResponseStatusException(FORBIDDEN, "You are not allowed to view this appointment");
        }

        if (a.getDoctor() != null) a.getDoctor().getId();
        if (a.getPatient() != null) a.getPatient().getId();
        if (a.getGuests() != null) a.getGuests().size();

        return ResponseEntity.ok(a);
    }

    @GetMapping
    public ResponseEntity<List<Appointment>> getAppointments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) Long patientId,
            @AuthenticationPrincipal User current) {

        List<Appointment> result;

        boolean isAdmin = hasRole(current, "ADMIN") || hasRole(current, "SUPER_ADMIN");
        boolean isTherapist = hasRole(current, "THERAPIST");
        boolean isPatient = hasRole(current, "USER");

        if (isAdmin) {
            result = (patientId != null)
                    ? appointmentRepo.findWindowByPatient(patientId, start, end)
                    : appointmentRepo.findWindow(start, end);
        } else if (isTherapist) {
            result = (patientId != null)
                    ? appointmentRepo.findWindowByDoctorAndPatient(current.getId(), patientId, start, end)
                    : appointmentRepo.findWindowByDoctor(current.getId(), start, end);
        } else if (isPatient) {
            result = appointmentRepo.findWindowByPatient(current.getId(), start, end);
        } else {
            result = Collections.emptyList();
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/by-guests")
    public ResponseEntity<List<Appointment>> getAppointmentsByGuests(@AuthenticationPrincipal User current) {
        if (current == null || current.getId() == null) return ResponseEntity.ok(Collections.emptyList());
        return ResponseEntity.ok(appointmentRepo.findAllByGuestIds(Set.of(current.getId())));
    }

    @Data
    public static class AppointmentRequest {
        private String title;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime startTime;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime endTime;

        private String description;
        private Long patientId;
        private Long doctorId;
        private Set<Long> guestIds;
    }
}
