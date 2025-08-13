package com.project.demo.logic.entity.journal;

import com.project.demo.logic.entity.rol.RoleEnum;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.logic.entity.wellness.WellnessAdviceGenerator;
import com.project.demo.logic.entity.wellness.WellnessTipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class JournalEntryService {

    private final JournalEntryRepository journalRepository;
    private final SentimentAnalysisService sentimentService;
    private final WellnessTipService wellnessTipService;
    private final WellnessAdviceGenerator wellnessAdviceGenerator;
    private final UserRepository userRepository;


    public JournalEntry create(User user, String content, boolean shared) {
        JournalEntry entry = JournalEntry.builder()
                .user(user)
                .content(content)
                .sharedWithProfessional(shared) // indicador visual/legado
                .createdAt(LocalDateTime.now())
                .build();

        // Análisis de sentimiento (tolerante a fallos)
        try {
            var result = sentimentService.analyze(content);
            entry.setSentimentLabel(result.label());
            entry.setSentimentScore(result.score());
        } catch (Exception e) {
            log.warn("Fallo al analizar sentimiento; guardo sin label/score. Causa: {}", e.getMessage());
        }

        JournalEntry saved = journalRepository.save(entry);

        // Tip de bienestar con cooldown
        try {
            var advice = wellnessAdviceGenerator.generate(
                    user, saved.getContent(), saved.getSentimentLabel(), saved.getSentimentScore());
            if (advice != null) {
                boolean delivered = wellnessTipService.deliverIfNotThrottled(
                        user, advice.title(), advice.content(), advice.category(), "HuggingFace/JournalAuto");
                log.info("Wellness tip {} para usuario {}{}", advice.category(), user.getId(),
                        delivered ? " (emitido)" : " (omitido por cooldown)");
            }
        } catch (Exception e) {
            log.warn("No se pudo generar tip wellness: {}", e.getMessage());
        }

        return saved;
    }


    public List<JournalEntry> getAllForUser(User user) {
        return journalRepository.findByUserOrderByCreatedAtDesc(user);
    }


    @Deprecated
    public void updateSharing(Long id, boolean shared, User user) {
        var entry = journalRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Entrada no encontrada o no pertenece al usuario"));
        entry.setSharedWithProfessional(shared);
        journalRepository.save(entry);
    }


    @Deprecated
    public List<JournalEntry> getSharedForUser(User user) {
        return journalRepository.findByUserAndSharedWithProfessionalTrueOrderByCreatedAtDesc(user);
    }


    public void shareWithTherapists(User owner, Long journalId, Set<String> therapistEmails) {
        if (therapistEmails == null || therapistEmails.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un terapeuta.");
        }

        var entry = journalRepository.findByIdAndUser(journalId, owner)
                .orElseThrow(() -> new IllegalArgumentException("Entrada no encontrada o no pertenece al usuario"));

        // Validar que todos existan y tengan rol THERAPIST
        for (String email : therapistEmails) {
            var u = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Terapeuta no existe: " + email));
            if (u.getRole() == null || u.getRole().getName() != RoleEnum.THERAPIST) {
                throw new IllegalArgumentException("El usuario no es terapeuta: " + email);
            }
        }

        boolean changed = entry.getSharedWithTherapists().addAll(therapistEmails);
        if (changed && !entry.getSharedWithTherapists().isEmpty()) {
            entry.setSharedWithProfessional(true); // enciende badge
        }
        journalRepository.save(entry);
    }


    public void revokeShare(User owner, Long journalId, String therapistEmail) {
        var entry = journalRepository.findByIdAndUser(journalId, owner)
                .orElseThrow(() -> new IllegalArgumentException("Entrada no encontrada o no pertenece al usuario"));

        entry.getSharedWithTherapists().remove(therapistEmail);
        if (entry.getSharedWithTherapists().isEmpty()) {
            entry.setSharedWithProfessional(false); // apaga badge si ya no quedan terapeutas
        }
        journalRepository.save(entry);
    }


    public record SharedJournalEntryDTO(
            Long id,
            String content,
            String sentimentLabel,
            Double sentimentScore,
            String patientName,
            String patientEmail,
            LocalDateTime createdAt
    ) {}


    public List<SharedJournalEntryDTO> getSharedWithMe(String therapistEmail) {
        return journalRepository.findSharedWithTherapist(therapistEmail).stream()
                .map(e -> new SharedJournalEntryDTO(
                        e.getId(),
                        e.getContent(),
                        e.getSentimentLabel(),
                        e.getSentimentScore(),
                        (e.getUser().getName() + " " + e.getUser().getLastname()).trim(),
                        e.getUser().getEmail(),
                        e.getCreatedAt()
                ))
                .toList();
    }
}


