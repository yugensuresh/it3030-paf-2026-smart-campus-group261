package campus_nexus.repository;

import campus_nexus.entity.MaintenanceTicket;
import campus_nexus.enums.PriorityLevel;
import campus_nexus.enums.TicketStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MaintenanceTicketRepositoryImpl implements MaintenanceTicketRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public MaintenanceTicketRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<MaintenanceTicket> searchTickets(Long userId, Long resourceId, Long technicianId, TicketStatus status,
                                                 PriorityLevel priority, String category, String search,
                                                 LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        List<Criteria> criteriaList = new ArrayList<>();

        if (userId != null) criteriaList.add(Criteria.where("user.id").is(userId));
        if (resourceId != null) criteriaList.add(Criteria.where("resource.id").is(resourceId));
        if (technicianId != null) criteriaList.add(Criteria.where("technician.id").is(technicianId));
        if (status != null) criteriaList.add(Criteria.where("status").is(status));
        if (priority != null) criteriaList.add(Criteria.where("priority").is(priority));
        if (category != null && !category.trim().isEmpty()) {
            criteriaList.add(Criteria.where("category").regex(".*" + Pattern.quote(category.trim()) + ".*", "i"));
        }
        if (search != null && !search.trim().isEmpty()) {
            criteriaList.add(Criteria.where("description").regex(".*" + Pattern.quote(search.trim()) + ".*", "i"));
        }
        if (startDate != null) criteriaList.add(Criteria.where("createdAt").gte(startDate));
        if (endDate != null) criteriaList.add(Criteria.where("createdAt").lte(endDate));

        Query query = new Query().with(pageable);
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        List<MaintenanceTicket> tickets = mongoTemplate.find(query, MaintenanceTicket.class);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), MaintenanceTicket.class);
        return new PageImpl<>(tickets, pageable, total);
    }

    @Override
    public List<Object[]> countTicketsByStatus() {
        Aggregation aggregation = Aggregation.newAggregation(Aggregation.group("status").count().as("count"));
        AggregationResults<GroupCountProjection> results = mongoTemplate.aggregate(
                aggregation, "maintenance_tickets", GroupCountProjection.class);
        return results.getMappedResults().stream()
                .map(result -> new Object[]{result.id(), result.count()})
                .toList();
    }

    @Override
    public List<Object[]> countTicketsByPriority() {
        Aggregation aggregation = Aggregation.newAggregation(Aggregation.group("priority").count().as("count"));
        AggregationResults<GroupCountProjection> results = mongoTemplate.aggregate(
                aggregation, "maintenance_tickets", GroupCountProjection.class);
        return results.getMappedResults().stream()
                .map(result -> new Object[]{result.id(), result.count()})
                .toList();
    }

    @Override
    public List<MaintenanceTicket> findActiveTicketsByTechnician(Long technicianId) {
        Query query = new Query().addCriteria(new Criteria().andOperator(
                Criteria.where("technician.id").is(technicianId),
                Criteria.where("status").in(TicketStatus.OPEN, TicketStatus.IN_PROGRESS)
        ));
        return mongoTemplate.find(query, MaintenanceTicket.class);
    }

    @Override
    public List<MaintenanceTicket> findUrgentHighPriorityTickets() {
        Query query = new Query().addCriteria(new Criteria().andOperator(
                Criteria.where("priority").is(PriorityLevel.HIGH),
                Criteria.where("status").in(TicketStatus.OPEN, TicketStatus.IN_PROGRESS)
        ));
        query.with(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "createdAt"));
        return mongoTemplate.find(query, MaintenanceTicket.class);
    }

    private record GroupCountProjection(String id, long count) {
    }
}
