package campus_nexus.controller;

import campus_nexus.entity.User;
import campus_nexus.enums.Role;
import campus_nexus.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(toUserPayload(user)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "User not found")));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOwnProfile(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return userRepository.findById(id)
                .map(user -> {
                    applyProfileUpdate(user, body);
                    User saved = userRepository.save(user);
                    return ResponseEntity.ok(toUserPayload(saved));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "User not found")));
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    static void applyProfileUpdate(User user, Map<String, Object> body) {
        String firstName = trimToNull(body.get("firstName"));
        String lastName = trimToNull(body.get("lastName"));
        String contactNumber = trimToNull(body.get("contactNumber"));
        String address = trimToNull(body.get("address"));
        String profileImageData = trimToNull(body.get("profileImageData"));

        if (firstName != null) {
            user.setFirstName(firstName);
        }
        if (lastName != null) {
            user.setLastName(lastName);
        }
        if (contactNumber != null) {
            user.setContactNumber(contactNumber);
        }
        if (address != null) {
            user.setAddress(address);
        }
        if (body.containsKey("profileImageData")) {
            user.setProfileImageData(profileImageData);
        }

        String combined = ((user.getFirstName() == null ? "" : user.getFirstName()) + " " +
                (user.getLastName() == null ? "" : user.getLastName())).trim();
        if (!combined.isBlank()) {
            user.setName(combined);
        }
        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }
    }

    static Map<String, Object> toUserPayload(User user) {
        Map<String, Object> out = new HashMap<>();
        out.put("userId", user.getId());
        out.put("firstName", user.getFirstName());
        out.put("lastName", user.getLastName());
        out.put("fullName", user.getFullName());
        out.put("email", user.getEmail());
        out.put("contactNumber", user.getContactNumber());
        out.put("address", user.getAddress());
        out.put("profileImageData", user.getProfileImageData());
        out.put("registeredAt", user.getRegisteredAt());
        out.put("role", user.getRole() == null ? Role.USER.name() : user.getRole().name());
        return out;
    }

    private static String trimToNull(Object value) {
        if (value == null) {
            return null;
        }
        String s = String.valueOf(value).trim();
        return s.isEmpty() ? "" : s;
    }

}