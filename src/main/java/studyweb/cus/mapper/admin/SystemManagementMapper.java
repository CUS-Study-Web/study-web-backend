package studyweb.cus.mapper.admin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import studyweb.cus.dto.response.admin.LearnerSummaryResponse;
import studyweb.cus.entity.progress.UserCourseProgress;
import studyweb.cus.entity.user.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SystemManagementMapper {
  DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm");

  @Mapping(target = "id", source = "user.id")
  @Mapping(target = "gmail", source = "user.gmail")
  @Mapping(target = "status", source = "user.status")
  @Mapping(target = "mainCourse", source = "progress.course.title", defaultValue = "N/A")
  @Mapping(target = "progress", source = "progress.progressPercent", defaultValue = "0.0")
  @Mapping(target = "averageScore", source = "averageScore", qualifiedByName = "roundGpa")
  @Mapping(target = "lastLogin", source = "user.lastLogin", qualifiedByName = "formatLastLogin")
  LearnerSummaryResponse toLearnerSummary(
      User user, UserCourseProgress progress, Double averageScore);

  @Named("roundGpa")
  default Double roundGpa(Double score) {
    if (score == null) {
      return 0.0;
    }
    return Math.round(score * 10.0) / 10.0;
  }

  @Named("formatLastLogin")
  default String formatLastLogin(LocalDateTime dateTime) {
    if (dateTime == null) {
      return "Chưa đăng nhập";
    }
    return dateTime.format(DATE_FORMATTER);
  }
}
