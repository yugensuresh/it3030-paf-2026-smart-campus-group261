package campus_nexus.repository;

import campus_nexus.entity.Resource;
import campus_nexus.enums.ResourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ResourceRepositoryImpl implements ResourceRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public ResourceRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<Resource> searchWithFilters(String name, ResourceType type, Integer minCapacity, Integer maxCapacity,
                                            String location, String status, Boolean hasWifi, Boolean hasAc,
                                            Pageable pageable) {
        List<Criteria> criteriaList = new ArrayList<>();

        if (name != null && !name.trim().isEmpty()) {
            criteriaList.add(Criteria.where("name").regex(".*" + Pattern.quote(name.trim()) + ".*", "i"));
        }
        if (type != null) {
            criteriaList.add(Criteria.where("type").is(type));
        }
        if (minCapacity != null) {
            criteriaList.add(Criteria.where("capacity").gte(minCapacity));
        }
        if (maxCapacity != null) {
            criteriaList.add(Criteria.where("capacity").lte(maxCapacity));
        }
        if (location != null && !location.trim().isEmpty()) {
            criteriaList.add(Criteria.where("location").regex(".*" + Pattern.quote(location.trim()) + ".*", "i"));
        }
        if (status != null && !status.trim().isEmpty()) {
            criteriaList.add(Criteria.where("status").is(status));
        }
        if (hasWifi != null) {
            criteriaList.add(Criteria.where("hasWifi").is(hasWifi));
        }
        if (hasAc != null) {
            criteriaList.add(Criteria.where("hasAc").is(hasAc));
        }

        Query query = new Query().with(pageable);
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        List<Resource> resources = mongoTemplate.find(query, Resource.class);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Resource.class);
        return new PageImpl<>(resources, pageable, total);
    }
}
