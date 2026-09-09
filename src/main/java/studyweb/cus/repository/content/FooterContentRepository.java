package studyweb.cus.repository.content;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import studyweb.cus.entity.content.FooterContent;

public interface FooterContentRepository extends JpaRepository<FooterContent, UUID> {
  Optional<FooterContent> findFirstByOrderByCreatedAtDesc();
}
