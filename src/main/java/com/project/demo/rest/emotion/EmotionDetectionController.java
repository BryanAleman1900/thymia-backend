package com.project.demo.rest.emotion;

import com.project.demo.logic.entity.call.CallSession;
import com.project.demo.logic.entity.call.CallSessionService;
import com.project.demo.logic.entity.emotion.EmotionDetection;
import com.project.demo.logic.entity.emotion.EmotionDetectionService;
import com.project.demo.logic.entity.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/emotions")
public class EmotionDetectionController {

    private final EmotionDetectionService emotionService;
    private final CallSessionService callSessionService;

    public EmotionDetectionController(EmotionDetectionService emotionService, CallSessionService callSessionService) {
        this.emotionService = emotionService;
        this.callSessionService = callSessionService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> saveEmotion(@RequestBody Map<String, String> body, Authentication auth) {
        String emotion = body.get("emotion");
        String roomId = body.get("roomId");
        if (emotion == null || emotion.isEmpty()) return ResponseEntity.badRequest().body("Emotion value is required.");

        User user = (User) auth.getPrincipal();
        EmotionDetection saved;

        if (roomId != null && !roomId.isBlank()) {
            CallSession s = callSessionService.getOrCreate(roomId);
            saved = emotionService.saveEmotion(emotion, user, s);
        } else {
            saved = emotionService.saveEmotion(emotion, user);
        }
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EmotionDetection>> getMyEmotions(Authentication auth) {
        User user = (User) auth.getPrincipal();
        List<EmotionDetection> list = emotionService.getUserEmotions(user);
        return ResponseEntity.ok(list);
    }
}
