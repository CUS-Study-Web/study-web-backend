package studyweb.cus.controller.content;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import studyweb.cus.controller.ResponseFactory;
import studyweb.cus.dto.request.content.RequestVipFormContentRequest;
import studyweb.cus.dto.response.content.RequestVipFormContentResponse;
import studyweb.cus.security.JwtAuthenticationFilter;
import studyweb.cus.service.content.RequestVipFormContentService;

@WebMvcTest(
    controllers = RequestVipFormContentController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class))
@Import(ResponseFactory.class)
@TestPropertySource(properties = {"logging.loki.url=http://localhost:3100"})
class RequestVipFormContentControllerTest {

  private static final UUID CONTENT_ID = UUID.randomUUID();
  private static final UUID ADMIN_ID = UUID.randomUUID();

  @Autowired private MockMvc mockMvc;

  @MockitoBean private RequestVipFormContentService requestVipFormContentService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @TestConfiguration
  @EnableMethodSecurity
  static class SliceSecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      http.csrf(AbstractHttpConfigurer::disable)
          .authorizeHttpRequests(
              auth ->
                  auth.requestMatchers(
                          HttpMethod.GET,
                          "/api/vip-form-content",
                          "/api/vip-form-content/**")
                      .permitAll()
                      .anyRequest()
                      .authenticated())
          .httpBasic(Customizer.withDefaults());
      return http.build();
    }
  }

  private static RequestPostProcessor authenticatedAdmin() {
    return SecurityMockMvcRequestPostProcessors.authentication(
        new UsernamePasswordAuthenticationToken(
            "admin@studyweb.edu", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
  }

  private RequestVipFormContentResponse sampleResponse() {
    return new RequestVipFormContentResponse(
        CONTENT_ID,
        "Đăng ký VIP",
        "Hướng dẫn thanh toán",
        "0901234567",
        "https://facebook.com/studyweb",
        "MB Bank",
        "CONG TY STUDYWEB",
        "999988886666",
        "VIP CK",
        "https://placehold.co/400x400",
        ADMIN_ID,
        LocalDateTime.now());
  }

  @Test
  @DisplayName("GET /api/vip-form-content/guest - Allowed without authentication")
  void getContent_guestAllowedWithoutAuth() throws Exception {
    when(requestVipFormContentService.getContent()).thenReturn(sampleResponse());

    mockMvc
        .perform(get("/api/vip-form-content/guest"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.formTitle").value("Đăng ký VIP"))
        .andExpect(jsonPath("$.data.bankName").value("MB Bank"));

    verify(requestVipFormContentService).getContent();
  }

  @Test
  @DisplayName("PUT /api/vip-form-content - Admin allowed to update content without file")
  void updateContent_adminAllowed() throws Exception {
    when(requestVipFormContentService.updateContent(any(), eq("admin@studyweb.edu")))
        .thenReturn(sampleResponse());

    mockMvc
        .perform(
            multipart(HttpMethod.PUT, "/api/vip-form-content")
                .param("formTitle", "New VIP Form")
                .param("bankName", "Techcombank")
                .with(authenticatedAdmin()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200));

    verify(requestVipFormContentService).updateContent(any(), eq("admin@studyweb.edu"));
  }

  @Test
  @DisplayName("PUT /api/vip-form-content (multipart) - Admin allowed to update with QR file")
  void updateContentMultipart_adminAllowed() throws Exception {
    MockMultipartFile qrFile =
        new MockMultipartFile("accountHolderQr", "qr.png", "image/png", new byte[] {1, 2, 3});

    when(requestVipFormContentService.updateContent(any(), eq("admin@studyweb.edu")))
        .thenReturn(sampleResponse());

    mockMvc
        .perform(
            multipart(HttpMethod.PUT, "/api/vip-form-content")
                .file(qrFile)
                .param("formTitle", "New VIP Form Multipart")
                .with(authenticatedAdmin()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.accountHolderQrUrl").value("https://placehold.co/400x400"));

    verify(requestVipFormContentService).updateContent(any(), eq("admin@studyweb.edu"));
  }

  @Test
  @WithMockUser(roles = "LEARNER")
  @DisplayName("PUT /api/vip-form-content - Learner forbidden from updating content")
  void updateContent_learnerForbidden() throws Exception {
    mockMvc
        .perform(
            multipart(HttpMethod.PUT, "/api/vip-form-content")
                .param("formTitle", "Title"))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("PUT /api/vip-form-content - Unauthenticated access returns 401")
  void updateContent_unauthenticatedUnauthorized() throws Exception {
    mockMvc
        .perform(
            multipart(HttpMethod.PUT, "/api/vip-form-content")
                .param("formTitle", "Title"))
        .andExpect(status().isUnauthorized());
  }
}
