package studyweb.cus.mapper.admin;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import studyweb.cus.dto.response.admin.FooterLinkResponse;
import studyweb.cus.dto.response.admin.FooterResponse;
import studyweb.cus.dto.response.admin.HomepageResponse;
import studyweb.cus.entity.content.FooterContent;
import studyweb.cus.entity.content.FooterLink;
import studyweb.cus.entity.content.HomepageContent;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WebsiteManagementMapper {

  @Mapping(target = "updatedByEmail", source = "content.updatedBy.gmail")
  HomepageResponse toHomepageResponse(HomepageContent content);

  FooterLinkResponse toFooterLinkResponse(FooterLink link);

  default FooterResponse toFooterResponse(FooterContent content, List<FooterLink> links) {
    if (content == null) {
      return null;
    }
    List<FooterLinkResponse> linkResponses =
        links == null ? List.of() : links.stream().map(this::toFooterLinkResponse).toList();

    return new FooterResponse(
        content.getId(),
        content.getCompanyName(),
        content.getAddress(),
        content.getFacebookUrl(),
        content.getInstagramUrl(),
        content.getYoutubeUrl(),
        content.getTiktokUrl(),
        content.getPhone(),
        content.getEmail(),
        content.getWebsite(),
        content.getWorkingHours(),
        content.getCopyrightText(),
        content.getPrivacyUrl(),
        content.getTermsUrl(),
        linkResponses,
        content.getUpdatedBy() != null ? content.getUpdatedBy().getGmail() : null,
        content.getUpdatedAt());
  }
}
