package com.project.demo.logic.entity.emotion;

import com.project.demo.logic.entity.call.CallSession;
import com.project.demo.logic.entity.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class EmotionDetection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String emotion;

    private LocalDateTime timestamp;

    @ManyToOne
    private User user;

    public EmotionDetection() {}

    public EmotionDetection(String emotion, LocalDateTime timestamp, User user) {
        this.emotion = emotion;
        this.timestamp = timestamp;
        this.user = user;
    }

    @ManyToOne
    private CallSession session; // <--- NUEVO

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmotion() { return emotion; }
    public void setEmotion(String emotion) { this.emotion = emotion; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public CallSession getSession() { return session; }
    public void setSession(CallSession session) { this.session = session; }
}
