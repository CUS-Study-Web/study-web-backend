package studyweb.cus.controller.assistant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import studyweb.cus.dto.response.assistant.AssistantActivityItemResponse;
import studyweb.cus.dto.response.assistant.AssistantDashboardResponse;
import studyweb.cus.dto.response.assistant.AssistantStatResponse;
import studyweb.cus.service.assistant.AssistantDashboardService;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

@WebMvcTest(
    controllers = AssistantDashboardController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = studyweb.cus.security.JwtAuthenticationFilter.class))
@Import({studyweb.cus.controller.ResponseFactory.class, AssistantDashboardControllerTest.TestConfig.class})
class AssistantDashboardControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private AssistantDashboardService dashboardService;

  @MockitoBean
  private studyweb.cus.security.JwtUtils jwtUtils;

  @MockitoBean
  private studyweb.cus.repository.user.UserRepository userRepository;

  @TestConfiguration
  static class TestConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http.csrf(AbstractHttpConfigurer::disable)
          .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
          .httpBasic(Customizer.withDefaults());
      return http.build();
    }
  }

  private static RequestPostProcessor authenticated() {
    return authentication(
        new UsernamePasswordAuthenticationToken(
            "test@assistant.com", null, List.of(new SimpleGrantedAuthority("ROLE_ASSISTANT"))));
  }

  @Test
  void getDashboardStats_shouldReturnStats() throws Exception {
    AssistantDashboardResponse mockResponse = new AssistantDashboardResponse(
        new AssistantStatResponse(100, 10),
        new AssistantStatResponse(50, 5),
        new AssistantStatResponse(30, 2),
        List.of(new AssistantActivityItemResponse(
            UUID.randomUUID(), "material", "Uploaded doc", LocalDateTime.now()
        ))
    );

    when(dashboardService.getDashboardStats("test@assistant.com")).thenReturn(mockResponse);

    mockMvc.perform(get("/api/assistant/dashboard").with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.totalLearners.value").value(100))
        .andExpect(jsonPath("$.data.totalLearners.delta").value(10))
        .andExpect(jsonPath("$.data.totalExercises.value").value(50))
        .andExpect(jsonPath("$.data.recentActivities[0].text").value("Uploaded doc"));
  }
}
