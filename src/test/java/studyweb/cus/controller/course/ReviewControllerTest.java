package studyweb.cus.controller.course;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import studyweb.cus.dto.response.course.ReviewResponse;
import studyweb.cus.security.JwtAuthenticationFilter;
import studyweb.cus.service.course.ReviewService;

@WebMvcTest(controllers = ReviewController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@Import(ResponseFactory.class)
@TestPropertySource(properties = {"logging.loki.url=http://localhost:3100"})
class ReviewControllerTest {

  private static final UUID REVIEW_ID = UUID.randomUUID();
  private static final UUID COURSE_ID = UUID.randomUUID();

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ReviewService reviewService;

  @TestConfiguration
  @EnableMethodSecurity
  static class SliceSecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      http.csrf(AbstractHttpConfigurer::disable)
          .authorizeHttpRequests(
              auth -> auth.requestMatchers("/api/auth/**")
                  .permitAll()
                  .requestMatchers(HttpMethod.GET, "/api/reviews/guest")
                  .permitAll()
                  .anyRequest()
                  .authenticated())
          .httpBasic(Customizer.withDefaults());
      return http.build();
    }
  }

  private ReviewResponse sampleReview() {
    return new ReviewResponse(REVIEW_ID, "Alice", COURSE_ID, "Great!", "1 hour ago", "avatar.png", null);
  }

  @Test
  void getReviews_guestIsAllowed() throws Exception {
    when(reviewService.getReviews(eq(COURSE_ID), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(sampleReview()), PageRequest.of(0, 10), 1));

    mockMvc
        .perform(get("/api/reviews/guest").param("courseId", COURSE_ID.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data[0].id").value(REVIEW_ID.toString()));

    verify(reviewService).getReviews(eq(COURSE_ID), any(Pageable.class));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void getReviewsAdmin_adminIsAllowed() throws Exception {
    when(reviewService.getReviews(eq(null), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(sampleReview()), PageRequest.of(0, 10), 1));

    mockMvc
        .perform(get("/api/reviews"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data[0].id").value(REVIEW_ID.toString()));

    verify(reviewService).getReviews(eq(null), any(Pageable.class));
  }

  @Test
  @WithMockUser(roles = "USER")
  void getReviewsAdmin_userIsForbidden() throws Exception {
    mockMvc.perform(get("/api/reviews")).andExpect(status().isForbidden());
  }
}
