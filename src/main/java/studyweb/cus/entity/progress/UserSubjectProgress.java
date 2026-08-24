package studyweb.cus.entity.progress;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import studyweb.cus.entity.AbstractBaseEntity;
import studyweb.cus.entity.course.Subject;
import studyweb.cus.entity.user.User;

@Entity
@Table(
    name = "user_subject_progress",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_user_subject",
          columnNames = {"user_id", "subject_id"})
    },
    indexes = {@Index(name = "idx_user_subject_progress_user", columnList = "user_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubjectProgress extends AbstractBaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subject_id", nullable = false)
  private Subject subject;

  @Column(name = "progress_percent", nullable = false)
  @Builder.Default
  private Integer progressPercent = 0;
}
