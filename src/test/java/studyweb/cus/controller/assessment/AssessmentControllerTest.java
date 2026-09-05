package studyweb.cus.controller.assessment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import studyweb.cus.controller.ResponseFactory;
import studyweb.cus.dto.request.assessment.AssessmentSubmitRequest;
import studyweb.cus.dto.request.assessment.CreateAssessmentRequest;
import studyweb.cus.dto.request.assessment.UpdateAssessmentRequest;
import studyweb.cus.dto.response.assessment.AssessmentDetailResponse;
import studyweb.cus.dto.response.assessment.AssessmentStartResponse;
import studyweb.cus.dto.response.assessment.AssessmentSubmitResponse;
import studyweb.cus.dto.response.assessment.AssessmentSummaryResponse;
import studyweb.cus.enums.AccessTier;
import studyweb.cus.enums.AssessmentFileType;
import studyweb.cus.enums.AssessmentStatus;
import studyweb.cus.enums.AssessmentType;
import studyweb.cus.security.JwtAuthenticationFilter;
import org.springframework.test.context.TestPropertySource;
import studyweb.cus.service.assessment.AssessmentService;
import studyweb.cus.service.assessment.LearnerAssessmentService;

@WebMvcTest(
    controllers = AssessmentController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class))
@Import(ResponseFactory.class)
@TestPropertySource(properties = {"logging.loki.url=http://localhost:3100"})
class AssessmentControllerTest {

  private static final UUID COURSE_ID = UUID.randomUUID();
  private static final UUID ASSESSMENT_ID = UUID.randomUUID();
  private static final UUID SUBJECT_ID = UUID.randomUUID();

  @Autowired private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private AssessmentService assessmentService;
  @MockitoBean private LearnerAssessmentService learnerAssessmentService;

