package studyweb.cus.controller.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import studyweb.cus.controller.ResponseFactory;
import studyweb.cus.dto.request.auth.ChangePasswordRequest;
import studyweb.cus.dto.response.auth.UserResponse;
import studyweb.cus.enums.Gender;
import studyweb.cus.security.JwtAuthenticationFilter;
import studyweb.cus.service.user.UserService;

@WebMvcTest(UserController.class)
@Import(ResponseFactory.class)
class UserControllerTest {

  private static final String GMAIL = "learner@studyweb.edu";

  @Autowired private WebApplicationContext wac;

  private MockMvc mockMvc;

  @MockitoBean private UserService userService;

  @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

  @TestConfiguration
  @EnableMethodSecurity
  static class SliceSecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      http.csrf(AbstractHttpConfigurer::disable)
          .authorizeHttpRequests(
              auth -> auth.requestMatchers("/api/auth/**").permitAll().anyRequest().authenticated())
          .httpBasic(Customizer.withDefaults());
      return http.build();
    }
  }

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  private UserResponse userResponse() {
    return new UserResponse(
        UUID.randomUUID(),
        GMAIL,
        "Tien",
        "0901234567",
        LocalDate.of(2000, 1, 1),
        Gender.MALE,
        "StudyWeb");
  }

  private static RequestPostProcessor authenticated() {
    return authentication(
        new UsernamePasswordAuthenticationToken(
            GMAIL, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
  }

  @Test
  void me_unauthenticatedReturns401() throws Exception {
    mockMvc.perform(get("/api/user/me")).andExpect(status().isUnauthorized());
  }

  @Test
  void me_authenticatedReturnsProfile() throws Exception {
    when(userService.getCurrentUser(GMAIL)).thenReturn(userResponse());

    mockMvc
        .perform(get("/api/user/me").with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.gmail").value(GMAIL));

    verify(userService).getCurrentUser(GMAIL);
  }

  @Test
  void changePassword_unauthenticatedReturns401() throws Exception {
    mockMvc
        .perform(
            post("/api/user/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newPassword\":\"password1\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void changePassword_authenticatedReturns200() throws Exception {
    mockMvc
        .perform(
            post("/api/user/change-password")
                .with(authenticated())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newPassword\":\"password1\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.message").value("Password changed successfully!"));

    verify(userService).changePassword(eq(GMAIL), any(ChangePasswordRequest.class));
  }

  @Test
  void changePassword_invalidPayloadReturns400() throws Exception {
    mockMvc
        .perform(
            post("/api/user/change-password")
                .with(authenticated())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newPassword\":\"short\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("SYS_002"));
  }

  @Test
  void changePassword_getMethodNotAllowed() throws Exception {
    mockMvc
        .perform(get("/api/user/change-password").with(authenticated()))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.statusCode").value(405));
  }
}