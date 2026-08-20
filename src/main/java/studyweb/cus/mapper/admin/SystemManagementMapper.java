package studyweb.cus.mapper.admin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import studyweb.cus.dto.response.admin.AssistantActivityResponse;
import studyweb.cus.dto.response.admin.AssistantSummaryResponse;
import studyweb.cus.dto.response.admin.LearnerSummaryResponse;
import studyweb.cus.entity.progress.UserCourseProgress;
import studyweb.cus.entity.user.ActivityLog;
import studyweb.cus.entity.user.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SystemManagementMapper {
  DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm");

  @Mapping(target = "id", source = "user.id")
  @Mapping(target = "gmail", source = "user.gmail")
  @Mapping(target = "name", source = "user.name")
  @Mapping(target = "status", source = "user.status")
  @Mapping(target = "tier", source = "user.tier")
  @Mapping(target = "mainCourse", source = "progress.course.title", defaultValue = "N/A")
  @Mapping(target = "progress", source = "progress.progressPercent", defaultValue = "0.0")
  @Mapping(target = "averageScore", source = "averageScore", qualifiedByName = "roundGpa")
  @Mapping(target = "lastLogin", source = "user.lastLogin", qualifiedByName = "formatLastLogin")
  @Mapping(target = "numExams", source = "numExams")
  @Mapping(target = "note", source = "user.note")
  @Mapping(target = "vipStartDate", source = "user.vipStartDate")
  @Mapping(target = "vipEndDate", source = "user.vipEndDate")
  @Mapping(target = "avatarUrl", source = "user.avatarUrl")
  LearnerSummaryResponse toLearnerSummary(
      User user, UserCourseProgress progress, Double averageScore, int numExams);

  @Mapping(target = "id", source = "user.id")
  @Mapping(target = "name", source = "user.name")
  @Mapping(target = "gmail", source = "user.gmail")
  @Mapping(target = "phone", source = "user.phone")
  @Mapping(target = "status", source = "user.status")
  @Mapping(target = "numExams", source = "numExams")
  @Mapping(target = "lastLogin", source = "user.lastLogin", qualifiedByName = "formatLastLogin")
  @Mapping(target = "recentActivities", source = "recentActivities")
  @Mapping(target = "avatarUrl", source = "user.avatarUrl")
  AssistantSummaryResponse toAssistantSummary(
      User user, int numExams, List<AssistantActivityResponse> recentActivities);

  @Mapping(target = "id", source = "activityLog.id")
  @Mapping(target = "description", source = "activityLog.description")
  @Mapping(
      target = "timestamp",
      source = "activityLog.createdAt",
      qualifiedByName = "formatLastLogin")
  AssistantActivityResponse toAssistantActivity(ActivityLog activityLog);

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
