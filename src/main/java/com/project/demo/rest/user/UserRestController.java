package com.project.demo.rest.user;

import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.rol.Role;
import com.project.demo.logic.entity.rol.RoleEnum;
import com.project.demo.logic.entity.rol.RoleRepository;
import com.project.demo.logic.entity.user.ChangeRoleRequest;
import com.project.demo.logic.entity.user.FaceIOLoginRequest;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserRestController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    // -------- GET /users con búsqueda + paginación + orden (default: createdAt DESC)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request) {

        // Page es 1-based para el front
        int pageIndex = Math.max(page - 1, 0);
        Sort sort = "asc".equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending(); // por defecto DESC
        Pageable pageable = PageRequest.of(pageIndex, size, sort);

        Page<User> usersPage = (q != null && !q.isBlank())
                ? userRepository.search(q.trim(), pageable)
                : userRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(usersPage.getTotalPages());
        meta.setTotalElements(usersPage.getTotalElements());
        meta.setPageNumber(usersPage.getNumber() + 1); // devolver 1-based
        meta.setPageSize(usersPage.getSize());

        return new GlobalResponseHandler().handleResponse(
                "Users retrieved successfully",
                usersPage.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> addUser(@RequestBody User user, HttpServletRequest request) {
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            return new GlobalResponseHandler().handleResponse("Password is required",
                    HttpStatus.BAD_REQUEST, request);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null || user.getRole().getName() == null) {
            Role defaultRole = roleRepository.findByName(RoleEnum.USER)
                    .orElseThrow(() -> new IllegalStateException("USER role not found"));
            user.setRole(defaultRole);
        }
        userRepository.save(user);
        return new GlobalResponseHandler().handleResponse("User created successfully",
                user, HttpStatus.OK, request);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody User user, HttpServletRequest request) {
        Optional<User> optional = userRepository.findById(userId);
        if (optional.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("User id " + userId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
        User existing = optional.get();
        if (user.getName() != null) existing.setName(user.getName());
        if (user.getLastname() != null) existing.setLastname(user.getLastname());
        if (user.getEmail() != null) existing.setEmail(user.getEmail());
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userRepository.save(existing);
        return new GlobalResponseHandler().handleResponse("User updated successfully",
                existing, HttpStatus.OK, request);
    }

    @PutMapping("/{userId}/role")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> changeRole(@PathVariable Long userId,
                                        @RequestBody ChangeRoleRequest body,
                                        HttpServletRequest request) {
        if (body == null || body.getRole() == null || body.getRole().isBlank()) {
            return new GlobalResponseHandler().handleResponse("Role is required",
                    HttpStatus.BAD_REQUEST, request);
        }
        String roleStr = body.getRole().trim().toUpperCase();
        if (!roleStr.equals("USER") && !roleStr.equals("THERAPIST")) {
            return new GlobalResponseHandler().handleResponse(
                    "Invalid role. Only USER or THERAPIST are allowed here",
                    HttpStatus.BAD_REQUEST, request);
        }
        Optional<User> optional = userRepository.findById(userId);
        if (optional.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("User id " + userId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
        Role newRole = roleRepository.findByName(RoleEnum.valueOf(roleStr))
                .orElseThrow(() -> new IllegalStateException("Role " + roleStr + " not found"));
        User u = optional.get();
        u.setRole(newRole);
        userRepository.save(u);
        return new GlobalResponseHandler().handleResponse("User role updated successfully",
                u, HttpStatus.OK, request);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId, HttpServletRequest request) {
        Optional<User> found = userRepository.findById(userId);
        if (found.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("User id " + userId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
        try {
            userRepository.deleteById(userId);
            return new GlobalResponseHandler().handleResponse("User deleted successfully",
                    found.get(), HttpStatus.OK, request);
        } catch (DataIntegrityViolationException ex) {
            return new GlobalResponseHandler().handleResponse(
                    "No se puede eliminar: el usuario tiene registros relacionados (citas, llamadas, etc.)",
                    HttpStatus.CONFLICT, request);
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public User authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    @PostMapping("/me/face-id/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> registerFaceID(@RequestBody FaceIOLoginRequest faceReq, HttpServletRequest httpRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        currentUser.setFaceIdValue(faceReq.getFacialId());
        userRepository.save(currentUser);
        return new GlobalResponseHandler().handleResponse("Face ID registrado",
                currentUser, HttpStatus.OK, httpRequest);
    }
}