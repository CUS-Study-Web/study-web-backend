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
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import studyweb.cus.dto.request.flashcard.CreateFlashcardRequest;
import studyweb.cus.dto.request.flashcard.CreateFlashcardTopicRequest;
import studyweb.cus.dto.request.flashcard.UpdateFlashcardRequest;
import studyweb.cus.dto.request.flashcard.UpdateFlashcardTopicRequest;
import studyweb.cus.dto.response.flashcard.FlashcardMetricsResponse;
import studyweb.cus.dto.response.flashcard.FlashcardResponse;
import studyweb.cus.dto.response.flashcard.FlashcardTopicResponse;
import studyweb.cus.entity.flashcard.Flashcard;
import studyweb.cus.entity.flashcard.FlashcardTopic;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.CourseCreateStatus;
import studyweb.cus.enums.UserRole;
import studyweb.cus.exception.flashcard.FlashcardErrorCode;
import studyweb.cus.exception.flashcard.FlashcardException;
import studyweb.cus.mapper.flashcard.FlashcardMapper;
import studyweb.cus.repository.flashcard.FlashcardRepository;
import studyweb.cus.repository.flashcard.FlashcardTopicRepository;
import studyweb.cus.repository.user.UserRepository;
import studyweb.cus.service.flashcard.impl.FlashcardServiceImpl;

@ExtendWith(MockitoExtension.class)
class FlashcardServiceTest {

  @Mock private FlashcardTopicRepository flashcardTopicRepository;
  @Mock private FlashcardRepository flashcardRepository;
  @Mock private UserRepository userRepository;

  private final FlashcardMapper flashcardMapper = Mappers.getMapper(FlashcardMapper.class);
  private FlashcardService flashcardService;

  private User assistantUser;
  private FlashcardTopic sampleTopic;
  private Flashcard sampleCard;

  @BeforeEach
  void setUp() {
    flashcardService =
        new FlashcardServiceImpl(
            flashcardTopicRepository, flashcardRepository, userRepository, flashcardMapper);

    assistantUser =
        User.builder()
            .gmail("assistant@studyweb.edu")
            .name("Assistant User")
            .role(UserRole.ASSISTANT)
            .build();
    assistantUser.setId(UUID.randomUUID());

    sampleTopic =
        FlashcardTopic.builder()
            .title("IELTS Vocabulary Band 8+")
            .description("High frequency academic vocabulary")
            .numWords(1)
            .status(CourseCreateStatus.PUBLISH)
            .updatedBy(assistantUser)
            .build();
    sampleTopic.setId(UUID.randomUUID());

    sampleCard =
        Flashcard.builder()
            .topic(sampleTopic)
            .word("Eloquent")
            .meaning("Fluent or persuasive in speaking or writing")
            .pronunciation("/ˈel.ə.kwənt/")
            .partOfSpeech("Adjective")
            .updatedBy(assistantUser)
            .build();
    sampleCard.setId(UUID.randomUUID());
  }

  @Nested
  @DisplayName("Metrics Tests")
  class MetricsTests {

    @Test
    @DisplayName("Should calculate metrics correctly")
    void testGetMetrics() {
      when(flashcardTopicRepository.countByDeletedAtIsNull()).thenReturn(5L);
      when(flashcardTopicRepository.countTotalWords()).thenReturn(350L);
      when(flashcardTopicRepository.countByDeletedAtIsNullAndStatus(CourseCreateStatus.PUBLISH))
          .thenReturn(3L);

      FlashcardMetricsResponse metrics = flashcardService.getMetrics();

      assertThat(metrics.totalTopics()).isEqualTo(5L);
      assertThat(metrics.totalWords()).isEqualTo(350L);
      assertThat(metrics.activeTopics()).isEqualTo(3L);
    }
  }

  @Nested
  @DisplayName("Topic Management Tests")
  class TopicTests {

    @Test
    @DisplayName("Should create topic successfully")
    void testCreateTopic() {
      CreateFlashcardTopicRequest request =
          new CreateFlashcardTopicRequest(
              "IELTS Vocab", "Topic description", CourseCreateStatus.DRAFT);
      when(userRepository.findByGmail("assistant@studyweb.edu"))
          .thenReturn(Optional.of(assistantUser));
      when(flashcardTopicRepository.save(any(FlashcardTopic.class))).thenReturn(sampleTopic);

      FlashcardTopicResponse response =
          flashcardService.createTopic(request, "assistant@studyweb.edu");

      assertThat(response).isNotNull();
      assertThat(response.title()).isEqualTo(sampleTopic.getTitle());
      verify(flashcardTopicRepository).save(any(FlashcardTopic.class));
    }

