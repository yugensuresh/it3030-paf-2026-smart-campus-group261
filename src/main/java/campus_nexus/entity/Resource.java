package campus_nexus.entity;

import campus_nexus.enums.ResourceReservationCategory;
import campus_nexus.enums.ResourceType;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "resources")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceType type;

    @Enumerated(EnumType.STRING)
    private ResourceReservationCategory category;

    private Integer capacity;

    private String location;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean hasWifi = false;

    @Column(nullable = false)
    private Boolean hasAc = false;

    private Boolean hasProjector = false;

    @Column(nullable = false)
    private String status = "ACTIVE";  // ACTIVE, OUT_OF_SERVICE, MAINTENANCE

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Auto-set timestamps before persisting
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "ACTIVE";
        if (hasWifi == null) hasWifi = false;
        if (hasAc == null) hasAc = false;
        if (hasProjector == null) hasProjector = false;
        if (category == null) category = ResourceReservationCategory.HALL_LAB;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (category == null) category = ResourceReservationCategory.HALL_LAB;
    }
}