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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import studyweb.cus.config.SecurityConfig;
import studyweb.cus.controller.ResponseFactory;
import studyweb.cus.dto.request.admin.CreateAssistantRequest;
import studyweb.cus.dto.request.admin.CreateVipAccountRequest;
import studyweb.cus.dto.request.admin.UpdateAccountRequest;
import studyweb.cus.dto.response.admin.AssistantActivityResponse;
import studyweb.cus.dto.response.admin.AssistantSummaryResponse;
import studyweb.cus.dto.response.admin.LearnerSummaryResponse;
import studyweb.cus.dto.response.admin.UserCountResponse;
import studyweb.cus.dto.response.admin.VipRequestCountResponse;
import studyweb.cus.dto.response.admin.VipRequestResponse;
import studyweb.cus.enums.UserRole;
import studyweb.cus.enums.UserStatus;
import studyweb.cus.enums.UserTier;
import studyweb.cus.enums.VipRequestStatus;
import studyweb.cus.exception.GlobalExceptionHandler;
import studyweb.cus.exception.admin.AdminErrorCode;
import studyweb.cus.exception.admin.AdminException;
import studyweb.cus.exception.system.SystemErrorCode;
import studyweb.cus.exception.system.SystemException;
import studyweb.cus.repository.user.UserRepository;
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
@TestPropertySource(properties = {"cors.allowed-origins=http://localhost:3000"})
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
  @MockitoBean private UserRepository userRepository;
  @MockitoBean private JwtUtils jwtUtils;

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  private static final UUID LEARNER_ID = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
  private static final String LEARNER_EMAIL = "learner@studyweb.edu";
  private static final UUID COURSE_ID = UUID.fromString("b1ffca88-1234-4ef8-bb6d-6bb9bd380b22");
  private static final UUID ASSISTANT_ID = UUID.fromString("c2bbcc00-1111-2222-3333-444455556666");

  private CreateVipAccountRequest sampleVipRequest() {
    return new CreateVipAccountRequest(
        "Trần Thị B",
        "vip.learner@studyweb.edu",
        LocalDate.of(2026, 8, 18),
        LocalDate.of(2027, 8, 18),
        "Kích hoạt gói VIP 1 năm qua admin",
        "Password@123");
  }

  private UpdateAccountRequest sampleUpdateAccountRequest() {
    return new UpdateAccountRequest(
        "Trần Thị B",
        "vip.learner@studyweb.edu",
        LocalDate.of(2026, 8, 18),
        LocalDate.of(2027, 8, 18),
        "Ghi chú cập nhật",
        UserTier.VIP,
        "Password@123");
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
        4,
        "Kích hoạt VIP 1 năm",
        LocalDate.of(2026, 8, 18),
        LocalDate.of(2027, 8, 18),
        "https://cdn.studyweb.edu/avatars/nguyenvana.png");
  }

  private CreateAssistantRequest sampleCreateAssistantRequest() {
    return new CreateAssistantRequest(
        "Trần Minh Hiếu", "hieu.tm@cus.edu.vn", "0901111222", "CustomPass@123");
  }

  private AssistantSummaryResponse sampleAssistantSummaryResponse() {
    return new AssistantSummaryResponse(
        ASSISTANT_ID,
        "Trần Minh Hiếu",
        "hieu.tm@cus.edu.vn",
        "0901111222",
        UserStatus.ACTIVE,
        12,
        "Hôm nay, 10:42",
        List.of(
            new AssistantActivityResponse(
                UUID.randomUUID(), "Đăng tải đề thi V-ACT mã đề 007", "Hôm nay, 10:42"),
            new AssistantActivityResponse(
                UUID.randomUUID(), "Tạo bài học mới: Tư duy logic nâng cao", "Hôm qua, 14:20")),
        "https://cdn.studyweb.edu/avatars/assistant1.png");
  }

  private static final UUID VIP_REQUEST_ID_1 =
      UUID.fromString("d3eeaa11-2222-3333-4444-555566667777");
  private static final UUID VIP_REQUEST_ID_2 =
      UUID.fromString("e4ffbb22-3333-4444-5555-666677778888");

  private VipRequestResponse sampleVipRequestResponse(
      UUID id, UUID userId, VipRequestStatus status) {
    return new VipRequestResponse(
        id,
        userId,
        "Nguyễn Văn A",
        "learner@studyweb.edu",
        "https://cdn.studyweb.edu/avatars/nguyenvana.png",
        "React Masterclass",
        "Cần kích hoạt VIP để học chuyên sâu",
        LocalDate.of(2026, 8, 20),
        status);
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
      when(systemManagementService.listLearners(isNull(), isNull(), any(Pageable.class)))
          .thenReturn(new PageImpl<>(List.of()));

      mockMvc.perform(get("/api/system-management/learners")).andExpect(status().isOk());

      ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
      verify(systemManagementService).listLearners(isNull(), isNull(), pageableCaptor.capture());

      Pageable bound = pageableCaptor.getValue();
      assertThat(bound.getPageNumber()).isEqualTo(0);
      assertThat(bound.getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("GET /learners - Custom query params bind page, size, search, and sort")
    void getLearners_bindsCustomQueryParams() throws Exception {
      when(systemManagementService.listLearners(eq("nguyen"), isNull(), any(Pageable.class)))
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
      verify(systemManagementService)
          .listLearners(eq("nguyen"), isNull(), pageableCaptor.capture());

      Pageable bound = pageableCaptor.getValue();
      assertThat(bound.getPageNumber()).isEqualTo(2);
      assertThat(bound.getPageSize()).isEqualTo(25);
      assertThat(bound.getSort().getOrderFor("name")).isNotNull();
      assertThat(bound.getSort().getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("GET /assistants - Default pagination binds page=0, size=10")
    void getAssistants_bindsDefaultPagination() throws Exception {
      when(systemManagementService.listAssistants(isNull(), isNull(), any(Pageable.class)))
          .thenReturn(new PageImpl<>(List.of()));

      mockMvc.perform(get("/api/system-management/assistants")).andExpect(status().isOk());

      ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
      verify(systemManagementService).listAssistants(isNull(), isNull(), pageableCaptor.capture());

      Pageable bound = pageableCaptor.getValue();
      assertThat(bound.getPageNumber()).isEqualTo(0);
      assertThat(bound.getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("GET /assistants - Custom query params bind page, size, search, and sort")
    void getAssistants_bindsCustomQueryParams() throws Exception {
      when(systemManagementService.listAssistants(eq("hieu"), isNull(), any(Pageable.class)))
          .thenReturn(new PageImpl<>(List.of()));

      mockMvc
          .perform(
              get("/api/system-management/assistants")
                  .param("search", "hieu")
                  .param("page", "1")
                  .param("size", "20")
                  .param("sort", "name,asc"))
          .andExpect(status().isOk());

      ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
      verify(systemManagementService)
          .listAssistants(eq("hieu"), isNull(), pageableCaptor.capture());

      Pageable bound = pageableCaptor.getValue();
      assertThat(bound.getPageNumber()).isEqualTo(1);
      assertThat(bound.getPageSize()).isEqualTo(20);
      assertThat(bound.getSort().getOrderFor("name")).isNotNull();
      assertThat(bound.getSort().getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("Assistant path endpoints - Path variable string binds to UUID")
    void assistantPathEndpoints_bindPathVariableToUuid() throws Exception {
      doNothing()
          .when(systemManagementService)
          .switchUserStatus(ASSISTANT_ID, UserStatus.INACTIVE, UserRole.ASSISTANT);
      doNothing()
          .when(systemManagementService)
          .switchUserStatus(ASSISTANT_ID, UserStatus.ACTIVE, UserRole.ASSISTANT);
      doNothing()
          .when(systemManagementService)
          .switchUserStatus(ASSISTANT_ID, UserStatus.BANNED, UserRole.ASSISTANT);

      mockMvc
          .perform(
              patch("/api/system-management/assistants/{id}/deactivate", ASSISTANT_ID.toString()))
          .andExpect(status().isOk());
      verify(systemManagementService)
          .switchUserStatus(ASSISTANT_ID, UserStatus.INACTIVE, UserRole.ASSISTANT);

      mockMvc
          .perform(
              patch("/api/system-management/assistants/{id}/activate", ASSISTANT_ID.toString()))
          .andExpect(status().isOk());
      verify(systemManagementService)
          .switchUserStatus(ASSISTANT_ID, UserStatus.ACTIVE, UserRole.ASSISTANT);

      mockMvc
          .perform(patch("/api/system-management/assistants/{id}/ban", ASSISTANT_ID.toString()))
          .andExpect(status().isOk());
      verify(systemManagementService)
          .switchUserStatus(ASSISTANT_ID, UserStatus.BANNED, UserRole.ASSISTANT);
    }

    @Test
    @DisplayName("POST /assistants - JSON body deserializes all fields")
    void createAssistant_deserializesAllFields() throws Exception {
      CreateAssistantRequest request = sampleCreateAssistantRequest();
      doNothing().when(systemManagementService).createAssistant(any(CreateAssistantRequest.class));

      mockMvc
          .perform(
              post("/api/system-management/assistants")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk());

      ArgumentCaptor<CreateAssistantRequest> captor =
          ArgumentCaptor.forClass(CreateAssistantRequest.class);
      verify(systemManagementService).createAssistant(captor.capture());

      CreateAssistantRequest bound = captor.getValue();
      assertThat(bound.name()).isEqualTo("Trần Minh Hiếu");
      assertThat(bound.gmail()).isEqualTo("hieu.tm@cus.edu.vn");
      assertThat(bound.phone()).isEqualTo("0901111222");
      assertThat(bound.password()).isEqualTo("CustomPass@123");
    }

    @Test
    @DisplayName("PATCH /{id}/lock, /{id}/unlock, /{id}/ban - Path variable string binds to UUID")
    void pathEndpoints_bindPathVariableToUuid() throws Exception {
      doNothing()
          .when(systemManagementService)
          .switchUserStatus(LEARNER_ID, UserStatus.INACTIVE, UserRole.LEARNER);
      doNothing()
          .when(systemManagementService)
          .switchUserStatus(LEARNER_ID, UserStatus.ACTIVE, UserRole.LEARNER);
      doNothing()
          .when(systemManagementService)
          .switchUserStatus(LEARNER_ID, UserStatus.BANNED, UserRole.LEARNER);

      mockMvc
          .perform(patch("/api/system-management/learners/{id}/lock", LEARNER_ID.toString()))
          .andExpect(status().isOk());
      verify(systemManagementService)
          .switchUserStatus(LEARNER_ID, UserStatus.INACTIVE, UserRole.LEARNER);

      mockMvc
          .perform(patch("/api/system-management/learners/{id}/unlock", LEARNER_ID.toString()))
          .andExpect(status().isOk());
      verify(systemManagementService)
          .switchUserStatus(LEARNER_ID, UserStatus.ACTIVE, UserRole.LEARNER);

      mockMvc
          .perform(patch("/api/system-management/learners/{id}/ban", LEARNER_ID.toString()))
          .andExpect(status().isOk());
      verify(systemManagementService)
          .switchUserStatus(LEARNER_ID, UserStatus.BANNED, UserRole.LEARNER);
    }

    @Test
    @DisplayName(
        "POST /learners/create-vip-account - JSON body deserializes all fields including ISO LocalDate")
    void createVipAccount_deserializesAllFields() throws Exception {
      LocalDate start = LocalDate.of(2026, 8, 18);
      LocalDate end = LocalDate.of(2027, 8, 18);

      CreateVipAccountRequest request =
          new CreateVipAccountRequest(
              "Trần Thị B",
              "vip.learner@studyweb.edu",
              start,
              end,
              "Ghi chú kích hoạt",
              "Password@123");

      doNothing()
          .when(systemManagementService)
          .createVipAccount(any(CreateVipAccountRequest.class));

      mockMvc
          .perform(
              post("/api/system-management/learners/create-vip-account")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk());

      ArgumentCaptor<CreateVipAccountRequest> captor =
          ArgumentCaptor.forClass(CreateVipAccountRequest.class);
      verify(systemManagementService).createVipAccount(captor.capture());

      CreateVipAccountRequest bound = captor.getValue();
      assertThat(bound.name()).isEqualTo("Trần Thị B");
      assertThat(bound.gmail()).isEqualTo("vip.learner@studyweb.edu");
      assertThat(bound.startDate()).isEqualTo(start);
      assertThat(bound.endDate()).isEqualTo(end);
      assertThat(bound.note()).isEqualTo("Ghi chú kích hoạt");
      assertThat(bound.password()).isEqualTo("Password@123");
    }

    @Test
    @DisplayName(
        "PATCH /learners/{id}/update-account - JSON body deserializes all fields for update")
    void updateAccount_deserializesAllFields() throws Exception {
      UpdateAccountRequest request = sampleUpdateAccountRequest();
      doNothing()
          .when(systemManagementService)
          .updateLearnerAccount(eq(LEARNER_ID), any(UpdateAccountRequest.class));

      mockMvc
          .perform(
              patch("/api/system-management/learners/{id}/update-account", LEARNER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk());

      ArgumentCaptor<UpdateAccountRequest> captor =
          ArgumentCaptor.forClass(UpdateAccountRequest.class);
      verify(systemManagementService).updateLearnerAccount(eq(LEARNER_ID), captor.capture());

      UpdateAccountRequest bound = captor.getValue();
      assertThat(bound.name()).isEqualTo("Trần Thị B");
      assertThat(bound.gmail()).isEqualTo("vip.learner@studyweb.edu");
      assertThat(bound.tier()).isEqualTo(UserTier.VIP);
      assertThat(bound.note()).isEqualTo("Ghi chú cập nhật");
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
    @DisplayName("POST /assistants - Rejects blank or empty name")
    void createAssistant_rejectsBlankName(String invalidName) throws Exception {
      CreateAssistantRequest request =
          new CreateAssistantRequest(
              invalidName, "assistant@cus.edu.vn", "0901111222", "CustomPass@123");

      mockMvc
          .perform(
              post("/api/system-management/assistants")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.statusCode").value(400))
          .andExpect(jsonPath("$.errorCode").value(SystemErrorCode.VALIDATION_ERROR.code()))
          .andExpect(jsonPath("$.message").value("Name is required"));

      verify(systemManagementService, never()).createAssistant(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("POST /assistants - Rejects null or empty email")
    void createAssistant_rejectsNullOrEmptyEmail(String invalidGmail) throws Exception {
      CreateAssistantRequest request =
          new CreateAssistantRequest(
              "Trần Minh Hiếu", invalidGmail, "0901111222", "CustomPass@123");

      mockMvc
          .perform(
              post("/api/system-management/assistants")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.statusCode").value(400))
          .andExpect(jsonPath("$.errorCode").value(SystemErrorCode.VALIDATION_ERROR.code()))
          .andExpect(jsonPath("$.message").value("Email is required"));

      verify(systemManagementService, never()).createAssistant(any());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
          "invalid-email-string",
          "user@",
          "@domain.com",
          "user@domain..com",
          "plainaddress"
        })
    @DisplayName("POST /assistants - Rejects invalid email format")
    void createAssistant_rejectsInvalidEmailFormat(String invalidEmail) throws Exception {
      CreateAssistantRequest request =
          new CreateAssistantRequest(
              "Trần Minh Hiếu", invalidEmail, "0901111222", "CustomPass@123");

      mockMvc
          .perform(
              post("/api/system-management/assistants")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.statusCode").value(400))
          .andExpect(jsonPath("$.errorCode").value(SystemErrorCode.VALIDATION_ERROR.code()))
          .andExpect(jsonPath("$.message").value("Invalid email format"));

      verify(systemManagementService, never()).createAssistant(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("POST /assistants - Rejects blank password")
    void createAssistant_rejectsBlankPassword(String invalidPassword) throws Exception {
      CreateAssistantRequest request =
          new CreateAssistantRequest(
              "Trần Minh Hiếu", "assistant@cus.edu.vn", "0901111222", invalidPassword);

      mockMvc
          .perform(
              post("/api/system-management/assistants")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.statusCode").value(400))
          .andExpect(jsonPath("$.errorCode").value(SystemErrorCode.VALIDATION_ERROR.code()))
          .andExpect(
              jsonPath("$.message")
                  .value(
                      Matchers.anyOf(
                          Matchers.is("Password is required"),
                          Matchers.is("Password must contain 8 characters"))));

      verify(systemManagementService, never()).createAssistant(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"short", "1234567"})
    @DisplayName("POST & PATCH createVipAccount - Rejects invalid password size")
    void createAndUpdate_rejectsInvalidPasswordSize(String invalidPassword) throws Exception {
      CreateVipAccountRequest request =
          new CreateVipAccountRequest(
              "Nguyễn Văn A",
              "valid@studyweb.edu",
              LocalDate.of(2026, 8, 18),
              LocalDate.of(2027, 8, 18),
              null,
              invalidPassword);

      mockMvc
          .perform(
              post("/api/system-management/learners/create-vip-account")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.statusCode").value(400))
          .andExpect(jsonPath("$.errorCode").value(SystemErrorCode.VALIDATION_ERROR.code()))
          .andExpect(jsonPath("$.message").value("Password must contain 8 characters"));

      verify(systemManagementService, never()).createVipAccount(any());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
          "invalid-email-string",
          "user@",
          "@domain.com",
          "user@domain..com",
          "plainaddress"
        })
    @DisplayName("POST & PATCH createVipAccount - Rejects invalid email format")
    void createAndUpdate_rejectsInvalidEmail(String invalidGmail) throws Exception {
      CreateVipAccountRequest request =
          new CreateVipAccountRequest(
              "Nguyễn Văn A",
              invalidGmail,
              LocalDate.of(2026, 8, 18),
              LocalDate.of(2027, 8, 18),
              null,
              "Password@123");

      mockMvc
          .perform(
              post("/api/system-management/learners/create-vip-account")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.statusCode").value(400))
          .andExpect(jsonPath("$.errorCode").value(SystemErrorCode.VALIDATION_ERROR.code()))
          .andExpect(jsonPath("$.message").value("Invalid email format"));

      UpdateAccountRequest updateReq =
          new UpdateAccountRequest(
              "Nguyễn Văn A",
              invalidGmail,
              LocalDate.of(2026, 8, 18),
              LocalDate.of(2027, 8, 18),
              null,
              UserTier.VIP,
              "Password@123");

      mockMvc
          .perform(
              patch("/api/system-management/learners/{id}/update-account", LEARNER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(updateReq)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.statusCode").value(400))
          .andExpect(jsonPath("$.errorCode").value(SystemErrorCode.VALIDATION_ERROR.code()))
          .andExpect(jsonPath("$.message").value("Invalid email format"));

      verify(systemManagementService, never()).createVipAccount(any());
      verify(systemManagementService, never()).updateLearnerAccount(any(), any());
    }

    @Test
    @DisplayName("POST & PATCH - Rejects null startDate")
    void createAndUpdate_rejectsNullStartDate() throws Exception {
      CreateVipAccountRequest request =
          new CreateVipAccountRequest(
              "Nguyễn Văn A",
              "valid@studyweb.edu",
              null,
              LocalDate.of(2027, 8, 18),
              null,
              "Password@123");

      mockMvc
          .perform(
              post("/api/system-management/learners/create-vip-account")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.statusCode").value(400))
          .andExpect(jsonPath("$.errorCode").value(SystemErrorCode.VALIDATION_ERROR.code()))
          .andExpect(jsonPath("$.message").value("Start date is required"));

      verify(systemManagementService, never()).createVipAccount(any());
    }

    @Test
    @DisplayName("POST & PATCH - Rejects null endDate")
    void createAndUpdate_rejectsNullEndDate() throws Exception {
      CreateVipAccountRequest request =
          new CreateVipAccountRequest(
              "Nguyễn Văn A",
              "valid@studyweb.edu",
              LocalDate.of(2026, 8, 18),
              null,
              null,
              "Password@123");

      mockMvc
          .perform(
              post("/api/system-management/learners/create-vip-account")
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
    @DisplayName("GET /learners - Delegates exact search term and returns wrapped PageResponse")
    void getLearners_delegatesAndWrapsResponse() throws Exception {
      LearnerSummaryResponse item = sampleLearnerResponse();
      PageImpl<LearnerSummaryResponse> serviceResult =
          new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1);

      when(systemManagementService.listLearners(
              eq("target_learner"), isNull(), any(Pageable.class)))
          .thenReturn(serviceResult);

      mockMvc
          .perform(
              get("/api/system-management/learners")
                  .param("search", "target_learner")
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Learners fetched successfully!"))
          .andExpect(jsonPath("$.data[0].id").value(LEARNER_ID.toString()))
          .andExpect(jsonPath("$.data[0].name").value("Nguyễn Văn A"))
          .andExpect(jsonPath("$.data[0].tier").value("VIP"))
          .andExpect(
              jsonPath("$.data[0].avatarUrl")
                  .value("https://cdn.studyweb.edu/avatars/nguyenvana.png"))
          .andExpect(jsonPath("$.paging.total").value(1));

      verify(systemManagementService)
          .listLearners(eq("target_learner"), isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName(
        "PATCH /learners/{id}/lock - Delegates to switchUserStatus and wraps in SuccessResponse")
    void lockLearner_delegatesAndWrapsSuccessResponse() throws Exception {
      doNothing()
          .when(systemManagementService)
          .switchUserStatus(LEARNER_ID, UserStatus.INACTIVE, UserRole.LEARNER);

      mockMvc
          .perform(patch("/api/system-management/learners/{id}/lock", LEARNER_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Lock learner successfully."));

      verify(systemManagementService)
          .switchUserStatus(LEARNER_ID, UserStatus.INACTIVE, UserRole.LEARNER);
    }

    @Test
    @DisplayName(
        "PATCH /learners/{id}/unlock - Delegates to switchUserStatus and wraps in SuccessResponse")
    void unlockLearner_delegatesAndWrapsSuccessResponse() throws Exception {
      doNothing()
          .when(systemManagementService)
          .switchUserStatus(LEARNER_ID, UserStatus.ACTIVE, UserRole.LEARNER);

      mockMvc
          .perform(patch("/api/system-management/learners/{id}/unlock", LEARNER_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Unlock learner successfully."));

      verify(systemManagementService)
          .switchUserStatus(LEARNER_ID, UserStatus.ACTIVE, UserRole.LEARNER);
    }

    @Test
    @DisplayName(
        "GET /assistants - Delegates search and returns wrapped PageResponse with activities")
    void getAssistants_delegatesAndWrapsResponse() throws Exception {
      AssistantSummaryResponse item = sampleAssistantSummaryResponse();
      PageImpl<AssistantSummaryResponse> serviceResult =
          new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1);

      when(systemManagementService.listAssistants(eq("hieu"), isNull(), any(Pageable.class)))
          .thenReturn(serviceResult);

      mockMvc
          .perform(
              get("/api/system-management/assistants")
                  .param("search", "hieu")
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Assistants fetched successfully!"))
          .andExpect(jsonPath("$.data[0].id").value(ASSISTANT_ID.toString()))
          .andExpect(jsonPath("$.data[0].name").value("Trần Minh Hiếu"))
          .andExpect(jsonPath("$.data[0].status").value("ACTIVE"))
          .andExpect(jsonPath("$.data[0].numExams").value(12))
          .andExpect(
              jsonPath("$.data[0].recentActivities[0].description")
                  .value("Đăng tải đề thi V-ACT mã đề 007"))
          .andExpect(
              jsonPath("$.data[0].avatarUrl")
                  .value("https://cdn.studyweb.edu/avatars/assistant1.png"))
          .andExpect(jsonPath("$.paging.total").value(1));

      verify(systemManagementService).listAssistants(eq("hieu"), isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("POST /assistants - Delegates to createAssistant and wraps payload")
    void createAssistant_delegatesAndWrapsPayload() throws Exception {
      CreateAssistantRequest request = sampleCreateAssistantRequest();
      doNothing().when(systemManagementService).createAssistant(any(CreateAssistantRequest.class));

      mockMvc
          .perform(
              post("/api/system-management/assistants")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Assistant created successfully!"));

      verify(systemManagementService).createAssistant(any(CreateAssistantRequest.class));
    }

    @Test
    @DisplayName("PATCH /assistants/{id}/deactivate - Delegates to switchUserStatus")
    void deactivateAssistant_delegatesAndWrapsResponse() throws Exception {
      doNothing()
          .when(systemManagementService)
          .switchUserStatus(ASSISTANT_ID, UserStatus.INACTIVE, UserRole.ASSISTANT);

      mockMvc
          .perform(patch("/api/system-management/assistants/{id}/deactivate", ASSISTANT_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Deactivate assistant successfully."));

      verify(systemManagementService)
          .switchUserStatus(ASSISTANT_ID, UserStatus.INACTIVE, UserRole.ASSISTANT);
    }

    @Test
    @DisplayName("PATCH /assistants/{id}/activate - Delegates to switchUserStatus")
    void activateAssistant_delegatesAndWrapsResponse() throws Exception {
      doNothing()
          .when(systemManagementService)
          .switchUserStatus(ASSISTANT_ID, UserStatus.ACTIVE, UserRole.ASSISTANT);

      mockMvc
          .perform(patch("/api/system-management/assistants/{id}/activate", ASSISTANT_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Activate assistant successfully."));

      verify(systemManagementService)
          .switchUserStatus(ASSISTANT_ID, UserStatus.ACTIVE, UserRole.ASSISTANT);
    }

    @Test
    @DisplayName("PATCH /assistants/{id}/ban - Delegates to switchUserStatus")
    void banAssistant_delegatesAndWrapsResponse() throws Exception {
      doNothing()
          .when(systemManagementService)
          .switchUserStatus(ASSISTANT_ID, UserStatus.BANNED, UserRole.ASSISTANT);

      mockMvc
          .perform(patch("/api/system-management/assistants/{id}/ban", ASSISTANT_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Ban assistant successfully."));

      verify(systemManagementService)
          .switchUserStatus(ASSISTANT_ID, UserStatus.BANNED, UserRole.ASSISTANT);
    }

    @Test
    @DisplayName(
        "PATCH /learners/{id}/ban - Delegates to switchUserStatus and wraps in SuccessResponse")
    void banLearner_delegatesAndWrapsSuccessResponse() throws Exception {
      doNothing()
          .when(systemManagementService)
          .switchUserStatus(LEARNER_ID, UserStatus.BANNED, UserRole.LEARNER);

      mockMvc
          .perform(patch("/api/system-management/learners/{id}/ban", LEARNER_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Ban learner successfully."));

      verify(systemManagementService)
          .switchUserStatus(LEARNER_ID, UserStatus.BANNED, UserRole.LEARNER);
    }

    @Test
    @DisplayName(
        "POST /learners/create-vip-account - Delegates request to createVipAccount and wraps payload")
    void createVipAccount_delegatesAndWrapsPayload() throws Exception {
      CreateVipAccountRequest request = sampleVipRequest();
      doNothing()
          .when(systemManagementService)
          .createVipAccount(any(CreateVipAccountRequest.class));

      mockMvc
          .perform(
              post("/api/system-management/learners/create-vip-account")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("A VIP Learner created successfully!"));

      verify(systemManagementService).createVipAccount(any(CreateVipAccountRequest.class));
    }

    @Test
    @DisplayName(
        "PATCH /learners/{id}/update-account - Delegates request to updateLearnerAccount and wraps payload")
    void updateAccount_delegatesAndWrapsPayload() throws Exception {
      UpdateAccountRequest request = sampleUpdateAccountRequest();
      doNothing()
          .when(systemManagementService)
          .updateLearnerAccount(eq(LEARNER_ID), any(UpdateAccountRequest.class));

      mockMvc
          .perform(
              patch("/api/system-management/learners/{id}/update-account", LEARNER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Update learner account succesfully!"));

      verify(systemManagementService)
          .updateLearnerAccount(eq(LEARNER_ID), any(UpdateAccountRequest.class));
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
    @DisplayName("AdminException(USER_EXISTED) on createAssistant -> 409 CONFLICT ADMIN_005")
    void createAssistant_emailExists_translatesTo409() throws Exception {
      doThrow(new AdminException(AdminErrorCode.USER_EXISTED))
          .when(systemManagementService)
          .createAssistant(any(CreateAssistantRequest.class));

      mockMvc
          .perform(
              post("/api/system-management/assistants")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(sampleCreateAssistantRequest())))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.statusCode").value(409))
          .andExpect(jsonPath("$.errorCode").value(AdminErrorCode.USER_EXISTED.code()))
          .andExpect(jsonPath("$.message").value(AdminErrorCode.USER_EXISTED.message()));
    }

    @Test
    @DisplayName("AdminException(USER_BANNED) on createAssistant -> 403 FORBIDDEN ADMIN_001")
    void createAssistant_accountBanned_translatesTo403() throws Exception {
      doThrow(new AdminException(AdminErrorCode.USER_BANNED))
          .when(systemManagementService)
          .createAssistant(any(CreateAssistantRequest.class));

      mockMvc
          .perform(
              post("/api/system-management/assistants")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(sampleCreateAssistantRequest())))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.statusCode").value(403))
          .andExpect(jsonPath("$.errorCode").value(AdminErrorCode.USER_BANNED.code()))
          .andExpect(jsonPath("$.message").value(AdminErrorCode.USER_BANNED.message()));
    }

    @Test
    @DisplayName("AdminException(USER_NOT_FOUND) on deactivateAssistant -> 404 NOT_FOUND ADMIN_003")
    void deactivateAssistant_notFound_translatesTo404() throws Exception {
      doThrow(new AdminException(AdminErrorCode.USER_NOT_FOUND))
          .when(systemManagementService)
          .switchUserStatus(ASSISTANT_ID, UserStatus.INACTIVE, UserRole.ASSISTANT);

      mockMvc
          .perform(patch("/api/system-management/assistants/{id}/deactivate", ASSISTANT_ID))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.statusCode").value(404))
          .andExpect(jsonPath("$.errorCode").value(AdminErrorCode.USER_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("AdminException(USER_NOT_FOUND) on activateAssistant -> 404 NOT_FOUND ADMIN_003")
    void activateAssistant_notFound_translatesTo404() throws Exception {
      doThrow(new AdminException(AdminErrorCode.USER_NOT_FOUND))
          .when(systemManagementService)
          .switchUserStatus(ASSISTANT_ID, UserStatus.ACTIVE, UserRole.ASSISTANT);

      mockMvc
          .perform(patch("/api/system-management/assistants/{id}/activate", ASSISTANT_ID))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.statusCode").value(404))
          .andExpect(jsonPath("$.errorCode").value(AdminErrorCode.USER_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("AdminException(USER_NOT_FOUND) on banAssistant -> 404 NOT_FOUND ADMIN_003")
    void banAssistant_notFound_translatesTo404() throws Exception {
      doThrow(new AdminException(AdminErrorCode.USER_NOT_FOUND))
          .when(systemManagementService)
          .switchUserStatus(ASSISTANT_ID, UserStatus.BANNED, UserRole.ASSISTANT);

      mockMvc
          .perform(patch("/api/system-management/assistants/{id}/ban", ASSISTANT_ID))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.statusCode").value(404))
          .andExpect(jsonPath("$.errorCode").value(AdminErrorCode.USER_NOT_FOUND.code()));
    }

    @Test
    @DisplayName(
        "AdminException(USER_NOT_FOUND) on ban -> 404 NOT_FOUND with ErrorResponse ADMIN_003")
    void ban_userNotFound_translatesTo404() throws Exception {
      doThrow(new AdminException(AdminErrorCode.USER_NOT_FOUND))
          .when(systemManagementService)
          .switchUserStatus(LEARNER_ID, UserStatus.BANNED, UserRole.LEARNER);

      mockMvc
          .perform(patch("/api/system-management/learners/{id}/ban", LEARNER_ID))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.statusCode").value(404))
          .andExpect(jsonPath("$.errorCode").value(AdminErrorCode.USER_NOT_FOUND.code()))
          .andExpect(jsonPath("$.message").value(AdminErrorCode.USER_NOT_FOUND.message()));
    }

    @Test
    @DisplayName("AdminException(USER_NOT_FOUND) on lock -> 404 NOT_FOUND with ADMIN_003")
    void lock_userNotFound_translatesTo404() throws Exception {
      doThrow(new AdminException(AdminErrorCode.USER_NOT_FOUND))
          .when(systemManagementService)
          .switchUserStatus(LEARNER_ID, UserStatus.INACTIVE, UserRole.LEARNER);

      mockMvc
          .perform(patch("/api/system-management/learners/{id}/lock", LEARNER_ID))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.statusCode").value(404))
          .andExpect(jsonPath("$.errorCode").value(AdminErrorCode.USER_NOT_FOUND.code()))
          .andExpect(jsonPath("$.message").value(AdminErrorCode.USER_NOT_FOUND.message()));
    }

    @Test
    @DisplayName("AdminException(USER_NOT_FOUND) on updateAccount -> 404 NOT_FOUND")
    void updateAccount_userNotFound_translatesTo404() throws Exception {
      doThrow(new AdminException(AdminErrorCode.USER_NOT_FOUND))
          .when(systemManagementService)
          .updateLearnerAccount(eq(LEARNER_ID), any(UpdateAccountRequest.class));

      mockMvc
          .perform(
              patch("/api/system-management/learners/{id}/update-account", LEARNER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(sampleUpdateAccountRequest())))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.statusCode").value(404))
          .andExpect(jsonPath("$.errorCode").value(AdminErrorCode.USER_NOT_FOUND.code()))
          .andExpect(jsonPath("$.message").value(AdminErrorCode.USER_NOT_FOUND.message()));
    }

    @Test
    @DisplayName("SystemException(INTERNAL_ERROR) -> 500 INTERNAL_ERROR with ErrorResponse SYS_001")
    void systemException_translatesTo500() throws Exception {
      doThrow(
              new SystemException(
                  SystemErrorCode.INTERNAL_ERROR, "No value provided for default password!"))
          .when(systemManagementService)
          .createVipAccount(any(CreateVipAccountRequest.class));

      CreateVipAccountRequest request = sampleVipRequest();

      mockMvc
          .perform(
              post("/api/system-management/learners/create-vip-account")
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
          .perform(put("/api/system-management/learners/" + LEARNER_ID + "/update-account"))
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
          .switchUserStatus(LEARNER_ID, UserStatus.INACTIVE, UserRole.LEARNER);

      mockMvc
          .perform(patch("/api/system-management/learners/{id}/lock", LEARNER_ID))
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
          .perform(patch("/api/system-management/learners/{id}/lock", LEARNER_ID))
          .andExpect(status().isUnauthorized());
      mockMvc
          .perform(patch("/api/system-management/learners/{id}/unlock", LEARNER_ID))
          .andExpect(status().isUnauthorized());
      mockMvc
          .perform(get("/api/system-management/assistants"))
          .andExpect(status().isUnauthorized());
      mockMvc
          .perform(
              post("/api/system-management/assistants")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(sampleCreateAssistantRequest())))
          .andExpect(status().isUnauthorized());
      mockMvc
          .perform(patch("/api/system-management/assistants/{id}/deactivate", ASSISTANT_ID))
          .andExpect(status().isUnauthorized());
      mockMvc
          .perform(patch("/api/system-management/assistants/{id}/activate", ASSISTANT_ID))
          .andExpect(status().isUnauthorized());
      mockMvc
          .perform(patch("/api/system-management/assistants/{id}/ban", ASSISTANT_ID))
          .andExpect(status().isUnauthorized());
      mockMvc
          .perform(patch("/api/system-management/learners/{id}/ban", LEARNER_ID))
          .andExpect(status().isUnauthorized());
      mockMvc
          .perform(
              post("/api/system-management/learners/create-vip-account")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(sampleVipRequest())))
          .andExpect(status().isUnauthorized());
      mockMvc
          .perform(
              patch("/api/system-management/learners/{id}/update-account", LEARNER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(sampleUpdateAccountRequest())))
          .andExpect(status().isUnauthorized());

      verify(systemManagementService, never()).listLearners(any(), any(), any());
      verify(systemManagementService, never()).switchUserStatus(any(), any(), any());
      verify(systemManagementService, never()).listAssistants(any(), any(), any());
      verify(systemManagementService, never()).createAssistant(any());
      verify(systemManagementService, never()).createVipAccount(any());
      verify(systemManagementService, never()).updateLearnerAccount(any(), any());
    }

    @Test
    @DisplayName("Non-ADMIN roles (LEARNER) are blocked -> 403 FORBIDDEN on all endpoints")
    @WithMockUser(roles = "LEARNER")
    void learnerRole_blockedWith403() throws Exception {
      mockMvc.perform(get("/api/system-management/learners")).andExpect(status().isForbidden());
      mockMvc.perform(get("/api/system-management/assistants")).andExpect(status().isForbidden());
      mockMvc
          .perform(
              post("/api/system-management/assistants")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(sampleCreateAssistantRequest())))
          .andExpect(status().isForbidden());
      mockMvc
          .perform(patch("/api/system-management/assistants/{id}/deactivate", ASSISTANT_ID))
          .andExpect(status().isForbidden());
      mockMvc
          .perform(patch("/api/system-management/assistants/{id}/activate", ASSISTANT_ID))
          .andExpect(status().isForbidden());
      mockMvc
          .perform(patch("/api/system-management/assistants/{id}/ban", ASSISTANT_ID))
          .andExpect(status().isForbidden());
      mockMvc
          .perform(patch("/api/system-management/learners/{id}/lock", LEARNER_ID))
          .andExpect(status().isForbidden());
      mockMvc
          .perform(patch("/api/system-management/learners/{id}/unlock", LEARNER_ID))
          .andExpect(status().isForbidden());
      mockMvc
          .perform(patch("/api/system-management/learners/{id}/ban", LEARNER_ID))
          .andExpect(status().isForbidden());
      mockMvc
          .perform(
              post("/api/system-management/learners/create-vip-account")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(sampleVipRequest())))
          .andExpect(status().isForbidden());
      mockMvc
          .perform(
              patch("/api/system-management/learners/{id}/update-account", LEARNER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(sampleUpdateAccountRequest())))
          .andExpect(status().isForbidden());

      verify(systemManagementService, never()).listLearners(any(), any(), any());
      verify(systemManagementService, never()).switchUserStatus(any(), any(), any());
      verify(systemManagementService, never()).listAssistants(any(), any(), any());
      verify(systemManagementService, never()).createAssistant(any());
      verify(systemManagementService, never()).createVipAccount(any());
      verify(systemManagementService, never()).updateLearnerAccount(any(), any());
    }

    @Test
    @DisplayName("Non-ADMIN roles (ASSISTANT) are blocked -> 403 FORBIDDEN on all endpoints")
    @WithMockUser(roles = "ASSISTANT")
    void assistantRole_blockedWith403() throws Exception {
      mockMvc.perform(get("/api/system-management/learners")).andExpect(status().isForbidden());
      mockMvc.perform(get("/api/system-management/assistants")).andExpect(status().isForbidden());
      mockMvc
          .perform(
              post("/api/system-management/assistants")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(sampleCreateAssistantRequest())))
          .andExpect(status().isForbidden());
      mockMvc
          .perform(patch("/api/system-management/assistants/{id}/deactivate", ASSISTANT_ID))
          .andExpect(status().isForbidden());
      mockMvc
          .perform(patch("/api/system-management/assistants/{id}/activate", ASSISTANT_ID))
          .andExpect(status().isForbidden());
      mockMvc
          .perform(patch("/api/system-management/assistants/{id}/ban", ASSISTANT_ID))
          .andExpect(status().isForbidden());
      mockMvc
          .perform(patch("/api/system-management/learners/{id}/lock", LEARNER_ID))
          .andExpect(status().isForbidden());
      mockMvc
          .perform(patch("/api/system-management/learners/{id}/unlock", LEARNER_ID))
          .andExpect(status().isForbidden());
      mockMvc
          .perform(patch("/api/system-management/learners/{id}/ban", LEARNER_ID))
          .andExpect(status().isForbidden());
      mockMvc
          .perform(
              post("/api/system-management/learners/create-vip-account")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(sampleVipRequest())))
          .andExpect(status().isForbidden());
      mockMvc
          .perform(
              patch("/api/system-management/learners/{id}/update-account", LEARNER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(sampleUpdateAccountRequest())))
          .andExpect(status().isForbidden());
      mockMvc.perform(get("/api/system-management/vip-requests")).andExpect(status().isForbidden());
      mockMvc
          .perform(patch("/api/system-management/vip-requests/{id}/approve", VIP_REQUEST_ID_1))
          .andExpect(status().isForbidden());
      mockMvc
          .perform(patch("/api/system-management/vip-requests/{id}/disapprove", VIP_REQUEST_ID_1))
          .andExpect(status().isForbidden());

      verify(systemManagementService, never()).listLearners(any(), any(), any());
      verify(systemManagementService, never()).switchUserStatus(any(), any(), any());
      verify(systemManagementService, never()).listAssistants(any(), any(), any());
      verify(systemManagementService, never()).createAssistant(any());
      verify(systemManagementService, never()).createVipAccount(any());
      verify(systemManagementService, never()).updateLearnerAccount(any(), any());
      verify(systemManagementService, never()).getVipRequests(any(), any(), any());
      verify(systemManagementService, never()).approveVipRequest(any());
      verify(systemManagementService, never()).disapproveVipRequest(any());
    }

    @Test
    @DisplayName("ADMIN role is authorized -> 200 OK")
    @WithMockUser(roles = "ADMIN")
    void adminRole_authorized200() throws Exception {
      when(systemManagementService.listLearners(isNull(), isNull(), any(Pageable.class)))
          .thenReturn(new PageImpl<>(List.of()));
      when(systemManagementService.listAssistants(isNull(), isNull(), any(Pageable.class)))
          .thenReturn(new PageImpl<>(List.of()));
      when(systemManagementService.getVipRequests(isNull(), isNull(), any(Pageable.class)))
          .thenReturn(new PageImpl<>(List.of()));
      when(systemManagementService.getVipRequestCounts(isNull()))
          .thenReturn(new VipRequestCountResponse(0));

      mockMvc.perform(get("/api/system-management/learners")).andExpect(status().isOk());
      mockMvc
          .perform(patch("/api/system-management/learners/{id}/lock", LEARNER_ID))
          .andExpect(status().isOk());
      mockMvc
          .perform(patch("/api/system-management/learners/{id}/unlock", LEARNER_ID))
          .andExpect(status().isOk());
      mockMvc
          .perform(patch("/api/system-management/learners/{id}/ban", LEARNER_ID))
          .andExpect(status().isOk());
      mockMvc.perform(get("/api/system-management/assistants")).andExpect(status().isOk());
      mockMvc.perform(get("/api/system-management/vip-requests")).andExpect(status().isOk());
      mockMvc.perform(get("/api/system-management/vip-requests/counts")).andExpect(status().isOk());
      mockMvc
          .perform(patch("/api/system-management/vip-requests/{id}/approve", VIP_REQUEST_ID_1))
          .andExpect(status().isOk());
      mockMvc
          .perform(patch("/api/system-management/vip-requests/{id}/disapprove", VIP_REQUEST_ID_1))
          .andExpect(status().isOk());
    }
  }

  // =========================================================================
  // 6. GET VIP REQUESTS ENDPOINT TESTS
  // =========================================================================
  @Nested
  @DisplayName("6. GET /api/system-management/vip-requests")
  @WithMockUser(roles = "ADMIN")
  class GetVipRequestsEndpointTests {

    @Test
    @DisplayName(
        "Default parameters -> returns 200 OK with PageResponse<VipRequestResponse> payload")
    void getVipRequests_defaultParams_returns200AndListResponse() throws Exception {
      VipRequestResponse res1 =
          sampleVipRequestResponse(VIP_REQUEST_ID_1, LEARNER_ID, VipRequestStatus.WAITING);
      VipRequestResponse res2 =
          sampleVipRequestResponse(VIP_REQUEST_ID_2, LEARNER_ID, VipRequestStatus.APPROVED);
      Page<VipRequestResponse> mockPage =
          new PageImpl<>(List.of(res1, res2), PageRequest.of(0, 10), 2);

      when(systemManagementService.getVipRequests(isNull(), isNull(), any(Pageable.class)))
          .thenReturn(mockPage);

      mockMvc
          .perform(get("/api/system-management/vip-requests").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("VIP requests fetched successfully!"))
          .andExpect(jsonPath("$.paging.total").value(2))
          .andExpect(jsonPath("$.data").isArray())
          .andExpect(jsonPath("$.data[0].id").value(VIP_REQUEST_ID_1.toString()))
          .andExpect(jsonPath("$.data[0].userId").value(LEARNER_ID.toString()))
          .andExpect(jsonPath("$.data[0].name").value("Nguyễn Văn A"))
          .andExpect(jsonPath("$.data[0].gmail").value("learner@studyweb.edu"))
          .andExpect(jsonPath("$.data[0].note").value("Cần kích hoạt VIP để học chuyên sâu"))
          .andExpect(jsonPath("$.data[0].status").value("WAITING"))
          .andExpect(jsonPath("$.data[0].mainCourse").value("React Masterclass"))
          .andExpect(jsonPath("$.data[1].id").value(VIP_REQUEST_ID_2.toString()))
          .andExpect(jsonPath("$.data[1].status").value("APPROVED"));

      ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
      verify(systemManagementService).getVipRequests(isNull(), isNull(), pageableCaptor.capture());
      assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(0);
      assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("With search, status and pagination -> forwards all parameters to service")
    void getVipRequests_withSearchAndStatus_passesParamsToService() throws Exception {
      VipRequestResponse res1 =
          sampleVipRequestResponse(VIP_REQUEST_ID_1, LEARNER_ID, VipRequestStatus.WAITING);
      Page<VipRequestResponse> mockPage = new PageImpl<>(List.of(res1), PageRequest.of(1, 5), 1);

      when(systemManagementService.getVipRequests(
              eq("nguyen"), eq(VipRequestStatus.WAITING), any(Pageable.class)))
          .thenReturn(mockPage);

      mockMvc
          .perform(
              get("/api/system-management/vip-requests")
                  .param("search", "nguyen")
                  .param("status", "WAITING")
                  .param("page", "1")
                  .param("size", "5")
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200));

      ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
      verify(systemManagementService)
          .getVipRequests(eq("nguyen"), eq(VipRequestStatus.WAITING), pageableCaptor.capture());
      assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(1);
      assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(5);
    }
  }

  // =========================================================================
  // 7. APPROVE VIP REQUEST ENDPOINT TESTS
  // =========================================================================
  @Nested
  @DisplayName("7. PATCH /api/system-management/vip-requests/{id}/approve")
  @WithMockUser(roles = "ADMIN")
  class ApproveVipRequestEndpointTests {

    @Test
    @DisplayName("Valid ID -> calls service and returns 200 OK")
    void approveVipRequest_validId_returns200AndCallsService() throws Exception {
      doNothing().when(systemManagementService).approveVipRequest(VIP_REQUEST_ID_1);

      mockMvc
          .perform(
              patch("/api/system-management/vip-requests/{id}/approve", VIP_REQUEST_ID_1)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("VIP request approved successfully."));

      verify(systemManagementService).approveVipRequest(VIP_REQUEST_ID_1);
    }
  }

  // =========================================================================
  // 8. DISAPPROVE VIP REQUEST ENDPOINT TESTS
  // =========================================================================
  @Nested
  @DisplayName("8. PATCH /api/system-management/vip-requests/{id}/disapprove")
  @WithMockUser(roles = "ADMIN")
  class DisapproveVipRequestEndpointTests {

    @Test
    @DisplayName("Call /disapprove -> calls service and returns 200 OK")
    void disapproveVipRequest_disapprovePath_returns200AndCallsService() throws Exception {
      doNothing().when(systemManagementService).disapproveVipRequest(VIP_REQUEST_ID_1);

      mockMvc
          .perform(
              patch("/api/system-management/vip-requests/{id}/disapprove", VIP_REQUEST_ID_1)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("VIP request disapproved successfully."));

      verify(systemManagementService).disapproveVipRequest(VIP_REQUEST_ID_1);
    }
  }

  // =========================================================================
  // 9. USER COUNT ENDPOINTS TESTS
  // =========================================================================
  @Nested
  @DisplayName("9. User Count Endpoints Tests")
  @WithMockUser(roles = "ADMIN")
  class UserCountEndpointsTests {

    @Test
    @DisplayName("GET /learners/counts/normal -> returns normal learners count 200 OK")
    void getNormalLearnersCount_returns200() throws Exception {
      when(systemManagementService.getUserCount(UserRole.LEARNER, UserTier.NORMAL, null))
          .thenReturn(new UserCountResponse(15));

      mockMvc
          .perform(
              get("/api/system-management/learners/counts/normal")
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Normal learners count fetched successfully!"))
          .andExpect(jsonPath("$.data.count").value(15));

      verify(systemManagementService).getUserCount(UserRole.LEARNER, UserTier.NORMAL, null);
    }

    @Test
    @DisplayName("GET /learners/counts/vip -> returns VIP learners count 200 OK")
    void getVipLearnersCount_returns200() throws Exception {
      when(systemManagementService.getUserCount(UserRole.LEARNER, UserTier.VIP, null))
          .thenReturn(new UserCountResponse(5));

      mockMvc
          .perform(
              get("/api/system-management/learners/counts/vip")
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("VIP learners count fetched successfully!"))
          .andExpect(jsonPath("$.data.count").value(5));

      verify(systemManagementService).getUserCount(UserRole.LEARNER, UserTier.VIP, null);
    }

    @Test
    @DisplayName("GET /assistants/counts -> returns assistants count 200 OK")
    void getAssistantsCount_returns200() throws Exception {
      when(systemManagementService.getUserCount(UserRole.ASSISTANT, null, null))
          .thenReturn(new UserCountResponse(3));

      mockMvc
          .perform(
              get("/api/system-management/assistants/counts")
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Assistants count fetched successfully!"))
          .andExpect(jsonPath("$.data.count").value(3));

      verify(systemManagementService).getUserCount(UserRole.ASSISTANT, null, null);
    }

    @Test
    @DisplayName("GET /learners/counts/locked -> returns locked accounts count 200 OK")
    void getLockedAccountsCount_returns200() throws Exception {
      when(systemManagementService.getUserCount(null, null, UserStatus.INACTIVE))
          .thenReturn(new UserCountResponse(2));

      mockMvc
          .perform(
              get("/api/system-management/learners/counts/locked")
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Locked accounts count fetched successfully!"))
          .andExpect(jsonPath("$.data.count").value(2));

      verify(systemManagementService).getUserCount(null, null, UserStatus.INACTIVE);
    }
  }
}
