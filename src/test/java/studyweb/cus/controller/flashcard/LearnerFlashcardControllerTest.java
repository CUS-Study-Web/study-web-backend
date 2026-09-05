package studyweb.cus.controller.flashcard;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import studyweb.cus.controller.ResponseFactory;
import studyweb.cus.dto.request.flashcard.UpdateLearnerProgressRequest;
import studyweb.cus.dto.response.flashcard.LearnerCardProgressResponse;
import studyweb.cus.dto.response.flashcard.LearnerFlashcardItemResponse;
import studyweb.cus.dto.response.flashcard.LearnerFlashcardMetricsResponse;
import studyweb.cus.dto.response.flashcard.LearnerFlashcardTopicResponse;
import studyweb.cus.dto.response.flashcard.LearnerTopicDetailResponse;
import studyweb.cus.enums.FlashcardProgressStatus;
import studyweb.cus.security.JwtAuthenticationFilter;
import studyweb.cus.service.flashcard.LearnerFlashcardService;

@WebMvcTest(
    controllers = LearnerFlashcardController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class))
@Import(ResponseFactory.class)
class LearnerFlashcardControllerTest {

  private static final UUID TOPIC_ID = UUID.randomUUID();
  private static final UUID CARD_ID = UUID.randomUUID();

  @Autowired private MockMvc mockMvc;

  @MockitoBean private LearnerFlashcardService learnerFlashcardService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @TestConfiguration
  @EnableMethodSecurity
  static class SliceSecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      http.csrf(AbstractHttpConfigurer::disable)
          .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
          .httpBasic(Customizer.withDefaults());
      return http.build();
    }
  }

  private static RequestPostProcessor authenticatedLearner() {
    return SecurityMockMvcRequestPostProcessors.authentication(
        new UsernamePasswordAuthenticationToken(
            "learner@studyweb.edu", null, List.of(new SimpleGrantedAuthority("ROLE_LEARNER"))));
  }

  private static RequestPostProcessor authenticatedAssistant() {
    return SecurityMockMvcRequestPostProcessors.authentication(
        new UsernamePasswordAuthenticationToken(
            "assistant@studyweb.edu",
            null,
            List.of(new SimpleGrantedAuthority("ROLE_ASSISTANT"))));
  }

  @Test
  @DisplayName("GET /api/learner/flashcards/metrics - Learner gets metrics")
  void getMetrics_success() throws Exception {
    LearnerFlashcardMetricsResponse metrics = new LearnerFlashcardMetricsResponse(6, 440, 214);
    when(learnerFlashcardService.getMetrics("learner@studyweb.edu")).thenReturn(metrics);

    mockMvc
        .perform(get("/api/learner/flashcards/metrics").with(authenticatedLearner()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.totalTopics").value(6))
        .andExpect(jsonPath("$.data.totalWords").value(440))
        .andExpect(jsonPath("$.data.totalRememberedWords").value(214));
  }

  @Test
  @DisplayName("GET /api/learner/flashcards/topics - List topics with progress")
  void listTopics_success() throws Exception {
    Pageable pageable = PageRequest.of(0, 10);
    LearnerFlashcardTopicResponse topicItem =
        new LearnerFlashcardTopicResponse(
            TOPIC_ID, "Từ vựng cốt lõi ĐGNL", "Core words", 50, 32, 64, false);

    when(learnerFlashcardService.listTopics(eq("learner@studyweb.edu"), any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(topicItem), pageable, 1));

    mockMvc
        .perform(get("/api/learner/flashcards/topics").with(authenticatedLearner()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data[0].title").value("Từ vựng cốt lõi ĐGNL"))
        .andExpect(jsonPath("$.data[0].learnedWords").value(32))
        .andExpect(jsonPath("$.data[0].numWords").value(50))
        .andExpect(jsonPath("$.data[0].progressPercent").value(64));
  }

  @Test
  @DisplayName("GET /api/learner/flashcards/topics/{topicId} - Get topic detail")
  void getTopicDetail_success() throws Exception {
    LearnerTopicDetailResponse detail =
        new LearnerTopicDetailResponse(
            TOPIC_ID, "Từ vựng cốt lõi ĐGNL", "Core words", 50, 32, 18, 64);
    when(learnerFlashcardService.getTopicDetail(TOPIC_ID, "learner@studyweb.edu"))
        .thenReturn(detail);

    mockMvc
        .perform(get("/api/learner/flashcards/topics/" + TOPIC_ID).with(authenticatedLearner()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.totalWords").value(50))
        .andExpect(jsonPath("$.data.rememberedWords").value(32))
        .andExpect(jsonPath("$.data.studyWords").value(18))
        .andExpect(jsonPath("$.data.progressPercent").value(64));
  }

  @Test
  @DisplayName("GET /api/learner/flashcards/topics/{topicId}/words - List words")
  void listTopicWords_success() throws Exception {
    Pageable pageable = PageRequest.of(0, 20);
    LearnerFlashcardItemResponse item =
        new LearnerFlashcardItemResponse(
            CARD_ID,
            TOPIC_ID,
            "Perseverance",
            "/ˌpɜː.sɪˈvɪə.rəns/",
            "Noun",
            "Sự kiên trì, bền bỉ",
            FlashcardProgressStatus.REMEMBER);

    when(learnerFlashcardService.listTopicWords(
            eq(TOPIC_ID), eq("learner@studyweb.edu"), any(), any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(item), pageable, 1));

    mockMvc
        .perform(
            get("/api/learner/flashcards/topics/" + TOPIC_ID + "/words")
                .with(authenticatedLearner()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data[0].word").value("Perseverance"))
        .andExpect(jsonPath("$.data[0].status").value("REMEMBER"));
  }

  @Test
  @DisplayName("GET /api/learner/flashcards/topics/{topicId}/study - Get study deck")
  void getStudyCards_success() throws Exception {
    LearnerFlashcardItemResponse item =
        new LearnerFlashcardItemResponse(
            CARD_ID,
            TOPIC_ID,
            "Perseverance",
            "/ˌpɜː.sɪˈvɪə.rəns/",
            "Noun",
            "Sự kiên trì, bền bỉ",
            FlashcardProgressStatus.REMEMBER);

    when(learnerFlashcardService.getStudyCards(TOPIC_ID, "learner@studyweb.edu", "LEARN"))
        .thenReturn(List.of(item));

    mockMvc
        .perform(
            get("/api/learner/flashcards/topics/" + TOPIC_ID + "/study?phase=LEARN")
                .with(authenticatedLearner()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data[0].word").value("Perseverance"));
  }

  @Test
  @DisplayName("POST /api/learner/flashcards/topics/{topicId}/cards/{cardId}/progress - Update progress")
  void updateCardProgress_success() throws Exception {
    UpdateLearnerProgressRequest request =
        new UpdateLearnerProgressRequest(FlashcardProgressStatus.REMEMBER);
    LearnerCardProgressResponse response =
        new LearnerCardProgressResponse(
            CARD_ID, TOPIC_ID, FlashcardProgressStatus.REMEMBER, 33, 50, 66);

    when(learnerFlashcardService.updateCardProgress(
            eq(TOPIC_ID), eq(CARD_ID), eq("learner@studyweb.edu"), any()))
        .thenReturn(response);

    mockMvc
        .perform(
            post("/api/learner/flashcards/topics/" + TOPIC_ID + "/cards/" + CARD_ID + "/progress")
                .with(authenticatedLearner())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.status").value("REMEMBER"))
        .andExpect(jsonPath("$.data.topicLearnedWords").value(33))
        .andExpect(jsonPath("$.data.topicProgressPercent").value(66));
  }
}
