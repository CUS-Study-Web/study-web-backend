package studyweb.cus.entity.course;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import studyweb.cus.entity.AbstractBaseEntity;
import studyweb.cus.entity.user.User;

@Entity
@Table(name = "assessment_attempts", indexes = {
    @Index(name = "idx_assessment_attempts_user", columnList = "user_id"),
    @Index(name = "idx_assessment_attempts_exam", columnList = "exam_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentAttempt extends AbstractBaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exam_id", nullable = false)
  private Assessment exam;

  @Column(name = "attempt_number", nullable = false)
  @Builder.Default
  private Integer attemptNumber = 1;

  @Column(name = "duration_min", nullable = false)
  @Builder.Default
  private Integer durationMin = 0;

  @Column(name = "completed_at", nullable = false)
  @Builder.Default
  private LocalDateTime completedAt = LocalDateTime.now();

  @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<AssessmentAttemptDetail> details = new ArrayList<>();
}
