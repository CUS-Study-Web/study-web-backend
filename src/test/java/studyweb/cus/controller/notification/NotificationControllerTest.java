package studyweb.cus.controller.notification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import studyweb.cus.config.WebMvcConfig;
import studyweb.cus.controller.ResponseFactory;
import studyweb.cus.converter.StringToUuidConverter;
import studyweb.cus.dto.response.notification.NotificationResponse;
import studyweb.cus.enums.NotificationType;
import studyweb.cus.security.JwtAuthenticationFilter;
import studyweb.cus.service.notification.NotificationService;

@WebMvcTest(
    controllers = NotificationController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class))
@Import({ResponseFactory.class, WebMvcConfig.class, StringToUuidConverter.class})
@TestPropertySource(properties = {"logging.loki.url=http://localhost:3100"})
class NotificationControllerTest {

  private static final String LEARNER_EMAIL = "learner@test.com";
  private static final UUID NOTIFICATION_ID = UUID.randomUUID();

  @Autowired private MockMvc mockMvc;

  @MockitoBean private NotificationService notificationService;

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
            LEARNER_EMAIL, null, List.of(new SimpleGrantedAuthority("ROLE_LEARNER"))));
  }

  private static RequestPostProcessor authenticatedAssistant() {
    return SecurityMockMvcRequestPostProcessors.authentication(
        new UsernamePasswordAuthenticationToken(
            "assistant@test.com", null, List.of(new SimpleGrantedAuthority("ROLE_ASSISTANT"))));
  }

  private NotificationResponse sampleNotificationResponse() {
    return new NotificationResponse(
        NOTIFICATION_ID,
        NotificationType.VIP_REQUEST_APPROVED,
        "Yêu cầu VIP đã duyệt",
        "Chào mừng bạn đến với VIP!",
        false,
        LocalDateTime.now());
  }

  @Test
  @DisplayName("GET /api/notifications - should return 200 with notifications for learner")
  void getNotifications_Success() throws Exception {
    Pageable pageable = PageRequest.of(0, 10);
    NotificationResponse item = sampleNotificationResponse();
    when(notificationService.getNotifications(eq(LEARNER_EMAIL), eq(false), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(item), pageable, 1));

    mockMvc
        .perform(
            get("/api/notifications")
                .with(authenticatedLearner())
                .param("isRead", "false")
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.message").value("Notifications fetched successfully"))
        .andExpect(jsonPath("$.data[0].id").value(NOTIFICATION_ID.toString()))
        .andExpect(jsonPath("$.data[0].type").value("VIP_REQUEST_APPROVED"))
        .andExpect(jsonPath("$.data[0].title").value("Yêu cầu VIP đã duyệt"))
        .andExpect(jsonPath("$.data[0].isRead").value(false));
  }

  @Test
  @DisplayName("GET /api/notifications - should return 401 when unauthenticated")
  void getNotifications_Unauthorized() throws Exception {
    mockMvc.perform(get("/api/notifications")).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("GET /api/notifications - should return 403 when role is not LEARNER")
  void getNotifications_Forbidden_WhenNotLearner() throws Exception {
    mockMvc
        .perform(get("/api/notifications").with(authenticatedAssistant()))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("PATCH /api/notifications/{id}/read - should return 200 when mark-as-read succeeds")
  void markAsRead_Success() throws Exception {
    mockMvc
        .perform(
            patch("/api/notifications/{id}/read", NOTIFICATION_ID)
                .with(authenticatedLearner()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.message").value("Notification marked as read successfully"));

    verify(notificationService).markAsRead(LEARNER_EMAIL, NOTIFICATION_ID);
  }

  @Test
  @DisplayName("PATCH /api/notifications/read-all - should return 200 when mark-all-as-read succeeds")
  void markAllAsRead_Success() throws Exception {
    mockMvc
        .perform(
            patch("/api/notifications/read-all")
                .with(authenticatedLearner()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.message").value("All notifications marked as read successfully"));

    verify(notificationService).markAllAsRead(LEARNER_EMAIL);
  }
}
