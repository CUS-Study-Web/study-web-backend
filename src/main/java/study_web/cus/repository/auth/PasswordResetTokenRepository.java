package study_web.cus.repository.auth;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import study_web.cus.entity.redis.PasswordResetOtp;

@Repository
public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetOtp, String> {}
