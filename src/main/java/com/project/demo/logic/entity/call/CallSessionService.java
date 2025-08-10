package com.project.demo.logic.entity.call;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class CallSessionService {
    private final CallSessionRepository repo;

    public CallSessionService(CallSessionRepository repo) {
        this.repo = repo;
    }

    public CallSession getOrCreate(String roomId) {
        return repo.findByRoomId(roomId).orElseGet(() -> {
            CallSession s = new CallSession();
            s.setRoomId(roomId);
            s.setStartedAt(LocalDateTime.now());
            return repo.save(s);
        });
    }

    public CallSession end(String roomId) {
        CallSession s = repo.findByRoomId(roomId).orElseThrow();
        if (s.getEndedAt() == null) {
            s.setEndedAt(LocalDateTime.now());
            s = repo.save(s);
        }
        return s;
    }

    public CallSession findByRoomId(String roomId) {
        return repo.findByRoomId(roomId).orElseThrow();
    }
}
