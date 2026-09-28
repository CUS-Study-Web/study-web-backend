package studyweb.cus.controller.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import studyweb.cus.config.SecurityConfig;
import studyweb.cus.controller.ResponseFactory;
import studyweb.cus.dto.request.admin.FooterLinkItemRequest;
import studyweb.cus.dto.request.admin.UpdateFooterRequest;
import studyweb.cus.dto.request.admin.UpdateHomepageRequest;
import studyweb.cus.dto.response.admin.FooterLinkResponse;
import studyweb.cus.dto.response.admin.FooterResponse;
import studyweb.cus.dto.response.admin.HomepageResponse;
import studyweb.cus.enums.CtaTarget;
import studyweb.cus.enums.FooterCategory;
import studyweb.cus.exception.GlobalExceptionHandler;
import studyweb.cus.repository.user.UserRepository;
import studyweb.cus.security.JwtAuthenticationEntryPoint;
import studyweb.cus.security.JwtAuthenticationFilter;
import studyweb.cus.security.JwtUtils;
import studyweb.cus.security.RestAccessDeniedHandler;
import studyweb.cus.service.admin.WebsiteManagementService;

@Slf4j
@WebMvcTest(WebsiteManagementController.class)
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  JwtAuthenticationEntryPoint.class,
  RestAccessDeniedHandler.class,
  GlobalExceptionHandler.class,
  ResponseFactory.class,
  WebsiteManagementControllerTest.TestConfig.class
})
@TestPropertySource(
    properties = {
      "cors.allowed-origins=http://localhost:3000",
      "logging.loki.url=http://localhost:3100"
    })
@DisplayName("WebsiteManagementController Unit Tests")
class WebsiteManagementControllerTest {

  @TestConfiguration
  static class TestConfig {
    @Bean
    public tools.jackson.databind.ObjectMapper toolsObjectMapper() {
      return new tools.jackson.databind.ObjectMapper();
    }
  }

  @Autowired private MockMvc mockMvc;

  @MockitoBean private WebsiteManagementService websiteManagementService;
  @MockitoBean private UserRepository userRepository;
  @MockitoBean private JwtUtils jwtUtils;

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  private static final String ADMIN_EMAIL = "admin@studyweb.edu";

  private HomepageResponse sampleHomepageResponse() {
    return new HomepageResponse(
        UUID.randomUUID(),
        "Badge Title",
        "Headline 1",
        "Headline 2",
        "Description",
        "Start",
        CtaTarget.REGISTER,
        "Courses",
        CtaTarget.COURSES,
        "https://cdn.example.com/main.png",
        "3000+",
        "Students",
        "99%",
        "Pass rate",
        "https://cdn.example.com/s1.png",
        "https://cdn.example.com/s2.png",
        "https://cdn.example.com/s3.png",
        "Top students",
        ADMIN_EMAIL,
        LocalDateTime.now());
  }

  private FooterResponse sampleFooterResponse() {
    return new FooterResponse(
        UUID.randomUUID(),
        "CUS Training Co.",
        "479 Ma Lo, Binh Tan, HCMC",
        "https://fb.com/cus",
        "https://ig.com/cus",
        "https://yt.com/cus",
        "https://tiktok.com/cus",
        "0362174805",
        "contact@cus.edu.vn",
        "https://cus.edu.vn",
        "7:30 - 21:00",
        "© 2026 CUS",
        "/privacy",
        "/terms",
        List.of(
            new FooterLinkResponse(
                UUID.randomUUID(), "V-ACT", "/courses/v-act", 0, FooterCategory.PROGRAM)),
        ADMIN_EMAIL,
        LocalDateTime.now());
  }

  // ==========================================
  // Homepage Tests
  // ==========================================

  @Nested
  @DisplayName("GET /api/website-management/homepage")
  class GetHomepageTests {

