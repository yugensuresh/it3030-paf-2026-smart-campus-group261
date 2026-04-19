package campus_nexus.repository;

import campus_nexus.entity.Resource;
import campus_nexus.enums.ResourceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ResourceRepositoryCustom {
    Page<Resource> searchWithFilters(
            String name,
            ResourceType type,
            Integer minCapacity,
            Integer maxCapacity,
            String location,
            String status,
            Boolean hasWifi,
            Boolean hasAc,
            Pageable pageable
    );
}
