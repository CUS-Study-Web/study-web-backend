package studyweb.cus.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import studyweb.cus.config.SecurityConfig;
import studyweb.cus.controller.ResponseFactory;
import studyweb.cus.dto.request.admin.CreateVipAccountRequest;
import studyweb.cus.dto.response.admin.LearnerSummaryResponse;
import studyweb.cus.enums.UserStatus;
import studyweb.cus.enums.UserTier;
import studyweb.cus.exception.GlobalExceptionHandler;
import studyweb.cus.exception.system.SystemErrorCode;
import studyweb.cus.exception.system.SystemException;
import studyweb.cus.exception.user.UserErrorCode;
import studyweb.cus.exception.user.UserException;
import studyweb.cus.security.JwtAuthenticationEntryPoint;
import studyweb.cus.security.JwtAuthenticationFilter;
import studyweb.cus.security.JwtUtils;
import studyweb.cus.security.RestAccessDeniedHandler;
import studyweb.cus.service.admin.SystemManagementService;

@Slf4j
@WebMvcTest(SystemManagementController.class)
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  JwtAuthenticationEntryPoint.class,
  RestAccessDeniedHandler.class,
  GlobalExceptionHandler.class,
  ResponseFactory.class,
  SystemManagementControllerTest.TestConfig.class
})
class SystemManagementControllerTest {

  @TestConfiguration
  static class TestConfig {
    @Bean
    public tools.jackson.databind.ObjectMapper toolsObjectMapper() {
      return new tools.jackson.databind.ObjectMapper();
    }
  }

  @Autowired private MockMvc mockMvc;

  @MockitoBean private SystemManagementService systemManagementService;
  @MockitoBean private JwtUtils jwtUtils;

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  private static final UUID LEARNER_ID = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
  private static final String LEARNER_EMAIL = "learner@studyweb.edu";

  private CreateVipAccountRequest sampleVipRequest() {
    return new CreateVipAccountRequest(
        "Trần Thị B",
        "vip.learner@studyweb.edu",
        "React & TypeScript Masterclass",
        LocalDateTime.of(2026, 8, 18, 0, 0, 0),
        LocalDateTime.of(2027, 8, 18, 23, 59, 59),
        "Kích hoạt gói VIP 1 năm qua admin");
  }

  private LearnerSummaryResponse sampleLearnerResponse() {
    return new LearnerSummaryResponse(
        LEARNER_ID,
        LEARNER_EMAIL,
        "Lập Trình Web Cơ Bản",
        75.5,
        8.5,
        "18/08/2026, 14:30",
        UserStatus.ACTIVE,
        UserTier.VIP,
        "Nguyễn Văn A",
        4);
  }

  // =========================================================================
  // 1. REQUEST BINDING & DESERIALIZATION
  // =========================================================================
  @Nested
  @DisplayName("1. Request Binding & Deserialization")
  @WithMockUser(roles = "ADMIN")
  class RequestBindingAndDeserializationTests {

