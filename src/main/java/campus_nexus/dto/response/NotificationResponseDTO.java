package campus_nexus.dto.response;

import campus_nexus.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponseDTO {

    private Long id;

    // User info
    private Long userId;
    private String userEmail;
    private String userName;

    // Notification details
    private NotificationType type;
    private String typeTitle;
    private String message;
    private String referenceId;
    private Boolean isRead;

    // Timestamp
    private LocalDateTime createdAt;
}