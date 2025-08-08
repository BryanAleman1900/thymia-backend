package com.project.demo.rest.session;

import com.project.demo.logic.entity.session.SessionStore;
import com.project.demo.logic.entity.session.CreateSessionResponse;
import com.project.demo.logic.entity.session.JoinSessionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.Locale;

@RestController
@RequestMapping("/sessions")
public class SessionController {

    private final SessionStore store;
    private final SecureRandom random = new SecureRandom();

    public SessionController(SessionStore store) {
        this.store = store;
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public CreateSessionResponse create() {
        String roomId = "room" + (100_000 + random.nextInt(900_000));
        String password = genPassword(8);
        store.put(roomId, password);
        return new CreateSessionResponse(roomId, password);
    }

    @GetMapping("/join/{roomId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<JoinSessionResponse> join(@PathVariable String roomId) {
        String pwd = store.getPassword(roomId);
        if (pwd == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new JoinSessionResponse(roomId, pwd));
    }

    private String genPassword(int len) {
        final String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString().toLowerCase(Locale.ROOT);
    }
}
