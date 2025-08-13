package com.project.demo.testutil;

import com.project.demo.logic.entity.user.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

public class Utils {
    public static Authentication auth(User u) {
        return new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities());
    }
}