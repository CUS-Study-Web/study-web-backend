package studyweb.cus.repository.badge;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import studyweb.cus.entity.badge.Badge;

public interface BadgeRepository
    extends JpaRepository<Badge, UUID>, JpaSpecificationExecutor<Badge> {

  boolean existsByName(String name);

  boolean existsByNameAndIdNot(String name, UUID id);

  Optional<Badge> findByName(String name);
}
