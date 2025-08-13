package com.project.demo.rest.journal;

import com.project.demo.logic.entity.journal.JournalEntry;
import com.project.demo.logic.entity.journal.JournalEntryService;
import com.project.demo.logic.entity.rol.RoleEnum;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/journal")
@RequiredArgsConstructor
@Slf4j
public class JournalEntryController {

    private final JournalEntryService service;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createEntry(@RequestBody JournalRequest request,
                                         @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado.");
        if (request.getContent() == null || request.getContent().isBlank())
            return ResponseEntity.badRequest().body("El contenido no puede estar vacío.");

        JournalEntry saved = service.create(user, request.getContent(), Boolean.TRUE.equals(request.getShared()));
        return ResponseEntity.status(HttpStatus.CREATED).body(JournalEntryDto.from(saved));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyEntries(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado.");
        List<JournalEntry> entries = service.getAllForUser(user);
        List<JournalEntryDto> dto = entries.stream().map(JournalEntryDto::from).collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/therapists")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TherapistDTO>> listTherapists() {
        try {
            List<TherapistDTO> data = userRepository.findByRole_Name(RoleEnum.THERAPIST).stream()
                    .map(u -> new TherapistDTO(
                            (nullToEmpty(u.getName()) + " " + nullToEmpty(u.getLastname())).trim(),
                            nullToEmpty(u.getEmail())
                    ))
                    .toList();
            log.info("Therapists list requested → {} items", data.size());
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.warn("Error building therapists list: {}", e.getMessage(), e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }
    public record TherapistDTO(String name, String email) {}

    @Data
    public static class ShareRequest { private Set<String> therapistEmails; }

    @PostMapping("/{id}/share")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> shareWithTherapists(@PathVariable Long id,
                                                 @RequestBody ShareRequest req,
                                                 @AuthenticationPrincipal(expression = "username") String currentEmail) {
        var owner = userRepository.findByEmail(currentEmail).orElseThrow();
        service.shareWithTherapists(owner, id, req.getTherapistEmails());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/share")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> revokeShare(@PathVariable Long id,
                                         @RequestParam String therapistEmail,
                                         @AuthenticationPrincipal(expression = "username") String currentEmail) {
        var owner = userRepository.findByEmail(currentEmail).orElseThrow();
        service.revokeShare(owner, id, therapistEmail);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/shared-with-me")
    @PreAuthorize("hasRole('THERAPIST')")
    public List<JournalEntryService.SharedJournalEntryDTO> sharedWithMe(
            @AuthenticationPrincipal(expression = "username") String currentEmail) {
        return service.getSharedWithMe(currentEmail);
    }

    @Data
    public static class JournalRequest {
        private String content;
        private Boolean shared;
        public Boolean getShared() { return shared; }
        public void setShared(Boolean shared) { this.shared = shared; }
    }

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




