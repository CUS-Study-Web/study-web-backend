package studyweb.cus.entity.flashcard;

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
import studyweb.cus.entity.user.User;

@Entity
@Table(
    name = "flashcards",
    indexes = {@Index(name = "idx_flashcards_topic", columnList = "topic_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flashcard extends AbstractBaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "topic_id", nullable = false)
  private FlashcardTopic topic;

  @Column(name = "word", nullable = false, length = 255)
  private String word;

  @Column(name = "meaning", nullable = false, columnDefinition = "TEXT")
  private String meaning;

  @Column(name = "pronunciation", length = 255)
  private String pronunciation;

  @Column(name = "part_of_speech", length = 100)
  private String partOfSpeech;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "updated_by")
  private User updatedBy;
}
