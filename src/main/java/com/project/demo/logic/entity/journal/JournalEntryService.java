package com.project.demo.logic.entity.journal;

import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.wellness.WellnessTipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.project.demo.logic.entity.wellness.WellnessAdviceGenerator;


import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JournalEntryService {

    private final JournalEntryRepository journalRepository;
    private final SentimentAnalysisService sentimentService;
    private final WellnessTipService wellnessTipService;
    private final WellnessAdviceGenerator wellnessAdviceGenerator;

    /**
     * Crea una entrada de diario, analiza sentimiento y la persiste.
     */
    public JournalEntry create(User user, String content, boolean shared) {
        // 1) crear entidad base
        JournalEntry entry = JournalEntry.builder()
                .user(user)
                .content(content)
                .sharedWithProfessional(shared)
                .createdAt(LocalDateTime.now())
                .build();

        // 2) analizar sentimiento con HuggingFace
        try {
            SentimentAnalysisService.SentimentResult result = sentimentService.analyze(content);
            entry.setSentimentLabel(result.label());
            entry.setSentimentScore(result.score());
        } catch (Exception e) {
            log.warn("Fallo al analizar sentimiento; guardo sin label/score. Causa: {}", e.getMessage());
        }

        // 3) guardar entrada con (o sin) sentimiento
        JournalEntry saved = journalRepository.save(entry);

        // 4) generar y entregar tip (si aplica) con cooldown
        try {
            WellnessAdviceGenerator.Advice advice =
                    wellnessAdviceGenerator.generate(user, saved.getContent(), saved.getSentimentLabel(), saved.getSentimentScore());

            if (advice != null) {
                boolean delivered = wellnessTipService.deliverIfNotThrottled(
                        user,
                        advice.title(),
                        advice.content(),
                        advice.category(),
                        "HuggingFace/JournalAuto"
                );
                log.info("Wellness tip {} para usuario {}{}", advice.category(), user.getId(),
                        delivered ? " (emitido)" : " (omitido por cooldown)");
            }
        } catch (Exception e) {
            log.warn("No se pudo generar tip wellness: {}", e.getMessage());
        }

        return saved;
    }

    /**
     * Lista entradas del usuario, más recientes primero.
     */
    public List<JournalEntry> getAllForUser(User user) {
        return journalRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Activa/desactiva compartir con profesional para una entrada del mismo usuario.
     */
    public void updateSharing(Long id, boolean shared, User user) {
        var entry = journalRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Entrada no encontrada o no pertenece al usuario"));
        entry.setSharedWithProfessional(shared);
        journalRepository.save(entry);
    }

    /**
     * Devuelve todas las entradas del usuario que están marcadas como compartidas.
     * (Útil para que el paciente vea qué está compartiendo.)
     */
    public List<JournalEntry> getSharedForUser(User user) {
        return journalRepository.findByUserAndSharedWithProfessionalTrueOrderByCreatedAtDesc(user);
    }

    // Si necesitas una lista “visible para el profesional”, dependerá de tu modelo de relación.
    // Podrías tener algo como:
    // public List<JournalEntry> getSharedVisibleToProfessional(User professional) {
    //     return journalRepository.findByProfessionalAndSharedWithProfessionalTrueOrderByCreatedAtDesc(professional);
    // }
}

