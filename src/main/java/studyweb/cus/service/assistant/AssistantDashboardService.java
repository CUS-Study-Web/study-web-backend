package studyweb.cus.service.assistant;

import studyweb.cus.dto.response.assistant.AssistantDashboardResponse;

public interface AssistantDashboardService {
  AssistantDashboardResponse getDashboardStats(String email);
}
