package campus_nexus.entity;

import campus_nexus.enums.PriorityLevel;
import campus_nexus.enums.TicketStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * MaintenanceTicket entity represents a reported issue for a specific resource.
 * Supports workflow: OPEN → IN_PROGRESS → RESOLVED → CLOSED
 * Includes support for 3 image attachments and priority categorization.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "maintenance_tickets")
@CompoundIndexes({
        @CompoundIndex(name = "ticket_status_idx", def = "{'status': 1}"),
        @CompoundIndex(name = "ticket_priority_idx", def = "{'priority': 1}"),
        @CompoundIndex(name = "ticket_user_idx", def = "{'user.id': 1}"),
        @CompoundIndex(name = "ticket_resource_idx", def = "{'resource.id': 1}")
})
public class MaintenanceTicket {

    @Id
    private Long id;

    private Resource resource;

    private User user;

    private User technician;  // Staff member assigned to fix the issue

    private String category;  // e.g., "ELECTRICAL", "PLUMBING", "EQUIPMENT", "FURNITURE"

    private String description;

    private String resolutionNotes;  // Notes from technician after resolving

    private PriorityLevel priority;

    private TicketStatus status;

    private String contactPhone;  // Preferred contact number
    private String contactEmail;  // Preferred contact email

    // Fields for storing image URLs/Paths (Max 3 as requested)
    private String imageUrl1;
    private String imageUrl2;
    private String imageUrl3;

    private String rejectionReason;  // If admin rejects the ticket

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime resolvedAt;  // When status changed to RESOLVED
    private LocalDateTime closedAt;    // When status changed to CLOSED

    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = TicketStatus.OPEN;
        }
        if (this.priority == null) {
            this.priority = PriorityLevel.MEDIUM;
        }
    }

    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Get all image URLs as a list (excluding nulls)
     */
    public List<String> getImageUrls() {
        List<String> images = new ArrayList<>();
        if (imageUrl1 != null && !imageUrl1.isEmpty()) images.add(imageUrl1);
        if (imageUrl2 != null && !imageUrl2.isEmpty()) images.add(imageUrl2);
        if (imageUrl3 != null && !imageUrl3.isEmpty()) images.add(imageUrl3);
        return images;
    }

    /**
     * Set image URLs from a list (max 3)
     */
    public void setImageUrls(List<String> imageUrls) {
        if (imageUrls != null) {
            if (imageUrls.size() > 0) this.imageUrl1 = imageUrls.get(0);
            if (imageUrls.size() > 1) this.imageUrl2 = imageUrls.get(1);
            if (imageUrls.size() > 2) this.imageUrl3 = imageUrls.get(2);
        }
    }

    /**
     * Check if ticket can be edited by user
     */
    public boolean isEditableByUser() {
        return this.status == TicketStatus.OPEN;
    }

    /**
     * Check if ticket can be assigned to technician
     */
    public boolean isAssignable() {
        return this.status == TicketStatus.OPEN;
    }
}
