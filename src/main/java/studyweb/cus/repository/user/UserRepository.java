package studyweb.cus.repository.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import studyweb.cus.entity.user.User;
import studyweb.cus.enums.UserRole;
import studyweb.cus.enums.UserStatus;

public interface UserRepository extends JpaRepository<User, UUID> {

  boolean existsByGmail(String gmail);

  Optional<User> findByGmail(String gmail);

  @Query(
      value =
          """
          SELECT u FROM User u
          WHERE u.role = 'LEARNER'
            AND (:status IS NULL OR u.status = :status)
            AND (:search IS NULL OR LOWER(u.gmail) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')))
          """,
      countQuery =
          """
          SELECT COUNT(u) FROM User u
          WHERE u.role = 'LEARNER'
            AND (:status IS NULL OR u.status = :status)
            AND (:search IS NULL OR LOWER(u.gmail) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')))
          """)
  Page<User> searchLearners(
      @Param("search") String search, @Param("status") UserStatus status, Pageable pageable);

  Optional<User> findByIdAndRole(UUID id, UserRole role);
}
