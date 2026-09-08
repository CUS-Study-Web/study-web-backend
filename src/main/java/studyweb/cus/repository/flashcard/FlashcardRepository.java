package studyweb.cus.repository.flashcard;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import studyweb.cus.entity.flashcard.Flashcard;
import studyweb.cus.exception.flashcard.FlashcardErrorCode;
import studyweb.cus.exception.flashcard.FlashcardException;

public interface FlashcardRepository
    extends JpaRepository<Flashcard, UUID>, JpaSpecificationExecutor<Flashcard> {

  Page<Flashcard> findByTopicId(UUID topicId, Pageable pageable);

  Optional<Flashcard> findByIdAndTopicId(UUID id, UUID topicId);

  default Flashcard requireFlashcard(UUID id, UUID topicId) {
    return findByIdAndTopicId(id, topicId)
        .orElseThrow(() -> new FlashcardException(FlashcardErrorCode.FLASHCARD_NOT_FOUND));
  }

  long countByTopicId(UUID topicId);

  void deleteByIdAndTopicId(UUID id, UUID topicId);
}
