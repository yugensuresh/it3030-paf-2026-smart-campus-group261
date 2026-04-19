package campus_nexus.entity;

import campus_nexus.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String name;

    private String password;

    private String firstName;

    private String lastName;

    private String contactNumber;

    @Column(length = 1000)
    private String address;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String profileImageData;

    private LocalDateTime registeredAt;

    @Enumerated(EnumType.STRING)
    private Role role;

    @PrePersist
    public void prePersist() {
        if (registeredAt == null) {
            registeredAt = LocalDateTime.now();
        }
        if (role == null) {
            role = Role.USER;
        }
    }

    @JsonProperty("userId")
    public Long getUserId() {
        return id;
    }

    @JsonProperty("fullName")
    public String getFullName() {
        if (name != null && !name.isBlank()) {
            return name;
        }
        String fn = firstName == null ? "" : firstName.trim();
        String ln = lastName == null ? "" : lastName.trim();
        String combined = (fn + " " + ln).trim();
        return combined.isEmpty() ? null : combined;
    }
}