package com.project.demo.logic.entity.journal;

import com.project.demo.logic.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    @EntityGraph(attributePaths = "sharedWithTherapists")
    List<JournalEntry> findByUserOrderByCreatedAtDesc(User user);
    Optional<JournalEntry> findByIdAndUser(Long id, User user);
    List<JournalEntry> findByUserAndSharedWithProfessionalTrueOrderByCreatedAtDesc(User user);


    @Query("""
    select j from JournalEntry j
    join j.sharedWithTherapists t
    where t = :email
    order by j.createdAt desc
""")
    List<JournalEntry> findSharedWithTherapist(@Param("email") String therapistEmail);

}

