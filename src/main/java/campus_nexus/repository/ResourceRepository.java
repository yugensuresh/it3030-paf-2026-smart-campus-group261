package campus_nexus.repository;

import campus_nexus.entity.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceRepository extends MongoRepository<Resource, Long>, ResourceRepositoryCustom {

    boolean existsByNameIgnoreCase(String name);

    Page<Resource> findByStatus(String status, Pageable pageable);
}
