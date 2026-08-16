package studyweb.cus.entity.course;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import studyweb.cus.entity.AbstractBaseEntity;

@Entity
@Table(name = "teacher_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherProfile extends AbstractBaseEntity {

  @Column(name = "name", nullable = false, length = 150)
  private String name;

  @Column(name = "subject", nullable = false, length = 255)
  private String subject;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "avatar_url", length = 500)
  private String avatarUrl;
}
