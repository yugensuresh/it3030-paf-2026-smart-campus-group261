package campus_nexus.dto.request;

import campus_nexus.enums.NotificationType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class NotificationRequestDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotBlank(message = "Message is required")
    @Size(min = 1, max = 2000, message = "Message must be between 1 and 2000 characters")
    private String message;

    @NotBlank(message = "Reference ID is required")
    private String referenceId;  // e.g., "BOOKING_123", "TICKET_456"
}