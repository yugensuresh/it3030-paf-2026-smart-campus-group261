package campus_nexus.repository;

import campus_nexus.entity.User;
import campus_nexus.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, Long>, UserRepositoryCustom {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    Page<User> findByRole(Role role, Pageable pageable);

    @Query("{ 'role': 'ADMIN' }")
    List<User> findAllAdmins();

    @Query("{ 'role': 'TECHNICIAN' }")
    List<User> findAllTechnicians();
}
