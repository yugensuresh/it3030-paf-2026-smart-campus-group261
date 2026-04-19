package campus_nexus.controller;

import campus_nexus.dto.response.AuthResponseDTO;
import campus_nexus.entity.User;
import campus_nexus.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

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
                .email(user.getEmail())
                .name(user.getName())
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
                .email(user.getEmail())
                .name(user.getName())
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