package com.project.demo.rest.audit;

import com.project.demo.logic.entity.audit.Audit;
import com.project.demo.logic.entity.audit.AuditRepository;
import com.project.demo.logic.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/audit")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AuditRestController {

    private final AuditRepository auditRepository;

    public AuditRestController(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @PostMapping("/log-failure")
    public ResponseEntity<Void> logFailedLogin(
            @RequestParam String username,
            @RequestParam String ipAddress) {

        Audit audit = new Audit();
        audit.setAction("LOGIN_FAILED");
        audit.setLoginTime(LocalDateTime.now());
        auditRepository.save(audit);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/log-success")
    public ResponseEntity<Void> logSuccessfulLogin(
            @RequestParam Long userId,
            @RequestParam String ipAddress) {

        User user = new User();
        user.setId(userId);

        Audit audit = new Audit();
        audit.setUser(user);
        audit.setAction("LOGIN");
        audit.setLoginTime(LocalDateTime.now());
        auditRepository.save(audit);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/logins")
    public ResponseEntity<Page<Map<String, Object>>> getLoginHistory(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Audit> logs;

        if (hasFilters(action, userId, startDate, endDate)) {
            LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : null;
            LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : null;

            logs = auditRepository.findFiltered(
                    action,
                    userId,
                    start,
                    end,
                    PageRequest.of(page , size)
            );
        } else {
            logs = auditRepository.findAllByOrderByLoginTimeDesc(
                    PageRequest.of(page , size)
            );
        }

        return ResponseEntity.ok(logs.map(log -> {
            Map<String, Object> response = new HashMap<>();
            response.put("id", log.getId());
            response.put("user", log.getUser() != null ? Map.of(
                    "id", log.getUser().getId(),
                    "name", log.getUser().getName(),
                    "email", log.getUser().getEmail()
            ) : null);
            response.put("action", log.getAction());
            response.put("loginTime", log.getLoginTime().toString());
            return response;
        }));
    }

    private boolean hasFilters(String action, Long userId, String startDate, String endDate) {
        return action != null || userId != null || startDate != null || endDate != null;
    }

    private String formatLogEntry(Audit log) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a dd/MM/yyyy");
        String formattedTime = log.getLoginTime().format(formatter);

        if (log.getUser() != null) {
            return String.format("%s ha iniciado sesión el %s",
                    log.getUser().getName(),
                    formattedTime);
        } else {
            return String.format("Intento de acceso (%s) el %s",
                    log.getAction(),
                    formattedTime);
        }
    }
}