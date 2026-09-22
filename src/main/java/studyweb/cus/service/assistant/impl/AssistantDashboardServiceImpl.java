package studyweb.cus.service.assistant.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import studyweb.cus.dto.response.admin.ActivityLogResponse;
import studyweb.cus.dto.response.assistant.AssistantActivityItemResponse;
import studyweb.cus.dto.response.assistant.AssistantDashboardResponse;
import studyweb.cus.dto.response.assistant.AssistantStatResponse;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.ActionType;
import studyweb.cus.enums.UserRole;
import studyweb.cus.repository.course.AssessmentRepository;
import studyweb.cus.repository.user.UserRepository;
import studyweb.cus.service.admin.SystemManagementService;
import studyweb.cus.service.assistant.AssistantDashboardService;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssistantDashboardServiceImpl implements AssistantDashboardService {

  private final UserRepository userRepository;
  private final AssessmentRepository assessmentRepository;
  private final SystemManagementService systemManagementService;

  private static final int DELTA_DAYS = 7;
  private static final int MAX_NEW_LEARNERS_LIMIT = 1000;
  private static final int MAX_NEW_ASSESSMENTS_LIMIT = 100;
  private static final int RECENT_ACTIVITIES_LIMIT = 10;

  @Override
  public AssistantDashboardResponse getDashboardStats(String email) {
    User currentUser = userRepository.findByGmail(email).orElseThrow(() -> new studyweb.cus.exception.user.UserException(studyweb.cus.exception.user.UserErrorCode.USER_NOT_FOUND));
    UUID currentUserId = currentUser.getId();

    // Total Learners Stats
    long totalLearners = userRepository.countByRole(UserRole.LEARNER);
    
    // We can use Loki for estimating new learners delta
    long newLearnersDelta = systemManagementService.getActivityLogs(MAX_NEW_LEARNERS_LIMIT, List.of(ActionType.REGISTER), DELTA_DAYS).size();

    // Total Exercises / Exams Stats (Estimating based on all assessments)
    List<Object[]> examsCounts = assessmentRepository.countExamsByAssistantIds(List.of(currentUserId));
    long totalAssessments = examsCounts.isEmpty() ? 0 : (long) examsCounts.get(0)[1];
    
    long totalExercises = totalAssessments; 
    long newExercisesDelta = systemManagementService.getActivityLogs(MAX_NEW_ASSESSMENTS_LIMIT, List.of(ActionType.CREATE_ASSESSMENT), DELTA_DAYS, email, null).size();
    
    long totalExams = totalAssessments; // Since AssessmentRepository doesn't distinguish in the group by
    long newExamsDelta = newExercisesDelta; 

    // Recent Activities via Loki
    List<ActionType> allowedActions = List.of(
        ActionType.CREATE_COURSE, ActionType.UPDATE_COURSE, ActionType.DELETE_COURSE,
        ActionType.CREATE_LESSON, ActionType.UPDATE_LESSON, ActionType.DELETE_LESSON,
        ActionType.CREATE_ASSESSMENT, ActionType.UPDATE_ASSESSMENT, ActionType.DELETE_ASSESSMENT, ActionType.SUBMIT_ASSESSMENT,
        ActionType.UPLOAD_DOCUMENT, ActionType.UPDATE_DOCUMENT, ActionType.DELETE_DOCUMENT, ActionType.CREATE_FLASHCARD_TOPIC
    );
    List<ActivityLogResponse> activities = systemManagementService.getActivityLogs(RECENT_ACTIVITIES_LIMIT, allowedActions, DELTA_DAYS, null, null);
    
    List<AssistantActivityItemResponse> recentActivities = activities.stream()
        .map(this::mapToActivityResponse)
        .toList();

    return new AssistantDashboardResponse(
        new AssistantStatResponse(totalLearners, newLearnersDelta),
        new AssistantStatResponse(totalExercises, newExercisesDelta),
        new AssistantStatResponse(totalExams, newExamsDelta),
        recentActivities
    );
  }

  private AssistantActivityItemResponse mapToActivityResponse(ActivityLogResponse log) {
    String type = mapActionTypeToFeType(log.actionType());
    LocalDateTime createdAt = null;
    if (log.timestamp() != null) {
        try {
            createdAt = LocalDateTime.parse(log.timestamp(), DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            // ignore
        }
    }
    return new AssistantActivityItemResponse(
        UUID.randomUUID(), // FE requires ID
        type,
        log.description(),
        createdAt
    );
  }

  private String mapActionTypeToFeType(ActionType actionType) {
    if (actionType == null) {
      return "other";
    }
    return switch (actionType) {
      case CREATE_ASSESSMENT, UPDATE_ASSESSMENT, DELETE_ASSESSMENT, UPLOAD_DOCUMENT, UPDATE_DOCUMENT, DELETE_DOCUMENT, CREATE_FLASHCARD_TOPIC -> "material";
      case CREATE_COURSE, UPDATE_COURSE, DELETE_COURSE, CREATE_LESSON, UPDATE_LESSON, DELETE_LESSON -> "course";
      case SUBMIT_ASSESSMENT, REGISTER, REQUEST_VIP -> "student";
      default -> "other";
    };
  }
}
