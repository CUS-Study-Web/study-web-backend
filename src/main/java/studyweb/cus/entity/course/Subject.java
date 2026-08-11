package studyweb.cus.entity.course;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import studyweb.cus.entity.AbstractBaseEntity;

@Entity
@Table(
    name = "subjects",
    indexes = {@Index(name = "idx_subjects_course", columnList = "course_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject extends AbstractBaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  @Column(name = "title", nullable = false, length = 255)
  private String title;

  @Column(name = "max_scores")
  @Builder.Default
  private Integer maxScores = 0;

  @Column(name = "num_lessons")
  @Builder.Default
  private Integer numLessons = 0;

  @Column(name = "duration_hour", precision = 5, scale = 2)
  @Builder.Default
  private BigDecimal durationHour = BigDecimal.ZERO;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
