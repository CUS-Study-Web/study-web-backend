package studyweb.cus.service.flashcard.impl;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import studyweb.cus.exception.flashcard.FlashcardErrorCode;
import studyweb.cus.exception.flashcard.FlashcardException;
import studyweb.cus.mapper.flashcard.FlashcardMapper;
import studyweb.cus.repository.flashcard.FlashcardRepository;
import studyweb.cus.repository.flashcard.FlashcardTopicRepository;
import studyweb.cus.repository.user.UserRepository;
import studyweb.cus.service.flashcard.FlashcardService;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlashcardServiceImpl implements FlashcardService {

  private final FlashcardTopicRepository flashcardTopicRepository;
  private final FlashcardRepository flashcardRepository;
  private final UserRepository userRepository;
  private final FlashcardMapper flashcardMapper;

  @Override
  @Transactional(readOnly = true)
  public FlashcardMetricsResponse getMetrics() {
    log.info("Calculating flashcard metrics");
    long totalTopics = flashcardTopicRepository.countByDeletedAtIsNull();
    long totalWords = flashcardTopicRepository.countTotalWords();
    long activeTopics =
        flashcardTopicRepository.countByDeletedAtIsNullAndStatus(CourseCreateStatus.PUBLISH);
    return new FlashcardMetricsResponse(totalTopics, totalWords, activeTopics);
  }

  @Override
  @Transactional
  public FlashcardTopicResponse createTopic(CreateFlashcardTopicRequest request, String userEmail) {
    log.info("Creating flashcard topic '{}' by user '{}'", request.title(), userEmail);
    if (request.title() == null || request.title().trim().isEmpty()) {
      throw new FlashcardException(FlashcardErrorCode.TOPIC_TITLE_EMPTY);
    }

    User user = findUserByEmail(userEmail);
    FlashcardTopic topic =
        FlashcardTopic.builder()
            .title(request.title().trim())
            .description(request.description())
            .status(request.status() != null ? request.status() : CourseCreateStatus.DRAFT)
            .numWords(0)
            .updatedBy(user)
            .build();

    FlashcardTopic savedTopic = flashcardTopicRepository.save(topic);
    log.info("Flashcard topic created with ID {}", savedTopic.getId());
    return flashcardMapper.toTopicResponse(savedTopic);
  }

  @Override
  @Transactional
  public FlashcardTopicResponse updateTopic(
      UUID topicId, UpdateFlashcardTopicRequest request, String userEmail) {
    log.info("Updating flashcard topic ID {} by user '{}'", topicId, userEmail);
    FlashcardTopic topic = requireTopic(topicId);

    if (request.title() != null) {
      if (request.title().trim().isEmpty()) {
        throw new FlashcardException(FlashcardErrorCode.TOPIC_TITLE_EMPTY);
      }
      topic.setTitle(request.title().trim());
    }

    if (request.description() != null) {
      topic.setDescription(request.description());
    }

    if (request.status() != null) {
      topic.setStatus(request.status());
    }

    User user = findUserByEmail(userEmail);
    if (user != null) {
      topic.setUpdatedBy(user);
    }

    FlashcardTopic updatedTopic = flashcardTopicRepository.save(topic);
    log.info("Flashcard topic ID {} updated successfully", topicId);
    return flashcardMapper.toTopicResponse(updatedTopic);
  }

  @Override
  @Transactional
  public void deleteTopic(UUID topicId, String userEmail) {
    log.info("Soft-deleting flashcard topic ID {} by user '{}'", topicId, userEmail);
    FlashcardTopic topic = requireTopic(topicId);
    topic.setDeletedAt(LocalDateTime.now());

    User user = findUserByEmail(userEmail);
    if (user != null) {
      topic.setUpdatedBy(user);
    }

    flashcardTopicRepository.save(topic);
    log.info("Flashcard topic ID {} soft-deleted successfully", topicId);
  }

  @Override
  @Transactional(readOnly = true)
  public FlashcardTopicResponse getTopicDetail(UUID topicId) {
    log.info("Fetching flashcard topic detail for ID {}", topicId);
    FlashcardTopic topic = requireTopic(topicId);
    return flashcardMapper.toTopicResponse(topic);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<FlashcardTopicResponse> listTopics(
      String search, CourseCreateStatus status, Pageable pageable) {
    log.info("Listing flashcard topics: search='{}', status='{}'", search, status);
    Specification<FlashcardTopic> spec =
        (root, query, cb) -> {
          List<Predicate> predicates = new ArrayList<>();
          predicates.add(cb.isNull(root.get("deletedAt")));

          if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
          }

          if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            predicates.add(
                cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)));
          }

          return cb.and(predicates.toArray(new Predicate[0]));
        };

    return flashcardTopicRepository.findAll(spec, pageable).map(flashcardMapper::toTopicResponse);
  }

  @Override
  @Transactional
  public FlashcardResponse createFlashcard(
      UUID topicId, CreateFlashcardRequest request, String userEmail) {
    log.info("Adding flashcard to topic ID {} by user '{}'", topicId, userEmail);
    FlashcardTopic topic = requireTopic(topicId);

    if (request.word() == null || request.word().trim().isEmpty()) {
      throw new FlashcardException(FlashcardErrorCode.FLASHCARD_WORD_EMPTY);
    }
    if (request.meaning() == null || request.meaning().trim().isEmpty()) {
      throw new FlashcardException(FlashcardErrorCode.FLASHCARD_MEANING_EMPTY);
    }

    User user = findUserByEmail(userEmail);
    Flashcard card =
        Flashcard.builder()
            .topic(topic)
            .word(request.word().trim())
            .meaning(request.meaning().trim())
            .pronunciation(request.pronunciation())
            .partOfSpeech(request.partOfSpeech())
            .updatedBy(user)
            .build();

    Flashcard savedCard = flashcardRepository.save(card);

    // Update topic numWords
    topic.setNumWords((topic.getNumWords() != null ? topic.getNumWords() : 0) + 1);
    if (user != null) {
      topic.setUpdatedBy(user);
    }
    flashcardTopicRepository.save(topic);

    log.info("Flashcard created with ID {} in topic ID {}", savedCard.getId(), topicId);
    return flashcardMapper.toFlashcardResponse(savedCard);
  }

  @Override
  @Transactional
  public FlashcardResponse updateFlashcard(
      UUID topicId, UUID cardId, UpdateFlashcardRequest request, String userEmail) {
    log.info("Updating flashcard ID {} in topic ID {} by user '{}'", cardId, topicId, userEmail);
    requireTopic(topicId);
    Flashcard card = requireFlashcard(cardId, topicId);

    if (request.word() != null) {
      if (request.word().trim().isEmpty()) {
        throw new FlashcardException(FlashcardErrorCode.FLASHCARD_WORD_EMPTY);
      }
      card.setWord(request.word().trim());
    }

    if (request.meaning() != null) {
      if (request.meaning().trim().isEmpty()) {
        throw new FlashcardException(FlashcardErrorCode.FLASHCARD_MEANING_EMPTY);
      }
      card.setMeaning(request.meaning().trim());
    }

    if (request.pronunciation() != null) {
      card.setPronunciation(request.pronunciation());
    }

    if (request.partOfSpeech() != null) {
      card.setPartOfSpeech(request.partOfSpeech());
    }

    User user = findUserByEmail(userEmail);
    if (user != null) {
      card.setUpdatedBy(user);
    }

    Flashcard updatedCard = flashcardRepository.save(card);
    log.info("Flashcard ID {} updated successfully", cardId);
    return flashcardMapper.toFlashcardResponse(updatedCard);
  }

  @Override
  @Transactional
  public void deleteFlashcard(UUID topicId, UUID cardId, String userEmail) {
    log.info("Deleting flashcard ID {} from topic ID {} by user '{}'", cardId, topicId, userEmail);
    FlashcardTopic topic = requireTopic(topicId);
    Flashcard card = requireFlashcard(cardId, topicId);

    flashcardRepository.delete(card);

    User user = findUserByEmail(userEmail);
    topic.setNumWords(Math.max(0, (topic.getNumWords() != null ? topic.getNumWords() : 1) - 1));
    if (user != null) {
      topic.setUpdatedBy(user);
    }
    flashcardTopicRepository.save(topic);

    log.info("Flashcard ID {} deleted successfully", cardId);
  }

  @Override
  @Transactional(readOnly = true)
  public FlashcardResponse getFlashcardDetail(UUID topicId, UUID cardId) {
    log.info("Fetching flashcard ID {} in topic ID {}", cardId, topicId);
    requireTopic(topicId);
    Flashcard card = requireFlashcard(cardId, topicId);
    return flashcardMapper.toFlashcardResponse(card);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<FlashcardResponse> listFlashcards(UUID topicId, String search, Pageable pageable) {
    log.info("Listing flashcards for topic ID {}: search='{}'", topicId, search);
    requireTopic(topicId);

    Specification<Flashcard> spec =
        (root, query, cb) -> {
          List<Predicate> predicates = new ArrayList<>();
          predicates.add(cb.equal(root.get("topic").get("id"), topicId));

          if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            predicates.add(
                cb.or(
                    cb.like(cb.lower(root.get("word")), pattern),
                    cb.like(cb.lower(root.get("meaning")), pattern)));
          }

          return cb.and(predicates.toArray(new Predicate[0]));
        };

    return flashcardRepository.findAll(spec, pageable).map(flashcardMapper::toFlashcardResponse);
  }

  private FlashcardTopic requireTopic(UUID id) {
    return flashcardTopicRepository
        .findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new FlashcardException(FlashcardErrorCode.TOPIC_NOT_FOUND));
  }

  private Flashcard requireFlashcard(UUID id, UUID topicId) {
    return flashcardRepository
        .findByIdAndTopicId(id, topicId)
        .orElseThrow(() -> new FlashcardException(FlashcardErrorCode.FLASHCARD_NOT_FOUND));
  }

  private User findUserByEmail(String email) {
    if (email == null || email.isBlank()) {
      return null;
    }
    return userRepository.findByGmail(email).orElse(null);
  }
}
