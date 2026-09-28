package studyweb.cus.dto.response.assistant;

import java.util.List;

public record AssistantDashboardResponse(
    AssistantStatResponse totalLearners,
    AssistantStatResponse totalExercises,
    AssistantStatResponse totalExams,
    List<AssistantActivityItemResponse> recentActivities
) {}
