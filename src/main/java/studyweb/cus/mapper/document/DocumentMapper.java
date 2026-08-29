package studyweb.cus.mapper.document;

import java.util.Collections;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import studyweb.cus.dto.response.badge.BadgeResponse;
import studyweb.cus.dto.response.document.DocumentDownloadResponse;
import studyweb.cus.dto.response.document.DocumentGuestResponse;
import studyweb.cus.dto.response.document.DocumentResponse;
import studyweb.cus.entity.document.Document;
import studyweb.cus.entity.document.DocumentBadge;
import studyweb.cus.mapper.badge.BadgeMapper;

@Mapper(
    componentModel = "spring",
    uses = {BadgeMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentMapper {

  @Mapping(
      source = "document.documentBadges",
      target = "badges",
      qualifiedByName = "mapDocumentBadges")
  DocumentResponse toResponse(Document document);

  @Mapping(
      source = "document.documentBadges",
      target = "badges",
      qualifiedByName = "mapDocumentBadges")
  DocumentGuestResponse toGuestResponse(Document document);

  @Mapping(source = "document.id", target = "id")
  @Mapping(source = "document.title", target = "title")
  @Mapping(source = "document.fileUrl", target = "downloadUrl")
  @Mapping(source = "document.fileType", target = "fileType")
  @Mapping(source = "document.downloadCount", target = "downloadCount")
  DocumentDownloadResponse toDownloadResponse(Document document);

  @Named("mapDocumentBadges")
  default List<BadgeResponse> mapDocumentBadges(List<DocumentBadge> documentBadges) {
    if (documentBadges == null) {
      return Collections.emptyList();
    }
    return documentBadges.stream()
        .filter(db -> db.getDeletedAt() == null && db.getBadge() != null)
        .map(
            db ->
                new BadgeResponse(
                    db.getBadge().getId(),
                    db.getBadge().getName(),
                    db.getBadge().getCreatedBy() != null
                        ? db.getBadge().getCreatedBy().getId()
                        : null,
                    db.getBadge().getCreatedAt(),
                    db.getBadge().getUpdatedAt()))
        .toList();
  }
}
