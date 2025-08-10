package com.project.demo.logic.entity.call;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CallSessionRepository extends JpaRepository<CallSession, Long> {
    Optional<CallSession> findByRoomId(String roomId);
}
