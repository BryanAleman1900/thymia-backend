package com.project.demo.rest.appointment;

import com.project.demo.logic.entity.appointment.Appointment;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.appointment.AppointmentRepository;
import com.project.demo.logic.entity.user.UserRepository;
import lombok.RequiredArgsConstructor;//Luis
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import com.project.demo.logic.entity.appointment.AppointmentService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor//Luis
public class AppointmentRestController {

    @Autowired
    private AppointmentRepository appointmentRepo;

    @Autowired
    private UserRepository userRepo;

    //Luis
    private final AppointmentService appointmentService;
//hasta aqui

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
        if (doctor == null) {
            // ⚠️ Solo para pruebas sin autenticación - eliminar cuando JWT esté implementado
            doctor = userRepo.findById(1L)
                    .orElseThrow(() -> new IllegalStateException("Doctor de prueba no encontrado"));
        }
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

    //Luis
    //Crear link del meeting
    @PostMapping(consumes = "application/json")
    public ResponseEntity<Appointment> create(@RequestBody Appointment appt) {
        var saved = appointmentService.createAppointment(appt);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }


    @GetMapping("/visible")
    public Page<Appointment> listMyAppointments(
            @AuthenticationPrincipal User me,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            Pageable pageable) {
        return appointmentRepo.findAllVisibleTo(me.getId(), start, end, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getOne(@PathVariable Long id,
                                              @AuthenticationPrincipal User me) {
        if (!appointmentRepo.existsByIdAndParticipant(id, me.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return appointmentRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //Luis
    @GetMapping("/{id}/meeting")
    public ResponseEntity<Map<String, Object>> resolveMeeting(
            @PathVariable Long id,
            @RequestParam("t") String token,
            @AuthenticationPrincipal User me) {

        var apptOpt = appointmentRepo.findById(id);
        if (apptOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var appt = apptOpt.get();


        if (!appointmentRepo.existsByIdAndParticipant(id, me.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }


        if (appt.getMeetingToken() == null || !appt.getMeetingToken().equals(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }


        if (appt.getMeetingExpiresAt() != null && LocalDateTime.now().isAfter(appt.getMeetingExpiresAt())) {
            return ResponseEntity.status(HttpStatus.GONE).build();
        }

        return ResponseEntity.ok(Map.of(
                "meetingUrl", appt.getMeetingUrl(),
                "expiresAt", appt.getMeetingExpiresAt()
        ));
    }
}