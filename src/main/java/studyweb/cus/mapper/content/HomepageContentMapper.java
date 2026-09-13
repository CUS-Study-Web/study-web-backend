package studyweb.cus.mapper.content;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import studyweb.cus.dto.response.content.FooterContentResponse;
import studyweb.cus.dto.response.content.FooterLinkItemResponse;
import studyweb.cus.dto.response.content.HomepageContentResponse;
import studyweb.cus.entity.content.FooterContent;
import studyweb.cus.entity.content.FooterLink;
import studyweb.cus.entity.content.HomepageContent;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HomepageContentMapper {

  HomepageContentResponse toHomepageContentResponse(HomepageContent content);

  FooterLinkItemResponse toFooterLinkItemResponse(FooterLink link);

  default FooterContentResponse toFooterContentResponse(FooterContent content, List<FooterLink> links) {
    List<FooterLinkItemResponse> linkResponses =
        links == null ? List.of() : links.stream().map(this::toFooterLinkItemResponse).toList();

    return new FooterContentResponse(
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
        linkResponses);
  }
}
