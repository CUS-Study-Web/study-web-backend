package studyweb.cus.service.admin;

import studyweb.cus.dto.request.website.UpdateFooterRequest;
import studyweb.cus.dto.request.website.UpdateHomepageRequest;
import studyweb.cus.dto.response.website.FooterResponse;
import studyweb.cus.dto.response.website.HomepageResponse;

public interface WebsiteManagementService {

  HomepageResponse getHomepageContent();

  HomepageResponse updateHomepageContent(UpdateHomepageRequest request, String updaterEmail);

  FooterResponse getFooterContent();

  FooterResponse updateFooterContent(UpdateFooterRequest request, String updaterEmail);
}
