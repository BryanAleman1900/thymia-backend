package com.project.demo.rest.journal;

import com.project.demo.logic.entity.journal.JournalEntry;
import com.project.demo.logic.entity.journal.JournalEntryService;
import com.project.demo.logic.entity.user.User;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/journal")
@RequiredArgsConstructor
public class JournalEntryController {

    private final JournalEntryService service;

    // Crear entrada (analiza sentimiento en el service y receta tip si aplica)
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

        JournalEntry saved = service.create(
                user,
                request.getContent(),
                Boolean.TRUE.equals(request.getShared())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(JournalEntryDto.from(saved));
    }

    // Listar mis entradas (más recientes primero)
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyEntries(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado.");
        }
        List<JournalEntry> entries = service.getAllForUser(user);
        List<JournalEntryDto> dto = entries.stream()
                .map(JournalEntryDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    // Actualizar visibilidad (compartir / dejar de compartir) una entrada
    @PatchMapping("/{id}/share")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateShare(@PathVariable Long id,
                                            @RequestBody ShareRequest body,
                                            @AuthenticationPrincipal User user) {
        service.updateSharing(id, Boolean.TRUE.equals(body.getShared()), user);
        return ResponseEntity.noContent().build();
    }

    // Obtener las entradas mías que están marcadas como compartidas
    @GetMapping("/shared")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyShared(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado.");
        }
        List<JournalEntry> shared = service.getSharedForUser(user);
        List<JournalEntryDto> dto = shared.stream()
                .map(JournalEntryDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    // ===== DTOs =====

    @Data
    public static class JournalRequest {
        private String content;
        private Boolean shared; // opcional
    }

    @Data
    public static class ShareRequest {
        private Boolean shared;
    }

    // DTO plano para evitar ciclos/LAZY y 500s en Jackson
    @lombok.Value
    public static class JournalEntryDto {
        Long id;
        String content;
        String sentimentLabel;
        Double sentimentScore;
        Boolean sharedWithProfessional;
        java.time.LocalDateTime createdAt;

        public static JournalEntryDto from(JournalEntry e) {
            return new JournalEntryDto(
                    e.getId(),
                    e.getContent(),
                    e.getSentimentLabel(),
                    e.getSentimentScore(),
                    e.isSharedWithProfessional(),
                    e.getCreatedAt()
            );
        }
    }
}



