package campus_nexus.entity;

import campus_nexus.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String purpose;

    // New fields for Industry Level Workflow
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private String rejectionReason;

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = BookingStatus.PENDING; // Default status as per workflow
        }
    }
}