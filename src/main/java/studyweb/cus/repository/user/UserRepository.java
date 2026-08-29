package studyweb.cus.repository.user;

import java.util.List;
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
import studyweb.cus.enums.UserTier;

public interface UserRepository extends JpaRepository<User, UUID> {

  int countByRoleAndTier(UserRole role, UserTier tier);

  int countByRole(UserRole role);

  int countByStatus(UserStatus status);

  int countByRoleAndStatus(UserRole role, UserStatus status);

  boolean existsByGmail(String gmail);

  Optional<User> findByGmail(String gmail);

  @Query(
      value =
          """
          SELECT u FROM User u
          WHERE u.role = 'LEARNER'
            AND (:status IS NULL OR u.status = :status)
            AND (CAST(:search AS string) IS NULL OR LOWER(u.gmail) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                 OR LOWER(u.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
          """,
      countQuery =
          """
          SELECT COUNT(u) FROM User u
          WHERE u.role = 'LEARNER'
            AND (:status IS NULL OR u.status = :status)
            AND (CAST(:search AS string) IS NULL OR LOWER(u.gmail) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                 OR LOWER(u.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
          """)
  Page<User> searchLearners(
      @Param("search") String search, @Param("status") UserStatus status, Pageable pageable);

  Optional<User> findByIdAndRole(UUID id, UserRole role);

  @Query(
      value =
          """
          SELECT u FROM User u
          WHERE u.role = 'ASSISTANT'
            AND (:status IS NULL OR u.status = :status)
            AND (CAST(:search AS string) IS NULL OR LOWER(u.gmail) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                 OR LOWER(u.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
          """,
      countQuery =
          """
          SELECT COUNT(u) FROM User u
          WHERE u.role = 'ASSISTANT'
            AND (:status IS NULL OR u.status = :status)
            AND (CAST(:search AS string) IS NULL OR LOWER(u.gmail) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                 OR LOWER(u.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
          """)
  Page<User> searchAssistants(
      @Param("search") String search, @Param("status") UserStatus status, Pageable pageable);

  @Query("SELECT u.id FROM User u WHERE u.role = :role")
  List<UUID> findIdsByRole(@Param("role") UserRole role);
}