  @TestConfiguration
  @EnableMethodSecurity
  static class SliceSecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      http.csrf(AbstractHttpConfigurer::disable)
          .authorizeHttpRequests(
              auth ->
                  auth.requestMatchers("/api/auth/**")
                      .permitAll()
                      .requestMatchers(
                          HttpMethod.GET,
                          "/api/courses",
                          "/api/courses/*",
                          "/api/courses/*/assessments/exams")
                      .permitAll()
                      .anyRequest()
                      .authenticated())
          .httpBasic(Customizer.withDefaults());
      return http.build();
    }
  }

  private MockMultipartFile mockFile() {
    return new MockMultipartFile("file", "exam.pdf", "application/pdf", new byte[] {1});
  }

  private static RequestPostProcessor authenticated() {
    return SecurityMockMvcRequestPostProcessors.authentication(
        new UsernamePasswordAuthenticationToken(
            "learner@test.com", null, List.of(new SimpleGrantedAuthority("ROLE_LEARNER"))));
  }

  // --- Tests for Assistant Endpoints ---

  @Test
  @WithMockUser(roles = "ASSISTANT")
  void createAssessment_assistantAllowed() throws Exception {
    AssessmentSummaryResponse summary =
        new AssessmentSummaryResponse(
            ASSESSMENT_ID,
            "Exam",
            AssessmentType.EXAM,
            AssessmentStatus.DRAFT,
            40,
            60,
            100,
            AccessTier.PUBLIC,
            "PDF",
            null,
            0L);
    when(assessmentService.createAssessment(eq(COURSE_ID), any(CreateAssessmentRequest.class)))
        .thenReturn(summary);

    mockMvc
        .perform(
            multipart("/api/courses/{courseId}/assessments", COURSE_ID)
                .file(mockFile())
                .param("assessmentType", "EXAM")
                .param("title", "Exam"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.id").value(ASSESSMENT_ID.toString()));
  }

  @Test
  @WithMockUser(roles = "LEARNER")
  void createAssessment_learnerForbidden() throws Exception {
    mockMvc
        .perform(
            multipart("/api/courses/{courseId}/assessments", COURSE_ID)
                .file(mockFile())
                .param("assessmentType", "EXAM")
                .param("title", "Exam"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ASSISTANT")
  void updateAssessment_assistantAllowed() throws Exception {
    AssessmentSummaryResponse summary =
        new AssessmentSummaryResponse(
            ASSESSMENT_ID,
            "Updated",
            AssessmentType.EXAM,
            AssessmentStatus.PUBLISHED,
            40,
            60,
            100,
            AccessTier.PUBLIC,
            "PDF",
            null,
            0L);
    when(assessmentService.updateAssessment(
            eq(COURSE_ID), eq(ASSESSMENT_ID), any(UpdateAssessmentRequest.class)))
        .thenReturn(summary);

    // PATCH with multipart requires a custom builder or using
    // multipart().with(request -> { request.setMethod("PATCH"); return request; })
    mockMvc
        .perform(
            MockMvcRequestBuilders.multipart(
                    "/api/courses/{courseId}/assessments/{assessmentId}", COURSE_ID, ASSESSMENT_ID)
                .with(
                    request -> {
                      request.setMethod("PATCH");
                      return request;
                    })
                .param("title", "Updated"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.title").value("Updated"));
  }

  @Test
  @WithMockUser(roles = "ASSISTANT")
  void deleteAssessment_assistantAllowed() throws Exception {
    mockMvc
        .perform(
            delete("/api/courses/{courseId}/assessments/{assessmentId}", COURSE_ID, ASSESSMENT_ID))
        .andExpect(status().isOk());

    verify(assessmentService).deleteAssessment(COURSE_ID, ASSESSMENT_ID);
  }

  @Test
  @WithMockUser(roles = "LEARNER")
  void deleteAssessment_learnerForbidden() throws Exception {
    mockMvc
        .perform(
            delete("/api/courses/{courseId}/assessments/{assessmentId}", COURSE_ID, ASSESSMENT_ID))
        .andExpect(status().isForbidden());
  }

  // --- Tests for Authenticated GET Endpoints ---

  @Test
  @WithMockUser(roles = "LEARNER")
  void getAssessmentDetail_authenticated() throws Exception {
    AssessmentDetailResponse detail =
        new AssessmentDetailResponse(
            ASSESSMENT_ID,
            "Exam",
            AssessmentType.EXAM,
            AssessmentStatus.PUBLISHED,
            40,
            60,
            100,
            AccessTier.PUBLIC,
            "PDF",
            "url",
            null,
            COURSE_ID,
            null,
            "Course",
            null,
            null,
            null,
            List.of());
    when(assessmentService.getAssessmentDetail(COURSE_ID, ASSESSMENT_ID)).thenReturn(detail);

    mockMvc
        .perform(
            get("/api/courses/{courseId}/assessments/{assessmentId}", COURSE_ID, ASSESSMENT_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(ASSESSMENT_ID.toString()));
  }

  @Test
  void getAssessmentDetail_unauthenticated_returns401() throws Exception {
    mockMvc
        .perform(
            get("/api/courses/{courseId}/assessments/{assessmentId}", COURSE_ID, ASSESSMENT_ID))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void listExams_public() throws Exception {
    AssessmentSummaryResponse summary =
        new AssessmentSummaryResponse(
            ASSESSMENT_ID,
            "Exam",
            AssessmentType.EXAM,
            AssessmentStatus.PUBLISHED,
            40,
            60,
            100,
            AccessTier.PUBLIC,
            "PDF",
            null,
            0L);
    when(assessmentService.listExamsByCourse(eq(COURSE_ID), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(summary), PageRequest.of(0, 10), 1));

    mockMvc
        .perform(get("/api/courses/{courseId}/assessments/exams", COURSE_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paging.total").value(1));
  }

  @Test
  @WithMockUser(roles = "LEARNER")
  void listHomework_authenticated() throws Exception {
    AssessmentSummaryResponse summary =
        new AssessmentSummaryResponse(
            ASSESSMENT_ID,
            "HW",
            AssessmentType.HOMEWORK,
            AssessmentStatus.PUBLISHED,
            10,
            null,
            null,
            null,
            "PDF",
            null,
            0L);
    when(assessmentService.listHomeworkBySubject(
            eq(COURSE_ID), eq(SUBJECT_ID), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(summary), PageRequest.of(0, 10), 1));

    mockMvc
        .perform(
            get("/api/courses/{courseId}/assessments/homework", COURSE_ID)
                .param("subjectId", SUBJECT_ID.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paging.total").value(1));
  }

  // --- Tests for Learner Endpoints ---

  @Test
  void startAssessment_learnerAllowed() throws Exception {
    AssessmentStartResponse startResponse =
        new AssessmentStartResponse(
            ASSESSMENT_ID, "Exam", AssessmentType.EXAM, 40, 60, AssessmentFileType.PDF, "url");
    when(learnerAssessmentService.getAssessmentForTaking(
            COURSE_ID, ASSESSMENT_ID, "learner@test.com"))
        .thenReturn(startResponse);

    mockMvc
        .perform(
            get(
                    "/api/courses/{courseId}/assessments/{assessmentId}/start",
                    COURSE_ID,
                    ASSESSMENT_ID)
                .with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.numQuestions").value(40));
  }

  @Test
  @WithMockUser(roles = "ASSISTANT")
  void startAssessment_assistantForbidden() throws Exception {
    mockMvc
        .perform(
            get(
                "/api/courses/{courseId}/assessments/{assessmentId}/start",
                COURSE_ID,
                ASSESSMENT_ID))
        .andExpect(status().isForbidden());
  }

  @Test
  void submitAssessment_learnerAllowed() throws Exception {
    AssessmentSubmitRequest req = new AssessmentSubmitRequest(60, List.of());
    AssessmentSubmitResponse resp =
        new AssessmentSubmitResponse(
            UUID.randomUUID(), 1, 40, 0, 40, new BigDecimal("10.0"), null, List.of());
    when(learnerAssessmentService.submitAssessment(
            eq(COURSE_ID), eq(ASSESSMENT_ID), eq("learner@test.com"), any()))
        .thenReturn(resp);

    mockMvc
        .perform(
            post(
                    "/api/courses/{courseId}/assessments/{assessmentId}/submit",
                    COURSE_ID,
                    ASSESSMENT_ID)
                .with(authenticated())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.numCorrect").value(40));
  }

  @Test
  void listAttempts_learnerAllowed() throws Exception {
    when(learnerAssessmentService.listAttempts(
            eq(COURSE_ID), eq(ASSESSMENT_ID), eq("learner@test.com"), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

    mockMvc
        .perform(
            get(
                    "/api/courses/{courseId}/assessments/{assessmentId}/attempts",
                    COURSE_ID,
                    ASSESSMENT_ID)
                .with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paging.total").value(0));
  }

  @Test
  void getAttemptDetail_learnerAllowed() throws Exception {
    UUID attemptId = UUID.randomUUID();
    AssessmentSubmitResponse resp =
        new AssessmentSubmitResponse(
            attemptId, 1, 40, 0, 40, new BigDecimal("10.0"), null, List.of());
    when(learnerAssessmentService.getAttemptDetail(
            COURSE_ID, ASSESSMENT_ID, attemptId, "learner@test.com"))
        .thenReturn(resp);

    mockMvc
        .perform(
            get(
                    "/api/courses/{courseId}/assessments/{assessmentId}/attempts/{attemptId}",
                    COURSE_ID,
                    ASSESSMENT_ID,
                    attemptId)
                .with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.attemptId").value(attemptId.toString()));
  }

  // --- Tests for Unauthenticated Access (should be blocked) ---

  @Test
  void startAssessment_unauthenticated_returns401() throws Exception {
    mockMvc
        .perform(
            get(
                "/api/courses/{courseId}/assessments/{assessmentId}/start",
                COURSE_ID,
                ASSESSMENT_ID))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void submitAssessment_unauthenticated_returns401() throws Exception {
    AssessmentSubmitRequest req = new AssessmentSubmitRequest(60, List.of());
    mockMvc
        .perform(
            post(
                    "/api/courses/{courseId}/assessments/{assessmentId}/submit",
                    COURSE_ID,
                    ASSESSMENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void listAttempts_unauthenticated_returns401() throws Exception {
    mockMvc
        .perform(
            get(
                "/api/courses/{courseId}/assessments/{assessmentId}/attempts",
                COURSE_ID,
                ASSESSMENT_ID))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void getAttemptDetail_unauthenticated_returns401() throws Exception {
    UUID attemptId = UUID.randomUUID();
    mockMvc
        .perform(
            get(
                "/api/courses/{courseId}/assessments/{assessmentId}/attempts/{attemptId}",
                COURSE_ID,
                ASSESSMENT_ID,
                attemptId))
        .andExpect(status().isUnauthorized());
  }
}
