package campus_nexus.entity;

import campus_nexus.enums.PriorityLevel;
import campus_nexus.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * MaintenanceTicket entity represents a reported issue for a specific resource.
 * Supports workflow: OPEN → IN_PROGRESS → RESOLVED → CLOSED
 * Includes support for 3 image attachments and priority categorization.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "maintenance_tickets", indexes = {
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_priority", columnList = "priority"),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_resource_id", columnList = "resource_id")
})
public class MaintenanceTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id")
    private User technician;  // Staff member assigned to fix the issue

    @Column(nullable = false, length = 100)
    private String category;  // e.g., "ELECTRICAL", "PLUMBING", "EQUIPMENT", "FURNITURE"

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(length = 1000)
    private String resolutionNotes;  // Notes from technician after resolving

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16, columnDefinition = "VARCHAR(16)")
    private PriorityLevel priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32, columnDefinition = "VARCHAR(32)")
    private TicketStatus status;

    private String contactPhone;  // Preferred contact number
    private String contactEmail;  // Preferred contact email

    // Max 3 attachments: HTTPS URLs and/or data URLs from uploaded images (LONGTEXT)
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String imageUrl1;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String imageUrl2;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String imageUrl3;

    private String rejectionReason;  // If admin rejects the ticket

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime resolvedAt;  // When status changed to RESOLVED
    private LocalDateTime closedAt;    // When status changed to CLOSED

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = TicketStatus.OPEN;
        }
        if (this.priority == null) {
            this.priority = PriorityLevel.MEDIUM;
        }
    }

    @PreUpdate
    protected void onUpdate() {
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