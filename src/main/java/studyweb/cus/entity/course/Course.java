package studyweb.cus.entity.course;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import studyweb.cus.entity.AbstractAuditEntity;
import studyweb.cus.enums.CourseCreateStatus;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course extends AbstractAuditEntity {

  @Column(name = "title", nullable = false, length = 255)
  private String title;

  @Column(name = "subtitle", length = 255)
  private String subtitle;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "badge_title", length = 100)
  private String badgeTitle;

  @Column(name = "thumbnail_url", length = 500)
  private String thumbnailUrl;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  @Builder.Default
  private CourseCreateStatus status = CourseCreateStatus.DRAFT;
}
