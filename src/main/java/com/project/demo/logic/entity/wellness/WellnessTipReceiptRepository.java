package com.project.demo.logic.entity.wellness;

import com.project.demo.logic.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface WellnessTipReceiptRepository extends JpaRepository<WellnessTipReceipt, Long> {
    Page<WellnessTipReceipt> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // contar por categoría en ventana de tiempo
    long countByUserAndCategoryAndCreatedAtAfter(User user, String category, Instant after);
}



