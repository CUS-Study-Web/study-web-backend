package studyweb.cus.entity.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import studyweb.cus.entity.AbstractBaseEntity;
import studyweb.cus.enums.FooterCategory;

@Entity
@Table(
    name = "footer_links",
    indexes = {@Index(name = "idx_footer_links_footer", columnList = "footer_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FooterLink extends AbstractBaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "footer_id", nullable = false)
  private FooterContent footer;

  @Enumerated(EnumType.STRING)
  @Column(name = "category", nullable = false, length = 20)
  @Builder.Default
  private FooterCategory category = FooterCategory.PROGRAM;

  @Column(name = "label", nullable = false, length = 150)
  private String label;

  @Column(name = "url", nullable = false, length = 500)
  private String url;

  @Column(name = "sort_order", nullable = false)
  @Builder.Default
  private Integer sortOrder = 0;
}
