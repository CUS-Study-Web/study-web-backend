package studyweb.cus.entity.course;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import studyweb.cus.entity.AbstractBaseEntity;

@Entity
@Table(
    name = "leaderboard",
    indexes = {@Index(name = "idx_leaderboard_course", columnList = "course_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Leaderboard extends AbstractBaseEntity {

  @Column(name = "student_name", nullable = false, length = 150)
  private String studentName;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  @Column(name = "achievement", length = 255)
  private String achievement;

  @Column(name = "avatar_url", length = 500)
  private String avatarUrl;

  @Column(name = "sum_score", precision = 5, scale = 2, nullable = false)
  @Builder.Default
  private BigDecimal sumScore = BigDecimal.ZERO;

  @Column(name = "university", length = 255)
  private String university;
}
