package studyweb.cus.service.content;

import studyweb.cus.dto.request.content.RequestVipFormContentRequest;
import studyweb.cus.dto.response.content.RequestVipFormContentResponse;

public interface RequestVipFormContentService {

  RequestVipFormContentResponse getContent();

  RequestVipFormContentResponse updateContent(
      RequestVipFormContentRequest request, String updaterEmail);
}
