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
import studyweb.cus.entity.flashcard.FlashcardTopic;
import studyweb.cus.entity.user.User;

@Entity
@Table(
    name = "user_topic_progress",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_user_topic",
          columnNames = {"user_id", "topic_id"})
    },
    indexes = {@Index(name = "idx_user_topic_progress_user", columnList = "user_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTopicProgress extends AbstractBaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "topic_id", nullable = false)
  private FlashcardTopic topic;

  @Column(name = "progress_percent", nullable = false)
  @Builder.Default
  private Integer progressPercent = 0;
}
