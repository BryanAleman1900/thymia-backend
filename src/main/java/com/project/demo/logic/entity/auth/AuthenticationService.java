package com.project.demo.logic.entity.auth;

import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User authenticate(User input) {
        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new RuntimeException("El usuario no existe."));

        // ✅ Verificar si el usuario está bloqueado
        if (user.getFechaBloqueo() != null && user.getFechaBloqueo().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.LOCKED, "El usuario está bloqueado hasta: " + user.getFechaBloqueo());
        }

        try {
            // ✅ Autenticación normal
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(input.getEmail(), input.getPassword())
            );

            // ✅ Resetear intentos fallidos si inicia sesión exitosamente
            user.setIntentosFallidos(0);
            user.setFechaBloqueo(null);
            userRepository.save(user);
            return user;

        } catch (Exception e) {
            // ❌ Falló la autenticación, aumentamos el contador
            int intentos = user.getIntentosFallidos() + 1;
            user.setIntentosFallidos(intentos);

            if (intentos >= 4) {
                // ⛔ Bloquear por 5 minutos
                user.setFechaBloqueo(LocalDateTime.now().plusMinutes(5));
                userRepository.save(user);
                throw new ResponseStatusException(HttpStatus.LOCKED, "El usuario está bloqueado hasta: " + user.getFechaBloqueo());
            }

            userRepository.save(user);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Las credenciales ingresadas son inválidas. Intentos fallidos: " + intentos);
        }
    }
}