package com.project.demo.logic.meeting;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MeetingLinkService {

    private final String baseUrl;
    private final int ttlMinutes;
    private final int tokenBytes;

    public MeetingLinkService(
            @Value("${thymia.meeting.base-url}") String baseUrl,
            @Value("${thymia.meeting.ttl-minutes:180}") int ttlMinutes,
            @Value("${thymia.meeting.token-bytes:32}") int tokenBytes) {
        this.baseUrl = baseUrl;
        this.ttlMinutes = ttlMinutes;
        this.tokenBytes = tokenBytes;
    }

    public record MeetingData(String url, String token, LocalDateTime expiresAt) {}

    public MeetingData createSecureLink(Long appointmentId) {

        byte[] bytes = new byte[tokenBytes];
        new java.security.SecureRandom().nextBytes(bytes);
        String token = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        LocalDateTime expires = LocalDateTime.now().plusMinutes(ttlMinutes);

        String url = String.format("%s/%d?t=%s", baseUrl, appointmentId, token);
        return new MeetingData(url, token, expires);
    }
}

