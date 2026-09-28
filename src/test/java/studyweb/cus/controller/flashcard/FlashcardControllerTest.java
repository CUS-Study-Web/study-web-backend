package studyweb.cus.controller.flashcard;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
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
import studyweb.cus.dto.request.flashcard.CreateFlashcardRequest;
import studyweb.cus.dto.request.flashcard.CreateFlashcardTopicRequest;
import studyweb.cus.dto.request.flashcard.UpdateFlashcardRequest;
import studyweb.cus.dto.request.flashcard.UpdateFlashcardTopicRequest;
import studyweb.cus.dto.response.flashcard.FlashcardMetricsResponse;
import studyweb.cus.dto.response.flashcard.FlashcardResponse;
import studyweb.cus.dto.response.flashcard.FlashcardTopicResponse;
import studyweb.cus.enums.CourseCreateStatus;
import studyweb.cus.security.JwtAuthenticationFilter;
import studyweb.cus.service.flashcard.FlashcardService;

@WebMvcTest(
    controllers = FlashcardController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class))
@Import(ResponseFactory.class)
class FlashcardControllerTest {

  private static final UUID TOPIC_ID = UUID.randomUUID();
  private static final UUID CARD_ID = UUID.randomUUID();
  private static final UUID USER_ID = UUID.randomUUID();

  @Autowired private MockMvc mockMvc;

  @MockitoBean private FlashcardService flashcardService;

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

  private static RequestPostProcessor authenticatedAssistant() {
    return SecurityMockMvcRequestPostProcessors.authentication(
        new UsernamePasswordAuthenticationToken(
            "assistant@studyweb.edu",
            null,
            List.of(new SimpleGrantedAuthority("ROLE_ASSISTANT"))));
  }

  private static RequestPostProcessor authenticatedLearner() {
    return SecurityMockMvcRequestPostProcessors.authentication(
        new UsernamePasswordAuthenticationToken(
            "learner@studyweb.edu", null, List.of(new SimpleGrantedAuthority("ROLE_LEARNER"))));
  }

  private FlashcardTopicResponse sampleTopicResponse() {
    return new FlashcardTopicResponse(
        TOPIC_ID,
        "IELTS Vocabulary",
        10,
        "Vocab list",
        CourseCreateStatus.PUBLISH,
        USER_ID,
        LocalDateTime.now(),
        LocalDateTime.now());
  }

  private FlashcardResponse sampleCardResponse() {
    return new FlashcardResponse(
        CARD_ID,
        TOPIC_ID,
        "Eloquent",
        "Fluent or persuasive",
        "/ˈel.ə.kwənt/",
        "Adjective",
        USER_ID,
        LocalDateTime.now(),
        LocalDateTime.now());
  }

  // --- Metrics ---

