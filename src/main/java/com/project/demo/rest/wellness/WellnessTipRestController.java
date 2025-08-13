package com.project.demo.rest.wellness;

import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.wellness.WellnessTipReceipt;
import com.project.demo.logic.entity.wellness.WellnessTipService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/wellness")
@RequiredArgsConstructor
public class WellnessTipRestController {

    private final WellnessTipService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> listMine(@AuthenticationPrincipal User me, Pageable pageable) {
        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado.");
        }
        Page<WellnessTipReceipt> page = service.listForUser(me, pageable);
        return ResponseEntity.ok(page);
    }

    @PostMapping("/{id}/view")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> view(@AuthenticationPrincipal User me, @PathVariable Long id) {
        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado.");
        }
        WellnessTipReceipt updated = service.viewTip(me, id);
        return ResponseEntity.ok(updated);
    }
}



