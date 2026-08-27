package studyweb.cus.controller.badge;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import studyweb.cus.controller.ResponseFactory;
import studyweb.cus.dto.request.badge.BadgeRequest;
import studyweb.cus.dto.response.badge.BadgeResponse;
import studyweb.cus.security.JwtAuthenticationFilter;
import studyweb.cus.service.badge.BadgeService;

@WebMvcTest(
    controllers = BadgeController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class))
@Import(ResponseFactory.class)
class BadgeControllerTest {

  private static final UUID BADGE_ID = UUID.randomUUID();
  private static final UUID ADMIN_ID = UUID.randomUUID();

  @Autowired private MockMvc mockMvc;

  @MockitoBean private BadgeService badgeService;

  private final ObjectMapper objectMapper = new ObjectMapper();

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

  private static RequestPostProcessor authenticatedAdmin() {
    return SecurityMockMvcRequestPostProcessors.authentication(
        new UsernamePasswordAuthenticationToken(
            "admin@studyweb.edu", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
  }

  private BadgeResponse sampleBadgeResponse() {
    return new BadgeResponse(
        BADGE_ID, "Toán", ADMIN_ID, LocalDateTime.now(), LocalDateTime.now());
  }

  // --- Create Badge Tests ---

  @Test
  @DisplayName("POST /api/badges - Admin role allowed to create badge")
  void createBadge_adminAllowed() throws Exception {
    BadgeRequest request = new BadgeRequest("Toán");
    when(badgeService.createBadge(any(BadgeRequest.class), any()))
        .thenReturn(sampleBadgeResponse());

    mockMvc
        .perform(
            post("/api/badges")
                .with(authenticatedAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.name").value("Toán"));
  }

  @Test
  @WithMockUser(roles = "ASSISTANT")
  @DisplayName("POST /api/badges - Assistant role forbidden from creating badge")
  void createBadge_assistantForbidden() throws Exception {
    BadgeRequest request = new BadgeRequest("Toán");

    mockMvc
        .perform(
            post("/api/badges")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "LEARNER")
  @DisplayName("POST /api/badges - Learner role forbidden from creating badge")
  void createBadge_learnerForbidden() throws Exception {
    BadgeRequest request = new BadgeRequest("Toán");

    mockMvc
        .perform(
            post("/api/badges")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  // --- List Badges Tests ---

  @Test
  @WithMockUser(roles = "ADMIN")
  @DisplayName("GET /api/badges - Admin can list badges")
  void listBadges_adminAllowed() throws Exception {
    Pageable pageable = PageRequest.of(0, 20);
    when(badgeService.listBadges(any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(sampleBadgeResponse()), pageable, 1));

    mockMvc
        .perform(get("/api/badges"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data[0].name").value("Toán"));
  }

  @Test
  @WithMockUser(roles = "ASSISTANT")
  @DisplayName("GET /api/badges - Assistant can list badges")
  void listBadges_assistantAllowed() throws Exception {
    Pageable pageable = PageRequest.of(0, 20);
    when(badgeService.listBadges(any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(sampleBadgeResponse()), pageable, 1));

    mockMvc
        .perform(get("/api/badges"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200));
  }

  @Test
  @WithMockUser(roles = "LEARNER")
  @DisplayName("GET /api/badges - Learner forbidden from listing badges")
  void listBadges_learnerForbidden() throws Exception {
    mockMvc.perform(get("/api/badges")).andExpect(status().isForbidden());
  }

  // --- Get Detail Tests ---

  @Test
  @WithMockUser(roles = "ADMIN")
  @DisplayName("GET /api/badges/{id} - Admin can get badge detail")
  void getBadgeDetail_adminAllowed() throws Exception {
    when(badgeService.getBadgeById(BADGE_ID)).thenReturn(sampleBadgeResponse());

    mockMvc
        .perform(get("/api/badges/{id}", BADGE_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.name").value("Toán"));
  }

  // --- Update & Delete Tests ---

  @Test
  @WithMockUser(roles = "ADMIN")
  @DisplayName("PUT /api/badges/{id} - Admin can update badge")
  void updateBadge_adminAllowed() throws Exception {
    BadgeRequest request = new BadgeRequest("Toán Nâng Cao");
    when(badgeService.updateBadge(eq(BADGE_ID), any(BadgeRequest.class)))
        .thenReturn(new BadgeResponse(BADGE_ID, "Toán Nâng Cao", ADMIN_ID, LocalDateTime.now(), LocalDateTime.now()));

    mockMvc
        .perform(
            put("/api/badges/{id}", BADGE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.name").value("Toán Nâng Cao"));
  }

  @Test
  @WithMockUser(roles = "ASSISTANT")
  @DisplayName("PUT /api/badges/{id} - Assistant forbidden from updating badge")
  void updateBadge_assistantForbidden() throws Exception {
    BadgeRequest request = new BadgeRequest("Toán Nâng Cao");

    mockMvc
        .perform(
            put("/api/badges/{id}", BADGE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  @DisplayName("DELETE /api/badges/{id} - Admin can delete badge")
  void deleteBadge_adminAllowed() throws Exception {
    mockMvc
        .perform(delete("/api/badges/{id}", BADGE_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200));

    verify(badgeService).deleteBadge(BADGE_ID);
  }

  @Test
  @WithMockUser(roles = "ASSISTANT")
  @DisplayName("DELETE /api/badges/{id} - Assistant forbidden from deleting badge")
  void deleteBadge_assistantForbidden() throws Exception {
    mockMvc.perform(delete("/api/badges/{id}", BADGE_ID)).andExpect(status().isForbidden());
  }
}
