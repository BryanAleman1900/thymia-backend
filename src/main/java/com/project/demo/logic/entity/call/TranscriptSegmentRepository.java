package com.project.demo.logic.entity.call;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TranscriptSegmentRepository extends JpaRepository<TranscriptSegment, Long> {
    List<TranscriptSegment> findBySessionOrderByTimestampAsc(CallSession session);
}
