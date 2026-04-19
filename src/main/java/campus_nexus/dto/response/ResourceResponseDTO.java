
package campus_nexus.dto.response;

import campus_nexus.enums.ResourceType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ResourceResponseDTO {
    private Long id;
    private String name;
    private ResourceType type;
    private Integer capacity;
    private String location;
    private String description;
    private Boolean hasWifi;
    private Boolean hasAc;
    private Boolean hasProjector;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}