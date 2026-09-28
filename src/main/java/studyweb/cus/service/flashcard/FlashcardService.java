package studyweb.cus.service.flashcard;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import studyweb.cus.dto.request.flashcard.CreateFlashcardRequest;
import studyweb.cus.dto.request.flashcard.CreateFlashcardTopicRequest;
import studyweb.cus.dto.request.flashcard.UpdateFlashcardRequest;
import studyweb.cus.dto.request.flashcard.UpdateFlashcardTopicRequest;
import studyweb.cus.dto.response.flashcard.FlashcardMetricsResponse;
import studyweb.cus.dto.response.flashcard.FlashcardResponse;
import studyweb.cus.dto.response.flashcard.FlashcardTopicResponse;
import studyweb.cus.enums.CourseCreateStatus;

public interface FlashcardService {

  FlashcardMetricsResponse getMetrics();

  FlashcardTopicResponse createTopic(CreateFlashcardTopicRequest request, String userEmail);

  FlashcardTopicResponse updateTopic(
      UUID topicId, UpdateFlashcardTopicRequest request, String userEmail);

  void deleteTopic(UUID topicId, String userEmail);

  FlashcardTopicResponse getTopicDetail(UUID topicId);

  Page<FlashcardTopicResponse> listTopics(
      String search, CourseCreateStatus status, Pageable pageable);

  FlashcardResponse createFlashcard(
      UUID topicId, CreateFlashcardRequest request, String userEmail);

  FlashcardResponse updateFlashcard(
      UUID topicId, UUID cardId, UpdateFlashcardRequest request, String userEmail);

  void deleteFlashcard(UUID topicId, UUID cardId, String userEmail);

  FlashcardResponse getFlashcardDetail(UUID topicId, UUID cardId);

  Page<FlashcardResponse> listFlashcards(UUID topicId, String search, Pageable pageable);
}
