package studyweb.cus.dto.response.flashcard;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.UUID;
import studyweb.cus.enums.CourseCreateStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FlashcardTopicResponse(
    UUID id,
    String title,
    Integer numWords,
    String description,
    CourseCreateStatus status,
    UUID updatedBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
