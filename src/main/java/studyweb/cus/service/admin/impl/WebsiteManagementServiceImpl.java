package studyweb.cus.service.admin.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import studyweb.cus.constant.admin.WebsiteManagementConstants;
import studyweb.cus.dto.request.admin.FooterLinkItemRequest;
import studyweb.cus.dto.request.admin.UpdateFooterRequest;
import studyweb.cus.dto.request.admin.UpdateHomepageRequest;
import studyweb.cus.dto.response.admin.FooterResponse;
import studyweb.cus.dto.response.admin.HomepageResponse;
import studyweb.cus.dto.response.document.UploadDocumentResult;
import studyweb.cus.entity.content.FooterContent;
import studyweb.cus.entity.content.FooterLink;
import studyweb.cus.entity.content.HomepageContent;
import studyweb.cus.entity.user.User;
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
import studyweb.cus.service.admin.WebsiteManagementService;
import studyweb.cus.service.file.FileService;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebsiteManagementServiceImpl implements WebsiteManagementService {

  private final HomepageContentRepository homepageContentRepository;
  private final FooterContentRepository footerContentRepository;
  private final FooterLinkRepository footerLinkRepository;
  private final UserRepository userRepository;
  private final FileService fileService;
  private final WebsiteManagementMapper websiteManagementMapper;

  @Override
  @Transactional(readOnly = true)
  public HomepageResponse getHomepageContent() {
    HomepageContent content =
        homepageContentRepository.findFirstByOrderByCreatedAtDesc().orElse(null);
    return websiteManagementMapper.toHomepageResponse(content);
  }

  @Override
  public HomepageResponse updateHomepageContent(
      UpdateHomepageRequest request, String updaterEmail) {
    User updater = requireUser(updaterEmail);

    HomepageContent content =
        homepageContentRepository.findFirstByOrderByCreatedAtDesc().orElseGet(HomepageContent::new);

    String oldMainImageUrl = content.getMainImageUrl();
    String oldStudent1Avatar = content.getStudent1Avatar();
    String oldStudent2Avatar = content.getStudent2Avatar();
    String oldStudent3Avatar = content.getStudent3Avatar();

    List<String> newUploadedFileKeys = new ArrayList<>();

    try {
      if (hasFile(request.mainImage())) {
        UploadDocumentResult result = fileService.uploadAvatarFile(request.mainImage());
        newUploadedFileKeys.add(result.fileKey());
        content.setMainImageUrl(result.fileUrl());
      }
      if (hasFile(request.student1Avatar())) {
        UploadDocumentResult result = fileService.uploadAvatarFile(request.student1Avatar());
        newUploadedFileKeys.add(result.fileKey());
        content.setStudent1Avatar(result.fileUrl());
      }
      if (hasFile(request.student2Avatar())) {
        UploadDocumentResult result = fileService.uploadAvatarFile(request.student2Avatar());
        newUploadedFileKeys.add(result.fileKey());
        content.setStudent2Avatar(result.fileUrl());
      }
      if (hasFile(request.student3Avatar())) {
        UploadDocumentResult result = fileService.uploadAvatarFile(request.student3Avatar());
        newUploadedFileKeys.add(result.fileKey());
        content.setStudent3Avatar(result.fileUrl());
      }

      if (request.badgeTitle() != null) {
        content.setBadgeTitle(request.badgeTitle());
      }
      if (request.headline1() != null) {
        content.setHeadline1(request.headline1());
      }
      if (request.headline2() != null) {
        content.setHeadline2(request.headline2());
      }
      if (request.description() != null) {
        content.setDescription(request.description());
      }
      if (request.ctaBtn1Name() != null) {
        content.setCtaBtn1Name(request.ctaBtn1Name());
      }
      if (request.ctaBtn1Target() != null) {
        content.setCtaBtn1Target(request.ctaBtn1Target());
      }
      if (request.ctaBtn2Name() != null) {
        content.setCtaBtn2Name(request.ctaBtn2Name());
      }
      if (request.ctaBtn2Target() != null) {
        content.setCtaBtn2Target(request.ctaBtn2Target());
      }
      if (request.stat1Number() != null) {
        content.setStat1Number(request.stat1Number());
      }
      if (request.stat1Desc() != null) {
        content.setStat1Desc(request.stat1Desc());
      }
      if (request.stat2Number() != null) {
        content.setStat2Number(request.stat2Number());
      }
      if (request.stat2Desc() != null) {
        content.setStat2Desc(request.stat2Desc());
      }
      if (request.studentStatsDesc() != null) {
        content.setStudentStatsDesc(request.studentStatsDesc());
      }
      if (updater != null) {
        content.setUpdatedBy(updater);
      }

      content = homepageContentRepository.save(content);
    } catch (Exception ex) {
      cleanupUploadedFiles(newUploadedFileKeys);
      throw ex;
    }

    if (hasFile(request.mainImage()) && oldMainImageUrl != null && !oldMainImageUrl.isBlank()) {
      fileService.deleteFile(oldMainImageUrl);
    }
    if (hasFile(request.student1Avatar()) && oldStudent1Avatar != null && !oldStudent1Avatar.isBlank()) {
      fileService.deleteFile(oldStudent1Avatar);
    }
    if (hasFile(request.student2Avatar()) && oldStudent2Avatar != null && !oldStudent2Avatar.isBlank()) {
      fileService.deleteFile(oldStudent2Avatar);
    }
    if (hasFile(request.student3Avatar()) && oldStudent3Avatar != null && !oldStudent3Avatar.isBlank()) {
      fileService.deleteFile(oldStudent3Avatar);
    }

    return websiteManagementMapper.toHomepageResponse(content);
  }

  @Override
  @Transactional(readOnly = true)
  public FooterResponse getFooterContent() {
    FooterContent content = footerContentRepository.findFirstByOrderByCreatedAtDesc().orElse(null);
    if (content == null) {
      return null;
    }
    List<FooterLink> links =
        footerLinkRepository.findByFooterIdOrderBySortOrderAsc(content.getId());
    return websiteManagementMapper.toFooterResponse(content, links);
  }

  @Override
  @Transactional
  public FooterResponse updateFooterContent(UpdateFooterRequest request, String updaterEmail) {
    User updater = requireUser(updaterEmail);
    FooterContent content =
        footerContentRepository.findFirstByOrderByCreatedAtDesc().orElseGet(FooterContent::new);

    if (request.companyName() != null) {
      content.setCompanyName(request.companyName());
    }
    if (request.address() != null) {
      content.setAddress(request.address());
    }
    if (request.facebookUrl() != null) {
      content.setFacebookUrl(request.facebookUrl());
    }
    if (request.instagramUrl() != null) {
      content.setInstagramUrl(request.instagramUrl());
    }
    if (request.youtubeUrl() != null) {
      content.setYoutubeUrl(request.youtubeUrl());
    }
    if (request.tiktokUrl() != null) {
      content.setTiktokUrl(request.tiktokUrl());
    }
    if (request.phone() != null) {
      content.setPhone(request.phone());
    }
    if (request.email() != null) {
      content.setEmail(request.email());
    }
    if (request.website() != null) {
      content.setWebsite(request.website());
    }
    if (request.workingHours() != null) {
      content.setWorkingHours(request.workingHours());
    }
    if (request.copyrightText() != null) {
      content.setCopyrightText(request.copyrightText());
    }
    if (request.privacyUrl() != null) {
      content.setPrivacyUrl(request.privacyUrl());
    }
    if (request.termsUrl() != null) {
      content.setTermsUrl(request.termsUrl());
    }
    if (updater != null) {
      content.setUpdatedBy(updater);
    }

    content = footerContentRepository.save(content);

    if (request.links() != null) {
      long programCount =
          request.links().stream()
              .filter(l -> l.category() == null || l.category() == FooterCategory.PROGRAM)
              .count();
      long aboutCount =
          request.links().stream().filter(l -> l.category() == FooterCategory.ABOUT).count();
      if (programCount > WebsiteManagementConstants.MAX_FOOTER_LINKS_PER_CATEGORY
          || aboutCount > WebsiteManagementConstants.MAX_FOOTER_LINKS_PER_CATEGORY) {
        throw new IllegalArgumentException(
            "Mỗi danh mục chỉ được tối đa "
                + WebsiteManagementConstants.MAX_FOOTER_LINKS_PER_CATEGORY
                + " liên kết.");
      }

      List<FooterLink> existingDbLinks =
          content.getId() != null
              ? footerLinkRepository.findByFooterIdOrderBySortOrderAsc(content.getId())
              : List.of();

      Set<UUID> incomingIds =
          request.links().stream()
              .map(FooterLinkItemRequest::id)
              .filter(Objects::nonNull)
              .collect(Collectors.toSet());

      List<FooterLink> linksToDelete =
          existingDbLinks.stream()
              .filter(link -> !incomingIds.contains(link.getId()))
              .toList();

      if (!linksToDelete.isEmpty()) {
        footerLinkRepository.deleteAll(linksToDelete);
      }

      List<FooterLink> linksToSave = new ArrayList<>();
      for (int i = 0; i < request.links().size(); i++) {
        FooterLinkItemRequest item = request.links().get(i);
        String normalizedUrl = normalizeUrl(item.url());
        if (item.id() != null) {
          FooterLink existingLink =
              existingDbLinks.stream()
                  .filter(l -> l.getId().equals(item.id()))
                  .findFirst()
                  .orElse(null);
          if (existingLink != null) {
            if (item.label() != null) {
              existingLink.setLabel(item.label());
            }
            if (normalizedUrl != null) {
              existingLink.setUrl(normalizedUrl);
            }
            if (item.sortOrder() != null) {
              existingLink.setSortOrder(item.sortOrder());
            }
            if (item.category() != null) {
              existingLink.setCategory(item.category());
            }
            linksToSave.add(existingLink);
            continue;
          }
        }
        linksToSave.add(
            FooterLink.builder()
                .footer(content)
                .category(item.category() != null ? item.category() : FooterCategory.PROGRAM)
                .label(item.label())
                .url(normalizedUrl)
                .sortOrder(item.sortOrder() != null ? item.sortOrder() : i)
                .build());
      }
      if (!linksToSave.isEmpty()) {
        footerLinkRepository.saveAll(linksToSave);
      }
    }

    List<FooterLink> currentLinks =
        footerLinkRepository.findByFooterIdOrderBySortOrderAsc(content.getId());
    return websiteManagementMapper.toFooterResponse(content, currentLinks);
  }

  private void cleanupUploadedFiles(List<String> fileKeys) {
    if (fileKeys == null || fileKeys.isEmpty()) {
      return;
    }
    log.warn(
        "Operation failed for updateHomepageContent. Cleaning up uploaded files from S3: {}",
        fileKeys);
    fileKeys.forEach(fileService::deleteFile);
  }

  private boolean hasFile(MultipartFile file) {
    return file != null && !file.isEmpty();
  }

  private User requireUser(String email) {
    if (email == null) {
      throw new UserException(UserErrorCode.USER_NOT_FOUND);
    }
    return userRepository
        .findByGmail(email)
        .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
  }

  private String normalizeUrl(String url) {
    if (url == null) {
      return null;
    }
    String trimmed = url.trim();
    if (trimmed.isEmpty()) {
      return trimmed;
    }
    for (String prefix : WebsiteManagementConstants.EXTERNAL_URL_PREFIXES) {
      if (trimmed.startsWith(prefix)) {
        return trimmed;
      }
    }
    if (trimmed.startsWith(WebsiteManagementConstants.PREFIX_WWW)) {
      return WebsiteManagementConstants.PREFIX_HTTPS + trimmed;
    }
    return trimmed.startsWith("/") ? trimmed : "/" + trimmed;
  }
}
