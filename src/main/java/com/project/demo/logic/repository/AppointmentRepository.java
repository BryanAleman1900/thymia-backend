package com.project.demo.logic.repository;

import com.project.demo.logic.entity.appointment.Appointment;
import com.project.demo.logic.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByProfessionalAndStartBetween(User professional, LocalDateTime from, LocalDateTime to);

    boolean existsByProfessionalAndStartLessThanEqualAndEndGreaterThanEqual(
        User professional, LocalDateTime end, LocalDateTime start);
}
