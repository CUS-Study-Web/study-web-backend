package studyweb.cus.dto.response.course;

import java.util.List;
import org.springframework.data.domain.Page;

public record LessonListResponse(
    int page,
    int size,
    long totalElements,
    int totalPages,
    List<LessonSummaryResponse> lessons,
    long totalLessons) {

  public static LessonListResponse of(Page<?> pageFeed, List<LessonSummaryResponse> lessons) {
    long total = pageFeed.getTotalElements();
    return new LessonListResponse(
        pageFeed.getNumber(), pageFeed.getSize(), total, pageFeed.getTotalPages(), lessons, total);
  }
}
