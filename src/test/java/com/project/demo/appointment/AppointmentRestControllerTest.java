package com.project.demo.appointment;

import com.project.demo.logic.entity.appointment.Appointment;
import com.project.demo.logic.entity.appointment.AppointmentRepository;
import com.project.demo.logic.entity.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // desactiva filtros de seguridad
class AppointmentRestControllerTest {

    @Autowired private MockMvc mvc;

    // Mockea los beans que el controlador usa por debajo.
    // Si tu controlador usa un Service en vez del Repository, cambia estos @MockBean al Service correspondiente.
    @MockBean private AppointmentRepository appointmentRepo;
    @MockBean private UserRepository userRepo;

    @Test
    void get_by_range_ok() throws Exception {
        when(appointmentRepo.findByStartTimeBetween(any(), any()))
                .thenReturn(List.of(new Appointment()));

        mvc.perform(get("/api/appointments")
                        .param("start", "2025-08-01T00:00:00")
                        .param("end", "2025-08-02T00:00:00"))
                .andExpect(status().isOk());
    }

    @Test
    void get_by_guests_ok() throws Exception {
        when(appointmentRepo.findAllByGuestIds(Set.of(1L, 2L)))
                .thenReturn(List.of(new Appointment()));

        mvc.perform(get("/api/appointments/by-guests")
                        .param("userIds", "1,2"))
                .andExpect(status().isOk());
    }
}




