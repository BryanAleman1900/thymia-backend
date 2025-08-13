package com.project.demo.appointment;

import com.project.demo.logic.entity.appointment.Appointment;
import com.project.demo.logic.entity.appointment.AppointmentRepository;
import com.project.demo.logic.entity.rol.Role;
import com.project.demo.logic.entity.rol.RoleEnum;
import com.project.demo.logic.entity.rol.RoleRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AppointmentRepositoryTest {

    @Autowired private AppointmentRepository repo;
    @Autowired private UserRepository users;
    @Autowired private RoleRepository roles;

    @Test
    void range_query_inclusive_edges() {
        // --- Roles requeridos por FK ---
        Role userRole = new Role();
        userRole.setName(RoleEnum.USER);
        userRole.setDescription("Usuario estándar");
        userRole = roles.save(userRole);

        Role therapistRole = new Role();
        therapistRole.setName(RoleEnum.THERAPIST);
        therapistRole.setDescription("Profesional terapeuta");
        therapistRole = roles.save(therapistRole);

        // --- Paciente (User) ---
        User patient = new User();
        patient.setEmail("user@x.com");
        patient.setPassword("pwd");      // NOT NULL en tu modelo
        patient.setRole(userRole);       // NOT NULL por FK
        patient = users.save(patient);

        // --- Doctor/Terapeuta ---
        User doctor = new User();
        doctor.setEmail("therapist@x.com");
        doctor.setPassword("pwd");
        doctor.setRole(therapistRole);
        doctor = users.save(doctor);

        // --- Cita ---
        LocalDateTime s1 = LocalDateTime.parse("2025-08-01T10:00:00");
        LocalDateTime e1 = LocalDateTime.parse("2025-08-01T11:00:00");

        Appointment a = new Appointment();
        a.setTitle("Consulta de prueba");   // opcional en tu modelo (puede ser NULL), lo ponemos por claridad
        a.setStartTime(s1);                 // NOT NULL
        a.setEndTime(e1);                   // NOT NULL
        a.setPatient(patient);              // <-- usa patient
        a.setDoctor(doctor);                // <-- usa doctor
        repo.save(a);

        // --- Query por rango (incluye el borde inferior) ---
        List<Appointment> hit = repo.findByStartTimeBetween(
                LocalDateTime.parse("2025-08-01T10:00:00"),
                LocalDateTime.parse("2025-08-01T12:00:00")
        );

        assertEquals(1, hit.size());
        assertEquals("Consulta de prueba", hit.get(0).getTitle());
        assertEquals("user@x.com", hit.get(0).getPatient().getEmail());
        assertEquals("therapist@x.com", hit.get(0).getDoctor().getEmail());
    }
}
