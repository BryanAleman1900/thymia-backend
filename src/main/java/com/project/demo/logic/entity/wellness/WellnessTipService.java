package com.project.demo.logic.entity.wellness;

import com.project.demo.logic.entity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class WellnessTipService {

    private final WellnessTipReceiptRepository repo;

    @Value("${wellness.tips.cooldown-hours:12}")
    private int cooldownHours;


    public Page<WellnessTipReceipt> listForUser(User user, Pageable pageable) {
        if (user == null) throw new IllegalArgumentException("Usuario requerido");
        return repo.findByUserOrderByCreatedAtDesc(user, pageable);
    }


    @Transactional
    public WellnessTipReceipt viewTip(User me, Long id) {
        if (me == null) throw new IllegalArgumentException("Usuario requerido");
        WellnessTipReceipt tip = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tip no encontrado"));

        // Control de propiedad
        if (tip.getUser() == null || tip.getUser().getId() == null || !tip.getUser().getId().equals(me.getId())) {
            throw new SecurityException("No puedes visualizar un tip de otro usuario");
        }

        Instant now = Instant.now();

        // Inicializa contador si es nulo
        Integer currentViews = tip.getViewCount();
        if (currentViews == null) currentViews = 0;
        tip.setViewCount(currentViews + 1);

        // Setea firstViewedAt una sola vez y actualiza lastViewedAt siempre
        if (tip.getFirstViewedAt() == null) {
            tip.setFirstViewedAt(now);
        }
        tip.setLastViewedAt(now);

        return repo.save(tip);
    }


    public boolean deliverIfNotThrottled(User user, String title, String content, String category, String source) {
        if (user == null) throw new IllegalArgumentException("Usuario requerido");
        if (isBlank(title) || isBlank(content)) return false;

        Instant cutoff = Instant.now().minus(getCooldown());
        long recent = repo.countByUserAndCategoryAndCreatedAtAfter(user, safe(category), cutoff);
        if (recent > 0) {
            return false; // omitido por cooldown
        }
        deliverToUser(user, title, content, category, source);
        return true;
    }


    public WellnessTipReceipt deliverToUser(User user, String title, String content, String category, String source) {
        if (user == null) throw new IllegalArgumentException("Usuario requerido");
        WellnessTipReceipt tip = WellnessTipReceipt.builder()
                .user(user)
                .title(title)
                .content(content)
                .category(safe(category))
                .source(safe(source))
                .viewCount(0)
                .build();
        return repo.save(tip);
    }



    private Duration getCooldown() {
        int hours = (cooldownHours <= 0) ? 12 : cooldownHours;
        return Duration.ofHours(hours);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}



