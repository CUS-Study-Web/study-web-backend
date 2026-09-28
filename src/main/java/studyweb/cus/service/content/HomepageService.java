package studyweb.cus.service.content;

import studyweb.cus.dto.response.content.FooterContentResponse;
import studyweb.cus.dto.response.content.HomepageContentResponse;

public interface HomepageService {

  HomepageContentResponse getHomepageContent();

  FooterContentResponse getFooterContent();
}
