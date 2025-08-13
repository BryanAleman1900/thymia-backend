package com.project.demo.logic.entity.journal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.demo.logic.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;
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

    @ElementCollection(fetch = FetchType.LAZY) // ← antes EAGER
    @CollectionTable(name = "journal_shared_with", joinColumns = @JoinColumn(name = "journal_id"))
    @Column(name = "therapist_email", nullable = false, length = 190)
    @BatchSize(size = 50) // opcional, reduce rondas cuando sí se necesita
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<String> sharedWithTherapists = new HashSet<>();

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

