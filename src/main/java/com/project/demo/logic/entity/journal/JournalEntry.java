package com.project.demo.logic.entity.journal;

import com.project.demo.logic.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 5000)
    private String content;

    @ManyToOne(optional = false)
    private User user;

    private LocalDateTime createdAt;

    @ManyToOne
    private User userAssigned;

    @Column(nullable = false)
    private boolean sharedWithProfessional;
}

