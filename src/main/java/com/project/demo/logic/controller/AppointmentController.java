package com.project.demo.logic.controller;

import com.project.demo.logic.entity.appointment.Appointment;
import com.project.demo.logic.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService service;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Appointment appointment) {
        try {
            return ResponseEntity.ok(service.schedule(appointment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<?> reschedule(
        @PathVariable Long id,
        @RequestBody Map<String, String> payload
    ) {
        try {
            LocalDateTime start = LocalDateTime.parse(payload.get("start"));
            LocalDateTime end = LocalDateTime.parse(payload.get("end"));
            Appointment updated = service.reschedule(id, start, end);
            return ResponseEntity.ok(updated);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Formato de fecha inválido.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
