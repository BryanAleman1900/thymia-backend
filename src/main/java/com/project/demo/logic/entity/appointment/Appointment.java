
package com.project.demo.logic.entity.appointment;

import com.project.demo.logic.entity.user.User;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String googleEventId;  // ID del evento en Google Calendar
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private User patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private User doctor;
}
