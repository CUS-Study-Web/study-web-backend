package studyweb.cus.service.admin.impl;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import studyweb.cus.dto.request.website.FooterLinkItemRequest;
import studyweb.cus.dto.request.website.UpdateFooterRequest;
import studyweb.cus.dto.request.website.UpdateHomepageRequest;
import studyweb.cus.dto.response.website.FooterResponse;
import studyweb.cus.dto.response.website.HomepageResponse;
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

    if (hasFile(request.mainImage())) {
      content.setMainImageUrl(uploadFileSafely(request.mainImage()));
    }
    if (hasFile(request.student1Avatar())) {
      content.setStudent1Avatar(uploadFileSafely(request.student1Avatar()));
    }
    if (hasFile(request.student2Avatar())) {
      content.setStudent2Avatar(uploadFileSafely(request.student2Avatar()));
    }
    if (hasFile(request.student3Avatar())) {
      content.setStudent3Avatar(uploadFileSafely(request.student3Avatar()));
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
    if (request.ctaBtn1Url() != null) {
      content.setCtaBtn1Url(request.ctaBtn1Url());
    }
    if (request.ctaBtn2Name() != null) {
      content.setCtaBtn2Name(request.ctaBtn2Name());
    }
    if (request.ctaBtn2Url() != null) {
      content.setCtaBtn2Url(request.ctaBtn2Url());
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
      List<FooterLink> linksToSave = new ArrayList<>();
      for (int i = 0; i < request.links().size(); i++) {
        FooterLinkItemRequest item = request.links().get(i);
        if (item.id() != null) {
          FooterLink existingLink = footerLinkRepository.findById(item.id()).orElse(null);
          if (existingLink != null) {
            if (item.label() != null) {
              existingLink.setLabel(item.label());
            }
            if (item.url() != null) {
              existingLink.setUrl(item.url());
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
                .url(item.url())
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

  private String uploadFileSafely(MultipartFile file) {
    try {
      return fileService.uploadAvatarFile(file).fileUrl();
    } catch (FileException e) {
      throw e;
    } catch (Exception e) {
      log.error("Failed to upload image file: {}", file.getOriginalFilename(), e);
      throw new FileException(FileErrorCode.UPLOAD_FAILED);
    }
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
}
