package campus_nexus.entity;

import campus_nexus.enums.Role;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "users")
public class User {
    @Id
    private Long id;

    @Indexed(unique = true)
    private String email;

    private String name;

    private Role role;
}
