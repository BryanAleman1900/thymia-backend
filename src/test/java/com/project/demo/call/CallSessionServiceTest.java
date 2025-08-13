package com.project.demo.call;

import com.project.demo.logic.entity.call.CallSession;
import com.project.demo.logic.entity.call.CallSessionRepository;
import com.project.demo.logic.entity.call.CallSessionService;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CallSessionServiceTest {

    @Mock private CallSessionRepository repo;
    @InjectMocks private CallSessionService service;
    private AutoCloseable closeable;

    @BeforeEach
    void init(){ closeable = MockitoAnnotations.openMocks(this); }

    @AfterEach
    void close() throws Exception { closeable.close(); }

    @Test
    void getOrCreate_creates_when_absent() {
        when(repo.findByRoomId("room-1")).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(a -> a.getArgument(0));

        CallSession s = service.getOrCreate("room-1");
        assertEquals("room-1", s.getRoomId());
        assertNotNull(s.getStartedAt());   // <-- antes decía getCreatedAt()
        assertNull(s.getEndedAt());
        verify(repo).save(any(CallSession.class));
    }

    @Test
    void end_sets_ended_once() {
        CallSession existing = new CallSession();
        existing.setRoomId("room-2");
        when(repo.findByRoomId("room-2")).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenAnswer(a -> a.getArgument(0));

        CallSession ended = service.end("room-2");
        assertNotNull(ended.getEndedAt());

        // Segunda llamada no debe volver a persistir
        CallSession endedAgain = service.end("room-2");
        assertNotNull(endedAgain.getEndedAt());
        verify(repo, times(1)).save(any(CallSession.class));
    }
}


