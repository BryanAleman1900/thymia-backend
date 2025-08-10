package com.project.demo.logic.entity.auth;

import com.project.demo.logic.entity.audit.Audit;
import com.project.demo.logic.entity.audit.AuditRepository;
import com.project.demo.logic.entity.user.User;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.*;
import org.springframework.stereotype.Service;
//Mauro
@Service
public class LoginAuditService {

    private final AuditRepository auditRepository;

    public LoginAuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        if (event.getAuthentication().getPrincipal() instanceof User user) {
            saveLog(user, "LOGIN");
        }
    }

    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        String email = event.getAuthentication().getName();
        saveLog(null, "LOGIN_FAILED - Email: " + email);
    }

    private void saveLog(User user, String action) {
        Audit log = new Audit();
        log.setUser(user);
        log.setAction(action);
        auditRepository.save(log);
    }
}