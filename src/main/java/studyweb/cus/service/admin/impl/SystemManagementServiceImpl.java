package studyweb.cus.service.admin.impl;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studyweb.cus.dto.request.admin.CreateVipAccountRequest;
import studyweb.cus.dto.response.admin.LearnerSummaryResponse;
import studyweb.cus.service.admin.SystemManagementService;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemManagementServiceImpl implements SystemManagementService {
  @Override
  @Transactional(readOnly = true)
  public Page<LearnerSummaryResponse> listLearners(String search, Pageable pageable) {
    throw new UnsupportedOperationException("listLearners not implemented yet");
  }

  @Override
  @Transactional
  public void banLearner(UUID id) {
    throw new UnsupportedOperationException("banLearner not implemented yet");
  }

  @Override
  @Transactional
  public void unbanLearner(UUID id) {
    throw new UnsupportedOperationException("unbanLearner not implemented yet");
  }

  @Override
  @Transactional
  public LearnerSummaryResponse createVipAccount(CreateVipAccountRequest request) {
    throw new UnsupportedOperationException("createVipAccount not implemented yet");
  }
}
