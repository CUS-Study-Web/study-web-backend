package studyweb.cus.service.flashcard.impl;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import studyweb.cus.exception.flashcard.FlashcardErrorCode;
import studyweb.cus.exception.flashcard.FlashcardException;
import studyweb.cus.exception.user.UserErrorCode;
import studyweb.cus.exception.user.UserException;
import studyweb.cus.repository.flashcard.FlashcardRepository;
import studyweb.cus.repository.flashcard.FlashcardTopicRepository;
import studyweb.cus.repository.flashcard.UserFlashcardProgressRepository;
import studyweb.cus.repository.flashcard.UserTopicProgressRepository;
import studyweb.cus.repository.user.UserRepository;
import studyweb.cus.service.flashcard.LearnerFlashcardService;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearnerFlashcardServiceImpl implements LearnerFlashcardService {

  private final FlashcardTopicRepository flashcardTopicRepository;
  private final FlashcardRepository flashcardRepository;
  private final UserFlashcardProgressRepository userFlashcardProgressRepository;
  private final UserTopicProgressRepository userTopicProgressRepository;
  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public LearnerFlashcardMetricsResponse getMetrics(String email) {
    log.info("Calculating learner flashcard metrics for user '{}'", email);
    User user = requireUser(email);

    long totalTopics =
        flashcardTopicRepository.countByDeletedAtIsNullAndStatus(CourseCreateStatus.PUBLISH);
    long totalWords = flashcardTopicRepository.countPublishedTotalWords();
    long totalRememberedWords =
        userFlashcardProgressRepository.countRememberedWordsByUserId(user.getId());

    return new LearnerFlashcardMetricsResponse(totalTopics, totalWords, totalRememberedWords);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<LearnerFlashcardTopicResponse> listTopics(
      String email, String search, Pageable pageable) {
    log.info("Listing learner flashcard topics: search='{}', user='{}'", search, email);
    User user = requireUser(email);

    Specification<FlashcardTopic> spec =
        (root, query, cb) -> {
          List<Predicate> predicates = new ArrayList<>();
          predicates.add(cb.isNull(root.get("deletedAt")));
          predicates.add(cb.equal(root.get("status"), CourseCreateStatus.PUBLISH));

          if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            predicates.add(
                cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)));
          }

          return cb.and(predicates.toArray(new Predicate[0]));
        };

    Page<FlashcardTopic> topicsPage = flashcardTopicRepository.findAll(spec, pageable);
    List<UUID> topicIds = topicsPage.map(FlashcardTopic::getId).toList();

    Map<UUID, Integer> learnedMap =
        topicIds.isEmpty()
            ? Collections.emptyMap()
            : userTopicProgressRepository
                .findByUserIdAndTopicIdIn(user.getId(), topicIds)
                .stream()
                .collect(
                    Collectors.toMap(
                        p -> p.getTopic().getId(), UserTopicProgress::getLearnedWords, (a, b) -> a));

    return topicsPage.map(
        topic -> {
          int numWords = topic.getNumWords() != null ? topic.getNumWords() : 0;
          int learnedWords = learnedMap.getOrDefault(topic.getId(), 0);
          int progressPercent =
              numWords > 0 ? Math.min(100, (int) ((learnedWords * 100L) / numWords)) : 0;
          boolean isCompleted = numWords > 0 && learnedWords >= numWords;

          return new LearnerFlashcardTopicResponse(
              topic.getId(),
              topic.getTitle(),
              topic.getDescription(),
              numWords,
              learnedWords,
              progressPercent,
              isCompleted);
        });
  }

  @Override
  @Transactional(readOnly = true)
  public LearnerTopicDetailResponse getTopicDetail(UUID topicId, String email) {
    log.info("Fetching learner topic detail for ID {} by user '{}'", topicId, email);
    User user = requireUser(email);
    FlashcardTopic topic = requirePublishedTopic(topicId);

    int totalWords = topic.getNumWords() != null ? topic.getNumWords() : 0;
    long rememberedCount =
        userFlashcardProgressRepository.countByUserIdAndTopicIdAndStatus(
            user.getId(), topicId, FlashcardProgressStatus.REMEMBER);
    int rememberedWords = (int) rememberedCount;
    int studyWords = Math.max(0, totalWords - rememberedWords);
    int progressPercent =
        totalWords > 0 ? Math.min(100, (int) ((rememberedWords * 100L) / totalWords)) : 0;

    return new LearnerTopicDetailResponse(
        topic.getId(),
        topic.getTitle(),
        topic.getDescription(),
        totalWords,
        rememberedWords,
        studyWords,
        progressPercent);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<LearnerFlashcardItemResponse> listTopicWords(
      UUID topicId, String email, String filterStatus, String search, Pageable pageable) {
    log.info(
        "Listing topic words for topic ID {}, filterStatus='{}', search='{}', user='{}'",
        topicId,
        filterStatus,
        search,
        email);
    User user = requireUser(email);
    requirePublishedTopic(topicId);

    Map<UUID, FlashcardProgressStatus> progressMap =
        userFlashcardProgressRepository
            .findByUserIdAndFlashcardTopicId(user.getId(), topicId)
            .stream()
            .collect(
                Collectors.toMap(
                    p -> p.getFlashcard().getId(),
                    UserFlashcardProgress::getStatus,
                    (a, b) -> a));

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

    String normalizedFilter = filterStatus != null ? filterStatus.trim().toUpperCase() : "ALL";

    if ("ALL".equals(normalizedFilter)) {
      Page<Flashcard> cardsPage = flashcardRepository.findAll(spec, pageable);
      return cardsPage.map(
          c ->
              new LearnerFlashcardItemResponse(
                  c.getId(),
                  topicId,
                  c.getWord(),
                  c.getPronunciation(),
                  c.getPartOfSpeech(),
                  c.getMeaning(),
                  progressMap.getOrDefault(c.getId(), FlashcardProgressStatus.STUDY)));
    }

    // When filtering by REMEMBER or STUDY, filter all matching cards
    List<Flashcard> allMatchingCards =
        flashcardRepository.findAll(spec, Sort.by("createdAt").ascending());

    List<LearnerFlashcardItemResponse> filtered =
        allMatchingCards.stream()
            .map(
                c ->
                    new LearnerFlashcardItemResponse(
                        c.getId(),
                        topicId,
                        c.getWord(),
                        c.getPronunciation(),
                        c.getPartOfSpeech(),
                        c.getMeaning(),
                        progressMap.getOrDefault(c.getId(), FlashcardProgressStatus.STUDY)))
            .filter(
                item -> {
                  if ("REMEMBER".equals(normalizedFilter)) {
                    return item.status() == FlashcardProgressStatus.REMEMBER;
                  } else if ("STUDY".equals(normalizedFilter)) {
                    return item.status() == FlashcardProgressStatus.STUDY;
                  }
                  return true;
                })
            .toList();

    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), filtered.size());
    List<LearnerFlashcardItemResponse> subList =
        start < filtered.size() ? filtered.subList(start, end) : Collections.emptyList();

    return new PageImpl<>(subList, pageable, filtered.size());
  }

  @Override
  @Transactional(readOnly = true)
  public List<LearnerFlashcardItemResponse> getStudyCards(
      UUID topicId, String email, String phase) {
    log.info("Fetching study cards for topic ID {}, phase='{}', user='{}'", topicId, phase, email);
    User user = requireUser(email);
    requirePublishedTopic(topicId);

    Map<UUID, FlashcardProgressStatus> progressMap =
        userFlashcardProgressRepository
            .findByUserIdAndFlashcardTopicId(user.getId(), topicId)
            .stream()
            .collect(
                Collectors.toMap(
                    p -> p.getFlashcard().getId(),
                    UserFlashcardProgress::getStatus,
                    (a, b) -> a));

    Specification<Flashcard> spec =
        (root, query, cb) -> cb.equal(root.get("topic").get("id"), topicId);
    List<Flashcard> cards =
        flashcardRepository.findAll(spec, Sort.by("createdAt").ascending());

    String normalizedPhase = phase != null ? phase.trim().toUpperCase() : "LEARN";

    return cards.stream()
        .map(
            c ->
                new LearnerFlashcardItemResponse(
                    c.getId(),
                    topicId,
                    c.getWord(),
                    c.getPronunciation(),
                    c.getPartOfSpeech(),
                    c.getMeaning(),
                    progressMap.getOrDefault(c.getId(), FlashcardProgressStatus.STUDY)))
        .filter(
            item -> {
              if ("REVIEW".equals(normalizedPhase) || "PHASE_2".equals(normalizedPhase)) {
                return item.status() == FlashcardProgressStatus.STUDY;
              }
              return true;
            })
        .toList();
  }

  @Override
  @Transactional
  public LearnerCardProgressResponse updateCardProgress(
      UUID topicId, UUID cardId, String email, UpdateLearnerProgressRequest request) {
    log.info(
        "Updating card progress for card ID {} in topic ID {} to '{}' by user '{}'",
        cardId,
        topicId,
        request.status(),
        email);
    User user = requireUser(email);
    FlashcardTopic topic = requirePublishedTopic(topicId);
    Flashcard card = requireFlashcard(cardId, topicId);

    UserFlashcardProgress cardProgress =
        userFlashcardProgressRepository
            .findByUserIdAndFlashcardId(user.getId(), cardId)
            .orElseGet(
                () ->
                    UserFlashcardProgress.builder()
                        .user(user)
                        .flashcard(card)
                        .status(request.status())
                        .build());

    cardProgress.setStatus(request.status());
    userFlashcardProgressRepository.save(cardProgress);

    long learnedCount =
        userFlashcardProgressRepository.countByUserIdAndTopicIdAndStatus(
            user.getId(), topicId, FlashcardProgressStatus.REMEMBER);

    UserTopicProgress topicProgress =
        userTopicProgressRepository
            .findByUserIdAndTopicId(user.getId(), topicId)
            .orElseGet(
                () -> UserTopicProgress.builder().user(user).topic(topic).learnedWords(0).build());

    topicProgress.setLearnedWords((int) learnedCount);
    userTopicProgressRepository.save(topicProgress);

    int topicTotalWords = topic.getNumWords() != null ? topic.getNumWords() : 0;
    int topicProgressPercent =
        topicTotalWords > 0 ? Math.min(100, (int) ((learnedCount * 100L) / topicTotalWords)) : 0;

    log.info(
        "Card ID {} updated to status '{}'. Topic learned count: {}/{}",
        cardId,
        request.status(),
        learnedCount,
        topicTotalWords);

    return new LearnerCardProgressResponse(
        cardId,
        topicId,
        request.status(),
        (int) learnedCount,
        topicTotalWords,
        topicProgressPercent);
  }

  private FlashcardTopic requirePublishedTopic(UUID id) {
    FlashcardTopic topic =
        flashcardTopicRepository
            .findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new FlashcardException(FlashcardErrorCode.TOPIC_NOT_FOUND));

    if (topic.getStatus() != CourseCreateStatus.PUBLISH) {
      throw new FlashcardException(FlashcardErrorCode.TOPIC_NOT_FOUND);
    }
    return topic;
  }

  private Flashcard requireFlashcard(UUID cardId, UUID topicId) {
    return flashcardRepository
        .findByIdAndTopicId(cardId, topicId)
        .orElseThrow(() -> new FlashcardException(FlashcardErrorCode.FLASHCARD_NOT_FOUND));
  }

  private User requireUser(String email) {
    if (email == null || email.isBlank()) {
      throw new UserException(UserErrorCode.USER_NOT_FOUND);
    }
    return userRepository
        .findByGmail(email)
        .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
  }
}
