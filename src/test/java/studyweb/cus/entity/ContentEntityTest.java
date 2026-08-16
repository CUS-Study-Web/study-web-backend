package studyweb.cus.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import studyweb.cus.entity.content.FooterContent;
import studyweb.cus.entity.content.FooterLink;
import studyweb.cus.entity.content.HomepageContent;
import studyweb.cus.entity.content.PricingPageContent;
import studyweb.cus.entity.content.VipFeature;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.FeatureIconAccess;
import studyweb.cus.enums.FooterCategory;

@DisplayName("Content Domain Entities Test")
class ContentEntityTest {

  @Test
  @DisplayName("Should build HomepageContent correctly")
  void testHomepageContentBuilder() {
    User admin = User.builder().gmail("admin@studyweb.edu").build();
    admin.setId(UUID.randomUUID());

    HomepageContent content =
        HomepageContent.builder()
            .badgeTitle("Welcome to StudyWeb")
            .headline1("Learn with the Best")
            .headline2("Achieve Your Dreams")
            .ctaBtn1Name("Get Started")
            .ctaBtn1Url("/register")
            .updatedBy(admin)
            .build();

    assertThat(content.getBadgeTitle()).isEqualTo("Welcome to StudyWeb");
    assertThat(content.getHeadline1()).isEqualTo("Learn with the Best");
    assertThat(content.getCtaBtn1Name()).isEqualTo("Get Started");
    assertThat(content.getUpdatedBy()).isEqualTo(admin);
  }

  @Test
  @DisplayName("Should build FooterContent and FooterLink correctly")
  void testFooterContentAndLinkBuilder() {
    FooterContent footer =
        FooterContent.builder()
            .companyName("StudyWeb Co., Ltd.")
            .address("123 Education Boulevard")
            .email("contact@studyweb.edu")
            .phone("0987654321")
            .build();
    footer.setId(UUID.randomUUID());

    FooterLink link =
        FooterLink.builder()
            .footer(footer)
            .category(FooterCategory.PROGRAM)
            .label("IELTS Prep")
            .url("/programs/ielts")
            .sortOrder(1)
            .build();

    assertThat(footer.getCompanyName()).isEqualTo("StudyWeb Co., Ltd.");
    assertThat(link.getFooter()).isEqualTo(footer);
    assertThat(link.getCategory()).isEqualTo(FooterCategory.PROGRAM);
    assertThat(link.getLabel()).isEqualTo("IELTS Prep");
  }

  @Test
  @DisplayName("Should build PricingPageContent and VipFeature correctly")
  void testPricingPageAndVipFeatureBuilder() {
    PricingPageContent pricing =
        PricingPageContent.builder()
            .normalPkgName("Standard Plan")
            .normalPkgPrice("Free")
            .vipPkgName("VIP Premium")
            .vipPkgPrice("199,000 VND")
            .vipPkgBillingPeriod("monthly")
            .build();
    pricing.setId(UUID.randomUUID());

    VipFeature feature =
        VipFeature.builder()
            .setting(pricing)
            .featureName("Full Mock Exam Access")
            .iconNormalAccess(FeatureIconAccess.UNCHECKED)
            .iconVipAccess(FeatureIconAccess.CHECKED)
            .normalAccess("Limited to 1 test")
            .vipAccess("Unlimited tests with detailed explanations")
            .build();

    assertThat(pricing.getNormalPkgName()).isEqualTo("Standard Plan");
    assertThat(pricing.getVipPkgName()).isEqualTo("VIP Premium");
    assertThat(feature.getSetting()).isEqualTo(pricing);
    assertThat(feature.getFeatureName()).isEqualTo("Full Mock Exam Access");
    assertThat(feature.getIconNormalAccess()).isEqualTo(FeatureIconAccess.UNCHECKED);
    assertThat(feature.getIconVipAccess()).isEqualTo(FeatureIconAccess.CHECKED);
  }
}
