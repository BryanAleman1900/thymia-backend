package com.project.demo.logic.entity.appointment;

import com.project.demo.logic.entity.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name = "appointments")
@EqualsAndHashCode(of = {"id"})
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(length = 500)
    private String description;

    @Column(name = "google_event_id")
    private String googleEventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;

    @ManyToMany
    @JoinTable(
            name = "appointment_guests",
            joinColumns = @JoinColumn(name = "appointment_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> guests = new HashSet<>();


//    public void addGuest(User guest) {
//        this.guests.add(guest);
//        guest.getAppointmentsAsGuest().add(this);
//    }
//
//
//    public void removeGuest(User guest) {
//        this.guests.remove(guest);
//        guest.getAppointmentsAsGuest().remove(this);
//    }
}