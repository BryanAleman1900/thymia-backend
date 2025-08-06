package com.project.demo.logic.entity.journal;

import com.project.demo.logic.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    List<JournalEntry> findByUserOrderByCreatedAtDesc(User user);
}

