package studyweb.cus.dto.response.course;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CourseDetailResponse(
    int page,
    int size,
    long totalElements,
    int totalPages,
    long totalSubjects,
    Integer learningProgress,
    List<SubjectSummaryResponse> subjects) {

  public static CourseDetailResponse of(
      long totalSubjects, Integer learningProgress, List<SubjectSummaryResponse> subjects) {
    return new CourseDetailResponse(
        1, subjects.size(), subjects.size(), 1, totalSubjects, learningProgress, subjects);
  }
}
