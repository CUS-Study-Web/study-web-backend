package studyweb.cus.service.admin;

import studyweb.cus.dto.request.admin.UpdateFooterRequest;
import studyweb.cus.dto.request.admin.UpdateHomepageRequest;
import studyweb.cus.dto.response.admin.FooterResponse;
import studyweb.cus.dto.response.admin.HomepageResponse;

public interface WebsiteManagementService {

  HomepageResponse getHomepageContent();

  HomepageResponse updateHomepageContent(UpdateHomepageRequest request, String updaterEmail);

  FooterResponse getFooterContent();

  FooterResponse updateFooterContent(UpdateFooterRequest request, String updaterEmail);
}
