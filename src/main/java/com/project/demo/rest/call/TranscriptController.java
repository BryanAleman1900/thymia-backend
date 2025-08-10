package com.project.demo.rest.call;

import com.project.demo.logic.entity.call.*;
import com.project.demo.logic.entity.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/calls/{roomId}/transcripts")
@PreAuthorize("isAuthenticated()")
public class TranscriptController {

    private final CallSessionService callService;
    private final TranscriptService transcriptService;

    public TranscriptController(CallSessionService callService, TranscriptService transcriptService) {
        this.callService = callService;
        this.transcriptService = transcriptService;
    }

    @PostMapping
    public ResponseEntity<?> add(@PathVariable String roomId, @RequestBody Map<String, Object> body, Authentication auth) {
        User user = (User) auth.getPrincipal();
        String text = String.valueOf(body.get("text"));
        long ts = Long.parseLong(String.valueOf(body.get("ts")));
        CallSession s = callService.getOrCreate(roomId);
        var seg = transcriptService.save(s, user, text, Instant.ofEpochMilli(ts));
        return ResponseEntity.ok(Map.of("id", seg.getId()));
    }

    @GetMapping
    public List<TranscriptSegment> list(@PathVariable String roomId) {
        CallSession s = callService.findByRoomId(roomId);
        return transcriptService.findBySession(s);
    }
}
