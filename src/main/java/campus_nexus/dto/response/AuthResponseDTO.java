package campus_nexus.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponseDTO {

    private Long userId;
    private String token;
    private String type;  // "Bearer"
    private String email;
    private String name;
    private String role;
}
