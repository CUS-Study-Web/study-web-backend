package studyweb.cus.service.admin;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import studyweb.cus.dto.request.admin.CreateAssistantRequest;
import studyweb.cus.dto.request.admin.CreateVipAccountRequest;
import studyweb.cus.dto.request.admin.UpdateAccountRequest;
import studyweb.cus.dto.response.admin.AssistantSummaryResponse;
import studyweb.cus.dto.response.admin.LearnerSummaryResponse;
import studyweb.cus.dto.response.admin.VipRequestListResponse;
import studyweb.cus.enums.UserRole;
import studyweb.cus.enums.UserStatus;
import studyweb.cus.enums.VipRequestStatus;

public interface SystemManagementService {
  Page<LearnerSummaryResponse> listLearners(String search, UserStatus status, Pageable pageable);

  void switchUserStatus(UUID id, UserStatus status, UserRole role);

  void createVipAccount(CreateVipAccountRequest request);

  void updateLearnerAccount(UUID id, UpdateAccountRequest request);

  Page<AssistantSummaryResponse> listAssistants(
      String search, UserStatus status, Pageable pageable);

  void createAssistant(CreateAssistantRequest request);

  VipRequestListResponse getVipRequests(String search, VipRequestStatus status, Pageable pageable);

  void approveVipRequest(UUID id);

  void disapproveVipRequest(UUID id);
}
