package studyweb.cus.repository.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import studyweb.cus.entity.user.User;

public interface UserRepository extends JpaRepository<User, UUID> {

  boolean existsByGmail(String gmail);

  Optional<User> findByGmail(String gmail);
}
