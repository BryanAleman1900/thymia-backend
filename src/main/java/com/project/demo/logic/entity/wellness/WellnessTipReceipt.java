package com.project.demo.logic.entity.wellness;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.demo.logic.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "wellness_tip_receipts", indexes = {
        @Index(name = "idx_wtr_user_createdat", columnList = "user_id, createdAt")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WellnessTipReceipt {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JsonIgnore
    private User user;

    @Column(nullable = false, length = 500)
    private String title;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(length = 64)
    private String category;

    @Column(length = 64)
    private String source;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant firstViewedAt;
    private Instant lastViewedAt;

    @Column(nullable = false)
    private int viewCount;
}



