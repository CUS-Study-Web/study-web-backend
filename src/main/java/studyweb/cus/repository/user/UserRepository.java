package studyweb.cus.repository.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.UserRole;

public interface UserRepository extends JpaRepository<User, UUID> {

  boolean existsByGmail(String gmail);

  Optional<User> findByGmail(String gmail);

  @Query("SELECT u.id FROM User u WHERE u.role = :role")
  List<UUID> findIdsByRole(@Param("role") UserRole role);
}