    @Test
    @WithMockUser(roles = "ADMIN", username = ADMIN_EMAIL)
    @DisplayName("Should return 200 with homepage content when caller is ADMIN")
    void getHomepage_asAdmin_returns200() throws Exception {
      when(websiteManagementService.getHomepageContent()).thenReturn(sampleHomepageResponse());

      mockMvc
          .perform(get("/api/website-management/homepage"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.data.badgeTitle").value("Badge Title"))
          .andExpect(jsonPath("$.data.ctaBtn1Target").value("REGISTER"))
          .andExpect(jsonPath("$.data.ctaBtn2Target").value("COURSES"));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Should return 401 when caller is unauthenticated")
    void getHomepage_asAnonymous_returns401() throws Exception {
      mockMvc.perform(get("/api/website-management/homepage")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "LEARNER", username = "learner@studyweb.edu")
    @DisplayName("Should return 403 when caller has non-ADMIN role")
    void getHomepage_asLearner_returns403() throws Exception {
      mockMvc.perform(get("/api/website-management/homepage")).andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("PATCH /api/website-management/homepage")
  class PatchHomepageTests {

    @Test
    @WithMockUser(roles = "ADMIN", username = ADMIN_EMAIL)
    @DisplayName("Should return 200 with updated content when caller is ADMIN")
    void patchHomepage_asAdmin_returns200() throws Exception {
      when(websiteManagementService.updateHomepageContent(
              any(UpdateHomepageRequest.class), eq(ADMIN_EMAIL)))
          .thenReturn(sampleHomepageResponse());

      MockMultipartFile file =
          new MockMultipartFile("mainImage", "main.png", "image/png", new byte[] {1, 2, 3});

      mockMvc
          .perform(
              multipart(HttpMethod.PATCH, "/api/website-management/homepage")
                  .file(file)
                  .param("badgeTitle", "Updated Badge")
                  .param("ctaBtn1Name", "Start Now")
                  .param("ctaBtn1Target", "REGISTER")
                  .param("ctaBtn2Name", "View Courses")
                  .param("ctaBtn2Target", "COURSES"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Homepage content updated successfully!"));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Should return 401 when caller is unauthenticated")
    void patchHomepage_asAnonymous_returns401() throws Exception {
      mockMvc
          .perform(
              multipart(HttpMethod.PATCH, "/api/website-management/homepage")
                  .param("badgeTitle", "Updated Badge"))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "LEARNER", username = "learner@studyweb.edu")
    @DisplayName("Should return 403 when caller is not ADMIN")
    void patchHomepage_asLearner_returns403() throws Exception {
      mockMvc
          .perform(
              multipart(HttpMethod.PATCH, "/api/website-management/homepage")
                  .param("badgeTitle", "Updated Badge"))
          .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = ADMIN_EMAIL)
    @DisplayName("Should return 400 when validation fails on text field length")
    void patchHomepage_validationError_returns400() throws Exception {
      String over255 = "a".repeat(256);

      mockMvc
          .perform(
              multipart(HttpMethod.PATCH, "/api/website-management/homepage")
                  .param("badgeTitle", over255))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.statusCode").value(400));
    }
  }

  // ==========================================
  // Footer Tests
  // ==========================================

  @Nested
  @DisplayName("GET /api/website-management/footer")
  class GetFooterTests {

    @Test
    @WithMockUser(roles = "ADMIN", username = ADMIN_EMAIL)
    @DisplayName("Should return 200 with footer content when caller is ADMIN")
    void getFooter_asAdmin_returns200() throws Exception {
      when(websiteManagementService.getFooterContent()).thenReturn(sampleFooterResponse());

      mockMvc
          .perform(get("/api/website-management/footer"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.data.companyName").value("CUS Training Co."))
          .andExpect(jsonPath("$.data.links").isArray());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Should return 401 when caller is unauthenticated")
    void getFooter_asAnonymous_returns401() throws Exception {
      mockMvc.perform(get("/api/website-management/footer")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "LEARNER", username = "learner@studyweb.edu")
    @DisplayName("Should return 403 when caller is not ADMIN")
    void getFooter_asLearner_returns403() throws Exception {
      mockMvc.perform(get("/api/website-management/footer")).andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("PATCH /api/website-management/footer")
  class PatchFooterTests {

    @Test
    @WithMockUser(roles = "ADMIN", username = ADMIN_EMAIL)
    @DisplayName("Should return 200 with updated footer when caller is ADMIN")
    void patchFooter_asAdmin_returns200() throws Exception {
      UpdateFooterRequest request =
          new UpdateFooterRequest(
              "Updated Co.",
              "123 Street",
              null,
              null,
              null,
              null,
              "0123456789",
              "admin@cus.vn",
              null,
              null,
              null,
              null,
              null,
              List.of(
                  new FooterLinkItemRequest(
                      null, "Program 1", "/prog-1", 0, FooterCategory.PROGRAM)));

      when(websiteManagementService.updateFooterContent(
              any(UpdateFooterRequest.class), eq(ADMIN_EMAIL)))
          .thenReturn(sampleFooterResponse());

      mockMvc
          .perform(
              patch("/api/website-management/footer")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Footer content updated successfully!"));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Should return 401 when caller is unauthenticated")
    void patchFooter_asAnonymous_returns401() throws Exception {
      UpdateFooterRequest request =
          new UpdateFooterRequest(
              "Updated Co.",
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null);

      mockMvc
          .perform(
              patch("/api/website-management/footer")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "LEARNER", username = "learner@studyweb.edu")
    @DisplayName("Should return 403 when caller is not ADMIN")
    void patchFooter_asLearner_returns403() throws Exception {
      UpdateFooterRequest request =
          new UpdateFooterRequest(
              "Updated Co.",
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null);

      mockMvc
          .perform(
              patch("/api/website-management/footer")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = ADMIN_EMAIL)
    @DisplayName("Should return 400 when sortOrder is negative")
    void patchFooter_negativeSortOrder_returns400() throws Exception {
      UpdateFooterRequest request =
          new UpdateFooterRequest(
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              List.of(
                  new FooterLinkItemRequest(null, "Label", "/url", -1, FooterCategory.PROGRAM)));

      mockMvc
          .perform(
              patch("/api/website-management/footer")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.statusCode").value(400));
    }
  }
}
