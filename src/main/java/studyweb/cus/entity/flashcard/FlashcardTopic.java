package studyweb.cus.entity.flashcard;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import studyweb.cus.entity.AbstractAuditEntity;
import studyweb.cus.entity.user.User;

@Entity
@Table(name = "flashcard_topics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashcardTopic extends AbstractAuditEntity {

  @Column(name = "title", nullable = false, length = 255)
  private String title;

  @Column(name = "num_words", nullable = false)
  @Builder.Default
  private Integer numWords = 0;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "updated_by")
  private User updatedBy;
}
