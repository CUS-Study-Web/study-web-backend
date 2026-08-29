package studyweb.cus.repository.content;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import studyweb.cus.entity.content.PricingPageContent;

public interface PricingPageContentRepository extends JpaRepository<PricingPageContent, UUID> {
  Optional<PricingPageContent> findFirstByOrderByCreatedAtDesc();
}
