package com.project.demo.logic.entity.wellness;

import com.project.demo.logic.entity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class WellnessTipService {

    private final WellnessTipReceiptRepository repo;

    public Page<WellnessTipReceipt> listForUser(User user, Pageable pageable) {
        return repo.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    public WellnessTipReceipt viewTip(User user, Long id) {
        WellnessTipReceipt tip = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tip no encontrado"));
        if (!tip.getUser().getId().equals(user.getId())) {
            throw new SecurityException("No autorizado");
        }
        tip.setViewCount(tip.getViewCount() + 1);
        if (tip.getFirstViewedAt() == null) tip.setFirstViewedAt(Instant.now());
        tip.setLastViewedAt(Instant.now());
        return repo.save(tip);
    }


    public WellnessTipReceipt deliverToUser(User user, String title, String content, String category, String source) {
        WellnessTipReceipt tip = WellnessTipReceipt.builder()
                .user(user)
                .title(title)
                .content(content)
                .category(category)
                .source(source)
                .viewCount(0)
                .build();
        return repo.save(tip);
    }
}


