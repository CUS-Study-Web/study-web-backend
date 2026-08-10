package studyweb.cus.repository.auth;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import studyweb.cus.entity.redis.PasswordResetOtp;

@Repository
public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetOtp, String> {}
