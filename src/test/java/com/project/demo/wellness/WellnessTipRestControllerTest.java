package com.project.demo.wellness;

import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.wellness.WellnessTipReceipt;
import com.project.demo.logic.entity.wellness.WellnessTipService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)  // desactiva filtros de seguridad en tests
class WellnessTipRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private WellnessTipService service;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void view_returns_200_and_calls_service() throws Exception {
        // Principal simulado para @AuthenticationPrincipal User me
        User me = new User();
        me.setEmail("me@x.com");
        me.setPassword("pwd");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        me,
                        null,
                        Collections.emptyList() // evita NPE por authorities null
                )
        );

        when(service.viewTip(any(User.class), eq(5L)))
                .thenReturn(new WellnessTipReceipt());

        mvc.perform(post("/api/wellness/5/view")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(service).viewTip(any(User.class), eq(5L));
    }
}


