package studyweb.cus.service.flashcard;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import studyweb.cus.dto.request.flashcard.UpdateLearnerProgressRequest;
import studyweb.cus.dto.response.flashcard.LearnerCardProgressResponse;
import studyweb.cus.dto.response.flashcard.LearnerFlashcardItemResponse;
import studyweb.cus.dto.response.flashcard.LearnerFlashcardMetricsResponse;
import studyweb.cus.dto.response.flashcard.LearnerFlashcardTopicResponse;
import studyweb.cus.dto.response.flashcard.LearnerTopicDetailResponse;

public interface LearnerFlashcardService {

  LearnerFlashcardMetricsResponse getMetrics(String email);

  Page<LearnerFlashcardTopicResponse> listTopics(
      String email, String search, Pageable pageable);

  LearnerTopicDetailResponse getTopicDetail(UUID topicId, String email);

  Page<LearnerFlashcardItemResponse> listTopicWords(
      UUID topicId, String email, String filterStatus, String search, Pageable pageable);

  List<LearnerFlashcardItemResponse> getStudyCards(
      UUID topicId, String email);

  LearnerCardProgressResponse updateCardProgress(
      UUID topicId, UUID cardId, String email, UpdateLearnerProgressRequest request);
}
