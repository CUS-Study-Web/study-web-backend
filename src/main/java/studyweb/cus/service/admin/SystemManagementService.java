package studyweb.cus.service.admin;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import studyweb.cus.dto.request.admin.CreateAssistantRequest;
import studyweb.cus.dto.request.admin.CreateVipAccountRequest;
import studyweb.cus.dto.response.admin.AssistantSummaryResponse;
import studyweb.cus.dto.response.admin.LearnerSummaryResponse;

public interface SystemManagementService {
  Page<LearnerSummaryResponse> listLearners(String search, Pageable pageable);

  void banLearner(UUID id);

  void unbanLearner(UUID id);

  LearnerSummaryResponse createVipAccount(CreateVipAccountRequest request);

  LearnerSummaryResponse updateAccount(CreateVipAccountRequest request);

  void deleteLearner(UUID id);

  Page<AssistantSummaryResponse> listAssistants(String search, Pageable pageable);

  AssistantSummaryResponse createAssistant(CreateAssistantRequest request);

  void deactivateAssistant(UUID id);

  void activateAssistant(UUID id);

  void deleteAssistant(UUID id);
}
