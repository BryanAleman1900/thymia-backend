package com.project.demo.logic.entity.appointment;

import com.project.demo.logic.meeting.MeetingLinkService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository repo;
    private final MeetingLinkService meetingLinkService;

    @Transactional
    public Appointment createAppointment(Appointment appt) {
        appt.setIsPrivate(true); // visibilidad por defecto
        var saved = repo.save(appt);

        var meeting = meetingLinkService.createSecureLink(saved.getId());
        saved.setMeetingUrl(meeting.url());
        saved.setMeetingToken(meeting.token());
        saved.setMeetingExpiresAt(meeting.expiresAt());

        return repo.save(saved);
    }

    public boolean userBelongsToAppointment(Long appointmentId, Long userId) {
        return repo.existsByIdAndParticipant(appointmentId, userId);
    }
}