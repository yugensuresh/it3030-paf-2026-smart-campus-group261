package campus_nexus.repository;

import campus_nexus.entity.MaintenanceTicket;
import campus_nexus.enums.PriorityLevel;
import campus_nexus.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface MaintenanceTicketRepositoryCustom {
    Page<MaintenanceTicket> searchTickets(
            Long userId,
            Long resourceId,
            Long technicianId,
            TicketStatus status,
            PriorityLevel priority,
            String category,
            String search,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    List<Object[]> countTicketsByStatus();

    List<Object[]> countTicketsByPriority();

    List<MaintenanceTicket> findActiveTicketsByTechnician(Long technicianId);

    List<MaintenanceTicket> findUrgentHighPriorityTickets();
}
