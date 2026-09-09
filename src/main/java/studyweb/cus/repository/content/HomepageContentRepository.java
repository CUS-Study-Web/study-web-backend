package studyweb.cus.repository.content;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import studyweb.cus.entity.content.HomepageContent;

public interface HomepageContentRepository extends JpaRepository<HomepageContent, UUID> {
  Optional<HomepageContent> findFirstByOrderByCreatedAtDesc();
}
