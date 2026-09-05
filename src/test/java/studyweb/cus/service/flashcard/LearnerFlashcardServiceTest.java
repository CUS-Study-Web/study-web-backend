package studyweb.cus.service.flashcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import studyweb.cus.dto.request.flashcard.UpdateLearnerProgressRequest;
import studyweb.cus.dto.response.flashcard.LearnerCardProgressResponse;
import studyweb.cus.dto.response.flashcard.LearnerFlashcardItemResponse;
import studyweb.cus.dto.response.flashcard.LearnerFlashcardMetricsResponse;
import studyweb.cus.dto.response.flashcard.LearnerFlashcardTopicResponse;
import studyweb.cus.dto.response.flashcard.LearnerTopicDetailResponse;
import studyweb.cus.entity.flashcard.Flashcard;
import studyweb.cus.entity.flashcard.FlashcardTopic;
import studyweb.cus.entity.progress.UserFlashcardProgress;
import studyweb.cus.entity.progress.UserTopicProgress;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.CourseCreateStatus;
import studyweb.cus.enums.FlashcardProgressStatus;
import studyweb.cus.enums.UserRole;
import studyweb.cus.exception.flashcard.FlashcardErrorCode;
import studyweb.cus.exception.flashcard.FlashcardException;
import studyweb.cus.repository.flashcard.FlashcardRepository;
import studyweb.cus.repository.flashcard.FlashcardTopicRepository;
import studyweb.cus.repository.flashcard.UserFlashcardProgressRepository;
import studyweb.cus.repository.flashcard.UserTopicProgressRepository;
import studyweb.cus.repository.user.UserRepository;
import studyweb.cus.service.flashcard.impl.LearnerFlashcardServiceImpl;

@ExtendWith(MockitoExtension.class)
class LearnerFlashcardServiceTest {

  @Mock private FlashcardTopicRepository flashcardTopicRepository;
  @Mock private FlashcardRepository flashcardRepository;
  @Mock private UserFlashcardProgressRepository userFlashcardProgressRepository;
  @Mock private UserTopicProgressRepository userTopicProgressRepository;
  @Mock private UserRepository userRepository;

  private LearnerFlashcardService learnerFlashcardService;

  private User learnerUser;
  private FlashcardTopic publishedTopic;
  private Flashcard card1;
  private Flashcard card2;

  @BeforeEach
  void setUp() {
    learnerFlashcardService =
        new LearnerFlashcardServiceImpl(
            flashcardTopicRepository,
            flashcardRepository,
            userFlashcardProgressRepository,
            userTopicProgressRepository,
            userRepository);

    learnerUser =
        User.builder()
            .gmail("learner@studyweb.edu")
            .name("Learner User")
            .role(UserRole.LEARNER)
            .build();
    learnerUser.setId(UUID.randomUUID());

    publishedTopic =
        FlashcardTopic.builder()
            .title("Từ vựng cốt lõi ĐGNL")
            .description("Core words")
            .numWords(50)
            .status(CourseCreateStatus.PUBLISH)
            .build();
    publishedTopic.setId(UUID.randomUUID());

    card1 =
        Flashcard.builder()
            .topic(publishedTopic)
            .word("Perseverance")
            .pronunciation("/ˌpɜː.sɪˈvɪə.rəns/")
            .partOfSpeech("Noun")
            .meaning("Sự kiên trì, bền bỉ")
            .build();
    card1.setId(UUID.randomUUID());

    card2 =
        Flashcard.builder()
            .topic(publishedTopic)
            .word("Diligent")
            .pronunciation("/ˈdɪl.ɪ.dʒənt/")
            .partOfSpeech("Adjective")
            .meaning("Chăm chỉ, cần cù")
            .build();
    card2.setId(UUID.randomUUID());
  }

  @Nested
  @DisplayName("Learner Metrics Tests")
  class MetricsTests {

    @Test
    @DisplayName("Should return learner metrics correctly")
    void testGetMetrics() {
      when(userRepository.findByGmail("learner@studyweb.edu"))
          .thenReturn(Optional.of(learnerUser));
      when(flashcardTopicRepository.countByDeletedAtIsNullAndStatus(CourseCreateStatus.PUBLISH))
          .thenReturn(6L);
      when(flashcardTopicRepository.countPublishedTotalWords()).thenReturn(440L);
      when(userFlashcardProgressRepository.countRememberedWordsByUserId(learnerUser.getId()))
          .thenReturn(214L);

      LearnerFlashcardMetricsResponse metrics =
          learnerFlashcardService.getMetrics("learner@studyweb.edu");

      assertThat(metrics.totalTopics()).isEqualTo(6L);
      assertThat(metrics.totalWords()).isEqualTo(440L);
      assertThat(metrics.totalRememberedWords()).isEqualTo(214L);
    }
  }

