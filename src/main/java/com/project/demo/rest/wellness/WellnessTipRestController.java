package com.project.demo.rest.wellness;

import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.wellness.WellnessTipReceipt;
import com.project.demo.logic.entity.wellness.WellnessTipService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wellness-tips")
@RequiredArgsConstructor
public class WellnessTipRestController {

    private final WellnessTipService service;

    // Lista cronológica para el paciente autenticado
    @GetMapping
    public Page<WellnessTipReceipt> listMine(@AuthenticationPrincipal User me, Pageable pageable) {
        return service.listForUser(me, pageable);
    }

    // Re-visualización (incrementa contador y actualiza lastViewedAt)
    @PostMapping("/{id}/view")
    public WellnessTipReceipt view(@AuthenticationPrincipal User me, @PathVariable Long id) {
        return service.viewTip(me, id);
    }
}


