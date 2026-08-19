package studyweb.cus.entity.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import studyweb.cus.entity.AbstractBaseEntity;
import studyweb.cus.enums.Gender;
import studyweb.cus.enums.UserRole;
import studyweb.cus.enums.UserStatus;
import studyweb.cus.enums.UserTier;

@Entity
@Table(
    name = "users",
    indexes = {@Index(name = "idx_user_gmail", columnList = "gmail", unique = true)})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends AbstractBaseEntity {

  @Column(name = "gmail", nullable = false, unique = true, length = 150)
  private String gmail;

  @Column(name = "name", nullable = false, length = 150)
  private String name;

  @Column(name = "phone", length = 20)
  private String phone;

  @Column(name = "birth")
  private LocalDate birth;

  @Enumerated(EnumType.STRING)
  @Column(name = "gender", length = 20)
  private Gender gender;

  @Column(name = "school", length = 150)
  private String school;

  @Column(name = "password", nullable = false, length = 255)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 20)
  @Builder.Default
  private UserRole role = UserRole.LEARNER;

  @Enumerated(EnumType.STRING)
  @Column(name = "tier", nullable = false, length = 20)
  @Builder.Default
  private UserTier tier = UserTier.NORMAL;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  @Builder.Default
  private UserStatus status = UserStatus.ACTIVE;

  @Column(name = "join_date")
  private LocalDateTime joinDate;

  @Column(name = "last_login")
  private LocalDateTime lastLogin;

  @Column(name = "note", columnDefinition = "TEXT")
  private String note;

  @Column(name = "vip_start_date")
  private LocalDateTime vipStartDate;

  @Column(name = "vip_end_date")
  private LocalDateTime vipEndDate;

  @Column(name = "avatar_url", length = 500)
  private String avatarUrl;
}
