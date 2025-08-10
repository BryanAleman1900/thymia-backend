package com.project.demo.rest.call;

import com.project.demo.logic.entity.call.CallSession;
import com.project.demo.logic.entity.call.CallSessionService;
import com.project.demo.logic.entity.call.TranscriptService;
import com.project.demo.logic.entity.emotion.EmotionDetectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/calls/{roomId}/summary")
@PreAuthorize("isAuthenticated()")
public class CallSummaryController {

    private final CallSessionService callService;
    private final TranscriptService transcriptService;
    private final EmotionDetectionService emotionService;

    public CallSummaryController(CallSessionService callService, TranscriptService transcriptService, EmotionDetectionService emotionService) {
        this.callService = callService;
        this.transcriptService = transcriptService;
        this.emotionService = emotionService;
    }

    @GetMapping
    public ResponseEntity<?> summary(@PathVariable String roomId) {
        CallSession s = callService.findByRoomId(roomId);
        var segments = transcriptService.findBySession(s);
        var emotions = emotionService.getBySession(s);

        var transcript = segments.stream().map(seg -> seg.getText()).collect(Collectors.joining(" "));
        var counts = emotions.stream().collect(Collectors.groupingBy(e -> e.getEmotion(), Collectors.counting()));

        return ResponseEntity.ok(Map.of(
                "roomId", roomId,
                "transcript", transcript,
                "emotionCounts", counts,
                "segments", segments,
                "emotions", emotions
        ));
    }
}
