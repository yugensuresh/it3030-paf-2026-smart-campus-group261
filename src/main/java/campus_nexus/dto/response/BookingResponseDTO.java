package campus_nexus.dto.response;

import campus_nexus.enums.BookingStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class BookingResponseDTO {
    private Long id;
    private Long userId;
    private String userEmail;
    private String userName;
    private Long resourceId;
    private String resourceName;
    private String resourceType;
    private String resourceLocation;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String purpose;
    private BookingStatus status;
    private String rejectionReason;
    private String createdAt;
}