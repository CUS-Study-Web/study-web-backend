package studyweb.cus.service.assistant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import studyweb.cus.dto.response.admin.ActivityLogResponse;
import studyweb.cus.dto.response.assistant.AssistantDashboardResponse;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.ActionType;
import studyweb.cus.enums.AssessmentType;
import studyweb.cus.enums.UserRole;
import studyweb.cus.exception.user.UserErrorCode;
import studyweb.cus.exception.user.UserException;
import studyweb.cus.repository.course.AssessmentRepository;
import studyweb.cus.repository.user.UserRepository;
import studyweb.cus.service.admin.SystemManagementService;
import studyweb.cus.service.assistant.impl.AssistantDashboardServiceImpl;

@ExtendWith(MockitoExtension.class)
class AssistantDashboardServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private AssessmentRepository assessmentRepository;
  @Mock private SystemManagementService systemManagementService;

  @InjectMocks private AssistantDashboardServiceImpl dashboardService;

  private User mockUser;
  private UUID mockUserId;

  @BeforeEach
  void setUp() {
    mockUserId = UUID.randomUUID();
    mockUser = new User();
    mockUser.setId(mockUserId);
    mockUser.setRole(UserRole.ASSISTANT);
  }

  @Test
  void getDashboardStats_shouldReturnCorrectStatsAndActivities() {
    // Arrange
    String mockEmail = "test@assistant.com";
    when(userRepository.findByGmail(mockEmail)).thenReturn(Optional.of(mockUser));
    when(userRepository.countByRole(UserRole.LEARNER)).thenReturn(1200);

    // Mock Loki queries for deltas
    when(systemManagementService.getActivityLogs(eq(1000), eq(List.of(ActionType.REGISTER)), eq(7)))
        .thenReturn(new PageImpl<>(List.of(
            new ActivityLogResponse("2026-09-22T10:00:00Z", "Learner 1", ActionType.REGISTER, "Reg"),
            new ActivityLogResponse("2026-09-21T10:00:00Z", "Learner 2", ActionType.REGISTER, "Reg")
        ))); // newLearnersDelta = 2

    // Mock Repository for total exercises (HOMEWORK) and total exams (EXAM)
    when(assessmentRepository.countByUploadedByIdAndAssessmentTypeAndDeletedAtIsNull(mockUserId, AssessmentType.HOMEWORK))
        .thenReturn(25L);
    when(assessmentRepository.countByUploadedByIdAndAssessmentTypeAndDeletedAtIsNull(mockUserId, AssessmentType.EXAM))
        .thenReturn(15L);

    // Mock Loki queries for new exercises/exams
    when(systemManagementService.getActivityLogs(
            eq(100), eq(List.of(ActionType.CREATE_ASSESSMENT)), eq(7), eq(mockEmail), isNull()))
        .thenReturn(new PageImpl<>(List.of(
            new ActivityLogResponse("2026-09-22T10:00:00Z", "test", ActionType.CREATE_ASSESSMENT, "Trợ giảng tạo bài tập 1"),
            new ActivityLogResponse("2026-09-22T10:00:00Z", "test", ActionType.CREATE_ASSESSMENT, "Created exam")
        )));

    // Mock Loki queries for recent activities
    when(systemManagementService.getActivityLogs(eq(10), org.mockito.ArgumentMatchers.anyList(), eq(7), isNull(), isNull()))
        .thenReturn(new PageImpl<>(List.of(
            new ActivityLogResponse("2026-09-22T10:00:00Z", "test", ActionType.CREATE_ASSESSMENT, "Created exam")
        )));

    // Act
    AssistantDashboardResponse response = dashboardService.getDashboardStats(mockEmail);

    // Assert
    assertNotNull(response);
    
    assertEquals(1200, response.totalLearners().value());
    assertEquals(2, response.totalLearners().delta());

    assertEquals(25, response.totalExercises().value());
    assertEquals(1, response.totalExercises().delta());

    assertEquals(15, response.totalExams().value());
    assertEquals(1, response.totalExams().delta());

    assertEquals(1, response.recentActivities().size());
    assertEquals("material", response.recentActivities().get(0).type());
    assertEquals("Created exam", response.recentActivities().get(0).text());
  }

  @Test
  void getDashboardStats_whenUserNotFound_shouldThrowUserException() {
    String mockEmail = "nonexistent@studyweb.edu";
    when(userRepository.findByGmail(mockEmail)).thenReturn(Optional.empty());

    UserException ex = assertThrows(UserException.class, () -> dashboardService.getDashboardStats(mockEmail));
    assertEquals(UserErrorCode.USER_NOT_FOUND.code(), ex.getCode());
  }

  @Test
  void getDashboardStats_whenNoAssessmentsOrActivities_shouldReturnZeroesAndEmptyList() {
    String mockEmail = "newbie@assistant.com";
    when(userRepository.findByGmail(mockEmail)).thenReturn(Optional.of(mockUser));
    when(userRepository.countByRole(UserRole.LEARNER)).thenReturn(0);

    when(systemManagementService.getActivityLogs(eq(1000), eq(List.of(ActionType.REGISTER)), eq(7)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));

    when(assessmentRepository.countByUploadedByIdAndAssessmentTypeAndDeletedAtIsNull(mockUserId, AssessmentType.HOMEWORK))
        .thenReturn(0L);
    when(assessmentRepository.countByUploadedByIdAndAssessmentTypeAndDeletedAtIsNull(mockUserId, AssessmentType.EXAM))
        .thenReturn(0L);

    when(systemManagementService.getActivityLogs(
            eq(100), eq(List.of(ActionType.CREATE_ASSESSMENT)), eq(7), eq(mockEmail), isNull()))
        .thenReturn(new PageImpl<>(Collections.emptyList()));

    when(systemManagementService.getActivityLogs(eq(10), org.mockito.ArgumentMatchers.anyList(), eq(7), isNull(), isNull()))
        .thenReturn(new PageImpl<>(Collections.emptyList()));

    AssistantDashboardResponse response = dashboardService.getDashboardStats(mockEmail);

    assertNotNull(response);
    assertEquals(0, response.totalLearners().value());
    assertEquals(0, response.totalLearners().delta());
    assertEquals(0, response.totalExercises().value());
    assertEquals(0, response.totalExercises().delta());
    assertEquals(0, response.totalExams().value());
    assertEquals(0, response.totalExams().delta());
    assertEquals(0, response.recentActivities().size());
  }

  @Test
  void getDashboardStats_withVariousActivityLogTypes_shouldMapToExpectedFeTypes() {
    String mockEmail = "active@assistant.com";
    when(userRepository.findByGmail(mockEmail)).thenReturn(Optional.of(mockUser));
    when(userRepository.countByRole(UserRole.LEARNER)).thenReturn(10);
    when(systemManagementService.getActivityLogs(eq(1000), eq(List.of(ActionType.REGISTER)), eq(7)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));

    when(assessmentRepository.countByUploadedByIdAndAssessmentTypeAndDeletedAtIsNull(mockUserId, AssessmentType.HOMEWORK))
        .thenReturn(8L);
    when(assessmentRepository.countByUploadedByIdAndAssessmentTypeAndDeletedAtIsNull(mockUserId, AssessmentType.EXAM))
        .thenReturn(5L);

    when(systemManagementService.getActivityLogs(
            eq(100), eq(List.of(ActionType.CREATE_ASSESSMENT)), eq(7), eq(mockEmail), isNull()))
        .thenReturn(new PageImpl<>(Collections.emptyList()));

    when(systemManagementService.getActivityLogs(eq(10), org.mockito.ArgumentMatchers.anyList(), eq(7), isNull(), isNull()))
        .thenReturn(new PageImpl<>(List.of(
            new ActivityLogResponse("2026-09-22T10:00:00Z", "u1", ActionType.CREATE_COURSE, "Tạo khóa học"),
            new ActivityLogResponse("2026-09-22T10:05:00Z", "u2", ActionType.SUBMIT_ASSESSMENT, "Học viên nộp bài"),
            new ActivityLogResponse("2026-09-22T10:10:00Z", "u3", ActionType.UPLOAD_DOCUMENT, "Tải lên tài liệu"),
            new ActivityLogResponse("2026-09-22T10:15:00Z", "u4", ActionType.LOGIN, "Đăng nhập")
        )));

    AssistantDashboardResponse response = dashboardService.getDashboardStats(mockEmail);

    assertEquals(4, response.recentActivities().size());
    assertEquals("course", response.recentActivities().get(0).type());
    assertEquals("student", response.recentActivities().get(1).type());
    assertEquals("material", response.recentActivities().get(2).type());
    assertEquals("other", response.recentActivities().get(3).type());
  }
}
