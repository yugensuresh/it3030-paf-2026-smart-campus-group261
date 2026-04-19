package campus_nexus.config;

import campus_nexus.entity.AuditLog;
import campus_nexus.entity.Booking;
import campus_nexus.entity.MaintenanceTicket;
import campus_nexus.entity.Notification;
import campus_nexus.entity.Resource;
import campus_nexus.entity.User;
import campus_nexus.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MongoDocumentPreparer {

    private final SequenceGeneratorService sequenceGeneratorService;

    @Autowired
    public MongoDocumentPreparer(SequenceGeneratorService sequenceGeneratorService) {
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    public User prepare(User user) {
        if (user.getId() == null) {
            user.setId(sequenceGeneratorService.generateSequence("users_sequence"));
        }
        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }
        return user;
    }

    public Resource prepare(Resource resource) {
        if (resource.getId() == null) {
            resource.setId(sequenceGeneratorService.generateSequence("resources_sequence"));
            resource.onCreate();
        } else {
            resource.onUpdate();
        }
        return resource;
    }

    public Booking prepare(Booking booking) {
        if (booking.getId() == null) {
            booking.setId(sequenceGeneratorService.generateSequence("bookings_sequence"));
        }
        booking.onCreate();
        return booking;
    }

    public MaintenanceTicket prepare(MaintenanceTicket ticket) {
        if (ticket.getId() == null) {
            ticket.setId(sequenceGeneratorService.generateSequence("maintenance_tickets_sequence"));
            ticket.onCreate();
        } else {
            ticket.onUpdate();
        }
        return ticket;
    }

    public Notification prepare(Notification notification) {
        if (notification.getId() == null) {
            notification.setId(sequenceGeneratorService.generateSequence("notifications_sequence"));
        }
        notification.onCreate();
        return notification;
    }

    public AuditLog prepare(AuditLog auditLog) {
        if (auditLog.getId() == null) {
            auditLog.setId(sequenceGeneratorService.generateSequence("audit_logs_sequence"));
        }
        if (auditLog.getTimestamp() == null) {
            auditLog.setTimestamp(LocalDateTime.now());
        }
        return auditLog;
    }
}
