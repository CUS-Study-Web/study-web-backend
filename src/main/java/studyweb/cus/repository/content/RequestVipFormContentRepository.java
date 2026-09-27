package studyweb.cus.repository.content;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import studyweb.cus.entity.content.RequestVipFormContent;

@Repository
public interface RequestVipFormContentRepository
    extends JpaRepository<RequestVipFormContent, UUID> {

  Optional<RequestVipFormContent> findFirstByOrderByCreatedAtDesc();
}
