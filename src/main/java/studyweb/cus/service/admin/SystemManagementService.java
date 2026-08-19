package studyweb.cus.service.admin;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import studyweb.cus.dto.request.admin.CreateVipAccountRequest;
import studyweb.cus.dto.response.admin.LearnerSummaryResponse;

public interface SystemManagementService {
  Page<LearnerSummaryResponse> listLearners(String search, Pageable pageable);

  void banLearner(UUID id);

  void unbanLearner(UUID id);

  LearnerSummaryResponse createVipAccount(CreateVipAccountRequest request);

  LearnerSummaryResponse updateAccount(CreateVipAccountRequest request);
}