    @Test
    @DisplayName("Should throw when creating topic with empty title")
    void testCreateTopicEmptyTitle() {
      CreateFlashcardTopicRequest request =
          new CreateFlashcardTopicRequest("", "Topic description", CourseCreateStatus.DRAFT);

      assertThatThrownBy(() -> flashcardService.createTopic(request, "assistant@studyweb.edu"))
          .isInstanceOf(FlashcardException.class)
          .extracting(e -> ((FlashcardException) e).getCode())
          .isEqualTo(FlashcardErrorCode.TOPIC_TITLE_EMPTY.code());
    }

    @Test
    @DisplayName("Should update topic successfully")
    void testUpdateTopic() {
      UUID topicId = sampleTopic.getId();
      UpdateFlashcardTopicRequest request =
          new UpdateFlashcardTopicRequest(
              "Updated Title", "Updated Description", CourseCreateStatus.PUBLISH);

      when(flashcardTopicRepository.findByIdAndDeletedAtIsNull(topicId))
          .thenReturn(Optional.of(sampleTopic));
      when(userRepository.findByGmail("assistant@studyweb.edu"))
          .thenReturn(Optional.of(assistantUser));
      when(flashcardTopicRepository.save(any(FlashcardTopic.class))).thenReturn(sampleTopic);

      FlashcardTopicResponse response =
          flashcardService.updateTopic(topicId, request, "assistant@studyweb.edu");

      assertThat(response).isNotNull();
      verify(flashcardTopicRepository).save(sampleTopic);
    }

    @Test
    @DisplayName("Should throw when updating topic with empty title")
    void testUpdateTopicEmptyTitle() {
      UUID topicId = sampleTopic.getId();
      UpdateFlashcardTopicRequest request =
          new UpdateFlashcardTopicRequest("  ", "Desc", CourseCreateStatus.DRAFT);

      when(flashcardTopicRepository.findByIdAndDeletedAtIsNull(topicId))
          .thenReturn(Optional.of(sampleTopic));

      assertThatThrownBy(
              () -> flashcardService.updateTopic(topicId, request, "assistant@studyweb.edu"))
          .isInstanceOf(FlashcardException.class)
          .extracting(e -> ((FlashcardException) e).getCode())
          .isEqualTo(FlashcardErrorCode.TOPIC_TITLE_EMPTY.code());
    }

    @Test
    @DisplayName("Should soft delete topic successfully")
    void testDeleteTopic() {
      UUID topicId = sampleTopic.getId();
      when(flashcardTopicRepository.findByIdAndDeletedAtIsNull(topicId))
          .thenReturn(Optional.of(sampleTopic));
      when(userRepository.findByGmail("assistant@studyweb.edu"))
          .thenReturn(Optional.of(assistantUser));

      flashcardService.deleteTopic(topicId, "assistant@studyweb.edu");

      assertThat(sampleTopic.getDeletedAt()).isNotNull();
      verify(flashcardTopicRepository).save(sampleTopic);
    }

    @Test
    @DisplayName("Should get topic detail")
    void testGetTopicDetail() {
      UUID topicId = sampleTopic.getId();
      when(flashcardTopicRepository.findByIdAndDeletedAtIsNull(topicId))
          .thenReturn(Optional.of(sampleTopic));

      FlashcardTopicResponse response = flashcardService.getTopicDetail(topicId);

      assertThat(response.id()).isEqualTo(sampleTopic.getId());
      assertThat(response.title()).isEqualTo(sampleTopic.getTitle());
    }

