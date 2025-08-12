package com.project.demo.logic.entity.user;

import com.project.demo.logic.entity.rol.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long>  {
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE %?1%")
    List<User> findUsersWithCharacterInName(String character);

    @Query("SELECT u FROM User u WHERE u.name = ?1")
    Optional<User> findByName(String name);

    Optional<User> findByLastname(String lastname);

    Optional<User> findByEmail(String email);

    Optional<User> findByFaceIdValue(String faceIdValue);

    @Query("SELECT u FROM User u WHERE u.id IN :ids")
    Set<User> findAllById(@Param("ids") Set<Long> ids);

    Set<User> findByIdIn(Set<Long> ids);
    @Query("""
           SELECT u FROM User u
           WHERE (:q IS NULL OR :q = '' 
             OR LOWER(u.name)     LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(u.lastname) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(u.email)    LIKE LOWER(CONCAT('%', :q, '%')))
           """)
    Page<User> search(@Param("q") String q, Pageable pageable);

    List<User> findByRole_Name(RoleEnum role);
}
