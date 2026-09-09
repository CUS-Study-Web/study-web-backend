package studyweb.cus.repository.content;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import studyweb.cus.entity.content.FooterLink;

public interface FooterLinkRepository extends JpaRepository<FooterLink, UUID> {
  List<FooterLink> findByFooterIdOrderBySortOrderAsc(UUID footerId);
}
