package studyweb.cus.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import studyweb.cus.entity.flashcard.Flashcard;
import studyweb.cus.entity.flashcard.FlashcardTopic;
import studyweb.cus.entity.user.User;

@DisplayName("Flashcard Domain Entities Test")
class FlashcardEntityTest {

  @Test
  @DisplayName("Should build FlashcardTopic and Flashcard entities")
  void testFlashcardTopicAndCardBuilder() {
    User admin = User.builder().gmail("admin@studyweb.edu").build();
    admin.setId(UUID.randomUUID());

    FlashcardTopic topic =
        FlashcardTopic.builder()
            .title("IELTS Vocabulary Band 8+")
            .numWords(50)
            .description("High frequency academic words")
            .updatedBy(admin)
            .build();

    topic.setId(UUID.randomUUID());

    Flashcard card =
        Flashcard.builder()
            .topic(topic)
            .word("Eloquent")
            .meaning("Fluent or persuasive in speaking or writing")
            .pronunciation("/ˈel.ə.kwənt/")
            .partOfSpeech("Adjective")
            .updatedBy(admin)
            .build();

    assertThat(topic.getTitle()).isEqualTo("IELTS Vocabulary Band 8+");
    assertThat(topic.getNumWords()).isEqualTo(50);
    assertThat(card.getTopic()).isEqualTo(topic);
    assertThat(card.getWord()).isEqualTo("Eloquent");
    assertThat(card.getPronunciation()).isEqualTo("/ˈel.ə.kwənt/");
    assertThat(card.getPartOfSpeech()).isEqualTo("Adjective");
  }
}
