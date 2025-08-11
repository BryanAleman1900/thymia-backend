package com.project.demo.logic.entity.journal;

import com.fasterxml.jackson.annotation.JsonIgnore;
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



    private LocalDateTime createdAt;

    @ManyToOne
    private User userAssigned;

    @Column(nullable = false)
    private boolean sharedWithProfessional;

    @Column(length = 24)
    private String sentimentLabel;

    @Column
    private Double sentimentScore;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

}

