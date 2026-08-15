package studyweb.cus.entity.course;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import studyweb.cus.entity.AbstractBaseEntity;

@Entity
@Table(
    name = "reviews",
    indexes = {@Index(name = "idx_reviews_course", columnList = "course_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends AbstractBaseEntity {

  @Column(name = "student_name", nullable = false, length = 150)
  private String studentName;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  @Column(name = "comment", nullable = false, columnDefinition = "TEXT")
  private String comment;

  @Column(name = "time_text", length = 50)
  private String timeText;

  @Column(name = "avatar_url", length = 500)
  private String avatarUrl;
}
