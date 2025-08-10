package com.project.demo.logic.entity.call;

import com.project.demo.logic.entity.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transcript_segment")
public class TranscriptSegment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) private CallSession session;
    @ManyToOne(optional = false) private User user;

    @Column(columnDefinition = "TEXT")
    private String text;

    private LocalDateTime timestamp;

    public Long getId() { return id; }
    public CallSession getSession() { return session; }
    public void setSession(CallSession session) { this.session = session; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
