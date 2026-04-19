package campus_nexus.entity;

import campus_nexus.enums.BookingStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Document(collection = "bookings")
@CompoundIndexes({
        @CompoundIndex(name = "booking_resource_date_idx", def = "{'resource.id': 1, 'bookingDate': 1}"),
        @CompoundIndex(name = "booking_user_idx", def = "{'user.id': 1}")
})
public class Booking {
    @Id
    private Long id;

    private User user;

    private Resource resource;

    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String purpose;

    // New fields for Industry Level Workflow
    private BookingStatus status;

    private String rejectionReason;

    public void onCreate() {
        if (this.status == null) {
            this.status = BookingStatus.PENDING; // Default status as per workflow
        }
    }
}
