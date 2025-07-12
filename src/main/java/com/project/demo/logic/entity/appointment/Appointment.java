package com.project.demo.logic.entity.appointment;

import com.project.demo.logic.entity.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id")
    private User patient;

    @ManyToOne(optional = false)
    @JoinColumn(name = "professional_id")
    private User professional;

    @Column(nullable = false)
    private LocalDateTime start;

    @Column(nullable = false)
    private LocalDateTime end;

    @Column(nullable = false)
    private String status; // pendiente, confirmada, cancelada

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getPatient() { return patient; }
    public void setPatient(User patient) { this.patient = patient; }

    public User getProfessional() { return professional; }
    public void setProfessional(User professional) { this.professional = professional; }

    public LocalDateTime getStart() { return start; }
    public void setStart(LocalDateTime start) { this.start = start; }

    public LocalDateTime getEnd() { return end; }
    public void setEnd(LocalDateTime end) { this.end = end; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