    @Test
    @DisplayName("Should list topics with pagination")
    void testListTopics() {
      Pageable pageable = PageRequest.of(0, 10);
      Page<FlashcardTopic> page = new PageImpl<>(List.of(sampleTopic), pageable, 1);
      when(flashcardTopicRepository.findAll(any(Specification.class), eq(pageable)))
          .thenReturn(page);

      Page<FlashcardTopicResponse> result =
          flashcardService.listTopics("ielts", CourseCreateStatus.PUBLISH, pageable);

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().get(0).title()).isEqualTo(sampleTopic.getTitle());
    }
  }

  @Nested
  @DisplayName("Flashcard CRUD Tests")
  class FlashcardCrudTests {

    @Test
    @DisplayName("Should create flashcard and increment numWords on topic")
    void testCreateFlashcard() {
      UUID topicId = sampleTopic.getId();
      CreateFlashcardRequest request =
          new CreateFlashcardRequest(
              "Ubiquitous", "Present everywhere", "/juːˈbɪk.wə.təs/", "Adjective");

      when(flashcardTopicRepository.findByIdAndDeletedAtIsNull(topicId))
          .thenReturn(Optional.of(sampleTopic));
      when(userRepository.findByGmail("assistant@studyweb.edu"))
          .thenReturn(Optional.of(assistantUser));
      when(flashcardRepository.save(any(Flashcard.class))).thenReturn(sampleCard);

      FlashcardResponse response =
          flashcardService.createFlashcard(topicId, request, "assistant@studyweb.edu");

      assertThat(response).isNotNull();
      assertThat(sampleTopic.getNumWords()).isEqualTo(2);
      verify(flashcardTopicRepository).save(sampleTopic);
      verify(flashcardRepository).save(any(Flashcard.class));
    }

    @Test
    @DisplayName("Should throw when creating flashcard with blank word or meaning")
    void testCreateFlashcardValidation() {
      UUID topicId = sampleTopic.getId();
      when(flashcardTopicRepository.findByIdAndDeletedAtIsNull(topicId))
          .thenReturn(Optional.of(sampleTopic));

      CreateFlashcardRequest reqEmptyWord =
          new CreateFlashcardRequest("", "meaning", null, null);
      assertThatThrownBy(
              () ->
                  flashcardService.createFlashcard(
                      topicId, reqEmptyWord, "assistant@studyweb.edu"))
          .isInstanceOf(FlashcardException.class)
          .extracting(e -> ((FlashcardException) e).getCode())
          .isEqualTo(FlashcardErrorCode.FLASHCARD_WORD_EMPTY.code());

      CreateFlashcardRequest reqEmptyMeaning =
          new CreateFlashcardRequest("word", "  ", null, null);
      assertThatThrownBy(
              () ->
                  flashcardService.createFlashcard(
                      topicId, reqEmptyMeaning, "assistant@studyweb.edu"))
          .isInstanceOf(FlashcardException.class)
          .extracting(e -> ((FlashcardException) e).getCode())
          .isEqualTo(FlashcardErrorCode.FLASHCARD_MEANING_EMPTY.code());
    }

    @Test
    @DisplayName("Should update flashcard successfully")
    void testUpdateFlashcard() {
      UUID topicId = sampleTopic.getId();
      UUID cardId = sampleCard.getId();
      UpdateFlashcardRequest request =
          new UpdateFlashcardRequest("Articulate", "Express clearly", null, "Verb");

      when(flashcardTopicRepository.findByIdAndDeletedAtIsNull(topicId))
          .thenReturn(Optional.of(sampleTopic));
      when(flashcardRepository.findByIdAndTopicId(cardId, topicId))
          .thenReturn(Optional.of(sampleCard));
      when(userRepository.findByGmail("assistant@studyweb.edu"))
          .thenReturn(Optional.of(assistantUser));
      when(flashcardRepository.save(any(Flashcard.class))).thenReturn(sampleCard);

      FlashcardResponse response =
          flashcardService.updateFlashcard(topicId, cardId, request, "assistant@studyweb.edu");

      assertThat(response).isNotNull();
      verify(flashcardRepository).save(sampleCard);
    }

    @Test
    @DisplayName("Should delete flashcard and decrement numWords on topic")
    void testDeleteFlashcard() {
      UUID topicId = sampleTopic.getId();
      UUID cardId = sampleCard.getId();
      sampleTopic.setNumWords(3);

      when(flashcardTopicRepository.findByIdAndDeletedAtIsNull(topicId))
          .thenReturn(Optional.of(sampleTopic));
      when(flashcardRepository.findByIdAndTopicId(cardId, topicId))
          .thenReturn(Optional.of(sampleCard));
      when(userRepository.findByGmail("assistant@studyweb.edu"))
          .thenReturn(Optional.of(assistantUser));

      flashcardService.deleteFlashcard(topicId, cardId, "assistant@studyweb.edu");

      assertThat(sampleTopic.getNumWords()).isEqualTo(2);
      verify(flashcardRepository).delete(sampleCard);
      verify(flashcardTopicRepository).save(sampleTopic);
    }

    @Test
    @DisplayName("Should get flashcard detail")
    void testGetFlashcardDetail() {
      UUID topicId = sampleTopic.getId();
      UUID cardId = sampleCard.getId();

      when(flashcardTopicRepository.findByIdAndDeletedAtIsNull(topicId))
          .thenReturn(Optional.of(sampleTopic));
      when(flashcardRepository.findByIdAndTopicId(cardId, topicId))
          .thenReturn(Optional.of(sampleCard));

      FlashcardResponse response = flashcardService.getFlashcardDetail(topicId, cardId);

      assertThat(response.id()).isEqualTo(sampleCard.getId());
      assertThat(response.word()).isEqualTo(sampleCard.getWord());
    }

    @Test
    @DisplayName("Should list flashcards with pagination")
    void testListFlashcards() {
      UUID topicId = sampleTopic.getId();
      Pageable pageable = PageRequest.of(0, 20);
      Page<Flashcard> page = new PageImpl<>(List.of(sampleCard), pageable, 1);

      when(flashcardTopicRepository.findByIdAndDeletedAtIsNull(topicId))
          .thenReturn(Optional.of(sampleTopic));
      when(flashcardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

      Page<FlashcardResponse> result =
          flashcardService.listFlashcards(topicId, "eloquent", pageable);

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().get(0).word()).isEqualTo("Eloquent");
    }
  }
}
