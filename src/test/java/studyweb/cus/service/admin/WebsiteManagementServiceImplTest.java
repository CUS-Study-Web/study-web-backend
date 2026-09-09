package studyweb.cus.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import studyweb.cus.dto.request.admin.FooterLinkItemRequest;
import studyweb.cus.dto.request.admin.UpdateFooterRequest;
import studyweb.cus.dto.request.admin.UpdateHomepageRequest;
import studyweb.cus.dto.response.admin.FooterLinkResponse;
import studyweb.cus.dto.response.admin.FooterResponse;
import studyweb.cus.dto.response.admin.HomepageResponse;
import studyweb.cus.dto.response.document.UploadDocumentResult;
import studyweb.cus.entity.content.FooterContent;
import studyweb.cus.entity.content.FooterLink;
import studyweb.cus.entity.content.HomepageContent;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.CtaTarget;
import studyweb.cus.enums.FooterCategory;
import studyweb.cus.exception.file.FileErrorCode;
import studyweb.cus.exception.file.FileException;
import studyweb.cus.exception.user.UserErrorCode;
import studyweb.cus.exception.user.UserException;
import studyweb.cus.mapper.admin.WebsiteManagementMapper;
import studyweb.cus.repository.content.FooterContentRepository;
import studyweb.cus.repository.content.FooterLinkRepository;
import studyweb.cus.repository.content.HomepageContentRepository;
import studyweb.cus.repository.user.UserRepository;
import studyweb.cus.service.admin.impl.WebsiteManagementServiceImpl;
import studyweb.cus.service.file.FileService;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebsiteManagementServiceImpl Unit Tests")
class WebsiteManagementServiceImplTest {

  @Mock private HomepageContentRepository homepageContentRepository;
  @Mock private FooterContentRepository footerContentRepository;
  @Mock private FooterLinkRepository footerLinkRepository;
  @Mock private UserRepository userRepository;
  @Mock private FileService fileService;
  @Mock private WebsiteManagementMapper websiteManagementMapper;

  @InjectMocks private WebsiteManagementServiceImpl websiteManagementService;

  private static final String ADMIN_EMAIL = "admin@studyweb.edu";

  private User createAdminUser() {
    User admin = User.builder().gmail(ADMIN_EMAIL).build();
    admin.setId(UUID.randomUUID());
    return admin;
  }

  // ==========================================
  // Homepage Tests
  // ==========================================

  @Nested
  @DisplayName("getHomepageContent")
  class GetHomepageContentTests {

    @Test
    @DisplayName("Should return mapped response when HomepageContent exists")
    void whenContentExists_returnsMappedResponse() {
      HomepageContent entity = HomepageContent.builder().badgeTitle("Test Badge").build();
      HomepageResponse response =
          new HomepageResponse(
              UUID.randomUUID(),
              "Test Badge",
              "H1",
              "H2",
              "Desc",
              "CTA 1",
              CtaTarget.REGISTER,
              "CTA 2",
              CtaTarget.COURSES,
              "image.png",
              "100+",
              "Stat 1",
              "90%",
              "Stat 2",
              "av1.png",
              "av2.png",
              "av3.png",
              "Students",
              ADMIN_EMAIL,
              LocalDateTime.now());

      when(homepageContentRepository.findFirstByOrderByCreatedAtDesc())
          .thenReturn(Optional.of(entity));
      when(websiteManagementMapper.toHomepageResponse(entity)).thenReturn(response);

      HomepageResponse result = websiteManagementService.getHomepageContent();

      assertThat(result).isNotNull();
      assertThat(result.badgeTitle()).isEqualTo("Test Badge");
      verify(homepageContentRepository).findFirstByOrderByCreatedAtDesc();
      verify(websiteManagementMapper).toHomepageResponse(entity);
    }

    @Test
    @DisplayName("Should return null when no HomepageContent exists")
    void whenContentAbsent_returnsNull() {
      when(homepageContentRepository.findFirstByOrderByCreatedAtDesc())
          .thenReturn(Optional.empty());
      when(websiteManagementMapper.toHomepageResponse(null)).thenReturn(null);

      HomepageResponse result = websiteManagementService.getHomepageContent();

      assertThat(result).isNull();
      verify(homepageContentRepository).findFirstByOrderByCreatedAtDesc();
      verify(websiteManagementMapper).toHomepageResponse(null);
    }
  }

