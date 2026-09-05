package studyweb.cus.repository.flashcard;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import studyweb.cus.entity.progress.UserFlashcardProgress;
import studyweb.cus.enums.FlashcardProgressStatus;

public interface UserFlashcardProgressRepository
    extends JpaRepository<UserFlashcardProgress, UUID> {

  Optional<UserFlashcardProgress> findByUserIdAndFlashcardId(UUID userId, UUID flashcardId);

  List<UserFlashcardProgress> findByUserIdAndFlashcardTopicId(UUID userId, UUID topicId);

  List<UserFlashcardProgress> findByUserIdAndFlashcardIdIn(
      UUID userId, Collection<UUID> flashcardIds);

  @Query(
      "SELECT COUNT(ufp) FROM UserFlashcardProgress ufp "
          + "WHERE ufp.user.id = :userId AND ufp.flashcard.topic.id = :topicId AND ufp.status = :status")
  long countByUserIdAndTopicIdAndStatus(
      @Param("userId") UUID userId,
      @Param("topicId") UUID topicId,
      @Param("status") FlashcardProgressStatus status);

  @Query(
      "SELECT COUNT(DISTINCT ufp.flashcard.id) FROM UserFlashcardProgress ufp "
          + "WHERE ufp.user.id = :userId AND ufp.status = studyweb.cus.enums.FlashcardProgressStatus.REMEMBER "
          + "AND ufp.flashcard.topic.deletedAt IS NULL AND ufp.flashcard.topic.status = studyweb.cus.enums.CourseCreateStatus.PUBLISH")
  long countRememberedWordsByUserId(@Param("userId") UUID userId);
}
