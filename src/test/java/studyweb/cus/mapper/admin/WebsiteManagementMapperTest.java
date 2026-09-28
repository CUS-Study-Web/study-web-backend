package studyweb.cus.mapper.admin;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import studyweb.cus.dto.response.admin.FooterLinkResponse;
import studyweb.cus.dto.response.admin.FooterResponse;
import studyweb.cus.dto.response.admin.HomepageResponse;
import studyweb.cus.entity.content.FooterContent;
import studyweb.cus.entity.content.FooterLink;
import studyweb.cus.entity.content.HomepageContent;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.CtaTarget;
import studyweb.cus.enums.FooterCategory;

@DisplayName("WebsiteManagementMapper Unit Tests")
class WebsiteManagementMapperTest {

  private WebsiteManagementMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(WebsiteManagementMapper.class);
  }

  @Test
  @DisplayName("Should map HomepageContent to HomepageResponse with all fields and targets")
  void toHomepageResponse_mapsAllFieldsCorrectly() {
    UUID contentId = UUID.randomUUID();
    User updater = User.builder().gmail("admin@studyweb.edu").build();
    LocalDateTime now = LocalDateTime.now();

    HomepageContent entity =
        HomepageContent.builder()
            .badgeTitle("Badge 1")
            .headline1("Headline 1")
            .headline2("Headline 2")
            .description("Description")
            .ctaBtn1Name("Start")
            .ctaBtn1Target(CtaTarget.REGISTER)
            .ctaBtn2Name("Courses")
            .ctaBtn2Target(CtaTarget.COURSES)
            .mainImageUrl("https://cdn.example.com/main.png")
            .stat1Number("3000+")
            .stat1Desc("Students")
            .stat2Number("99%")
            .stat2Desc("Pass rate")
            .student1Avatar("https://cdn.example.com/s1.png")
            .student2Avatar("https://cdn.example.com/s2.png")
            .student3Avatar("https://cdn.example.com/s3.png")
            .studentStatsDesc("Top students")
            .updatedBy(updater)
            .build();
    entity.setId(contentId);
    entity.setUpdatedAt(now);

    HomepageResponse response = mapper.toHomepageResponse(entity);

    assertThat(response).isNotNull();
    assertThat(response.id()).isEqualTo(contentId);
    assertThat(response.badgeTitle()).isEqualTo("Badge 1");
    assertThat(response.headline1()).isEqualTo("Headline 1");
    assertThat(response.headline2()).isEqualTo("Headline 2");
    assertThat(response.description()).isEqualTo("Description");
    assertThat(response.ctaBtn1Name()).isEqualTo("Start");
    assertThat(response.ctaBtn1Target()).isEqualTo(CtaTarget.REGISTER);
    assertThat(response.ctaBtn2Name()).isEqualTo("Courses");
    assertThat(response.ctaBtn2Target()).isEqualTo(CtaTarget.COURSES);
    assertThat(response.mainImageUrl()).isEqualTo("https://cdn.example.com/main.png");
    assertThat(response.stat1Number()).isEqualTo("3000+");
    assertThat(response.stat1Desc()).isEqualTo("Students");
    assertThat(response.stat2Number()).isEqualTo("99%");
    assertThat(response.stat2Desc()).isEqualTo("Pass rate");
    assertThat(response.student1Avatar()).isEqualTo("https://cdn.example.com/s1.png");
    assertThat(response.student2Avatar()).isEqualTo("https://cdn.example.com/s2.png");
    assertThat(response.student3Avatar()).isEqualTo("https://cdn.example.com/s3.png");
    assertThat(response.studentStatsDesc()).isEqualTo("Top students");
    assertThat(response.updatedByEmail()).isEqualTo("admin@studyweb.edu");
    assertThat(response.updatedAt()).isEqualTo(now);
  }

  @Test
  @DisplayName("Should return null when HomepageContent is null")
  void toHomepageResponse_whenNull_returnsNull() {
    assertThat(mapper.toHomepageResponse(null)).isNull();
  }

  @Test
  @DisplayName("Should map FooterLink to FooterLinkResponse correctly")
  void toFooterLinkResponse_mapsAllFieldsCorrectly() {
    UUID linkId = UUID.randomUUID();
    FooterLink link =
        FooterLink.builder()
            .category(FooterCategory.PROGRAM)
            .label("IELTS Course")
            .url("/courses/ielts")
            .sortOrder(2)
            .build();
    link.setId(linkId);

    FooterLinkResponse response = mapper.toFooterLinkResponse(link);

    assertThat(response).isNotNull();
    assertThat(response.id()).isEqualTo(linkId);
    assertThat(response.category()).isEqualTo(FooterCategory.PROGRAM);
    assertThat(response.label()).isEqualTo("IELTS Course");
    assertThat(response.url()).isEqualTo("/courses/ielts");
    assertThat(response.sortOrder()).isEqualTo(2);
  }

  @Test
  @DisplayName("Should map FooterContent and FooterLinks to FooterResponse correctly")
  void toFooterResponse_mapsFooterAndLinksCorrectly() {
    UUID footerId = UUID.randomUUID();
    User updater = User.builder().gmail("admin@studyweb.edu").build();
    LocalDateTime now = LocalDateTime.now();

    FooterContent footer =
        FooterContent.builder()
            .companyName("CUS Training Co.")
            .address("479 Ma Lo, Binh Tan, HCMC")
            .facebookUrl("https://fb.com/cus")
            .instagramUrl("https://ig.com/cus")
            .youtubeUrl("https://yt.com/cus")
            .tiktokUrl("https://tiktok.com/cus")
            .phone("0362174805")
            .email("contact@cus.edu.vn")
            .website("https://cus.edu.vn")
            .workingHours("7:30 - 21:00")
            .copyrightText("© 2026 CUS")
            .privacyUrl("/privacy")
            .termsUrl("/terms")
            .updatedBy(updater)
            .build();
    footer.setId(footerId);
    footer.setUpdatedAt(now);

    FooterLink link =
        FooterLink.builder()
            .category(FooterCategory.ABOUT)
            .label("About Us")
            .url("/about")
            .sortOrder(0)
            .build();
    link.setId(UUID.randomUUID());

    FooterResponse response = mapper.toFooterResponse(footer, List.of(link));

    assertThat(response).isNotNull();
    assertThat(response.id()).isEqualTo(footerId);
    assertThat(response.companyName()).isEqualTo("CUS Training Co.");
    assertThat(response.address()).isEqualTo("479 Ma Lo, Binh Tan, HCMC");
    assertThat(response.facebookUrl()).isEqualTo("https://fb.com/cus");
    assertThat(response.instagramUrl()).isEqualTo("https://ig.com/cus");
    assertThat(response.youtubeUrl()).isEqualTo("https://yt.com/cus");
    assertThat(response.tiktokUrl()).isEqualTo("https://tiktok.com/cus");
    assertThat(response.phone()).isEqualTo("0362174805");
    assertThat(response.email()).isEqualTo("contact@cus.edu.vn");
    assertThat(response.website()).isEqualTo("https://cus.edu.vn");
    assertThat(response.workingHours()).isEqualTo("7:30 - 21:00");
    assertThat(response.copyrightText()).isEqualTo("© 2026 CUS");
    assertThat(response.privacyUrl()).isEqualTo("/privacy");
    assertThat(response.termsUrl()).isEqualTo("/terms");
    assertThat(response.updatedByEmail()).isEqualTo("admin@studyweb.edu");
    assertThat(response.updatedAt()).isEqualTo(now);
    assertThat(response.links()).hasSize(1);
    assertThat(response.links().get(0).label()).isEqualTo("About Us");
  }

  @Test
  @DisplayName("Should return null when FooterContent is null")
  void toFooterResponse_whenContentNull_returnsNull() {
    assertThat(mapper.toFooterResponse(null, List.of())).isNull();
  }

  @Test
  @DisplayName("Should return empty list of links when links argument is null")
  void toFooterResponse_whenLinksNull_returnsEmptyList() {
    FooterContent footer = FooterContent.builder().companyName("CUS").build();
    footer.setId(UUID.randomUUID());

    FooterResponse response = mapper.toFooterResponse(footer, null);

    assertThat(response).isNotNull();
    assertThat(response.links()).isEmpty();
  }
}
