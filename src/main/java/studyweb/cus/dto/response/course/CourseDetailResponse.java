package studyweb.cus.dto.response.course;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CourseDetailResponse(
    long subjectCount, Integer learningProgress, List<SubjectSummaryResponse> subjects) {

  public static CourseDetailResponse of(long subjectCount, List<SubjectSummaryResponse> subjects) {
    int avg =
        subjects.isEmpty()
            ? 0
            : (int)
                subjects.stream()
                    .mapToInt(s -> s.learningProgress() != null ? s.learningProgress() : 0)
                    .average()
                    .orElse(0);
    return new CourseDetailResponse(subjectCount, avg, subjects);
  }
}
