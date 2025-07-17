// AppointmentRequest.java (en `logic/entity/appointment/`)
package com.project.demo.logic.entity.appointment;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentRequest {
    private Long patientId;
    private String doctorEmail;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;
}