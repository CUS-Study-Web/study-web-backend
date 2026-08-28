package studyweb.cus.service.badge;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import studyweb.cus.dto.request.badge.BadgeRequest;
import studyweb.cus.dto.response.badge.BadgeResponse;

public interface BadgeService {

  BadgeResponse createBadge(BadgeRequest request, String adminEmail);

  BadgeResponse getBadgeById(UUID id);

  Page<BadgeResponse> listBadges(String search, Pageable pageable);

  List<BadgeResponse> listAllBadges();

  BadgeResponse updateBadge(UUID id, BadgeRequest request);

  void deleteBadge(UUID id);
}
