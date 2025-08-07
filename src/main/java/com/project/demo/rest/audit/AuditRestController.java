package com.project.demo.rest.audit;

import com.project.demo.logic.entity.audit.Audit;
import com.project.demo.logic.entity.audit.AuditRepository;
import com.project.demo.logic.entity.http.HttpResponse;
import com.project.demo.logic.entity.http.Meta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/audit")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AuditRestController {

    private final AuditRepository auditRepository;

    public AuditRestController(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @GetMapping
    public ResponseEntity<HttpResponse<Page<Audit>>> getAuditLogs(
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Audit.ActionType enumActionType = actionType != null ?
                Audit.ActionType.valueOf(actionType) : null;
        LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : null;
        LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : null;

        Page<Audit> logs = auditRepository.findFiltered(
                enumActionType, userId, start, end, pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(logs.getTotalPages());
        meta.setTotalElements(logs.getTotalElements());
        meta.setPageNumber(logs.getNumber() + 1);
        meta.setPageSize(logs.getSize());

        return ResponseEntity.ok(
                new HttpResponse<>("Audit logs retrieved", logs, meta));
    }
}