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
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import studyweb.cus.entity.AbstractBaseEntity;
import studyweb.cus.enums.AccessTier;
import studyweb.cus.enums.AssessmentFileType;
import studyweb.cus.enums.AssessmentStatus;
import studyweb.cus.enums.AssessmentType;

@Entity
@Table(
    name = "assessments",
    indexes = {
      @Index(name = "idx_assessments_course", columnList = "course_id"),
      @Index(name = "idx_assessments_lesson", columnList = "lesson_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assessment extends AbstractBaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id")
  private Course course;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "lesson_id")
  private Lesson lesson;

  @Column(name = "title", nullable = false, length = 255)
  private String title;

  @Column(name = "duration_min")
  @Builder.Default
  private Integer durationMin = 0;

  @Column(name = "num_questions")
  @Builder.Default
  private Integer numQuestions = 0;

  @Column(name = "max_score")
  @Builder.Default
  private Integer maxScore = 100;

  @Enumerated(EnumType.STRING)
  @Column(name = "file_type", length = 20)
  private AssessmentFileType fileType;

  @Column(name = "file_url", length = 500)
  private String fileUrl;

  @Enumerated(EnumType.STRING)
  @Column(name = "access", nullable = false, length = 20)
  @Builder.Default
  private AccessTier access = AccessTier.PUBLIC;

  @Enumerated(EnumType.STRING)
  @Column(name = "assessment_type", nullable = false, length = 20)
  @Builder.Default
  private AssessmentType assessmentType = AssessmentType.HOMEWORK;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 50)
  @Builder.Default
  private AssessmentStatus status = AssessmentStatus.DRAFT;

  @Column(name = "explanation_url", length = 500)
  private String explanationUrl;

  @Column(name = "is_draft", nullable = false)
  @Builder.Default
  private Boolean isDraft = true;

  @Column(name = "published_at")
  private LocalDateTime publishedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
