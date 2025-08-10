package com.project.demo.logic.entity.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AuditRepository extends JpaRepository<Audit, Long> {

    Page<Audit> findAllByOrderByLoginTimeDesc(Pageable pageable);

    @Query("SELECT a FROM Audit a WHERE " +
            "(:action IS NULL OR a.action = :action) AND " +
            "(:userId IS NULL OR a.user.id = :userId) AND " +
            "(:startDate IS NULL OR a.loginTime >= :startDate) AND " +
            "(:endDate IS NULL OR a.loginTime <= :endDate) " +
            "ORDER BY a.loginTime DESC")
    Page<Audit> findFiltered(
            @Param("action") String action,
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}