package studyweb.cus.service.content.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studyweb.cus.dto.response.content.FooterContentResponse;
import studyweb.cus.dto.response.content.HomepageContentResponse;
import studyweb.cus.entity.content.FooterContent;
import studyweb.cus.entity.content.FooterLink;
import studyweb.cus.entity.content.HomepageContent;
import studyweb.cus.mapper.content.HomepageContentMapper;
import studyweb.cus.repository.content.FooterContentRepository;
import studyweb.cus.repository.content.FooterLinkRepository;
import studyweb.cus.repository.content.HomepageContentRepository;
import studyweb.cus.service.content.HomepageService;

@Service
@RequiredArgsConstructor
@Slf4j
public class HomepageServiceImpl implements HomepageService {

  private final HomepageContentRepository homepageContentRepository;
  private final FooterContentRepository footerContentRepository;
  private final FooterLinkRepository footerLinkRepository;
  private final HomepageContentMapper homepageContentMapper;

  @Override
  @Transactional(readOnly = true)
  public HomepageContentResponse getHomepageContent() {
    HomepageContent content =
        homepageContentRepository.findFirstByOrderByCreatedAtDesc().orElseThrow();
    return homepageContentMapper.toHomepageContentResponse(content);
  }

  @Override
  @Transactional(readOnly = true)
  public FooterContentResponse getFooterContent() {
    FooterContent content =
        footerContentRepository.findFirstByOrderByCreatedAtDesc().orElseThrow();
    List<FooterLink> links =
        footerLinkRepository.findByFooterIdOrderBySortOrderAsc(content.getId());
    return homepageContentMapper.toFooterContentResponse(content, links);
  }
}
