package campus_nexus.dto.request;

import campus_nexus.enums.ResourceType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ResourceRequestDTO {

    @NotBlank(message = "Resource name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Resource type is required")
    private ResourceType type;

    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 500, message = "Capacity cannot exceed 500")
    private Integer capacity;

    @NotBlank(message = "Location is required")
    private String location;

    private String description;

    private Boolean hasWifi = false;
    private Boolean hasAc = false;
    private Boolean hasProjector = false;

    @Pattern(regexp = "^(ACTIVE|OUT_OF_SERVICE|MAINTENANCE)$", message = "Status must be ACTIVE, OUT_OF_SERVICE, or MAINTENANCE")
    private String status = "ACTIVE";
}
