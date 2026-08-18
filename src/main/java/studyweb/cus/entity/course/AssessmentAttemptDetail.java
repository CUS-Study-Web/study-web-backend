package studyweb.cus.entity.course;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import studyweb.cus.entity.AbstractBaseEntity;
import studyweb.cus.enums.CorrectAnswer;

@Entity
@Table(name = "assessment_attempt_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentAttemptDetail extends AbstractBaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "attempt_id", nullable = false)
  private AssessmentAttempt attempt;

  @Column(name = "question_number", nullable = false)
  private Integer questionNumber;

  @Enumerated(EnumType.STRING)
  @Column(name = "selected_answer")
  private CorrectAnswer selectedAnswer;

  @Enumerated(EnumType.STRING)
  @Column(name = "correct_answer", nullable = false)
  private CorrectAnswer correctAnswer;

  @Column(name = "is_correct", nullable = false)
  private Boolean isCorrect;
}
