package campus_nexus.repository;

import campus_nexus.entity.MaintenanceTicket;
import campus_nexus.enums.PriorityLevel;
import campus_nexus.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MaintenanceTicketRepository extends MongoRepository<MaintenanceTicket, Long>, MaintenanceTicketRepositoryCustom {

    Page<MaintenanceTicket> findByStatus(TicketStatus status, Pageable pageable);

    List<MaintenanceTicket> findByStatus(TicketStatus status);

    long countByStatus(TicketStatus status);

    Page<MaintenanceTicket> findByPriority(PriorityLevel priority, Pageable pageable);

    List<MaintenanceTicket> findByPriorityOrderByCreatedAtAsc(PriorityLevel priority);

    @Query("{ 'user.id': ?0 }")
    Page<MaintenanceTicket> findByUserId(Long userId, Pageable pageable);

    @Query("{ 'user.id': ?0 }")
    List<MaintenanceTicket> findByUserId(Long userId);

    @Query("{ 'technician.id': ?0 }")
    Page<MaintenanceTicket> findByTechnicianId(Long technicianId, Pageable pageable);

    @Query("{ 'technician.id': ?0, 'status': ?1 }")
    List<MaintenanceTicket> findByTechnicianIdAndStatus(Long technicianId, TicketStatus status);

    @Query("{ 'resource.id': ?0 }")
    Page<MaintenanceTicket> findByResourceId(Long resourceId, Pageable pageable);

    @Query("{ 'resource.id': ?0 }")
    List<MaintenanceTicket> findByResourceId(Long resourceId);

    List<MaintenanceTicket> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    Page<MaintenanceTicket> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
