package com.project.demo.logic.dto;

public class AppointmentDTO {
    private Long patientId;
    private Long professionalId;
    private String date;
    private String time;
    private String videoCallUrl;

    // Getters y setters
    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getProfessionalId() {
        return professionalId;
    }

    public void setProfessionalId(Long professionalId) {
        this.professionalId = professionalId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getVideoCallUrl() {
        return videoCallUrl;
    }

    public void setVideoCallUrl(String videoCallUrl) {
        this.videoCallUrl = videoCallUrl;
    }
}

