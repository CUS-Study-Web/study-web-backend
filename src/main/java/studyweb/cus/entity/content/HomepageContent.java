package studyweb.cus.entity.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;
import studyweb.cus.entity.user.User;

@Entity
@Table(name = "homepage_content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomepageContent {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "badge_title", length = 255)
  private String badgeTitle;

  @Column(name = "headline_1", length = 255)
  private String headline1;

  @Column(name = "headline_2", length = 255)
  private String headline2;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "cta_btn1_name", length = 100)
  private String ctaBtn1Name;

  @Column(name = "cta_btn1_url", length = 500)
  private String ctaBtn1Url;

  @Column(name = "cta_btn2_name", length = 100)
  private String ctaBtn2Name;

  @Column(name = "cta_btn2_url", length = 500)
  private String ctaBtn2Url;

  @Column(name = "main_image_url", length = 500)
  private String mainImageUrl;

  @Column(name = "stat1_number", length = 50)
  private String stat1Number;

  @Column(name = "stat1_desc", length = 255)
  private String stat1Desc;

  @Column(name = "stat2_number", length = 50)
  private String stat2Number;

  @Column(name = "stat2_desc", length = 255)
  private String stat2Desc;

  @Column(name = "student1_avatar", length = 500)
  private String student1Avatar;

  @Column(name = "student_stats_desc", columnDefinition = "TEXT")
  private String studentStatsDesc;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "updated_by")
  private User updatedBy;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
