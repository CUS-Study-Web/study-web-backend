package studyweb.cus.entity.progress;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import studyweb.cus.entity.flashcard.Flashcard;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.FlashcardProgressStatus;

@Entity
@Table(
    name = "user_flashcard_progress",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_user_flashcard",
          columnNames = {"user_id", "flashcard_id"})
    },
    indexes = {@Index(name = "idx_user_flashcard_progress_user", columnList = "user_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFlashcardProgress extends AbstractBaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "flashcard_id", nullable = false)
  private Flashcard flashcard;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  @Builder.Default
  private FlashcardProgressStatus status = FlashcardProgressStatus.STUDY;
}
