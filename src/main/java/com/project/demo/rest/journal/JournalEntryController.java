package com.project.demo.rest.journal;

import com.project.demo.logic.entity.journal.JournalEntry;
import com.project.demo.logic.entity.journal.JournalEntryService;
import com.project.demo.logic.entity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/journal")
@RequiredArgsConstructor
public class JournalEntryController {

    private final JournalEntryService service;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createEntry(@RequestBody JournalRequest request,
                                         @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado.");
        }

        if (request.getContent() == null || request.getContent().isBlank()) {
            return ResponseEntity.badRequest().body("El contenido no puede estar vacío.");
        }

        JournalEntry saved = service.create(request.getContent(), user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyEntries(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado.");
        }

        List<JournalEntry> entries = service.getAllForUser(user);
        return ResponseEntity.ok(entries);
    }

    // DTO para entrada de datos
    public static class JournalRequest {
        private String content;
        public String getContent() {
            return content;
        }
        public void setContent(String content) {
            this.content = content;
        }
    }
}


