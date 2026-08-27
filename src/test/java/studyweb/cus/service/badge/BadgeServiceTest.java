package studyweb.cus.service.badge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import studyweb.cus.dto.request.badge.BadgeRequest;
import studyweb.cus.dto.response.badge.BadgeResponse;
import studyweb.cus.entity.badge.Badge;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.UserRole;
import studyweb.cus.enums.UserTier;
import studyweb.cus.exception.badge.BadgeErrorCode;
import studyweb.cus.exception.badge.BadgeException;
import studyweb.cus.mapper.badge.BadgeMapper;
import studyweb.cus.repository.badge.BadgeRepository;
import studyweb.cus.repository.user.UserRepository;
import studyweb.cus.service.badge.impl.BadgeServiceImpl;

@ExtendWith(MockitoExtension.class)
class BadgeServiceTest {

  @Mock private BadgeRepository badgeRepository;
  @Mock private UserRepository userRepository;

  private BadgeMapper badgeMapper = Mappers.getMapper(BadgeMapper.class);

  private BadgeService badgeService;

  private User adminUser;
  private Badge sampleBadge;

  @BeforeEach
  void setUp() {
    badgeService = new BadgeServiceImpl(badgeRepository, userRepository, badgeMapper);

    adminUser =
        User.builder()
            .gmail("admin@studyweb.edu")
            .name("Admin User")
            .role(UserRole.ADMIN)
            .tier(UserTier.VIP)
            .build();
    adminUser.setId(UUID.randomUUID());

    sampleBadge = Badge.builder().name("Toán").createdBy(adminUser).build();
    sampleBadge.setId(UUID.randomUUID());
  }

  @Nested
  @DisplayName("Create Badge Tests")
  class CreateBadgeTests {

    @Test
    @DisplayName("Should successfully create a badge")
    void shouldCreateBadgeSuccessfully() {
      BadgeRequest request = new BadgeRequest("Toán");

      when(badgeRepository.existsByName("Toán")).thenReturn(false);
      when(userRepository.findByGmail("admin@studyweb.edu")).thenReturn(Optional.of(adminUser));
      when(badgeRepository.save(any(Badge.class)))
          .thenAnswer(
              inv -> {
                Badge b = inv.getArgument(0);
                b.setId(UUID.randomUUID());
                return b;
              });

      BadgeResponse response = badgeService.createBadge(request, "admin@studyweb.edu");

      assertThat(response).isNotNull();
      assertThat(response.name()).isEqualTo("Toán");
      assertThat(response.createdBy()).isEqualTo(adminUser.getId());
    }

    @Test
    @DisplayName("Should throw BADGE_NAME_EXISTS when badge name is duplicate")
    void shouldThrowWhenBadgeNameExists() {
      BadgeRequest request = new BadgeRequest("Toán");

      when(badgeRepository.existsByName("Toán")).thenReturn(true);

      assertThatThrownBy(() -> badgeService.createBadge(request, "admin@studyweb.edu"))
          .isInstanceOf(BadgeException.class)
          .satisfies(
              ex ->
                  assertThat(((BadgeException) ex).getCode())
                      .isEqualTo(BadgeErrorCode.BADGE_NAME_EXISTS.code()));
    }

    @Test
    @DisplayName("Should throw BADGE_NAME_EMPTY when badge name is blank")
    void shouldThrowWhenBadgeNameEmpty() {
      BadgeRequest request = new BadgeRequest("   ");

      assertThatThrownBy(() -> badgeService.createBadge(request, "admin@studyweb.edu"))
          .isInstanceOf(BadgeException.class)
          .satisfies(
              ex ->
                  assertThat(((BadgeException) ex).getCode())
                      .isEqualTo(BadgeErrorCode.BADGE_NAME_EMPTY.code()));
    }
  }

  @Nested
  @DisplayName("Get & List Badges Tests")
  class GetAndListBadgesTests {

    @Test
    @DisplayName("Should get badge by ID")
    void shouldGetBadgeById() {
      when(badgeRepository.findById(sampleBadge.getId())).thenReturn(Optional.of(sampleBadge));

      BadgeResponse response = badgeService.getBadgeById(sampleBadge.getId());

      assertThat(response).isNotNull();
      assertThat(response.name()).isEqualTo("Toán");
    }

    @Test
    @DisplayName("Should throw BADGE_NOT_FOUND when badge does not exist")
    void shouldThrowWhenBadgeNotFound() {
      UUID missingId = UUID.randomUUID();
      when(badgeRepository.findById(missingId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> badgeService.getBadgeById(missingId))
          .isInstanceOf(BadgeException.class)
          .satisfies(
              ex ->
                  assertThat(((BadgeException) ex).getCode())
                      .isEqualTo(BadgeErrorCode.BADGE_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("Should list badges with pagination")
    void shouldListBadges() {
      Pageable pageable = PageRequest.of(0, 10);
      Page<Badge> page = new PageImpl<>(List.of(sampleBadge), pageable, 1);
      when(badgeRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

      Page<BadgeResponse> result = badgeService.listBadges("Toán", pageable);

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().get(0).name()).isEqualTo("Toán");
    }
  }

  @Nested
  @DisplayName("Update & Delete Badges Tests")
  class UpdateAndDeleteBadgesTests {

    @Test
    @DisplayName("Should update badge name")
    void shouldUpdateBadgeName() {
      BadgeRequest updateRequest = new BadgeRequest("Toán Cao Cấp");

      when(badgeRepository.findById(sampleBadge.getId())).thenReturn(Optional.of(sampleBadge));
      when(badgeRepository.existsByNameAndIdNot("Toán Cao Cấp", sampleBadge.getId()))
          .thenReturn(false);
      when(badgeRepository.save(any(Badge.class))).thenAnswer(inv -> inv.getArgument(0));

      BadgeResponse response = badgeService.updateBadge(sampleBadge.getId(), updateRequest);

      assertThat(response.name()).isEqualTo("Toán Cao Cấp");
    }

    @Test
    @DisplayName("Should throw BADGE_NAME_EXISTS when updating to existing name")
    void shouldThrowWhenUpdatingToExistingName() {
      BadgeRequest updateRequest = new BadgeRequest("Văn");

      when(badgeRepository.findById(sampleBadge.getId())).thenReturn(Optional.of(sampleBadge));
      when(badgeRepository.existsByNameAndIdNot("Văn", sampleBadge.getId())).thenReturn(true);

      assertThatThrownBy(() -> badgeService.updateBadge(sampleBadge.getId(), updateRequest))
          .isInstanceOf(BadgeException.class)
          .satisfies(
              ex ->
                  assertThat(((BadgeException) ex).getCode())
                      .isEqualTo(BadgeErrorCode.BADGE_NAME_EXISTS.code()));
    }

    @Test
    @DisplayName("Should delete badge")
    void shouldDeleteBadge() {
      when(badgeRepository.findById(sampleBadge.getId())).thenReturn(Optional.of(sampleBadge));

      badgeService.deleteBadge(sampleBadge.getId());

      verify(badgeRepository).delete(sampleBadge);
    }
  }
}
