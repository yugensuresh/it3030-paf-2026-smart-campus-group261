package campus_nexus.dto.response;

import campus_nexus.enums.PriorityLevel;
import campus_nexus.enums.TicketStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TicketResponseDTO {

    private Long id;

    // User info
    private Long userId;
    private String userEmail;
    private String userName;

    // Resource info
    private Long resourceId;
    private String resourceName;
    private String resourceLocation;

    // Technician info (if assigned)
    private Long technicianId;
    private String technicianName;

    // Ticket details
    private String category;
    private String description;
    private String resolutionNotes;
    private PriorityLevel priority;
    private TicketStatus status;

    // Contact info
    private String contactPhone;
    private String contactEmail;

    // Attachments (max 3 images)
    private List<String> imageUrls;

    // Admin actions
    private String rejectionReason;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
}