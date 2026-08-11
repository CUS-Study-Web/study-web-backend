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
@Table(name = "footer_content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FooterContent {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "company_name", nullable = false, length = 255)
  private String companyName;

  @Column(name = "address", columnDefinition = "TEXT")
  private String address;

  @Column(name = "facebook_url", length = 500)
  private String facebookUrl;

  @Column(name = "instagram_url", length = 500)
  private String instagramUrl;

  @Column(name = "youtube_url", length = 500)
  private String youtubeUrl;

  @Column(name = "tiktok_url", length = 500)
  private String tiktokUrl;

  @Column(name = "phone", length = 50)
  private String phone;

  @Column(name = "email", length = 150)
  private String email;

  @Column(name = "website", length = 255)
  private String website;

  @Column(name = "working_hours", length = 100)
  private String workingHours;

  @Column(name = "copyright_text", length = 255)
  private String copyrightText;

  @Column(name = "privacy_url", length = 500)
  private String privacyUrl;

  @Column(name = "terms_url", length = 500)
  private String termsUrl;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "updated_by")
  private User updatedBy;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