    @Test
    @DisplayName("GET /learners - Default pagination binds page=0, size=10")
    void getLearners_bindsDefaultPagination() throws Exception {
      when(systemManagementService.listLearners(isNull(), any(Pageable.class)))
          .thenReturn(new PageImpl<>(List.of()));

      mockMvc.perform(get("/api/system-management/learners")).andExpect(status().isOk());

      ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
      verify(systemManagementService).listLearners(isNull(), pageableCaptor.capture());

      Pageable bound = pageableCaptor.getValue();
      assertThat(bound.getPageNumber()).isEqualTo(0);
      assertThat(bound.getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("GET /learners - Custom query params bind page, size, search, and sort")
    void getLearners_bindsCustomQueryParams() throws Exception {
      when(systemManagementService.listLearners(eq("nguyen"), any(Pageable.class)))
          .thenReturn(new PageImpl<>(List.of()));

      mockMvc
          .perform(
              get("/api/system-management/learners")
                  .param("search", "nguyen")
                  .param("page", "2")
                  .param("size", "25")
                  .param("sort", "name,desc"))
          .andExpect(status().isOk());

      ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
      verify(systemManagementService).listLearners(eq("nguyen"), pageableCaptor.capture());

      Pageable bound = pageableCaptor.getValue();
      assertThat(bound.getPageNumber()).isEqualTo(2);
      assertThat(bound.getPageSize()).isEqualTo(25);
      assertThat(bound.getSort().getOrderFor("name")).isNotNull();
      assertThat(bound.getSort().getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("PATCH /{id}/ban & unban - Path variable string binds to UUID")
    void patchEndpoints_bindPathVariableToUuid() throws Exception {
      doNothing().when(systemManagementService).banLearner(LEARNER_ID);
      doNothing().when(systemManagementService).unbanLearner(LEARNER_ID);

      mockMvc
          .perform(patch("/api/system-management/{id}/ban", LEARNER_ID.toString()))
          .andExpect(status().isOk());
      verify(systemManagementService).banLearner(LEARNER_ID);

      mockMvc
          .perform(patch("/api/system-management/{id}/unban", LEARNER_ID.toString()))
          .andExpect(status().isOk());
      verify(systemManagementService).unbanLearner(LEARNER_ID);
    }

    @Test
    @DisplayName(
        "POST /create-vip-account - JSON body deserializes all fields including ISO LocalDateTime")
    void createVipAccount_deserializesAllFields() throws Exception {
      LocalDateTime start = LocalDateTime.of(2026, 8, 18, 0, 0, 0);
      LocalDateTime end = LocalDateTime.of(2027, 8, 18, 23, 59, 59);

      CreateVipAccountRequest request =
          new CreateVipAccountRequest(
              "Trần Thị B",
              "vip.learner@studyweb.edu",
              "React & TypeScript",
              start,
              end,
              "Ghi chú kích hoạt");

      when(systemManagementService.createVipAccount(any(CreateVipAccountRequest.class)))
          .thenReturn(sampleLearnerResponse());

      mockMvc
          .perform(
              post("/api/system-management/create-vip-account")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk());

      ArgumentCaptor<CreateVipAccountRequest> captor =
          ArgumentCaptor.forClass(CreateVipAccountRequest.class);
      verify(systemManagementService).createVipAccount(captor.capture());

      CreateVipAccountRequest bound = captor.getValue();
      assertThat(bound.name()).isEqualTo("Trần Thị B");
      assertThat(bound.gmail()).isEqualTo("vip.learner@studyweb.edu");
      assertThat(bound.mainCourse()).isEqualTo("React & TypeScript");
      assertThat(bound.startDate()).isEqualTo(start);
      assertThat(bound.endDate()).isEqualTo(end);
      assertThat(bound.note()).isEqualTo("Ghi chú kích hoạt");
    }
  }

  // =========================================================================
  // 2. INPUT VALIDATION (@Valid)
  // =========================================================================
  @Nested
  @DisplayName("2. Input Validation (@Valid)")
  @WithMockUser(roles = "ADMIN")
  class InputValidationTests {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("POST /create-vip-account - Rejects blank or empty name")
    void createVipAccount_rejectsBlankName(String invalidName) throws Exception {
      CreateVipAccountRequest request =
          new CreateVipAccountRequest(
              invalidName,
              "valid@studyweb.edu",
              "React",
              LocalDateTime.of(2026, 8, 18, 0, 0, 0),
              LocalDateTime.of(2027, 8, 18, 23, 59, 59),
              null);

      mockMvc
          .perform(
              post("/api/system-management/create-vip-account")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.statusCode").value(400))
          .andExpect(jsonPath("$.errorCode").value(SystemErrorCode.VALIDATION_ERROR.code()))
          .andExpect(jsonPath("$.message").value("Name is required"));

      verify(systemManagementService, never()).createVipAccount(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("POST /create-vip-account - Rejects blank or empty gmail")
    void createVipAccount_rejectsBlankGmail(String invalidGmail) throws Exception {
      CreateVipAccountRequest request =
          new CreateVipAccountRequest(
              "Nguyễn Văn A",
              invalidGmail,
              "React",
              LocalDateTime.of(2026, 8, 18, 0, 0, 0),
              LocalDateTime.of(2027, 8, 18, 23, 59, 59),
              null);

      mockMvc
          .perform(
              post("/api/system-management/create-vip-account")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.statusCode").value(400))
          .andExpect(jsonPath("$.errorCode").value(SystemErrorCode.VALIDATION_ERROR.code()))
          .andExpect(jsonPath("$.message").value("Email is required"));

      verify(systemManagementService, never()).createVipAccount(any());
    }

    @Test
    @DisplayName("POST /create-vip-account - Rejects null startDate")
    void createVipAccount_rejectsNullStartDate() throws Exception {
      CreateVipAccountRequest request =
          new CreateVipAccountRequest(
              "Nguyễn Văn A",
              "valid@studyweb.edu",
              "React",
              null,
              LocalDateTime.of(2027, 8, 18, 23, 59, 59),
              null);

      mockMvc
          .perform(
              post("/api/system-management/create-vip-account")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.statusCode").value(400))
          .andExpect(jsonPath("$.errorCode").value(SystemErrorCode.VALIDATION_ERROR.code()))
          .andExpect(jsonPath("$.message").value("Start date is required"));

      verify(systemManagementService, never()).createVipAccount(any());
    }

    @Test
    @DisplayName("POST /create-vip-account - Rejects null endDate")
    void createVipAccount_rejectsNullEndDate() throws Exception {
      CreateVipAccountRequest request =
          new CreateVipAccountRequest(
              "Nguyễn Văn A",
              "valid@studyweb.edu",
              "React",
              LocalDateTime.of(2026, 8, 18, 0, 0, 0),
              null,
              null);

      mockMvc
          .perform(
              post("/api/system-management/create-vip-account")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.statusCode").value(400))
          .andExpect(jsonPath("$.errorCode").value(SystemErrorCode.VALIDATION_ERROR.code()))
          .andExpect(jsonPath("$.message").value("End date is required"));

      verify(systemManagementService, never()).createVipAccount(any());
    }
  }

  // =========================================================================
  // 3. DELEGATION & SERVICE INTERACTION
  // =========================================================================
  @Nested
  @DisplayName("3. Delegation & Service Interaction")
  @WithMockUser(roles = "ADMIN")
  class DelegationAndServiceInteractionTests {

    @Test
    @DisplayName("GET /learners - Delegates exact search term and returns wrapped SingleResponse")
    void getLearners_delegatesAndWrapsResponse() throws Exception {
      LearnerSummaryResponse item = sampleLearnerResponse();
      PageImpl<LearnerSummaryResponse> serviceResult =
          new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1);

      when(systemManagementService.listLearners(eq("target_learner"), any(Pageable.class)))
          .thenReturn(serviceResult);

      mockMvc
          .perform(
              get("/api/system-management/learners")
                  .param("search", "target_learner")
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Learners fetched successfully!"))
          .andExpect(jsonPath("$.data.content[0].id").value(LEARNER_ID.toString()))
          .andExpect(jsonPath("$.data.content[0].name").value("Nguyễn Văn A"))
          .andExpect(jsonPath("$.data.content[0].tier").value("VIP"))
          .andExpect(jsonPath("$.data.totalElements").value(1));

      verify(systemManagementService).listLearners(eq("target_learner"), any(Pageable.class));
    }

    @Test
    @DisplayName("PATCH /{id}/ban - Delegates to banLearner and wraps in SuccessResponse")
    void banLearner_delegatesAndWrapsSuccessResponse() throws Exception {
      doNothing().when(systemManagementService).banLearner(LEARNER_ID);

      mockMvc
          .perform(patch("/api/system-management/{id}/ban", LEARNER_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Ban learner sucessfully."));

      verify(systemManagementService).banLearner(LEARNER_ID);
    }

    @Test
    @DisplayName("PATCH /{id}/unban - Delegates to unbanLearner and wraps in SuccessResponse")
    void unbanLearner_delegatesAndWrapsSuccessResponse() throws Exception {
      doNothing().when(systemManagementService).unbanLearner(LEARNER_ID);

      mockMvc
          .perform(patch("/api/system-management/{id}/unban", LEARNER_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Unban learner sucessfully."));

      verify(systemManagementService).unbanLearner(LEARNER_ID);
    }

    @Test
    @DisplayName(
        "POST /create-vip-account - Delegates request to createVipAccount and wraps payload")
    void createVipAccount_delegatesAndWrapsPayload() throws Exception {
      CreateVipAccountRequest request = sampleVipRequest();
      LearnerSummaryResponse created = sampleLearnerResponse();

      when(systemManagementService.createVipAccount(any(CreateVipAccountRequest.class)))
          .thenReturn(created);

      mockMvc
          .perform(
              post("/api/system-management/create-vip-account")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Learners fetched successfully!"))
          .andExpect(jsonPath("$.data.id").value(LEARNER_ID.toString()))
          .andExpect(jsonPath("$.data.gmail").value(LEARNER_EMAIL));

      verify(systemManagementService).createVipAccount(any(CreateVipAccountRequest.class));
    }
  }

  // =========================================================================
  // 4. EXCEPTION TRANSLATION
  // =========================================================================
  @Nested
  @DisplayName("4. Exception Translation via GlobalExceptionHandler")
  @WithMockUser(roles = "ADMIN")
  class ExceptionTranslationTests {

    @Test
    @DisplayName("UserException(USER_NOT_FOUND) -> 404 NOT_FOUND with ErrorResponse USER_001")
    void userNotFound_translatesTo404() throws Exception {
      doThrow(new UserException(UserErrorCode.USER_NOT_FOUND))
          .when(systemManagementService)
          .banLearner(LEARNER_ID);

      mockMvc
          .perform(patch("/api/system-management/{id}/ban", LEARNER_ID))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.statusCode").value(404))
          .andExpect(jsonPath("$.errorCode").value(UserErrorCode.USER_NOT_FOUND.code()))
          .andExpect(jsonPath("$.message").value(UserErrorCode.USER_NOT_FOUND.message()));
    }

    @Test
    @DisplayName("SystemException(INTERNAL_ERROR) -> 500 INTERNAL_ERROR with ErrorResponse SYS_001")
    void systemException_translatesTo500() throws Exception {
      when(systemManagementService.createVipAccount(any(CreateVipAccountRequest.class)))
          .thenThrow(
              new SystemException(
                  SystemErrorCode.INTERNAL_ERROR, "No value provided for default password!"));

      CreateVipAccountRequest request = sampleVipRequest();

      mockMvc
          .perform(
              post("/api/system-management/create-vip-account")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.statusCode").value(500))
          .andExpect(jsonPath("$.errorCode").value(SystemErrorCode.INTERNAL_ERROR.code()))
          .andExpect(jsonPath("$.message").value("No value provided for default password!"));
    }

    @Test
    @DisplayName("HttpRequestMethodNotSupportedException -> 405 METHOD_NOT_ALLOWED with SYS_004")
    void methodNotSupported_translatesTo405() throws Exception {
      mockMvc
          .perform(put("/api/system-management/create-vip-account"))
          .andExpect(status().isMethodNotAllowed())
          .andExpect(jsonPath("$.statusCode").value(405))
          .andExpect(jsonPath("$.errorCode").value(SystemErrorCode.METHOD_NOT_ALLOWED.code()))
          .andExpect(jsonPath("$.message").value(SystemErrorCode.METHOD_NOT_ALLOWED.message()));
    }

    @Test
    @DisplayName("Unhandled generic Exception -> 500 INTERNAL_ERROR with SYS_001")
    void unhandledException_translatesTo500() throws Exception {
      doThrow(new RuntimeException("Unexpected fatal crash"))
          .when(systemManagementService)
          .unbanLearner(LEARNER_ID);

      mockMvc
          .perform(patch("/api/system-management/{id}/unban", LEARNER_ID))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.statusCode").value(500))
          .andExpect(jsonPath("$.errorCode").value(SystemErrorCode.INTERNAL_ERROR.code()))
          .andExpect(jsonPath("$.message").value(SystemErrorCode.INTERNAL_ERROR.message()));
    }
  }

  // =========================================================================
  // 5. SECURITY & ACCESS CONTROL (@PreAuthorize)
  // =========================================================================
  @Nested
  @DisplayName("5. Security & Access Control (@PreAuthorize)")
  class SecurityAndAccessControlTests {

    @Test
    @DisplayName("Anonymous access is blocked -> 401 UNAUTHORIZED on all endpoints")
    @WithAnonymousUser
    void anonymousAccess_blockedWith401() throws Exception {
      mockMvc.perform(get("/api/system-management/learners")).andExpect(status().isUnauthorized());
      mockMvc
          .perform(patch("/api/system-management/{id}/ban", LEARNER_ID))
          .andExpect(status().isUnauthorized());
      mockMvc
          .perform(patch("/api/system-management/{id}/unban", LEARNER_ID))
          .andExpect(status().isUnauthorized());
      mockMvc
          .perform(
              post("/api/system-management/create-vip-account")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(sampleVipRequest())))
          .andExpect(status().isUnauthorized());

      verify(systemManagementService, never()).listLearners(any(), any());
      verify(systemManagementService, never()).banLearner(any());
      verify(systemManagementService, never()).unbanLearner(any());
      verify(systemManagementService, never()).createVipAccount(any());
    }

    @Test
    @DisplayName("Non-ADMIN roles (LEARNER) are blocked -> 403 FORBIDDEN on all endpoints")
    @WithMockUser(roles = "LEARNER")
    void learnerRole_blockedWith403() throws Exception {
      mockMvc.perform(get("/api/system-management/learners")).andExpect(status().isForbidden());
      mockMvc
          .perform(patch("/api/system-management/{id}/ban", LEARNER_ID))
          .andExpect(status().isForbidden());
      mockMvc
          .perform(patch("/api/system-management/{id}/unban", LEARNER_ID))
          .andExpect(status().isForbidden());
      mockMvc
          .perform(
              post("/api/system-management/create-vip-account")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(sampleVipRequest())))
          .andExpect(status().isForbidden());

      verify(systemManagementService, never()).listLearners(any(), any());
      verify(systemManagementService, never()).banLearner(any());
      verify(systemManagementService, never()).unbanLearner(any());
      verify(systemManagementService, never()).createVipAccount(any());
    }

    @Test
    @DisplayName("Non-ADMIN roles (ASSISTANT) are blocked -> 403 FORBIDDEN on all endpoints")
    @WithMockUser(roles = "ASSISTANT")
    void assistantRole_blockedWith403() throws Exception {
      mockMvc.perform(get("/api/system-management/learners")).andExpect(status().isForbidden());
      mockMvc
          .perform(patch("/api/system-management/{id}/ban", LEARNER_ID))
          .andExpect(status().isForbidden());
      mockMvc
          .perform(patch("/api/system-management/{id}/unban", LEARNER_ID))
          .andExpect(status().isForbidden());
      mockMvc
          .perform(
              post("/api/system-management/create-vip-account")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(sampleVipRequest())))
          .andExpect(status().isForbidden());

      verify(systemManagementService, never()).listLearners(any(), any());
      verify(systemManagementService, never()).banLearner(any());
      verify(systemManagementService, never()).unbanLearner(any());
      verify(systemManagementService, never()).createVipAccount(any());
    }

    @Test
    @DisplayName("ADMIN role is authorized -> 200 OK")
    @WithMockUser(roles = "ADMIN")
    void adminRole_authorized200() throws Exception {
      when(systemManagementService.listLearners(isNull(), any(Pageable.class)))
          .thenReturn(new PageImpl<>(List.of()));

      mockMvc.perform(get("/api/system-management/learners")).andExpect(status().isOk());
    }
  }
}
