package campus_nexus.controller;

import campus_nexus.entity.Resource;
import campus_nexus.enums.ResourceReservationCategory;
import campus_nexus.enums.ResourceType;
import campus_nexus.service.ResourceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resources")
@CrossOrigin(origins = "*")
public class ResourceController {

    private static final Logger logger = LoggerFactory.getLogger(ResourceController.class);

    @Autowired
    private ResourceService resourceService;

    /**
     * GET /api/resources - Get all resources (no pagination)
     * Used for dropdowns and small lists
     */
    @GetMapping
    public ResponseEntity<List<Resource>> getAllResources() {
        logger.info("GET /api/resources - Fetching all resources");
        List<Resource> resources = resourceService.getAllResources();
        return ResponseEntity.ok(resources);
    }

    /**
     * GET /api/resources/paged - Get resources with pagination and filtering
     * Query params:
     * - page: page number (default 0)
     * - size: page size (default 10)
     * - sort: sort field (default id)
     * - direction: asc/desc (default asc)
     * - name: search by name (optional)
     * - type: filter by resource type (optional)
     * - category: reservation category (optional)
     * - minCapacity: minimum capacity (optional)
     * - maxCapacity: maximum capacity (optional)
     * - location: filter by location (optional)
     * - status: filter by status (optional)
     * - hasWifi: true/false (optional)
     * - hasAc: true/false (optional)
     */
    @GetMapping("/paged")
    public ResponseEntity<Map<String, Object>> getPagedResources(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) ResourceType type,
            @RequestParam(required = false) ResourceReservationCategory category,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) Integer maxCapacity,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean hasWifi,
            @RequestParam(required = false) Boolean hasAc) {

        logger.info("GET /api/resources/paged - page: {}, size: {}, sort: {}, direction: {}", page, size, sort, direction);

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<Resource> resourcePage = resourceService.searchResources(
                name, type, category, minCapacity, maxCapacity, location, status, hasWifi, hasAc, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", resourcePage.getContent());
        response.put("currentPage", resourcePage.getNumber());
        response.put("totalItems", resourcePage.getTotalElements());
        response.put("totalPages", resourcePage.getTotalPages());
        response.put("pageSize", resourcePage.getSize());
        response.put("hasNext", resourcePage.hasNext());
        response.put("hasPrevious", resourcePage.hasPrevious());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/resources/active - Get only active resources with pagination
     */
    @GetMapping("/active")
    public ResponseEntity<Page<Resource>> getActiveResources(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        logger.info("GET /api/resources/active - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Resource> resources = resourceService.getActiveResources(pageable);
        return ResponseEntity.ok(resources);
    }

    /**
     * GET /api/resources/{id} - Get single resource by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Resource> getResourceById(@PathVariable Long id) {
        logger.info("GET /api/resources/{} - Fetching resource", id);
        Resource resource = resourceService.getResourceById(id);
        return ResponseEntity.ok(resource);
    }

    /**
     * POST /api/resources - Create new resource
     */
    @PostMapping
    public ResponseEntity<Resource> createResource(@Valid @RequestBody Resource resource) {
        logger.info("POST /api/resources - Creating new resource: {}", resource.getName());
        Resource created = resourceService.createResource(resource);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/resources/{id} - Update existing resource
     */
    @PutMapping("/{id}")
    public ResponseEntity<Resource> updateResource(
            @PathVariable Long id,
            @Valid @RequestBody Resource resource) {

        logger.info("PUT /api/resources/{} - Updating resource", id);
        Resource updated = resourceService.updateResource(id, resource);
        return ResponseEntity.ok(updated);
    }

    /**
     * PATCH /api/resources/{id}/status - Update resource status only
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Resource> updateResourceStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate) {

        String status = statusUpdate.get("status");
        logger.info("PATCH /api/resources/{}/status - Changing status to: {}", id, status);
        Resource updated = resourceService.updateResourceStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/resources/{id} - Delete resource
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        logger.info("DELETE /api/resources/{} - Deleting resource", id);
        resourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }
}
