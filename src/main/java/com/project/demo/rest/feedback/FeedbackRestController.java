package com.project.demo.rest.feedback;

import com.project.demo.logic.entity.feedback.Feedback;
import com.project.demo.logic.entity.feedback.FeedbackRepository;
import com.project.demo.logic.entity.appointment.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.project.demo.logic.entity.user.User;

import java.util.List;

@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackRestController {

    @Autowired
    private FeedbackRepository feedbackRepo;

    @Autowired
    private AppointmentRepository appointmentRepo;

    @PostMapping
    public ResponseEntity<Feedback> createFeedback(
            @RequestParam Long appointmentId,
            @RequestParam String comments,
            @RequestParam Integer rating,
            @AuthenticationPrincipal User patient) {

        Feedback feedback = new Feedback();
        feedback.setAppointment(appointmentRepo.findById(appointmentId).orElseThrow());
        feedback.setPatient(patient);
        feedback.setComments(comments);
        feedback.setRating(rating);

        return ResponseEntity.ok(feedbackRepo.save(feedback));
    }

    @GetMapping("/by-appointment/{appointmentId}")
    public ResponseEntity<List<Feedback>> getAppointmentFeedbacks(
            @PathVariable Long appointmentId) {
        return ResponseEntity.ok(feedbackRepo.findByAppointmentId(appointmentId));
    }
}