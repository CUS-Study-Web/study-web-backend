package studyweb.cus.entity.course;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "achievement_scores",
    indexes = {@Index(name = "idx_achievement_scores_leaderboard", columnList = "achievement_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementScore {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exam_subject_id", nullable = false)
  private Subject examSubject;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "achievement_id", nullable = false)
  private Leaderboard achievement;

  @Column(name = "score", nullable = false)
  @Builder.Default
  private Integer score = 0;
}
