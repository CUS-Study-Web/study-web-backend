package studyweb.cus.service.assistant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import studyweb.cus.dto.response.admin.ActivityLogResponse;
import studyweb.cus.dto.response.assistant.AssistantDashboardResponse;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.ActionType;
import studyweb.cus.enums.UserRole;
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
    when(userRepository.findByGmail(mockEmail)).thenReturn(java.util.Optional.of(mockUser));
    when(userRepository.countByRole(UserRole.LEARNER)).thenReturn(1200);

    // Mock Loki queries for deltas
    when(systemManagementService.getActivityLogs(eq(1000), eq(List.of(ActionType.REGISTER)), eq(7)))
        .thenReturn(List.of(
            new ActivityLogResponse("2026-09-22T10:00:00Z", "Learner 1", ActionType.REGISTER, "Reg"),
            new ActivityLogResponse("2026-09-21T10:00:00Z", "Learner 2", ActionType.REGISTER, "Reg")
        )); // newLearnersDelta = 2

    // Mock Repository for total exercises/exams
    List<Object[]> examsCountResult = java.util.Collections.singletonList(new Object[]{mockUserId, 40L});
    when(assessmentRepository.countExamsByAssistantIds(List.of(mockUserId)))
        .thenReturn(examsCountResult);

    // Mock Loki queries for new exercises/exams
    when(systemManagementService.getActivityLogs(
            eq(100), eq(List.of(ActionType.CREATE_ASSESSMENT)), eq(7), eq(mockEmail), isNull()))
        .thenReturn(List.of(
            new ActivityLogResponse("2026-09-22T10:00:00Z", "test", ActionType.CREATE_ASSESSMENT, "Created exam")
        )); // newExercisesDelta = 1

    // Mock Loki queries for recent activities
    when(systemManagementService.getActivityLogs(eq(10), org.mockito.ArgumentMatchers.anyList(), eq(7), isNull(), isNull()))
        .thenReturn(List.of(
            new ActivityLogResponse("2026-09-22T10:00:00Z", "test", ActionType.CREATE_ASSESSMENT, "Created exam")
        ));

    // Act
    AssistantDashboardResponse response = dashboardService.getDashboardStats(mockEmail);

    // Assert
    assertNotNull(response);
    
    assertEquals(1200, response.totalLearners().value());
    assertEquals(2, response.totalLearners().delta());

    assertEquals(40, response.totalExercises().value());
    assertEquals(1, response.totalExercises().delta());

    assertEquals(40, response.totalExams().value());
    assertEquals(1, response.totalExams().delta());

    assertEquals(1, response.recentActivities().size());
    assertEquals("material", response.recentActivities().get(0).type());
    assertEquals("Created exam", response.recentActivities().get(0).text());
  }
}
