package studyweb.cus.entity.document;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import studyweb.cus.entity.AbstractBaseEntity;
import studyweb.cus.enums.AccessTier;
import studyweb.cus.enums.DocType;
import studyweb.cus.enums.DocumentFileType;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document extends AbstractBaseEntity {

  @Column(name = "title", nullable = false, length = 255)
  private String title;

  @Enumerated(EnumType.STRING)
  @Column(name = "doc_type", nullable = false, length = 50)
  @Builder.Default
  private DocType docType = DocType.THEORY;

  @Enumerated(EnumType.STRING)
  @Column(name = "file_type", nullable = false, length = 20)
  private DocumentFileType fileType;

  @Column(name = "file_url", nullable = false, length = 500)
  private String fileUrl;

  @Column(name = "num_pages")
  @Builder.Default
  private Integer numPages = 0;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "download_count", nullable = false)
  @Builder.Default
  private Integer downloadCount = 0;

  @Column(name = "youtube_url", columnDefinition = "TEXT")
  private String youtubeUrl;

  @Enumerated(EnumType.STRING)
  @Column(name = "access_tier", nullable = false, length = 20)
  @Builder.Default
  private AccessTier accessTier = AccessTier.PUBLIC;

  @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<DocumentBadge> documentBadges = new ArrayList<>();
}
