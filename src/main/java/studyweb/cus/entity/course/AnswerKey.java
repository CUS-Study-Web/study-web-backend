package studyweb.cus.entity.course;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import studyweb.cus.entity.AuditAbstractEntity;
import studyweb.cus.enums.CorrectAnswer;

@Entity
@Table(
    name = "answer_keys",
    indexes = {@Index(name = "idx_answer_keys_exam", columnList = "exam_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerKey extends AuditAbstractEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exam_id", nullable = false)
  private Assessment exam;

  @Column(name = "question_number", nullable = false)
  private Integer questionNumber;

  @Column(name = "question_type", length = 50)
  @Builder.Default
  private String questionType = "MULTIPLE_CHOICE";

  @Enumerated(EnumType.STRING)
  @Column(name = "correct_answer", nullable = false, length = 10)
  private CorrectAnswer correctAnswer;
}