  @Nested
  @DisplayName("Topic Catalog Tests")
  class TopicCatalogTests {

    @Test
    @DisplayName("Should list published topics with progress")
    void testListTopics() {
      Pageable pageable = PageRequest.of(0, 10);
      Page<FlashcardTopic> topicPage =
          new PageImpl<>(List.of(publishedTopic), pageable, 1);

      UserTopicProgress topicProgress =
          UserTopicProgress.builder()
              .user(learnerUser)
              .topic(publishedTopic)
              .learnedWords(32)
              .build();

      when(userRepository.findByGmail("learner@studyweb.edu"))
          .thenReturn(Optional.of(learnerUser));
      when(flashcardTopicRepository.findAll(any(Specification.class), eq(pageable)))
          .thenReturn(topicPage);
      when(userTopicProgressRepository.findByUserIdAndTopicIdIn(
              eq(learnerUser.getId()), any()))
          .thenReturn(List.of(topicProgress));

      Page<LearnerFlashcardTopicResponse> response =
          learnerFlashcardService.listTopics("learner@studyweb.edu", null, pageable);

      assertThat(response.getContent()).hasSize(1);
      LearnerFlashcardTopicResponse item = response.getContent().get(0);
      assertThat(item.title()).isEqualTo("Từ vựng cốt lõi ĐGNL");
      assertThat(item.numWords()).isEqualTo(50);
      assertThat(item.learnedWords()).isEqualTo(32);
      assertThat(item.progressPercent()).isEqualTo(64);
      assertThat(item.isCompleted()).isFalse();
    }
  }

  @Nested
  @DisplayName("Topic Detail Tests")
  class TopicDetailTests {

    @Test
    @DisplayName("Should return topic detail and learner stats")
    void testGetTopicDetail() {
      UUID topicId = publishedTopic.getId();
      when(userRepository.findByGmail("learner@studyweb.edu"))
          .thenReturn(Optional.of(learnerUser));
      when(flashcardTopicRepository.findByIdAndDeletedAtIsNull(topicId))
          .thenReturn(Optional.of(publishedTopic));
      when(userFlashcardProgressRepository.countByUserIdAndTopicIdAndStatus(
              learnerUser.getId(), topicId, FlashcardProgressStatus.REMEMBER))
          .thenReturn(32L);

      LearnerTopicDetailResponse response =
          learnerFlashcardService.getTopicDetail(topicId, "learner@studyweb.edu");

      assertThat(response.totalWords()).isEqualTo(50);
      assertThat(response.rememberedWords()).isEqualTo(32);
      assertThat(response.studyWords()).isEqualTo(18);
      assertThat(response.progressPercent()).isEqualTo(64);
    }
  }

  @Nested
  @DisplayName("Topic Words & Study Tests")
  class WordsAndStudyTests {

    @Test
    @DisplayName("Should list topic words with ALL filter")
    void testListTopicWords_All() {
      UUID topicId = publishedTopic.getId();
      Pageable pageable = PageRequest.of(0, 20);
      Page<Flashcard> cardsPage = new PageImpl<>(List.of(card1, card2), pageable, 2);

      UserFlashcardProgress progress1 =
          UserFlashcardProgress.builder()
              .user(learnerUser)
              .flashcard(card1)
              .status(FlashcardProgressStatus.REMEMBER)
              .build();

      when(userRepository.findByGmail("learner@studyweb.edu"))
          .thenReturn(Optional.of(learnerUser));
      when(flashcardTopicRepository.findByIdAndDeletedAtIsNull(topicId))
          .thenReturn(Optional.of(publishedTopic));
      when(userFlashcardProgressRepository.findByUserIdAndFlashcardTopicId(
              learnerUser.getId(), topicId))
          .thenReturn(List.of(progress1));
      when(flashcardRepository.findAll(any(Specification.class), eq(pageable)))
          .thenReturn(cardsPage);

      Page<LearnerFlashcardItemResponse> result =
          learnerFlashcardService.listTopicWords(
              topicId, "learner@studyweb.edu", "ALL", null, pageable);

      assertThat(result.getContent()).hasSize(2);
      assertThat(result.getContent().get(0).status()).isEqualTo(FlashcardProgressStatus.REMEMBER);
      assertThat(result.getContent().get(1).status()).isEqualTo(FlashcardProgressStatus.STUDY);
    }

