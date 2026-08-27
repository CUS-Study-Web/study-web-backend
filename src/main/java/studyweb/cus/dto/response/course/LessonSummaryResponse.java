package studyweb.cus.dto.response.course;

import java.util.List;
import java.util.UUID;

public record LessonSummaryResponse(Integer lessonCount, List<LessonCardResponse> lessons) {
  public record LessonCardResponse(
      UUID id,
      Integer orderNum,
      String title,
      Integer durationMin,
      String youtubeUrl,
      boolean isClicked,
      boolean isVip) {}
}
