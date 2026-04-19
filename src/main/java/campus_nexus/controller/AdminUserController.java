package campus_nexus.controller;

import campus_nexus.entity.User;
import campus_nexus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "*")
public class AdminUserController {

    @Autowired
    private UserRepository userRepository;

    @Value("${app.resource-admin.username:admin}")
    private String resourceAdminUsername;

    @Value("${app.resource-admin.password:123456}")
    private String resourceAdminPassword;

    @GetMapping
    public ResponseEntity<?> getAdminUsers(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (!hasValidAdminHeader(auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Admin authorization required"));
        }

        List<Map<String, Object>> rows = userRepository.findAll()
                .stream()
                .map(UserController::toUserPayload)
                .toList();
        return ResponseEntity.ok(rows);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAdminUser(@PathVariable Long id,
                                             @RequestHeader(value = "Authorization", required = false) String auth,
                                             @RequestBody Map<String, Object> body) {
        if (!hasValidAdminHeader(auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Admin authorization required"));
        }
        if (id == 0L) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Portal admin cannot be modified"));
        }

        return userRepository.findById(id)
                .map(user -> {
                    UserController.applyProfileUpdate(user, body);
                    User saved = userRepository.save(user);
                    return ResponseEntity.ok(UserController.toUserPayload(saved));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "User not found")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAdminUser(@PathVariable Long id,
                                             @RequestHeader(value = "Authorization", required = false) String auth) {
        if (!hasValidAdminHeader(auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Admin authorization required"));
        }
        if (id == 0L) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Portal admin cannot be removed"));
        }
        if (!userRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private boolean hasValidAdminHeader(String authHeader) {
        String expected = "Basic " + Base64.getEncoder()
                .encodeToString((resourceAdminUsername + ":" + resourceAdminPassword).getBytes());
        return expected.equals(authHeader);
    }
}
