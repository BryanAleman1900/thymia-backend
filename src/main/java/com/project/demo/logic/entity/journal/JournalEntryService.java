package com.project.demo.logic.entity.journal;

import com.project.demo.logic.entity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JournalEntryService {

    private final JournalEntryRepository repo;

    public JournalEntry create(String content, User user) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Contenido del diario no puede estar vacío.");
        }

        var entry = JournalEntry.builder()
                .content(content.trim())
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        return repo.save(entry);
    }

    public List<JournalEntry> getAllForUser(User user) {
        return repo.findByUserOrderByCreatedAtDesc(user);
    }
}
