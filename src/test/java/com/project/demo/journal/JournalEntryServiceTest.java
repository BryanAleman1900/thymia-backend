package com.project.demo.journal;

import com.project.demo.logic.entity.journal.JournalEntry;
import com.project.demo.logic.entity.journal.JournalEntryRepository;
import com.project.demo.logic.entity.journal.JournalEntryService;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.logic.entity.rol.Role;
import com.project.demo.logic.entity.rol.RoleEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JournalEntryServiceTest {

    @Mock private JournalEntryRepository journalRepo;
    @Mock private UserRepository userRepo;
    @InjectMocks private JournalEntryService service;

    private AutoCloseable closeable;
    private User owner;

    @BeforeEach
    void init() {
        closeable = MockitoAnnotations.openMocks(this);
        owner = new User(); owner.setEmail("owner@x.com");
    }

    @Test
    void share_throws_if_empty_list() {
        assertThrows(IllegalArgumentException.class,
                () -> service.shareWithTherapists(owner, 1L, Collections.emptySet()));
    }

    @Test
    void share_throws_if_non_therapist_email() {
        JournalEntry entry = new JournalEntry();
        when(journalRepo.findByIdAndUser(1L, owner)).thenReturn(Optional.of(entry));

        User notTherapist = new User();
        Role r = new Role(); r.setName(RoleEnum.USER);
        notTherapist.setRole(r);
        when(userRepo.findByEmail("nottherapist@x.com")).thenReturn(Optional.of(notTherapist));

        assertThrows(IllegalArgumentException.class, () ->
                service.shareWithTherapists(owner, 1L, Set.of("nottherapist@x.com"))
        );
    }
}

