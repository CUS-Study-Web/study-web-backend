package studyweb.cus.dto.response.flashcard;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LearnerTopicDetailResponse(
    UUID id,
    String title,
    String description,
    Integer totalWords,
    Integer rememberedWords,
    Integer studyWords,
    Integer progressPercent) {}
