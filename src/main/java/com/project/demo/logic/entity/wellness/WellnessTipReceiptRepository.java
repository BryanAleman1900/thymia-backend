package com.project.demo.logic.entity.wellness;

import com.project.demo.logic.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WellnessTipReceiptRepository extends JpaRepository<WellnessTipReceipt, Long> {
    Page<WellnessTipReceipt> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}


