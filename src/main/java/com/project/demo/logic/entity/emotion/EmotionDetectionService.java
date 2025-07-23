package com.project.demo.logic.entity.emotion;

import com.project.demo.logic.entity.user.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmotionDetectionService {

    private final EmotionDetectionRepository repository;

    public EmotionDetectionService(EmotionDetectionRepository repository) {
        this.repository = repository;
    }

    public EmotionDetection saveEmotion(String emotion, User user) {
        EmotionDetection detection = new EmotionDetection();
        detection.setEmotion(emotion);
        detection.setTimestamp(LocalDateTime.now());
        detection.setUser(user);

        return repository.save(detection);
    }

    public List<EmotionDetection> getUserEmotions(User user) {
        return repository.findByUserOrderByTimestampDesc(user);
    }
}
