package studyweb.cus.repository.auth;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import studyweb.cus.entity.redis.RefreshToken;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

  Optional<RefreshToken> findByEmail(String email);

  void deleteByEmail(String email);

  boolean existsByEmail(String email);
}
