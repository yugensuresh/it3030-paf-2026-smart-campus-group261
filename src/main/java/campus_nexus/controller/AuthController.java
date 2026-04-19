package campus_nexus.controller;

import campus_nexus.dto.request.LoginRequestDTO;
import campus_nexus.dto.request.SignupRequestDTO;
import campus_nexus.dto.response.AuthResponseDTO;
import campus_nexus.entity.User;
import campus_nexus.enums.Role;
import campus_nexus.repository.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AuthController handles authentication endpoints.
 * Uses OAuth2 (Google Login) for authentication.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignupRequestDTO request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "An account with this email already exists"));
        }

        User user = new User();
        user.setEmail(email);
        user.setName(request.getFullName().trim());
        user.setPassword(request.getPassword());
        user.setRole(Role.USER);
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "email", user.getEmail(),
                        "fullName", user.getName()
                ));
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@Valid @RequestBody LoginRequestDTO request) {
        String email = request.getEmail().trim().toLowerCase();
        return userRepository.findByEmail(email)
                .map(user -> {
                    if (user.getPassword() == null || !user.getPassword().equals(request.getPassword())) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("message", "Invalid email or password"));
                    }

                    String fullName = user.getName();
                    if (fullName == null || fullName.isBlank()) {
                        String first = user.getFirstName() == null ? "" : user.getFirstName().trim();
                        String last = user.getLastName() == null ? "" : user.getLastName().trim();
                        fullName = (first + " " + last).trim();
                    }

                    AuthResponseDTO response = AuthResponseDTO.builder()
                            .userId(user.getId())
                            .email(user.getEmail())
                            .name(fullName)
                            .fullName(fullName)
                            .role(user.getRole() == null ? Role.USER.name() : user.getRole().name())
                            .build();
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid email or password")));
    }

    /**
     * Get current authenticated user info from OAuth2 session
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<AuthResponseDTO> getCurrentUser(@AuthenticationPrincipal OAuth2User oAuth2User) {

        if (oAuth2User == null) {
            logger.warn("No authenticated user found");
            return ResponseEntity.status(401).build();
        }

        String email = oAuth2User.getAttribute("email");
        logger.info("Getting current user info for: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AuthResponseDTO response = AuthResponseDTO.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .fullName(user.getName())
                .role(user.getRole().name())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Check if user is authenticated
     * GET /api/auth/status
     */
    @GetMapping("/status")
    public ResponseEntity<AuthResponseDTO> checkAuthStatus(@AuthenticationPrincipal OAuth2User oAuth2User) {

        if (oAuth2User == null) {
            return ResponseEntity.status(401).build();
        }

        String email = oAuth2User.getAttribute("email");
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        AuthResponseDTO response = AuthResponseDTO.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .fullName(user.getName())
                .role(user.getRole().name())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Logout endpoint
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        logger.info("Logout request received");
        return ResponseEntity.ok("Logged out successfully");
    }
}