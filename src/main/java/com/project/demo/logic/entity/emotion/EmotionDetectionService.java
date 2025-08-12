package com.project.demo.logic.entity.emotion;

import com.project.demo.logic.entity.call.CallSession;
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
        EmotionDetection d = new EmotionDetection();
        d.setEmotion(emotion);
        d.setTimestamp(LocalDateTime.now());
        d.setUser(user);
        return repository.save(d);
    }

    public EmotionDetection saveEmotion(String emotion, User user, CallSession session) {
        EmotionDetection d = new EmotionDetection();
        d.setEmotion(emotion);
        d.setTimestamp(LocalDateTime.now());
        d.setUser(user);
        d.setSession(session);
        return repository.save(d);
    }

    public List<EmotionDetection> getUserEmotions(User user) {
        return repository.findByUserOrderByTimestampDesc(user);
    }

    public List<EmotionDetection> getBySession(CallSession session) {
        return repository.findBySessionOrderByTimestampAsc(session);
    }
}
