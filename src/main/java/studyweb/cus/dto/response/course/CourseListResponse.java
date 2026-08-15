package studyweb.cus.dto.response.course;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import org.springframework.data.domain.Page;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CourseListResponse(
    int page,
    int size,
    long totalElements,
    int totalPages,
    List<CourseSummaryResponse> courses,
    long total) {

  public static CourseListResponse of(Page<?> pageFeed, List<CourseSummaryResponse> courses) {
    long total = pageFeed.getTotalElements();
    return new CourseListResponse(
        pageFeed.getNumber(), pageFeed.getSize(), total, pageFeed.getTotalPages(), courses, total);
  }
}
