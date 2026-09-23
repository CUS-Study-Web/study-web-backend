package studyweb.cus.controller.registration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import studyweb.cus.controller.ResponseFactory;
import studyweb.cus.dto.request.registration.RegisterFormRequest;
import studyweb.cus.dto.response.registration.RegisterFormResponse;
import studyweb.cus.security.JwtAuthenticationFilter;
import studyweb.cus.service.registration.RegisterFormService;

@WebMvcTest(
    controllers = RegisterFormController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class))
@Import(ResponseFactory.class)
@TestPropertySource(properties = {"logging.loki.url=http://localhost:3100"})
class RegisterFormControllerTest {

  private static final UUID FORM_ID = UUID.randomUUID();

  @Autowired private MockMvc mockMvc;

  @MockitoBean private RegisterFormService registerFormService;

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  @TestConfiguration
  @EnableMethodSecurity
  static class SliceSecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      http.csrf(AbstractHttpConfigurer::disable)
          .authorizeHttpRequests(
              auth ->
                  auth.requestMatchers(HttpMethod.POST, "/api/register-forms/guest")
                      .permitAll()
                      .anyRequest()
                      .authenticated())
          .httpBasic(Customizer.withDefaults());
      return http.build();
    }
  }

  private RegisterFormResponse sampleResponse() {
    return new RegisterFormResponse(
        FORM_ID,
        "Nguyen Van A",
        "0987654321",
        "nguyenvana@example.com",
        "Toán học",
        "Đăng ký thi offline",
        LocalDate.of(2026, 10, 15),
        LocalDateTime.now());
  }

  // --- POST /api/register-forms/guest ---

  @Test
  @DisplayName("POST /api/register-forms/guest - Guest allowed without authentication")
  void registerForm_guestAllowedWithoutAuth() throws Exception {
    RegisterFormRequest request =
        new RegisterFormRequest(
            "Nguyen Van A",
            "0987654321",
            "nguyenvana@example.com",
            "Toán học",
            "Đăng ký thi offline",
            LocalDate.of(2026, 10, 15));

    when(registerFormService.createRegisterForm(any(RegisterFormRequest.class)))
        .thenReturn(sampleResponse());

    mockMvc
        .perform(
            post("/api/register-forms/guest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.id").value(FORM_ID.toString()))
        .andExpect(jsonPath("$.data.name").value("Nguyen Van A"))
        .andExpect(jsonPath("$.data.phoneNumer").value("0987654321"))
        .andExpect(jsonPath("$.data.email").value("nguyenvana@example.com"))
        .andExpect(jsonPath("$.data.subject").value("Toán học"))
        .andExpect(jsonPath("$.data.registeredDate").value("2026-10-15"));

    verify(registerFormService).createRegisterForm(any(RegisterFormRequest.class));
  }

  @Test
  @DisplayName("POST /api/register-forms/guest - Supports phoneNumber and registered_date aliases")
  void registerForm_supportsAliases() throws Exception {
    String jsonPayload =
        """
        {
          "name": "Le Thi C",
          "phoneNumber": "0912345678",
          "email": "lethic@example.com",
          "subject": "Vật lý",
          "registered_date": "2026-11-20"
        }
        """;

    when(registerFormService.createRegisterForm(any(RegisterFormRequest.class)))
        .thenReturn(sampleResponse());

    mockMvc
        .perform(
            post("/api/register-forms/guest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
        .andExpect(status().isOk());

    verify(registerFormService).createRegisterForm(any(RegisterFormRequest.class));
  }

  @Test
  @DisplayName("POST /api/register-forms/guest - Validation error on missing required fields returns 400")
  void registerForm_validationErrors() throws Exception {
    String invalidJson =
        """
        {
          "name": "",
          "phoneNumer": "",
          "email": "not-an-email"
        }
        """;

    mockMvc
        .perform(
            post("/api/register-forms/guest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST /api/register-forms/guest - Invalid phone number format returns 400")
  void registerForm_invalidPhoneReturns400() throws Exception {
    String invalidPhoneJson =
        """
        {
          "name": "Nguyen Van A",
          "phoneNumer": "0123456789",
          "email": "nguyenvana@example.com"
        }
        """;

    mockMvc
        .perform(
            post("/api/register-forms/guest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPhoneJson))
        .andExpect(status().isBadRequest());
  }

  // --- GET /api/register-forms ---

  @Test
  @WithMockUser(roles = "ADMIN")
  @DisplayName("GET /api/register-forms - Admin allowed to view all forms")
  void listRegisterForms_adminAllowed() throws Exception {
    when(registerFormService.listRegisterForms(any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(sampleResponse()), PageRequest.of(0, 10), 1));

    mockMvc
        .perform(get("/api/register-forms"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data[0].id").value(FORM_ID.toString()));
  }

  @Test
  @WithMockUser(roles = "ASSISTANT")
  @DisplayName("GET /api/register-forms - Assistant allowed to view all forms")
  void listRegisterForms_assistantAllowed() throws Exception {
    when(registerFormService.listRegisterForms(any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(sampleResponse()), PageRequest.of(0, 10), 1));

    mockMvc
        .perform(get("/api/register-forms"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200));
  }

  @Test
  @WithMockUser(roles = "LEARNER")
  @DisplayName("GET /api/register-forms - Learner forbidden from viewing forms")
  void listRegisterForms_learnerForbidden() throws Exception {
    mockMvc
        .perform(get("/api/register-forms"))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("GET /api/register-forms - Unauthenticated access returns 401")
  void listRegisterForms_unauthenticatedUnauthorized() throws Exception {
    mockMvc
        .perform(get("/api/register-forms"))
        .andExpect(status().isUnauthorized());
  }

  // --- GET /api/register-forms/{id} ---

  @Test
  @WithMockUser(roles = "ADMIN")
  @DisplayName("GET /api/register-forms/{id} - Admin allowed to view form by ID")
  void getRegisterForm_adminAllowed() throws Exception {
    when(registerFormService.getRegisterFormById(FORM_ID)).thenReturn(sampleResponse());

    mockMvc
        .perform(get("/api/register-forms/" + FORM_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.id").value(FORM_ID.toString()));
  }

  @Test
  @WithMockUser(roles = "ASSISTANT")
  @DisplayName("GET /api/register-forms/{id} - Assistant allowed to view form by ID")
  void getRegisterForm_assistantAllowed() throws Exception {
    when(registerFormService.getRegisterFormById(FORM_ID)).thenReturn(sampleResponse());

    mockMvc
        .perform(get("/api/register-forms/" + FORM_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200));
  }

  @Test
  @WithMockUser(roles = "LEARNER")
  @DisplayName("GET /api/register-forms/{id} - Learner forbidden from viewing form by ID")
  void getRegisterForm_learnerForbidden() throws Exception {
    mockMvc
        .perform(get("/api/register-forms/" + FORM_ID))
        .andExpect(status().isForbidden());
  }
}
