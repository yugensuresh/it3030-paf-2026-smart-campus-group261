package campus_nexus.config;

import campus_nexus.entity.User;
import campus_nexus.enums.Role;
import campus_nexus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;

    @Value("${app.portal-admin.email:admin@gmail.com}")
    private String portalAdminEmail;

    @Value("${app.portal-admin.password:12345}")
    private String portalAdminPassword;

    @Value("${app.portal-admin.full-name:System Admin}")
    private String portalAdminFullName;

    @Value("${app.technician.email:tech@gmail.com}")
    private String technicianEmail;

    @Value("${app.technician.password:12345}")
    private String technicianPassword;

    @Value("${app.technician.full-name:Technician User}")
    private String technicianFullName;

    public AdminDataSeeder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        upsertUser(
                portalAdminEmail,
                portalAdminPassword,
                portalAdminFullName,
                "System",
                "Admin",
                Role.ADMIN
        );
        upsertUser(
                technicianEmail,
                technicianPassword,
                technicianFullName,
                "Tech",
                "User",
                Role.TECHNICIAN
        );
    }

    private void upsertUser(String emailRaw, String password, String fullNameRaw, String defaultFirstName, String defaultLastName, Role role) {
        String email = emailRaw == null ? "" : emailRaw.trim().toLowerCase();
        if (email.isEmpty()) {
            return;
        }

        User user = userRepository.findByEmail(email).orElseGet(User::new);

        String fullName = fullNameRaw == null ? "" : fullNameRaw.trim();
        if (fullName.isEmpty()) {
            fullName = defaultFirstName + " " + defaultLastName;
        }

        String[] parts = fullName.split("\\s+", 2);
        String firstName = parts.length > 0 ? parts[0] : defaultFirstName;
        String lastName = parts.length > 1 ? parts[1] : defaultLastName;

        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        user.setName(fullName);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        userRepository.save(user);
    }
}
