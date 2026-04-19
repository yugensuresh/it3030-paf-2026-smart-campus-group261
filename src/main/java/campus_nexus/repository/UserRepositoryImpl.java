package campus_nexus.repository;

import campus_nexus.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.regex.Pattern;

public class UserRepositoryImpl implements UserRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public UserRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<User> searchUsers(String searchTerm, Pageable pageable) {
        Query query = new Query().with(pageable);
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String pattern = ".*" + Pattern.quote(searchTerm.trim()) + ".*";
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("name").regex(pattern, "i"),
                    Criteria.where("email").regex(pattern, "i")
            ));
        }

        List<User> users = mongoTemplate.find(query, User.class);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), User.class);
        return new PageImpl<>(users, pageable, total);
    }

    @Override
    public List<Object[]> countUsersByRole() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("role").count().as("count")
        );
        AggregationResults<RoleCountProjection> results = mongoTemplate.aggregate(
                aggregation, "users", RoleCountProjection.class);
        return results.getMappedResults().stream()
                .map(result -> new Object[]{result.id(), result.count()})
                .toList();
    }

    private record RoleCountProjection(String id, long count) {
    }
}