  @Test
  @DisplayName("GET /api/flashcards/metrics - Assistant can fetch metrics")
  void getMetrics_success() throws Exception {
    FlashcardMetricsResponse metrics = new FlashcardMetricsResponse(5, 350, 3);
    when(flashcardService.getMetrics()).thenReturn(metrics);

    mockMvc
        .perform(get("/api/flashcards/metrics").with(authenticatedAssistant()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.totalTopics").value(5))
        .andExpect(jsonPath("$.data.totalWords").value(350))
        .andExpect(jsonPath("$.data.activeTopics").value(3));
  }

  @Test
  @DisplayName("GET /api/flashcards/metrics - Learner forbidden")
  void getMetrics_learnerForbidden() throws Exception {
    mockMvc
        .perform(get("/api/flashcards/metrics").with(authenticatedLearner()))
        .andExpect(status().isForbidden());
  }

  // --- Topic Endpoints ---

  @Test
  @DisplayName("POST /api/flashcards/topics - Assistant creates topic")
  void createTopic_success() throws Exception {
    CreateFlashcardTopicRequest request =
        new CreateFlashcardTopicRequest("IELTS Vocab", "Description", CourseCreateStatus.DRAFT);
    when(flashcardService.createTopic(any(), any())).thenReturn(sampleTopicResponse());

    mockMvc
        .perform(
            post("/api/flashcards/topics")
                .with(authenticatedAssistant())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.title").value("IELTS Vocabulary"));
  }

  @Test
  @DisplayName("GET /api/flashcards/topics - List topics with paging")
  void listTopics_success() throws Exception {
    Pageable pageable = PageRequest.of(0, 10);
    when(flashcardService.listTopics(any(), any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(sampleTopicResponse()), pageable, 1));

    mockMvc
        .perform(get("/api/flashcards/topics").with(authenticatedAssistant()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data[0].title").value("IELTS Vocabulary"));
  }

  @Test
  @DisplayName("GET /api/flashcards/topics/{topicId} - Get topic detail")
  void getTopicDetail_success() throws Exception {
    when(flashcardService.getTopicDetail(TOPIC_ID)).thenReturn(sampleTopicResponse());

    mockMvc
        .perform(get("/api/flashcards/topics/" + TOPIC_ID).with(authenticatedAssistant()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.id").value(TOPIC_ID.toString()));
  }

  @Test
  @DisplayName("PUT /api/flashcards/topics/{topicId} - Update topic")
  void updateTopic_success() throws Exception {
    UpdateFlashcardTopicRequest request =
        new UpdateFlashcardTopicRequest("New Title", "New Desc", CourseCreateStatus.PUBLISH);
    when(flashcardService.updateTopic(eq(TOPIC_ID), any(), any()))
        .thenReturn(sampleTopicResponse());

    mockMvc
        .perform(
            put("/api/flashcards/topics/" + TOPIC_ID)
                .with(authenticatedAssistant())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200));
  }

  @Test
  @DisplayName("DELETE /api/flashcards/topics/{topicId} - Delete topic")
  void deleteTopic_success() throws Exception {
    mockMvc
        .perform(delete("/api/flashcards/topics/" + TOPIC_ID).with(authenticatedAssistant()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200));

    verify(flashcardService).deleteTopic(eq(TOPIC_ID), any());
  }

  // --- Flashcard Endpoints ---

  @Test
  @DisplayName("POST /api/flashcards/topics/{topicId}/cards - Add flashcard")
  void createFlashcard_success() throws Exception {
    CreateFlashcardRequest request =
        new CreateFlashcardRequest("Eloquent", "Fluent speaking", "/ˈel.ə.kwənt/", "Adjective");
    when(flashcardService.createFlashcard(eq(TOPIC_ID), any(), any()))
        .thenReturn(sampleCardResponse());

    mockMvc
        .perform(
            post("/api/flashcards/topics/" + TOPIC_ID + "/cards")
                .with(authenticatedAssistant())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.word").value("Eloquent"));
  }

  @Test
  @DisplayName("GET /api/flashcards/topics/{topicId}/cards - List flashcards")
  void listFlashcards_success() throws Exception {
    Pageable pageable = PageRequest.of(0, 20);
    when(flashcardService.listFlashcards(eq(TOPIC_ID), any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(sampleCardResponse()), pageable, 1));

    mockMvc
        .perform(
            get("/api/flashcards/topics/" + TOPIC_ID + "/cards").with(authenticatedAssistant()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data[0].word").value("Eloquent"));
  }

  @Test
  @DisplayName("GET /api/flashcards/topics/{topicId}/cards/{cardId} - Get flashcard detail")
  void getFlashcardDetail_success() throws Exception {
    when(flashcardService.getFlashcardDetail(TOPIC_ID, CARD_ID)).thenReturn(sampleCardResponse());

    mockMvc
        .perform(
            get("/api/flashcards/topics/" + TOPIC_ID + "/cards/" + CARD_ID)
                .with(authenticatedAssistant()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.id").value(CARD_ID.toString()));
  }

  @Test
  @DisplayName("PUT /api/flashcards/topics/{topicId}/cards/{cardId} - Update flashcard")
  void updateFlashcard_success() throws Exception {
    UpdateFlashcardRequest request =
        new UpdateFlashcardRequest("Articulate", "Express clearly", null, "Verb");
    when(flashcardService.updateFlashcard(eq(TOPIC_ID), eq(CARD_ID), any(), any()))
        .thenReturn(sampleCardResponse());

    mockMvc
        .perform(
            put("/api/flashcards/topics/" + TOPIC_ID + "/cards/" + CARD_ID)
                .with(authenticatedAssistant())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200));
  }

  @Test
  @DisplayName("DELETE /api/flashcards/topics/{topicId}/cards/{cardId} - Delete flashcard")
  void deleteFlashcard_success() throws Exception {
    mockMvc
        .perform(
            delete("/api/flashcards/topics/" + TOPIC_ID + "/cards/" + CARD_ID)
                .with(authenticatedAssistant()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200));

    verify(flashcardService).deleteFlashcard(eq(TOPIC_ID), eq(CARD_ID), any());
  }
}
