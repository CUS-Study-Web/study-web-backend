package studyweb.cus.dto.response.course;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CourseDetailResponse(
    long subjectCount, float learningProgress, List<SubjectSummaryResponse> subjects) {

  public static CourseDetailResponse of(
      long subjectCount, Float learningProgress, List<SubjectSummaryResponse> subjects) {
    return new CourseDetailResponse(subjectCount, learningProgress, subjects);
  }
}
