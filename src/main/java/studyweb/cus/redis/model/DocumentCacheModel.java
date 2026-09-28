package studyweb.cus.redis.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import studyweb.cus.entity.badge.Badge;
import studyweb.cus.entity.document.Document;
import studyweb.cus.entity.document.DocumentBadge;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.AccessTier;
import studyweb.cus.enums.DocType;
import studyweb.cus.enums.DocumentFileType;

public record DocumentCacheModel(
    UUID id,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String title,
    DocType docType,
    DocumentFileType fileType,
    String fileUrl,
    Integer numPages,
    String description,
    Integer downloadCount,
    String youtubeUrl,
    AccessTier accessTier,
    List<CachedBadgeInfo> badges) {

  public record CachedBadgeInfo(
      UUID id, String name, UUID createdById, LocalDateTime createdAt, LocalDateTime updatedAt) {}

  public static DocumentCacheModel fromDocument(Document doc) {
    if (doc == null) {
      return null;
    }
    List<CachedBadgeInfo> badgeInfos = new ArrayList<>();
    if (doc.getDocumentBadges() != null) {
      for (DocumentBadge db : doc.getDocumentBadges()) {
        if (db != null && db.getDeletedAt() == null && db.getBadge() != null) {
          Badge b = db.getBadge();
          UUID createdById = b.getCreatedBy() != null ? b.getCreatedBy().getId() : null;
          badgeInfos.add(
              new CachedBadgeInfo(
                  b.getId(), b.getName(), createdById, b.getCreatedAt(), b.getUpdatedAt()));
        }
      }
    }

    return new DocumentCacheModel(
        doc.getId(),
        doc.getCreatedAt(),
        doc.getUpdatedAt(),
        doc.getTitle(),
        doc.getDocType(),
        doc.getFileType(),
        doc.getFileUrl(),
        doc.getNumPages(),
        doc.getDescription(),
        doc.getDownloadCount(),
        doc.getYoutubeUrl(),
        doc.getAccessTier(),
        badgeInfos);
  }

  public Document toDocument() {
    Document doc =
        Document.builder()
            .title(this.title)
            .docType(this.docType)
            .fileType(this.fileType)
            .fileUrl(this.fileUrl)
            .numPages(this.numPages)
            .description(this.description)
            .downloadCount(this.downloadCount)
            .youtubeUrl(this.youtubeUrl)
            .accessTier(this.accessTier)
            .documentBadges(new ArrayList<>())
            .build();

    doc.setId(this.id);
    doc.setCreatedAt(this.createdAt);
    doc.setUpdatedAt(this.updatedAt);

    if (this.badges != null && !this.badges.isEmpty()) {
      List<DocumentBadge> docBadges = new ArrayList<>();
      for (CachedBadgeInfo info : this.badges) {
        Badge badge = Badge.builder().name(info.name()).build();
        badge.setId(info.id());
        badge.setCreatedAt(info.createdAt());
        badge.setUpdatedAt(info.updatedAt());

        if (info.createdById() != null) {
          User creator = new User();
          creator.setId(info.createdById());
          badge.setCreatedBy(creator);
        }

        DocumentBadge db = DocumentBadge.builder().document(doc).badge(badge).build();
        docBadges.add(db);
      }
      doc.setDocumentBadges(docBadges);
    }

    return doc;
  }
}
