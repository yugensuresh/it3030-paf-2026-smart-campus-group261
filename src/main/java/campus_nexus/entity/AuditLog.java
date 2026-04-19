package campus_nexus.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "audit_logs")
@CompoundIndex(name = "audit_timestamp_idx", def = "{'timestamp': -1}")
public class AuditLog {
    @Id
    private Long id;

    private String action;      // e.g., "CREATED_BOOKING", "RESOLVED_TICKET"
    private String performedBy; // User email or Name
    private String details;     // e.g., "Booking ID 5 created for Hall A"
    private LocalDateTime timestamp;

    public AuditLog(String action, String performedBy, String details) {
        this.action = action;
        this.performedBy = performedBy;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
}
