package studyweb.cus.service.badge.impl;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studyweb.cus.dto.request.badge.BadgeRequest;
import studyweb.cus.dto.response.badge.BadgeResponse;
import studyweb.cus.entity.badge.Badge;
import studyweb.cus.entity.user.User;
import studyweb.cus.exception.badge.BadgeErrorCode;
import studyweb.cus.exception.badge.BadgeException;
import studyweb.cus.mapper.badge.BadgeMapper;
import studyweb.cus.repository.badge.BadgeRepository;
import studyweb.cus.repository.user.UserRepository;
import studyweb.cus.service.badge.BadgeService;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeServiceImpl implements BadgeService {

  private final BadgeRepository badgeRepository;
  private final UserRepository userRepository;
  private final BadgeMapper badgeMapper;

  @Override
  @Transactional
  public BadgeResponse createBadge(BadgeRequest request, String adminEmail) {
    String name = request.name() != null ? request.name().trim() : "";
    if (name.isBlank()) {
      throw new BadgeException(BadgeErrorCode.BADGE_NAME_EMPTY);
    }
    if (badgeRepository.existsByName(name)) {
      throw new BadgeException(BadgeErrorCode.BADGE_NAME_EXISTS);
    }

    User createdBy = null;
    if (adminEmail != null && !adminEmail.isBlank()) {
      createdBy = userRepository.findByGmail(adminEmail).orElse(null);
    }

    Badge badge = Badge.builder().name(name).createdBy(createdBy).build();
    Badge savedBadge = badgeRepository.save(badge);
    log.info("Badge created successfully with ID {}", savedBadge.getId());
    return badgeMapper.toResponse(savedBadge);
  }

  @Override
  @Transactional(readOnly = true)
  public BadgeResponse getBadgeById(UUID id) {
    log.info("Fetching badge ID {}", id);
    Badge badge = requireBadge(id);
    return badgeMapper.toResponse(badge);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<BadgeResponse> listBadges(String search, Pageable pageable) {
    log.info("Listing badges: search='{}'", search);
    Specification<Badge> spec =
        (root, query, cb) -> {
          if (search != null && !search.isBlank()) {
            return cb.like(cb.lower(root.get("name")), "%" + search.trim().toLowerCase() + "%");
          }
          return cb.conjunction();
        };
    return badgeRepository.findAll(spec, pageable).map(badgeMapper::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public List<BadgeResponse> listAllBadges() {
    log.info("Listing all badges");
    return badgeRepository.findAll().stream().map(badgeMapper::toResponse).toList();
  }

  @Override
  @Transactional
  public BadgeResponse updateBadge(UUID id, BadgeRequest request) {
    log.info("Updating badge ID {}", id);
    Badge badge = requireBadge(id);
    String name = request.name() != null ? request.name().trim() : "";
    if (name.isBlank()) {
      throw new BadgeException(BadgeErrorCode.BADGE_NAME_EMPTY);
    }
    if (badgeRepository.existsByNameAndIdNot(name, id)) {
      throw new BadgeException(BadgeErrorCode.BADGE_NAME_EXISTS);
    }

    badge.setName(name);
    Badge updatedBadge = badgeRepository.save(badge);
    log.info("Badge ID {} updated successfully", id);
    return badgeMapper.toResponse(updatedBadge);
  }

  @Override
  @Transactional
  public void deleteBadge(UUID id) {
    log.info("Deleting badge ID {}", id);
    Badge badge = requireBadge(id);
    badgeRepository.delete(badge);
    log.info("Badge ID {} deleted successfully", id);
  }

  private Badge requireBadge(UUID id) {
    return badgeRepository
        .findById(id)
        .orElseThrow(() -> new BadgeException(BadgeErrorCode.BADGE_NOT_FOUND));
  }
}
