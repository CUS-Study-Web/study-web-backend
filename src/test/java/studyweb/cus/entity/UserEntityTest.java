package studyweb.cus.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import studyweb.cus.entity.user.ActivityLog;
import studyweb.cus.entity.user.User;
import studyweb.cus.entity.user.VipRequest;
import studyweb.cus.enums.ActionType;
import studyweb.cus.enums.Gender;
import studyweb.cus.enums.UserRole;
import studyweb.cus.enums.UserStatus;
import studyweb.cus.enums.UserTier;
import studyweb.cus.enums.VipRequestStatus;

@DisplayName("User Domain Entities Test")
class UserEntityTest {

  @Test
  @DisplayName("Should build User entity with default values")
  void testUserBuilderDefaults() {
    User user =
        User.builder()
            .gmail("student@studyweb.edu")
            .name("Student")
            .password("secret123")
            .gender(Gender.MALE)
            .birth(LocalDate.of(2000, 1, 1))
            .school("UIT")
            .phone("0123456789")
            .build();

    assertThat(user.getGmail()).isEqualTo("student@studyweb.edu");
    assertThat(user.getName()).isEqualTo("Student");
    assertThat(user.getRole()).isEqualTo(UserRole.LEARNER);
    assertThat(user.getTier()).isEqualTo(UserTier.NORMAL);
    assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
  }

  @Test
  @DisplayName("Should build VipRequest entity correctly")
  void testVipRequestBuilder() {
    User user = User.builder().gmail("vip@studyweb.edu").name("VIP User").password("pass").build();
    user.setId(UUID.randomUUID());

    VipRequest request =
        VipRequest.builder()
            .user(user)
            .status(VipRequestStatus.WAITING)
            .requestDate(LocalDateTime.now())
            .build();

    assertThat(request.getUser()).isEqualTo(user);
    assertThat(request.getStatus()).isEqualTo(VipRequestStatus.WAITING);
    assertThat(request.getRequestDate()).isNotNull();
  }

  @Test
  @DisplayName("Should build ActivityLog entity correctly")
  void testActivityLogBuilder() {
    User user = User.builder().gmail("user@studyweb.edu").name("User").password("pass").build();
    user.setId(UUID.randomUUID());

    ActivityLog log =
        ActivityLog.builder()
            .user(user)
            .actionType(ActionType.LOGIN)
            .description("User logged in successfully")
            .build();

    assertThat(log.getUser()).isEqualTo(user);
    assertThat(log.getActionType()).isEqualTo(ActionType.LOGIN);
    assertThat(log.getDescription()).isEqualTo("User logged in successfully");
  }
}
