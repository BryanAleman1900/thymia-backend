package com.project.demo.logic.entity.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface AuditRepository extends JpaRepository<Audit, Long> {

    @Query("SELECT a FROM Audit a WHERE " +
            "(:username IS NULL OR a.user.email LIKE :username) AND " +
            "(:startDate IS NULL OR a.loginTime >= :startDate) AND " +
            "(:endDate IS NULL OR a.loginTime <= :endDate)")
    Page<Audit> findFiltered(
            @Param("username") String username,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}