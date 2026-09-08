package studyweb.cus.repository.flashcard;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import studyweb.cus.entity.progress.UserTopicProgress;

public interface UserTopicProgressRepository extends JpaRepository<UserTopicProgress, UUID> {

  Optional<UserTopicProgress> findByUserIdAndTopicId(UUID userId, UUID topicId);

  List<UserTopicProgress> findByUserIdAndTopicIdIn(UUID userId, Collection<UUID> topicIds);
}
