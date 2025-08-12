package com.project.demo.logic.entity.call;

import com.project.demo.logic.entity.user.User;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class TranscriptService {
    private final TranscriptSegmentRepository repo;

    public TranscriptService(TranscriptSegmentRepository repo) {
        this.repo = repo;
    }

    public TranscriptSegment save(CallSession session, User user, String text, Instant instant) {
        TranscriptSegment seg = new TranscriptSegment();
        seg.setSession(session);
        seg.setUser(user);
        seg.setText(text);
        seg.setTimestamp(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
        return repo.save(seg);
    }

    public List<TranscriptSegment> findBySession(CallSession s) {
        return repo.findBySessionOrderByTimestampAsc(s);
    }
}
