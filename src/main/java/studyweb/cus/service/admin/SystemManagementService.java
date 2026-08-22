package studyweb.cus.service.admin;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import studyweb.cus.dto.request.admin.CreateVipAccountRequest;
import studyweb.cus.dto.request.admin.UpdateAccountRequest;
import studyweb.cus.dto.response.admin.LearnerSummaryResponse;
import studyweb.cus.enums.UserStatus;

public interface SystemManagementService {
  Page<LearnerSummaryResponse> listLearners(String search, UserStatus status, Pageable pageable);

  void lockLearner(UUID id);

  void unlockLearner(UUID id);

  void createVipAccount(CreateVipAccountRequest request);

  void updateLearnerAccount(UUID id, UpdateAccountRequest request);

  void banLearner(UUID id);
}
