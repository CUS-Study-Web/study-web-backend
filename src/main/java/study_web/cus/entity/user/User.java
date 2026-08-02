package study_web.cus.entity.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import study_web.cus.entity.AbstractBaseEntity;
import study_web.cus.enums.Gender;

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
}
