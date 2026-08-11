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
import studyweb.cus.enums.AccessTier;

@Entity
@Table(
    name = "lessons",
    indexes = {@Index(name = "idx_lessons_subject", columnList = "subject_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson extends AuditAbstractEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subject_id", nullable = false)
  private Subject subject;

  @Column(name = "order_num", nullable = false)
  @Builder.Default
  private Integer orderNum = 1;

  @Column(name = "title", nullable = false, length = 255)
  private String title;

  @Column(name = "youtube_url", length = 500)
  private String youtubeUrl;

  @Column(name = "duration_min")
  @Builder.Default
  private Integer durationMin = 0;

  @Enumerated(EnumType.STRING)
  @Column(name = "access", nullable = false, length = 20)
  @Builder.Default
  private AccessTier access = AccessTier.PUBLIC;
}
