package studyweb.cus.controller.course;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import studyweb.cus.dto.response.course.LeaderboardResponse;
import studyweb.cus.security.JwtAuthenticationFilter;
import studyweb.cus.service.course.LeaderboardService;

@WebMvcTest(controllers = LeaderboardController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@Import(ResponseFactory.class)
@TestPropertySource(properties = {"logging.loki.url=http://localhost:3100"})
class LeaderboardControllerTest {

  private static final UUID LEADERBOARD_ID = UUID.randomUUID();

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private LeaderboardService leaderboardService;

  @TestConfiguration
  @EnableMethodSecurity
  static class SliceSecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      http.csrf(AbstractHttpConfigurer::disable)
          .authorizeHttpRequests(
              auth -> auth.requestMatchers("/api/auth/**")
                  .permitAll()
                  .requestMatchers(HttpMethod.GET, "/api/leaderboards/guest")
                  .permitAll()
                  .anyRequest()
                  .authenticated())
          .httpBasic(Customizer.withDefaults());
      return http.build();
    }
  }

  private LeaderboardResponse sampleLeaderboard() {
    return new LeaderboardResponse(LEADERBOARD_ID, "Student", null, "Course", "Top 1", "avatar.png", new BigDecimal("30.00"), List.of());
  }

  @Test
  void getLeaderboards_guestIsAllowed() throws Exception {
    when(leaderboardService.getLeaderboards(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(sampleLeaderboard()), PageRequest.of(0, 10), 1));

    mockMvc
        .perform(get("/api/leaderboards/guest"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data[0].id").value(LEADERBOARD_ID.toString()));

    verify(leaderboardService).getLeaderboards(any(Pageable.class));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void getLeaderboardsAdmin_adminIsAllowed() throws Exception {
    when(leaderboardService.getLeaderboards(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(sampleLeaderboard()), PageRequest.of(0, 10), 1));

    mockMvc
        .perform(get("/api/leaderboards"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data[0].id").value(LEADERBOARD_ID.toString()));

    verify(leaderboardService).getLeaderboards(any(Pageable.class));
  }

  @Test
  @WithMockUser(roles = "USER")
  void getLeaderboardsAdmin_userIsForbidden() throws Exception {
    mockMvc.perform(get("/api/leaderboards")).andExpect(status().isForbidden());
  }
}
