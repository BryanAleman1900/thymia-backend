package com.project.demo.logic.controller;

import com.project.demo.logic.entity.appointment.Appointment;
import com.project.demo.logic.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService service;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Appointment appointment, Principal principal) {
        try {
            String email = principal.getName();

            // Verifica que el usuario autenticado sea el paciente que crea la cita
            if (!appointment.getPatient().getEmail().equalsIgnoreCase(email)) {
                return ResponseEntity.status(403).body("No autorizado para agendar esta cita.");
            }

            return ResponseEntity.ok(service.schedule(appointment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<?> reschedule(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            Principal principal
    ) {
        try {
            String email = principal.getName();

            LocalDateTime start = LocalDateTime.parse(payload.get("start"));
            LocalDateTime end = LocalDateTime.parse(payload.get("end"));

            Appointment updated = service.reschedule(id, start, end);

            // Verifica que el usuario autenticado esté involucrado (paciente o profesional)
            boolean isInvolved = updated.getPatient().getEmail().equalsIgnoreCase(email)
                    || updated.getProfessional().getEmail().equalsIgnoreCase(email);

            if (!isInvolved) {
                return ResponseEntity.status(403).body("No autorizado para modificar esta cita.");
            }

            return ResponseEntity.ok(updated);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Formato de fecha inválido.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

