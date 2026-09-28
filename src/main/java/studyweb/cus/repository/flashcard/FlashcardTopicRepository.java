package studyweb.cus.repository.flashcard;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import studyweb.cus.entity.flashcard.FlashcardTopic;
import studyweb.cus.enums.CourseCreateStatus;
import studyweb.cus.exception.flashcard.FlashcardErrorCode;
import studyweb.cus.exception.flashcard.FlashcardException;

public interface FlashcardTopicRepository
    extends JpaRepository<FlashcardTopic, UUID>, JpaSpecificationExecutor<FlashcardTopic> {

  Optional<FlashcardTopic> findByIdAndDeletedAtIsNull(UUID id);

  default FlashcardTopic requireTopic(UUID id) {
    return findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new FlashcardException(FlashcardErrorCode.TOPIC_NOT_FOUND));
  }

  Page<FlashcardTopic> findByDeletedAtIsNull(Pageable pageable);

  Page<FlashcardTopic> findByDeletedAtIsNullAndStatus(Pageable pageable, CourseCreateStatus status);

  long countByDeletedAtIsNull();

  @Query("SELECT COALESCE(SUM(t.numWords), 0) FROM FlashcardTopic t WHERE t.deletedAt IS NULL")
  long countTotalWords();

  @Query("SELECT COALESCE(SUM(t.numWords), 0) FROM FlashcardTopic t WHERE t.deletedAt IS NULL AND t.status = studyweb.cus.enums.CourseCreateStatus.PUBLISH")
  long countPublishedTotalWords();

  long countByDeletedAtIsNullAndStatus(CourseCreateStatus status);
}