  @Nested
  @DisplayName("updateHomepageContent")
  class UpdateHomepageContentTests {

    @Test
    @DisplayName("Should update all text and enum fields when non-null in request")
    void updateHomepageContent_success_updatesAllNonNullFields() {
      User admin = createAdminUser();
      when(userRepository.findByGmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));

      HomepageContent existing =
          HomepageContent.builder()
              .badgeTitle("Old Badge")
              .headline1("Old H1")
              .headline2("Old H2")
              .description("Old Desc")
              .ctaBtn1Name("Old CTA1")
              .ctaBtn1Target(CtaTarget.LOGIN)
              .ctaBtn2Name("Old CTA2")
              .ctaBtn2Target(CtaTarget.ABOUT)
              .stat1Number("10")
              .stat1Desc("Old Stat1")
              .stat2Number("20")
              .stat2Desc("Old Stat2")
              .studentStatsDesc("Old Student Desc")
              .build();

      when(homepageContentRepository.findFirstByOrderByCreatedAtDesc())
          .thenReturn(Optional.of(existing));
      when(homepageContentRepository.save(any(HomepageContent.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      UpdateHomepageRequest request =
          new UpdateHomepageRequest(
              "New Badge",
              "New H1",
              "New H2",
              "New Desc",
              "New CTA1",
              CtaTarget.REGISTER,
              "New CTA2",
              CtaTarget.COURSES,
              "50",
              "New Stat1",
              "80%",
              "New Stat2",
              "New Student Desc",
              null,
              null,
              null,
              null);

      websiteManagementService.updateHomepageContent(request, ADMIN_EMAIL);

      ArgumentCaptor<HomepageContent> captor = ArgumentCaptor.forClass(HomepageContent.class);
      verify(homepageContentRepository).save(captor.capture());
      HomepageContent saved = captor.getValue();

      assertThat(saved.getBadgeTitle()).isEqualTo("New Badge");
      assertThat(saved.getHeadline1()).isEqualTo("New H1");
      assertThat(saved.getHeadline2()).isEqualTo("New H2");
      assertThat(saved.getDescription()).isEqualTo("New Desc");
      assertThat(saved.getCtaBtn1Name()).isEqualTo("New CTA1");
      assertThat(saved.getCtaBtn1Target()).isEqualTo(CtaTarget.REGISTER);
      assertThat(saved.getCtaBtn2Name()).isEqualTo("New CTA2");
      assertThat(saved.getCtaBtn2Target()).isEqualTo(CtaTarget.COURSES);
      assertThat(saved.getStat1Number()).isEqualTo("50");
      assertThat(saved.getStat1Desc()).isEqualTo("New Stat1");
      assertThat(saved.getStat2Number()).isEqualTo("80%");
      assertThat(saved.getStat2Desc()).isEqualTo("New Stat2");
      assertThat(saved.getStudentStatsDesc()).isEqualTo("New Student Desc");
      assertThat(saved.getUpdatedBy()).isEqualTo(admin);
    }

    @Test
    @DisplayName("Should create new entity when none exists")
    void updateHomepageContent_createsNewEntityWhenNoneExists() {
      User admin = createAdminUser();
      when(userRepository.findByGmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));
      when(homepageContentRepository.findFirstByOrderByCreatedAtDesc())
          .thenReturn(Optional.empty());
      when(homepageContentRepository.save(any(HomepageContent.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      UpdateHomepageRequest request =
          new UpdateHomepageRequest(
              "Brand New Badge",
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null);

      websiteManagementService.updateHomepageContent(request, ADMIN_EMAIL);

      ArgumentCaptor<HomepageContent> captor = ArgumentCaptor.forClass(HomepageContent.class);
      verify(homepageContentRepository).save(captor.capture());
      HomepageContent saved = captor.getValue();
      assertThat(saved.getBadgeTitle()).isEqualTo("Brand New Badge");
      assertThat(saved.getUpdatedBy()).isEqualTo(admin);
    }

    @Test
    @DisplayName("Should preserve existing fields when request fields are null")
    void updateHomepageContent_preservesExistingFieldsWhenRequestFieldsNull() {
      User admin = createAdminUser();
      when(userRepository.findByGmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));

      HomepageContent existing =
          HomepageContent.builder()
              .badgeTitle("Preserved Badge")
              .headline1("Preserved H1")
              .ctaBtn1Name("Preserved CTA")
              .ctaBtn1Target(CtaTarget.REGISTER)
              .build();

      when(homepageContentRepository.findFirstByOrderByCreatedAtDesc())
          .thenReturn(Optional.of(existing));
      when(homepageContentRepository.save(any(HomepageContent.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      UpdateHomepageRequest emptyRequest =
          new UpdateHomepageRequest(
              null, null, null, null, null, null, null, null, null, null, null, null, null, null,
              null, null, null);

      websiteManagementService.updateHomepageContent(emptyRequest, ADMIN_EMAIL);

      ArgumentCaptor<HomepageContent> captor = ArgumentCaptor.forClass(HomepageContent.class);
      verify(homepageContentRepository).save(captor.capture());
      HomepageContent saved = captor.getValue();

      assertThat(saved.getBadgeTitle()).isEqualTo("Preserved Badge");
      assertThat(saved.getHeadline1()).isEqualTo("Preserved H1");
      assertThat(saved.getCtaBtn1Name()).isEqualTo("Preserved CTA");
      assertThat(saved.getCtaBtn1Target()).isEqualTo(CtaTarget.REGISTER);
    }

    @Test
    @DisplayName("Should upload images when valid multipart files are provided")
    void updateHomepageContent_uploadsImagesWhenProvided() {
      User admin = createAdminUser();
      when(userRepository.findByGmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));

      HomepageContent existing = new HomepageContent();
      when(homepageContentRepository.findFirstByOrderByCreatedAtDesc())
          .thenReturn(Optional.of(existing));
      when(homepageContentRepository.save(any(HomepageContent.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      MockMultipartFile mainImg =
          new MockMultipartFile("mainImage", "cover.png", "image/png", new byte[] {1, 2, 3});
      MockMultipartFile av1 =
          new MockMultipartFile("student1Avatar", "av1.png", "image/png", new byte[] {4, 5});
      MockMultipartFile av2 =
          new MockMultipartFile("student2Avatar", "av2.png", "image/png", new byte[] {6, 7});
      MockMultipartFile av3 =
          new MockMultipartFile("student3Avatar", "av3.png", "image/png", new byte[] {8, 9});

      when(fileService.uploadAvatarFile(mainImg))
          .thenReturn(new UploadDocumentResult(3, "key-main", "https://cdn.example.com/cover.png"));
      when(fileService.uploadAvatarFile(av1))
          .thenReturn(new UploadDocumentResult(2, "key-av1", "https://cdn.example.com/av1.png"));
      when(fileService.uploadAvatarFile(av2))
          .thenReturn(new UploadDocumentResult(2, "key-av2", "https://cdn.example.com/av2.png"));
      when(fileService.uploadAvatarFile(av3))
          .thenReturn(new UploadDocumentResult(2, "key-av3", "https://cdn.example.com/av3.png"));

      UpdateHomepageRequest request =
          new UpdateHomepageRequest(
              null, null, null, null, null, null, null, null, null, null, null, null, null, mainImg,
              av1, av2, av3);

      websiteManagementService.updateHomepageContent(request, ADMIN_EMAIL);

      ArgumentCaptor<HomepageContent> captor = ArgumentCaptor.forClass(HomepageContent.class);
      verify(homepageContentRepository).save(captor.capture());
      HomepageContent saved = captor.getValue();

      assertThat(saved.getMainImageUrl()).isEqualTo("https://cdn.example.com/cover.png");
      assertThat(saved.getStudent1Avatar()).isEqualTo("https://cdn.example.com/av1.png");
      assertThat(saved.getStudent2Avatar()).isEqualTo("https://cdn.example.com/av2.png");
      assertThat(saved.getStudent3Avatar()).isEqualTo("https://cdn.example.com/av3.png");
    }

    @Test
    @DisplayName("Should not upload when multipart files are empty")
    void updateHomepageContent_doesNotUploadEmptyFiles() {
      User admin = createAdminUser();
      when(userRepository.findByGmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));

      HomepageContent existing = new HomepageContent();
      when(homepageContentRepository.findFirstByOrderByCreatedAtDesc())
          .thenReturn(Optional.of(existing));
      when(homepageContentRepository.save(any(HomepageContent.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      MockMultipartFile emptyFile =
          new MockMultipartFile("mainImage", "empty.png", "image/png", new byte[0]);

      UpdateHomepageRequest request =
          new UpdateHomepageRequest(
              null, null, null, null, null, null, null, null, null, null, null, null, null,
              emptyFile, null, null, null);

      websiteManagementService.updateHomepageContent(request, ADMIN_EMAIL);

      verify(fileService, never()).uploadAvatarFile(any());
    }

    @Test
    @DisplayName("Should rethrow FileException when fileService throws FileException")
    void updateHomepageContent_rethrowsFileException() {
      User admin = createAdminUser();
      when(userRepository.findByGmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));
      when(homepageContentRepository.findFirstByOrderByCreatedAtDesc())
          .thenReturn(Optional.of(new HomepageContent()));

      MockMultipartFile file =
          new MockMultipartFile("mainImage", "cover.png", "image/png", new byte[] {1, 2});
      when(fileService.uploadAvatarFile(file))
          .thenThrow(new FileException(FileErrorCode.FILE_EMPTY));

      UpdateHomepageRequest request =
          new UpdateHomepageRequest(
              null, null, null, null, null, null, null, null, null, null, null, null, null, file,
              null, null, null);

      assertThatThrownBy(() -> websiteManagementService.updateHomepageContent(request, ADMIN_EMAIL))
          .isInstanceOf(FileException.class)
          .satisfies(
              e ->
                  assertThat(((FileException) e).getCode())
                      .isEqualTo(FileErrorCode.FILE_EMPTY.code()));
    }

    @Test
    @DisplayName("Should wrap generic Exception in FileException(UPLOAD_FAILED)")
    void updateHomepageContent_wrapsGenericExceptionInFileException() {
      User admin = createAdminUser();
      when(userRepository.findByGmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));
      when(homepageContentRepository.findFirstByOrderByCreatedAtDesc())
          .thenReturn(Optional.of(new HomepageContent()));

      MockMultipartFile file =
          new MockMultipartFile("mainImage", "cover.png", "image/png", new byte[] {1, 2});
      when(fileService.uploadAvatarFile(file))
          .thenThrow(new RuntimeException("S3 connection timeout"));

      UpdateHomepageRequest request =
          new UpdateHomepageRequest(
              null, null, null, null, null, null, null, null, null, null, null, null, null, file,
              null, null, null);

      assertThatThrownBy(() -> websiteManagementService.updateHomepageContent(request, ADMIN_EMAIL))
          .isInstanceOf(FileException.class)
          .satisfies(
              e ->
                  assertThat(((FileException) e).getCode())
                      .isEqualTo(FileErrorCode.UPLOAD_FAILED.code()));
    }

    @Test
    @DisplayName("Should throw UserException when updaterEmail is null")
    void updateHomepageContent_whenEmailNull_throwsUserNotFound() {
      UpdateHomepageRequest request =
          new UpdateHomepageRequest(
              null, null, null, null, null, null, null, null, null, null, null, null, null, null,
              null, null, null);

      assertThatThrownBy(() -> websiteManagementService.updateHomepageContent(request, null))
          .isInstanceOf(UserException.class)
          .satisfies(
              e ->
                  assertThat(((UserException) e).getCode())
                      .isEqualTo(UserErrorCode.USER_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("Should throw UserException when user is not found in repository")
    void updateHomepageContent_whenUserNotFound_throwsUserNotFound() {
      when(userRepository.findByGmail("nonexistent@studyweb.edu")).thenReturn(Optional.empty());

      UpdateHomepageRequest request =
          new UpdateHomepageRequest(
              null, null, null, null, null, null, null, null, null, null, null, null, null, null,
              null, null, null);

      assertThatThrownBy(
              () ->
                  websiteManagementService.updateHomepageContent(
                      request, "nonexistent@studyweb.edu"))
          .isInstanceOf(UserException.class)
          .satisfies(
              e ->
                  assertThat(((UserException) e).getCode())
                      .isEqualTo(UserErrorCode.USER_NOT_FOUND.code()));
    }
  }

  // ==========================================
  // Footer Tests
  // ==========================================

  @Nested
  @DisplayName("getFooterContent")
  class GetFooterContentTests {

    @Test
    @DisplayName("Should return mapped response with links when FooterContent exists")
    void whenFooterExists_returnsMappedResponseWithLinks() {
      UUID footerId = UUID.randomUUID();
      FooterContent footer = FooterContent.builder().companyName("CUS Edu").build();
      footer.setId(footerId);

      FooterLink link1 =
          FooterLink.builder()
              .footer(footer)
              .category(FooterCategory.PROGRAM)
              .label("V-ACT")
              .sortOrder(0)
              .build();
      List<FooterLink> links = List.of(link1);

      FooterResponse response =
          new FooterResponse(
              footerId,
              "CUS Edu",
              "123 Str",
              "fb.com",
              "ig.com",
              "yt.com",
              "tt.com",
              "0123456789",
              "email@cus.vn",
              "cus.vn",
              "8:00-17:00",
              "Copyright",
              "/privacy",
              "/terms",
              List.of(
                  new FooterLinkResponse(
                      UUID.randomUUID(), "V-ACT", "/v-act", 0, FooterCategory.PROGRAM)),
              ADMIN_EMAIL,
              LocalDateTime.now());

      when(footerContentRepository.findFirstByOrderByCreatedAtDesc())
          .thenReturn(Optional.of(footer));
      when(footerLinkRepository.findByFooterIdOrderBySortOrderAsc(footerId)).thenReturn(links);
      when(websiteManagementMapper.toFooterResponse(footer, links)).thenReturn(response);

      FooterResponse result = websiteManagementService.getFooterContent();

      assertThat(result).isNotNull();
      assertThat(result.companyName()).isEqualTo("CUS Edu");
      verify(footerContentRepository).findFirstByOrderByCreatedAtDesc();
      verify(footerLinkRepository).findByFooterIdOrderBySortOrderAsc(footerId);
    }

    @Test
    @DisplayName("Should return null when no FooterContent exists")
    void whenFooterAbsent_returnsNull() {
      when(footerContentRepository.findFirstByOrderByCreatedAtDesc()).thenReturn(Optional.empty());

      FooterResponse result = websiteManagementService.getFooterContent();

      assertThat(result).isNull();
      verify(footerContentRepository).findFirstByOrderByCreatedAtDesc();
      verify(footerLinkRepository, never()).findByFooterIdOrderBySortOrderAsc(any());
    }
  }

  @Nested
  @DisplayName("updateFooterContent")
  class UpdateFooterContentTests {

    @Test
    @DisplayName("Should update company fields and preserve existing when null")
    void updateFooterContent_success_updatesCompanyFields() {
      User admin = createAdminUser();
      when(userRepository.findByGmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));

      UUID footerId = UUID.randomUUID();
      FooterContent existing =
          FooterContent.builder()
              .companyName("Old Co")
              .address("Old Addr")
              .phone("Old Phone")
              .email("Old Email")
              .build();
      existing.setId(footerId);

      when(footerContentRepository.findFirstByOrderByCreatedAtDesc())
          .thenReturn(Optional.of(existing));
      when(footerContentRepository.save(any(FooterContent.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));
      when(footerLinkRepository.findByFooterIdOrderBySortOrderAsc(footerId)).thenReturn(List.of());

      UpdateFooterRequest request =
          new UpdateFooterRequest(
              "New Co",
              "New Addr",
              "https://fb.com/new",
              "https://ig.com/new",
              "https://yt.com/new",
              "https://tt.com/new",
              "0987654321",
              "new@cus.vn",
              "https://new.cus.vn",
              "T2-T6: 8:00 - 17:00",
              "© 2026 CUS",
              "/new-privacy",
              "/new-terms",
              null);

      websiteManagementService.updateFooterContent(request, ADMIN_EMAIL);

      ArgumentCaptor<FooterContent> captor = ArgumentCaptor.forClass(FooterContent.class);
      verify(footerContentRepository).save(captor.capture());
      FooterContent saved = captor.getValue();

      assertThat(saved.getCompanyName()).isEqualTo("New Co");
      assertThat(saved.getAddress()).isEqualTo("New Addr");
      assertThat(saved.getFacebookUrl()).isEqualTo("https://fb.com/new");
      assertThat(saved.getInstagramUrl()).isEqualTo("https://ig.com/new");
      assertThat(saved.getYoutubeUrl()).isEqualTo("https://yt.com/new");
      assertThat(saved.getTiktokUrl()).isEqualTo("https://tt.com/new");
      assertThat(saved.getPhone()).isEqualTo("0987654321");
      assertThat(saved.getEmail()).isEqualTo("new@cus.vn");
      assertThat(saved.getWebsite()).isEqualTo("https://new.cus.vn");
      assertThat(saved.getWorkingHours()).isEqualTo("T2-T6: 8:00 - 17:00");
      assertThat(saved.getCopyrightText()).isEqualTo("© 2026 CUS");
      assertThat(saved.getPrivacyUrl()).isEqualTo("/new-privacy");
      assertThat(saved.getTermsUrl()).isEqualTo("/new-terms");
      assertThat(saved.getUpdatedBy()).isEqualTo(admin);
    }

    @Test
    @DisplayName("Should create new FooterContent entity when none exists")
    void updateFooterContent_createsNewWhenNoneExists() {
      User admin = createAdminUser();
      when(userRepository.findByGmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));
      when(footerContentRepository.findFirstByOrderByCreatedAtDesc()).thenReturn(Optional.empty());

      FooterContent savedWithId = new FooterContent();
      savedWithId.setId(UUID.randomUUID());
      when(footerContentRepository.save(any(FooterContent.class))).thenReturn(savedWithId);
      when(footerLinkRepository.findByFooterIdOrderBySortOrderAsc(savedWithId.getId()))
          .thenReturn(List.of());

      UpdateFooterRequest request =
          new UpdateFooterRequest(
              "Fresh Co",
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null);

      websiteManagementService.updateFooterContent(request, ADMIN_EMAIL);

      verify(footerContentRepository).save(any(FooterContent.class));
    }

    @Test
    @DisplayName("Should update existing link when link ID matches")
    void updateFooterContent_updatesExistingLink() {
      User admin = createAdminUser();
      when(userRepository.findByGmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));

      UUID footerId = UUID.randomUUID();
      FooterContent footer = FooterContent.builder().companyName("CUS").build();
      footer.setId(footerId);
      when(footerContentRepository.findFirstByOrderByCreatedAtDesc())
          .thenReturn(Optional.of(footer));
      when(footerContentRepository.save(any(FooterContent.class))).thenReturn(footer);

      UUID linkId = UUID.randomUUID();
      FooterLink existingLink =
          FooterLink.builder()
              .footer(footer)
              .category(FooterCategory.PROGRAM)
              .label("Old Link")
              .url("/old")
              .sortOrder(0)
              .build();
      existingLink.setId(linkId);

      when(footerLinkRepository.findById(linkId)).thenReturn(Optional.of(existingLink));
      when(footerLinkRepository.findByFooterIdOrderBySortOrderAsc(footerId))
          .thenReturn(List.of(existingLink));

      UpdateFooterRequest request =
          new UpdateFooterRequest(
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              List.of(
                  new FooterLinkItemRequest(
                      linkId, "Updated Link", "/updated", 5, FooterCategory.ABOUT)));

      websiteManagementService.updateFooterContent(request, ADMIN_EMAIL);

      assertThat(existingLink.getLabel()).isEqualTo("Updated Link");
      assertThat(existingLink.getUrl()).isEqualTo("/updated");
      assertThat(existingLink.getSortOrder()).isEqualTo(5);
      assertThat(existingLink.getCategory()).isEqualTo(FooterCategory.ABOUT);
      verify(footerLinkRepository).saveAll(any());
    }

    @Test
    @DisplayName("Should create new link when link ID is null")
    void updateFooterContent_createsNewLinkWhenIdNull() {
      User admin = createAdminUser();
      when(userRepository.findByGmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));

      UUID footerId = UUID.randomUUID();
      FooterContent footer = FooterContent.builder().companyName("CUS").build();
      footer.setId(footerId);
      when(footerContentRepository.findFirstByOrderByCreatedAtDesc())
          .thenReturn(Optional.of(footer));
      when(footerContentRepository.save(any(FooterContent.class))).thenReturn(footer);
      when(footerLinkRepository.findByFooterIdOrderBySortOrderAsc(footerId)).thenReturn(List.of());

      UpdateFooterRequest request =
          new UpdateFooterRequest(
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              List.of(
                  new FooterLinkItemRequest(
                      null, "New Item", "/new-item", 1, FooterCategory.PROGRAM)));

      websiteManagementService.updateFooterContent(request, ADMIN_EMAIL);

      @SuppressWarnings("unchecked")
      ArgumentCaptor<List<FooterLink>> captor = ArgumentCaptor.forClass(List.class);
      verify(footerLinkRepository).saveAll(captor.capture());
      List<FooterLink> savedLinks = captor.getValue();

      assertThat(savedLinks).hasSize(1);
      FooterLink newLink = savedLinks.get(0);
      assertThat(newLink.getLabel()).isEqualTo("New Item");
      assertThat(newLink.getUrl()).isEqualTo("/new-item");
      assertThat(newLink.getSortOrder()).isEqualTo(1);
      assertThat(newLink.getCategory()).isEqualTo(FooterCategory.PROGRAM);
      assertThat(newLink.getFooter()).isEqualTo(footer);
    }

    @Test
    @DisplayName("Should default category to PROGRAM and sortOrder to index when omitted")
    void updateFooterContent_defaultsCategoryAndSortOrder() {
      User admin = createAdminUser();
      when(userRepository.findByGmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));

      UUID footerId = UUID.randomUUID();
      FooterContent footer = FooterContent.builder().companyName("CUS").build();
      footer.setId(footerId);
      when(footerContentRepository.findFirstByOrderByCreatedAtDesc())
          .thenReturn(Optional.of(footer));
      when(footerContentRepository.save(any(FooterContent.class))).thenReturn(footer);
      when(footerLinkRepository.findByFooterIdOrderBySortOrderAsc(footerId)).thenReturn(List.of());

      UpdateFooterRequest request =
          new UpdateFooterRequest(
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              List.of(new FooterLinkItemRequest(null, "Default Link", "/default", null, null)));

      websiteManagementService.updateFooterContent(request, ADMIN_EMAIL);

      @SuppressWarnings("unchecked")
      ArgumentCaptor<List<FooterLink>> captor = ArgumentCaptor.forClass(List.class);
      verify(footerLinkRepository).saveAll(captor.capture());
      List<FooterLink> savedLinks = captor.getValue();

      assertThat(savedLinks).hasSize(1);
      FooterLink newLink = savedLinks.get(0);
      assertThat(newLink.getCategory()).isEqualTo(FooterCategory.PROGRAM);
      assertThat(newLink.getSortOrder()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should throw UserException when updater email is null")
    void updateFooterContent_whenEmailNull_throwsUserNotFound() {
      UpdateFooterRequest request =
          new UpdateFooterRequest(
              null, null, null, null, null, null, null, null, null, null, null, null, null, null);

      assertThatThrownBy(() -> websiteManagementService.updateFooterContent(request, null))
          .isInstanceOf(UserException.class)
          .satisfies(
              e ->
                  assertThat(((UserException) e).getCode())
                      .isEqualTo(UserErrorCode.USER_NOT_FOUND.code()));
    }
  }
}
