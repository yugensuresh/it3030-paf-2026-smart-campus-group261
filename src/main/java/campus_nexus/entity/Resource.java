package campus_nexus.entity;

import campus_nexus.enums.ResourceType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "resources")
public class Resource {
    @Id
    private Long id;

    @Indexed(unique = true)
    private String name;

    private ResourceType type;

    private Integer capacity;
    private String location;
    private String description;
    private Boolean hasWifi;
    private Boolean hasAc;
    private Boolean hasProjector;

    // THIS FIELD IS CRITICAL - MAKE SURE IT EXISTS
    private String status = "ACTIVE";

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "ACTIVE";
        if (hasWifi == null) hasWifi = false;
        if (hasAc == null) hasAc = false;
        if (hasProjector == null) hasProjector = false;
    }

    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
