package studyweb.cus.mapper.flashcard;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import studyweb.cus.dto.response.flashcard.FlashcardResponse;
import studyweb.cus.dto.response.flashcard.FlashcardTopicResponse;
import studyweb.cus.entity.flashcard.Flashcard;
import studyweb.cus.entity.flashcard.FlashcardTopic;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FlashcardMapper {

  @Mapping(source = "topic.updatedBy.id", target = "updatedBy")
  FlashcardTopicResponse toTopicResponse(FlashcardTopic topic);

  @Mapping(source = "card.topic.id", target = "topicId")
  @Mapping(source = "card.updatedBy.id", target = "updatedBy")
  FlashcardResponse toFlashcardResponse(Flashcard card);
}
