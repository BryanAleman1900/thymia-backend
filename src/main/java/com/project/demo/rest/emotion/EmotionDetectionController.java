package com.project.demo.rest.emotion;

import com.project.demo.logic.entity.emotion.EmotionDetection;
import com.project.demo.logic.entity.emotion.EmotionDetectionService;
import com.project.demo.logic.entity.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/emotions")
public class EmotionDetectionController {

    private final EmotionDetectionService emotionService;

    public EmotionDetectionController(EmotionDetectionService emotionService) {
        this.emotionService = emotionService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> saveEmotion(@RequestBody Map<String, String> body, Principal principal) {
        String emotion = body.get("emotion");
        if (emotion == null || emotion.isEmpty()) {
            return ResponseEntity.badRequest().body("Emotion value is required.");
        }
        User user = (User) ((org.springframework.security.core.Authentication) principal).getPrincipal();

        EmotionDetection saved = emotionService.saveEmotion(emotion, user);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EmotionDetection>> getMyEmotions(Principal principal) {
        User user = (User) ((org.springframework.security.core.Authentication) principal).getPrincipal();
        List<EmotionDetection> list = emotionService.getUserEmotions(user);
        return ResponseEntity.ok(list);
    }
}
