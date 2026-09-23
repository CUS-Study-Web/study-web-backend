package studyweb.cus.repository.registration;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import studyweb.cus.entity.registration.RegisterForm;

@Repository
public interface RegisterFormRepository extends JpaRepository<RegisterForm, UUID> {}
