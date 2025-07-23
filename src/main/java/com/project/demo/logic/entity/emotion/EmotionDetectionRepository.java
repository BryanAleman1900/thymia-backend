package com.project.demo.logic.entity.emotion;

import com.project.demo.logic.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmotionDetectionRepository extends JpaRepository<EmotionDetection, Long> {
    List<EmotionDetection> findByUserOrderByTimestampDesc(User user);
}
