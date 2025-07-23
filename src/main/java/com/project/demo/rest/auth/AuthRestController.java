package com.project.demo.rest.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.project.demo.logic.entity.auth.AuthenticationService;
import com.project.demo.logic.entity.auth.GoogleTokenRequest;
import com.project.demo.logic.entity.auth.GoogleTokenVerifier;
import com.project.demo.logic.entity.auth.JwtService;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.rol.Role;
import com.project.demo.logic.entity.rol.RoleEnum;
import com.project.demo.logic.entity.rol.RoleRepository;
import com.project.demo.logic.entity.user.FaceIOLoginRequest;
import com.project.demo.logic.entity.user.LoginResponse;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RequestMapping("/auth")
@RestController
public class AuthRestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;

    private final GlobalResponseHandler responseHandler;

    public AuthRestController(JwtService jwtService, AuthenticationService authenticationService, GlobalResponseHandler responseHandler) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.responseHandler = responseHandler;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody User user) {
        User authenticatedUser = authenticationService.authenticate(user);

        String jwtToken = jwtService.generateToken(authenticatedUser);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(jwtToken);
        loginResponse.setExpiresIn(jwtService.getExpirationTime());

        Optional<User> foundedUser = userRepository.findByEmail(user.getEmail());
        foundedUser.ifPresent(loginResponse::setAuthUser);

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already in use");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.USER);

        if (optionalRole.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Role not found");
        }

        user.setRole(optionalRole.get());
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody GoogleTokenRequest googleTokenRequest) {
        String idToken = googleTokenRequest.getIdToken();

        Optional<GoogleIdToken.Payload> payloadOpt = GoogleTokenVerifier.verify(idToken);
        if (payloadOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Google ID token");
        }

        GoogleIdToken.Payload payload = payloadOpt.get();
        String email = payload.getEmail();
        String name = (String) payload.get("name");

        Optional<User> userOpt = userRepository.findByEmail(email);
        User user;

        if (userOpt.isEmpty()) {
            user = new User();
            user.setEmail(email);
            user.setName(name != null ? name : "Google User");
            user.setLastname("");

            Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.USER);
            if (optionalRole.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("USER role not found");
            }

            user.setRole(optionalRole.get());
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

            user = userRepository.save(user);
        } else {
            user = userOpt.get();
        }

        String jwtToken = jwtService.generateToken(user);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(jwtToken);
        loginResponse.setExpiresIn(jwtService.getExpirationTime());
        loginResponse.setAuthUser(user);

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/face-id/login")
    public ResponseEntity<?> loginWithFaceIO(@RequestBody FaceIOLoginRequest request) {
        Optional<User> userOpt = userRepository.findByFaceIdValue(request.getFacialId());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String token = jwtService.generateToken(user);

            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setExpiresIn(jwtService.getExpirationTime());
            response.setAuthUser(user);

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no encontrado con ese facialId");
    }

    @GetMapping("/status")
    public ResponseEntity<?> verificarBloqueo(@RequestParam String email, HttpServletRequest request) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return responseHandler.handleResponse("Usuario no encontrado", HttpStatus.NOT_FOUND, request);
        }

        if (user.getFechaBloqueo() != null && user.getFechaBloqueo().isAfter(LocalDateTime.now())) {
            return responseHandler.handleResponse("Usuario bloqueado hasta: " + user.getFechaBloqueo(), HttpStatus.LOCKED, request);
        }

        return responseHandler.handleResponse("Usuario no está bloqueado", HttpStatus.OK, request);
    }
}
