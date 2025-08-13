package com.project.demo.rest.audit;

import com.project.demo.logic.entity.audit.Audit;
import com.project.demo.logic.entity.audit.AuditRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @GetMapping("/logins")
    public ResponseEntity<Page<Map<String, Object>>> getLoginHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Audit> logs = auditRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "loginTime"))
        );

        return ResponseEntity.ok(logs.map(this::convertToDto));
    }

    private Map<String, Object> convertToDto(Audit log) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", log.getId());

        if (log.getUser() != null) {
            dto.put("user", Map.of(
                    "id", log.getUser().getId(),
                    "name", log.getUser().getName(),
                    "email", log.getUser().getEmail()
            ));
        } else {
            dto.put("user", null);
        }

        // Normalización de acciones
        String action = log.getAction();
        if ("LOGIN".equalsIgnoreCase(action) || "LOGIN_EXITOSO".equalsIgnoreCase(action)) {
            dto.put("action", "LOGIN_EXITOSO");
        } else if ("LOGIN_FAILED".equalsIgnoreCase(action) || "LOGIN_FALLIDO".equalsIgnoreCase(action)) {
            dto.put("action", "LOGIN_FALLIDO");
        } else {
            dto.put("action", action); 
        }

        dto.put("loginTime", log.getLoginTime().toString());
        return dto;
    }
}