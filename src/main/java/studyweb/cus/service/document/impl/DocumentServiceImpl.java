package studyweb.cus.service.document.impl;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studyweb.cus.dto.request.document.CreateDocumentRequest;
import studyweb.cus.dto.request.document.UpdateDocumentRequest;
import studyweb.cus.dto.response.document.DocumentDownloadResponse;
import studyweb.cus.dto.response.document.DocumentGuestResponse;
import studyweb.cus.dto.response.document.DocumentResponse;
import studyweb.cus.dto.response.document.UploadDocumentResult;
import studyweb.cus.entity.badge.Badge;
import studyweb.cus.entity.document.Document;
import studyweb.cus.entity.document.DocumentBadge;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.AccessTier;
import studyweb.cus.enums.DocType;
import studyweb.cus.enums.UserRole;
import studyweb.cus.enums.UserTier;
import studyweb.cus.exception.document.DocumentErrorCode;
import studyweb.cus.exception.document.DocumentException;
import studyweb.cus.exception.user.UserErrorCode;
import studyweb.cus.exception.user.UserException;
import studyweb.cus.mapper.document.DocumentMapper;
import studyweb.cus.repository.badge.BadgeRepository;
import studyweb.cus.repository.document.DocumentBadgeRepository;
import studyweb.cus.repository.document.DocumentRepository;
import studyweb.cus.repository.user.UserRepository;
import studyweb.cus.service.document.DocumentService;
import studyweb.cus.service.file.FileService;
import studyweb.cus.util.FileUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

  private final DocumentRepository documentRepository;
  private final DocumentBadgeRepository documentBadgeRepository;
  private final BadgeRepository badgeRepository;
  private final UserRepository userRepository;
  private final FileService fileService;
  private final DocumentMapper documentMapper;

  @Override
  @Transactional
  public DocumentResponse uploadDocument(CreateDocumentRequest request) {
    log.info("Uploading document: title='{}', docType={}", request.title(), request.docType());

    UploadDocumentResult uploadResult = fileService.uploadDocumentFile(request.file());
    try {
      var fileType = FileUtils.resolveDocumentFileType(request.file(), request.fileType());

      Document document =
          Document.builder()
              .title(request.title())
              .docType(request.docType() != null ? request.docType() : DocType.THEORY)
              .fileType(fileType)
              .fileUrl(uploadResult.fileUrl())
              .numPages(request.numPages() != null ? request.numPages() : 0)
              .description(request.description())
              .downloadCount(0)
              .youtubeUrl(request.youtubeUrl())
              .accessTier(request.accessTier() != null ? request.accessTier() : AccessTier.PUBLIC)
              .documentBadges(new ArrayList<>())
              .build();

      Document savedDocument = documentRepository.save(document);

      if (request.badgeIds() != null && !request.badgeIds().isEmpty()) {
        List<UUID> nonNullBadgeIds = request.badgeIds().stream().filter(Objects::nonNull).toList();
        if (!nonNullBadgeIds.isEmpty()) {
          List<Badge> badges = badgeRepository.findAllById(nonNullBadgeIds);
          List<DocumentBadge> documentBadges =
              badges.stream()
                  .map(
                      badge -> DocumentBadge.builder().badge(badge).document(savedDocument).build())
                  .toList();
          documentBadgeRepository.saveAll(documentBadges);
          savedDocument.setDocumentBadges(new ArrayList<>(documentBadges));
        }
      }

      log.info("Document created successfully with ID {}", savedDocument.getId());
      return documentMapper.toResponse(savedDocument);
    } catch (Exception ex) {
      log.warn(
          "Database operation failed for uploadDocument. Cleaning up uploaded file on S3: {}",
          uploadResult.fileKey());
      try {
        fileService.deleteFile(uploadResult.fileKey());
      } catch (Exception s3Ex) {
        log.error(
            "Failed to delete file from S3 during uploadDocument rollback: {}",
            uploadResult.fileKey(),
            s3Ex);
      }
      throw ex;
    }
  }

  @Override
  @Transactional(readOnly = true)
  public DocumentResponse getDocumentById(UUID id, String userEmail) {
    log.info("Fetching document details for ID {}", id);
    Document document = requireDocument(id);
    checkVipAccess(document, userEmail);
    return documentMapper.toResponse(document);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<DocumentResponse> listDocuments(
      DocType docType, AccessTier accessTier, UUID badgeId, String search, Pageable pageable) {
    log.info(
        "Listing documents: docType={}, accessTier={}, badgeId={}, search='{}'",
        docType,
        accessTier,
        badgeId,
        search);

    Specification<Document> spec =
        (root, query, cb) -> {
          List<Predicate> predicates = new ArrayList<>();
          if (docType != null) {
            predicates.add(cb.equal(root.get("docType"), docType));
          }
          if (accessTier != null) {
            predicates.add(cb.equal(root.get("accessTier"), accessTier));
          }
          if (badgeId != null) {
            Join<Document, DocumentBadge> badgesJoin = root.join("documentBadges");
            predicates.add(cb.equal(badgesJoin.get("badge").get("id"), badgeId));
            predicates.add(cb.isNull(badgesJoin.get("deletedAt")));
          }
          if (search != null && !search.isBlank()) {
            predicates.add(
                cb.like(cb.lower(root.get("title")), "%" + search.trim().toLowerCase() + "%"));
          }
          if (query != null) {
            query.distinct(true);
          }
          return cb.and(predicates.toArray(new Predicate[0]));
        };

    return documentRepository.findAll(spec, pageable).map(documentMapper::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<DocumentGuestResponse> listDocumentsForGuest(
      DocType docType, UUID badgeId, String search, Pageable pageable) {
    log.info(
        "Listing documents for guest: docType={}, badgeId={}, search='{}'",
        docType,
        badgeId,
        search);

    Specification<Document> spec =
        (root, query, cb) -> {
          List<Predicate> predicates = new ArrayList<>();
          if (docType != null) {
            predicates.add(cb.equal(root.get("docType"), docType));
          }
          if (badgeId != null) {
            Join<Document, DocumentBadge> badgesJoin = root.join("documentBadges");
            predicates.add(cb.equal(badgesJoin.get("badge").get("id"), badgeId));
            predicates.add(cb.isNull(badgesJoin.get("deletedAt")));
          }
          if (search != null && !search.isBlank()) {
            predicates.add(
                cb.like(cb.lower(root.get("title")), "%" + search.trim().toLowerCase() + "%"));
          }
          if (query != null) {
            query.distinct(true);
          }
          return cb.and(predicates.toArray(new Predicate[0]));
        };

    return documentRepository.findAll(spec, pageable).map(documentMapper::toGuestResponse);
  }

  @Override
  @Transactional
  public DocumentDownloadResponse downloadDocument(UUID id, String userEmail) {
    log.info("Processing download request for document ID {} by user '{}'", id, userEmail);
    Document document = requireDocument(id);
    checkVipAccess(document, userEmail);

    documentRepository.incrementDownloadCount(id);
    Document updatedDocument = requireDocument(id);

    log.info(
        "Document ID {} downloaded. Updated count: {}", id, updatedDocument.getDownloadCount());
    return documentMapper.toDownloadResponse(updatedDocument);
  }

  @Override
  @Transactional
  public DocumentResponse updateDocument(UUID id, UpdateDocumentRequest request) {
    log.info("Updating document ID {}", id);
    Document document = requireDocument(id);

    String oldFileUrl = document.getFileUrl();
    String newFileKey = null;

    if (request.file() != null && !request.file().isEmpty()) {
      UploadDocumentResult uploadResult = fileService.uploadDocumentFile(request.file());
      newFileKey = uploadResult.fileKey();
      document.setFileUrl(uploadResult.fileUrl());
      document.setFileType(FileUtils.resolveDocumentFileType(request.file(), request.fileType()));
    } else if (request.fileType() != null) {
      document.setFileType(request.fileType());
    }

    try {
      if (request.title() != null && !request.title().isBlank()) {
        document.setTitle(request.title());
      }
      if (request.docType() != null) {
        document.setDocType(request.docType());
      }
      if (request.numPages() != null) {
        document.setNumPages(request.numPages());
      }
      if (request.description() != null) {
        document.setDescription(request.description());
      }
      if (request.youtubeUrl() != null) {
        document.setYoutubeUrl(request.youtubeUrl());
      }
      if (request.accessTier() != null) {
        document.setAccessTier(request.accessTier());
      }

      if (request.badgeIds() != null) {
        document.getDocumentBadges().clear();
        List<UUID> nonNullBadgeIds = request.badgeIds().stream().filter(Objects::nonNull).toList();
        if (!nonNullBadgeIds.isEmpty()) {
          List<Badge> badges = badgeRepository.findAllById(nonNullBadgeIds);
          List<DocumentBadge> newBadges =
              badges.stream()
                  .map(b -> DocumentBadge.builder().badge(b).document(document).build())
                  .toList();
          document.getDocumentBadges().addAll(newBadges);
        }
      }

      Document updatedDocument = documentRepository.save(document);

      if (newFileKey != null && oldFileUrl != null && !oldFileUrl.isBlank()) {
        try {
          fileService.deleteFile(oldFileUrl);
        } catch (Exception s3Ex) {
          log.error("Failed to delete old file from S3: {}", oldFileUrl, s3Ex);
        }
      }

      return documentMapper.toResponse(updatedDocument);
    } catch (Exception ex) {
      if (newFileKey != null) {
        log.warn(
            "Database operation failed for updateDocument. Cleaning up new uploaded file on S3: {}",
            newFileKey);
        try {
          fileService.deleteFile(newFileKey);
        } catch (Exception s3Ex) {
          log.error(
              "Failed to delete file from S3 during updateDocument rollback: {}", newFileKey, s3Ex);
        }
      }
      throw ex;
    }
  }

  @Override
  @Transactional
  public void deleteDocument(UUID id) {
    log.info("Deleting document ID {}", id);
    Document document = requireDocument(id);
    String fileUrl = document.getFileUrl();
    documentRepository.delete(document);
    if (fileUrl != null && !fileUrl.isBlank()) {
      try {
        fileService.deleteFile(fileUrl);
      } catch (Exception e) {
        log.error("Failed to delete document file from S3: {}", fileUrl, e);
      }
    }
    log.info("Document ID {} deleted successfully", id);
  }

  private Document requireDocument(UUID id) {
    return documentRepository
        .findById(id)
        .orElseThrow(() -> new DocumentException(DocumentErrorCode.DOCUMENT_NOT_FOUND));
  }

  private void checkVipAccess(Document document, String userEmail) {
    if (document.getAccessTier() == AccessTier.VIP) {
      if (userEmail == null || userEmail.isBlank()) {
        throw new DocumentException(DocumentErrorCode.VIP_ONLY);
      }
      User user =
          userRepository
              .findByGmail(userEmail)
              .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
      if (user.getRole() != UserRole.ASSISTANT
          && user.getRole() != UserRole.ADMIN
          && user.getTier() != UserTier.VIP) {
        throw new DocumentException(DocumentErrorCode.VIP_ONLY);
      }
    }
  }
}
