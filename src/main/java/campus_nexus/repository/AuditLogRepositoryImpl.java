package campus_nexus.repository;

import campus_nexus.entity.AuditLog;
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
import java.util.Comparator;
import java.util.List;

public class AuditLogRepositoryImpl implements AuditLogRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public AuditLogRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<AuditLog> searchAuditLogs(String action, String performedBy, LocalDateTime startDate,
                                          LocalDateTime endDate, Pageable pageable) {
        List<Criteria> criteriaList = new ArrayList<>();
        if (action != null && !action.trim().isEmpty()) criteriaList.add(Criteria.where("action").is(action));
        if (performedBy != null && !performedBy.trim().isEmpty()) criteriaList.add(Criteria.where("performedBy").is(performedBy));
        if (startDate != null) criteriaList.add(Criteria.where("timestamp").gte(startDate));
        if (endDate != null) criteriaList.add(Criteria.where("timestamp").lte(endDate));

        Query query = new Query().with(pageable);
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        List<AuditLog> logs = mongoTemplate.find(query, AuditLog.class);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), AuditLog.class);
        return new PageImpl<>(logs, pageable, total);
    }

    @Override
    public List<Object[]> countLogsByAction() {
        Aggregation aggregation = Aggregation.newAggregation(Aggregation.group("action").count().as("count"));
        AggregationResults<GroupCountProjection> results = mongoTemplate.aggregate(
                aggregation, "audit_logs", GroupCountProjection.class);
        return results.getMappedResults().stream()
                .sorted(Comparator.comparingLong(GroupCountProjection::count).reversed())
                .map(result -> new Object[]{result.id(), result.count()})
                .toList();
    }

    @Override
    public List<Object[]> countLogsByUser() {
        Aggregation aggregation = Aggregation.newAggregation(Aggregation.group("performedBy").count().as("count"));
        AggregationResults<GroupCountProjection> results = mongoTemplate.aggregate(
                aggregation, "audit_logs", GroupCountProjection.class);
        return results.getMappedResults().stream()
                .sorted(Comparator.comparingLong(GroupCountProjection::count).reversed())
                .map(result -> new Object[]{result.id(), result.count()})
                .toList();
    }

    private record GroupCountProjection(String id, long count) {
    }
}