    @Test
    @DisplayName("Should get study cards for LEARN phase")
    void testGetStudyCards_LearnPhase() {
      UUID topicId = publishedTopic.getId();
      when(userRepository.findByGmail("learner@studyweb.edu"))
          .thenReturn(Optional.of(learnerUser));
      when(flashcardTopicRepository.findByIdAndDeletedAtIsNull(topicId))
          .thenReturn(Optional.of(publishedTopic));
      when(flashcardRepository.findAll(any(Specification.class), any(Sort.class)))
          .thenReturn(List.of(card1, card2));
      when(userFlashcardProgressRepository.findByUserIdAndFlashcardTopicId(
              learnerUser.getId(), topicId))
          .thenReturn(List.of());

      List<LearnerFlashcardItemResponse> studyCards =
          learnerFlashcardService.getStudyCards(topicId, "learner@studyweb.edu", "LEARN");

      assertThat(studyCards).hasSize(2);
    }

    @Test
    @DisplayName("Should get study cards for REVIEW phase (only STUDY status)")
    void testGetStudyCards_ReviewPhase() {
      UUID topicId = publishedTopic.getId();
      UserFlashcardProgress progress1 =
          UserFlashcardProgress.builder()
              .user(learnerUser)
              .flashcard(card1)
              .status(FlashcardProgressStatus.REMEMBER)
              .build();

      when(userRepository.findByGmail("learner@studyweb.edu"))
          .thenReturn(Optional.of(learnerUser));
      when(flashcardTopicRepository.findByIdAndDeletedAtIsNull(topicId))
          .thenReturn(Optional.of(publishedTopic));
      when(flashcardRepository.findAll(any(Specification.class), any(Sort.class)))
          .thenReturn(List.of(card1, card2));
      when(userFlashcardProgressRepository.findByUserIdAndFlashcardTopicId(
              learnerUser.getId(), topicId))
          .thenReturn(List.of(progress1));

      List<LearnerFlashcardItemResponse> studyCards =
          learnerFlashcardService.getStudyCards(topicId, "learner@studyweb.edu", "REVIEW");

      // card1 is REMEMBER, so only card2 (STUDY) is returned in REVIEW phase
      assertThat(studyCards).hasSize(1);
      assertThat(studyCards.get(0).id()).isEqualTo(card2.getId());
    }
  }

  @Nested
  @DisplayName("Update Progress Tests")
  class UpdateProgressTests {

    @Test
    @DisplayName("Should update card learning progress and sync topic progress")
    void testUpdateCardProgress() {
      UUID topicId = publishedTopic.getId();
      UUID cardId = card1.getId();
      UpdateLearnerProgressRequest request =
          new UpdateLearnerProgressRequest(FlashcardProgressStatus.REMEMBER);

      when(userRepository.findByGmail("learner@studyweb.edu"))
          .thenReturn(Optional.of(learnerUser));
      when(flashcardTopicRepository.findByIdAndDeletedAtIsNull(topicId))
          .thenReturn(Optional.of(publishedTopic));
      when(flashcardRepository.findByIdAndTopicId(cardId, topicId))
          .thenReturn(Optional.of(card1));
      when(userFlashcardProgressRepository.findByUserIdAndFlashcardId(learnerUser.getId(), cardId))
          .thenReturn(Optional.empty());
      when(userFlashcardProgressRepository.countByUserIdAndTopicIdAndStatus(
              learnerUser.getId(), topicId, FlashcardProgressStatus.REMEMBER))
          .thenReturn(1L);
      when(userTopicProgressRepository.findByUserIdAndTopicId(learnerUser.getId(), topicId))
          .thenReturn(Optional.empty());

      LearnerCardProgressResponse response =
          learnerFlashcardService.updateCardProgress(
              topicId, cardId, "learner@studyweb.edu", request);

      assertThat(response.cardId()).isEqualTo(cardId);
      assertThat(response.status()).isEqualTo(FlashcardProgressStatus.REMEMBER);
      assertThat(response.topicLearnedWords()).isEqualTo(1);
      assertThat(response.topicTotalWords()).isEqualTo(50);
      assertThat(response.topicProgressPercent()).isEqualTo(2);

      verify(userFlashcardProgressRepository).save(any(UserFlashcardProgress.class));
      verify(userTopicProgressRepository).save(any(UserTopicProgress.class));
    }
  }
}
