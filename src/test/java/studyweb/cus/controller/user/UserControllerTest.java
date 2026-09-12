package studyweb.cus.controller.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import studyweb.cus.controller.ResponseFactory;
import studyweb.cus.dto.request.auth.ChangePasswordRequest;
import studyweb.cus.dto.request.user.VipSubscriptionRequest;
import studyweb.cus.dto.response.auth.UserResponse;
import studyweb.cus.dto.response.user.VipInfoResponse;
import studyweb.cus.enums.Gender;
import studyweb.cus.enums.UserTier;
import studyweb.cus.enums.VipRequestStatus;
import studyweb.cus.security.JwtAuthenticationFilter;
import studyweb.cus.service.user.UserService;

@WebMvcTest(
    controllers = UserController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class))
@Import(ResponseFactory.class)
@TestPropertySource(properties = {"logging.loki.url=http://localhost:3100"})
class UserControllerTest {

  private static final String GMAIL = "learner@studyweb.edu";

  @Autowired private MockMvc mockMvc;

  @MockitoBean private UserService userService;

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

  @Test
  void subscribeVip_unauthenticatedReturns401() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile(
            "evidence", "proof.png", MediaType.IMAGE_PNG_VALUE, new byte[] {1, 2, 3});

    mockMvc
        .perform(
            multipart("/api/user/vip-subscription")
                .file(file)
                .param("name", "Nguyen Van A")
                .param("email", "learner@studyweb.edu")
                .param("birth", "2000-01-01")
                .param("phone", "0901234567")
                .param("note", "Need VIP"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void subscribeVip_authenticatedWithValidDataReturns200() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile(
            "evidence", "proof.png", MediaType.IMAGE_PNG_VALUE, new byte[] {1, 2, 3});

    mockMvc
        .perform(
            multipart("/api/user/vip-subscription")
                .file(file)
                .param("name", "Nguyen Van A")
                .param("email", "learner@studyweb.edu")
                .param("birth", "2000-01-01")
                .param("phone", "0901234567")
                .param("note", "Need VIP")
                .with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.message").value("VIP subscription request submitted successfully!"));

    verify(userService).createVipRequest(eq(GMAIL), any(VipSubscriptionRequest.class), eq(false));
  }

  @Test
  void subscribeVip_missingRequiredFieldReturns400() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile(
            "evidence", "proof.png", MediaType.IMAGE_PNG_VALUE, new byte[] {1, 2, 3});

    mockMvc
        .perform(
            multipart("/api/user/vip-subscription")
                .file(file)
                .param("name", "")
                .param("email", "invalid-email")
                .param("birth", "2000-01-01")
                .param("phone", "")
                .with(authenticated()))
        .andExpect(status().isBadRequest());
  }

  @Test
  void renewVip_unauthenticatedReturns401() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile(
            "evidence", "proof.png", MediaType.IMAGE_PNG_VALUE, new byte[] {1, 2, 3});

    mockMvc
        .perform(
            multipart("/api/user/vip-renewal")
                .file(file)
                .param("name", "Nguyen Van A")
                .param("email", "learner@studyweb.edu")
                .param("birth", "2000-01-01")
                .param("phone", "0901234567")
                .param("note", "Renew VIP"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void renewVip_authenticatedWithValidDataReturns200() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile(
            "evidence", "proof.png", MediaType.IMAGE_PNG_VALUE, new byte[] {1, 2, 3});

    mockMvc
        .perform(
            multipart("/api/user/vip-renewal")
                .file(file)
                .param("name", "Nguyen Van A")
                .param("email", "learner@studyweb.edu")
                .param("birth", "2000-01-01")
                .param("phone", "0901234567")
                .param("note", "Renew VIP")
                .with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.message").value("VIP renewal request submitted successfully!"));

    verify(userService).createVipRequest(eq(GMAIL), any(VipSubscriptionRequest.class), eq(true));
  }

  @Test
  void getVipInfo_unauthenticatedReturns401() throws Exception {
    mockMvc.perform(get("/api/user/vip-info")).andExpect(status().isUnauthorized());
  }

  @Test
  void getVipInfo_authenticatedReturnsVipInfo() throws Exception {
    VipInfoResponse vipInfo =
        new VipInfoResponse(
            UserTier.VIP,
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 2, 1),
            VipRequestStatus.APPROVED,
            LocalDate.of(2026, 1, 1),
            "Payment verified",
            "https://s3.example.com/vip-evidence/proof.png");

    when(userService.getVipInfo(GMAIL)).thenReturn(vipInfo);

    mockMvc
        .perform(get("/api/user/vip-info").with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.tier").value("VIP"))
        .andExpect(jsonPath("$.data.vipStartDate").value("2026-01-01"))
        .andExpect(jsonPath("$.data.vipEndDate").value("2026-02-01"))
        .andExpect(jsonPath("$.data.status").value("APPROVED"))
        .andExpect(jsonPath("$.data.requestDate").value("2026-01-01"))
        .andExpect(jsonPath("$.data.note").value("Payment verified"))
        .andExpect(jsonPath("$.data.evidenceUrl").value("https://s3.example.com/vip-evidence/proof.png"));

    verify(userService).getVipInfo(GMAIL);
  }
}
