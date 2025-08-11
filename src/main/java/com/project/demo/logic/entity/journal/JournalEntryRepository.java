package com.project.demo.logic.entity.journal;

import com.project.demo.logic.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    List<JournalEntry> findByUserOrderByCreatedAtDesc(User user);
    Optional<JournalEntry> findByIdAndUser(Long id, User user);
    List<JournalEntry> findByUserAndSharedWithProfessionalTrueOrderByCreatedAtDesc(User user);

    // Si vas a listar lo visible para el profesional, define según tu modelo:
    // List<JournalEntry> findByProfessionalAndSharedWithProfessionalTrueOrderByCreatedAtDesc(User professional);
}

