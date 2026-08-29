package studyweb.cus.service.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
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
import studyweb.cus.enums.DocumentFileType;
import studyweb.cus.enums.UserRole;
import studyweb.cus.enums.UserTier;
import studyweb.cus.exception.document.DocumentErrorCode;
import studyweb.cus.exception.document.DocumentException;
import studyweb.cus.mapper.document.DocumentMapper;
import studyweb.cus.repository.badge.BadgeRepository;
import studyweb.cus.repository.document.DocumentBadgeRepository;
import studyweb.cus.repository.document.DocumentRepository;
import studyweb.cus.repository.user.UserRepository;
import studyweb.cus.service.document.impl.DocumentServiceImpl;
import studyweb.cus.service.file.FileService;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

  @Mock private DocumentRepository documentRepository;
  @Mock private DocumentBadgeRepository documentBadgeRepository;
  @Mock private BadgeRepository badgeRepository;
  @Mock private UserRepository userRepository;
  @Mock private FileService fileService;

  private DocumentMapper documentMapper = Mappers.getMapper(DocumentMapper.class);

  private DocumentService documentService;

  @Captor private ArgumentCaptor<Document> documentCaptor;

  private Badge sampleBadge;
  private Document publicDocument;
  private Document vipDocument;
  private User normalUser;
  private User vipUser;
  private User assistantUser;

  @BeforeEach
  void setUp() {
    documentService =
        new DocumentServiceImpl(
            documentRepository,
            documentBadgeRepository,
            badgeRepository,
            userRepository,
            fileService,
            documentMapper);

    sampleBadge = Badge.builder().name("Toán").build();
    sampleBadge.setId(UUID.randomUUID());

    publicDocument =
        Document.builder()
            .title("English Grammar PDF")
            .docType(DocType.THEORY)
            .fileType(DocumentFileType.PDF)
            .fileUrl("https://s3.example.com/grammar.pdf")
            .numPages(10)
            .downloadCount(5)
            .accessTier(AccessTier.PUBLIC)
            .documentBadges(new ArrayList<>())
            .build();
    publicDocument.setId(UUID.randomUUID());

    DocumentBadge docBadge =
        DocumentBadge.builder().badge(sampleBadge).document(publicDocument).build();
    publicDocument.getDocumentBadges().add(docBadge);

    vipDocument =
        Document.builder()
            .title("Advanced VIP IELTS Prep")
            .docType(DocType.EXERCISE)
            .fileType(DocumentFileType.PDF)
            .fileUrl("https://s3.example.com/vip-ielts.pdf")
            .numPages(25)
            .downloadCount(2)
            .accessTier(AccessTier.VIP)
            .documentBadges(new ArrayList<>())
            .build();
    vipDocument.setId(UUID.randomUUID());

    normalUser =
        User.builder()
            .gmail("learner@gmail.com")
            .name("Normal Learner")
            .role(UserRole.LEARNER)
            .tier(UserTier.NORMAL)
            .build();

    vipUser =
        User.builder()
            .gmail("vip@gmail.com")
            .name("VIP Learner")
            .role(UserRole.LEARNER)
            .tier(UserTier.VIP)
            .build();

    assistantUser =
        User.builder()
            .gmail("assistant@gmail.com")
            .name("Assistant Admin")
            .role(UserRole.ASSISTANT)
            .tier(UserTier.NORMAL)
            .build();
  }

  @Nested
  @DisplayName("Upload Document Tests")
  class UploadDocumentTests {

    @Test
    @DisplayName("Should successfully upload document with badges")
    void shouldUploadDocumentSuccessfully() {
      MockMultipartFile file =
          new MockMultipartFile("file", "guide.pdf", "application/pdf", "content".getBytes());
      CreateDocumentRequest request =
          new CreateDocumentRequest(
              file,
              "Complete Guide",
              DocType.THEORY,
              DocumentFileType.PDF,
              15,
              "Description",
              "https://youtube.com/watch?v=123",
              AccessTier.PUBLIC,
              List.of(sampleBadge.getId()));

      when(fileService.uploadDocumentFile(file))
          .thenReturn(
              new UploadDocumentResult(100L, "documents/guide.pdf", "https://s3/guide.pdf"));
      when(badgeRepository.findAllById(List.of(sampleBadge.getId())))
          .thenReturn(List.of(sampleBadge));
      when(documentRepository.save(any(Document.class)))
          .thenAnswer(
              inv -> {
                Document doc = inv.getArgument(0);
                doc.setId(UUID.randomUUID());
                return doc;
              });

      DocumentResponse response = documentService.uploadDocument(request);

      assertThat(response).isNotNull();
      assertThat(response.title()).isEqualTo("Complete Guide");
      assertThat(response.docType()).isEqualTo(DocType.THEORY);
      assertThat(response.accessTier()).isEqualTo(AccessTier.PUBLIC);
      assertThat(response.badges()).hasSize(1);
      assertThat(response.badges().get(0).name()).isEqualTo("Toán");
      verify(documentRepository).save(documentCaptor.capture());
      assertThat(documentCaptor.getValue().getDownloadCount()).isZero();
      assertThat(documentCaptor.getValue().getFileType()).isEqualTo(DocumentFileType.PDF);
    }

    @Test
    @DisplayName(
        "Should auto-detect fileType from uploaded file extension when not explicitly provided")
    void shouldAutoDetectFileTypeWhenExplicitTypeIsNull() {
      MockMultipartFile docxFile =
          new MockMultipartFile(
              "file",
              "lecture_notes.docx",
              "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
              "content".getBytes());
      CreateDocumentRequest request =
          new CreateDocumentRequest(
              docxFile,
              "Lecture Notes",
              DocType.THEORY,
              null,
              10,
              "Desc",
              null,
              AccessTier.PUBLIC,
              null);

      when(fileService.uploadDocumentFile(docxFile))
          .thenReturn(
              new UploadDocumentResult(
                  50L, "documents/lecture_notes.docx", "https://s3/lecture_notes.docx"));
      when(documentRepository.save(any(Document.class)))
          .thenAnswer(
              inv -> {
                Document doc = inv.getArgument(0);
                doc.setId(UUID.randomUUID());
                return doc;
              });

      DocumentResponse response = documentService.uploadDocument(request);

      assertThat(response).isNotNull();
      verify(documentRepository).save(documentCaptor.capture());
      assertThat(documentCaptor.getValue().getFileType()).isEqualTo(DocumentFileType.DOCX);
    }

    @Test
    @DisplayName("Should rollback S3 upload if database save fails")
    void shouldRollbackS3UploadWhenDatabaseSaveFails() {
      MockMultipartFile file =
          new MockMultipartFile("file", "guide.pdf", "application/pdf", "content".getBytes());
      CreateDocumentRequest request =
          new CreateDocumentRequest(
              file,
              "Complete Guide",
              DocType.THEORY,
              DocumentFileType.PDF,
              15,
              "Description",
              null,
              AccessTier.PUBLIC,
              null);

      when(fileService.uploadDocumentFile(file))
          .thenReturn(
              new UploadDocumentResult(100L, "documents/guide.pdf", "https://s3/guide.pdf"));
      when(documentRepository.save(any(Document.class)))
          .thenThrow(new RuntimeException("Database error"));

      assertThatThrownBy(() -> documentService.uploadDocument(request))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Database error");

      verify(fileService).deleteFile("documents/guide.pdf");
    }
  }

  @Nested
  @DisplayName("Watch/View Document Detail Tests")
  class GetDocumentDetailTests {

    @Test
    @DisplayName("Should allow normal user to view public document")
    void shouldAllowNormalUserToViewPublicDocument() {
      when(documentRepository.findById(publicDocument.getId()))
          .thenReturn(Optional.of(publicDocument));

      DocumentResponse response =
          documentService.getDocumentById(publicDocument.getId(), "learner@gmail.com");

      assertThat(response).isNotNull();
      assertThat(response.title()).isEqualTo(publicDocument.getTitle());
      assertThat(response.accessTier()).isEqualTo(AccessTier.PUBLIC);
      assertThat(response.badges()).hasSize(1);
    }

    @Test
    @DisplayName("Should throw VIP_ONLY when normal user views VIP document")
    void shouldBlockNormalUserFromViewingVipDocument() {
      when(documentRepository.findById(vipDocument.getId())).thenReturn(Optional.of(vipDocument));
      when(userRepository.findByGmail("learner@gmail.com")).thenReturn(Optional.of(normalUser));

      assertThatThrownBy(
              () -> documentService.getDocumentById(vipDocument.getId(), "learner@gmail.com"))
          .isInstanceOf(DocumentException.class)
          .satisfies(
              ex ->
                  assertThat(((DocumentException) ex).getCode())
                      .isEqualTo(DocumentErrorCode.VIP_ONLY.code()));
    }

    @Test
    @DisplayName("Should allow VIP user to view VIP document")
    void shouldAllowVipUserToViewVipDocument() {
      when(documentRepository.findById(vipDocument.getId())).thenReturn(Optional.of(vipDocument));
      when(userRepository.findByGmail("vip@gmail.com")).thenReturn(Optional.of(vipUser));

      DocumentResponse response =
          documentService.getDocumentById(vipDocument.getId(), "vip@gmail.com");

      assertThat(response).isNotNull();
      assertThat(response.title()).isEqualTo(vipDocument.getTitle());
    }

    @Test
    @DisplayName("Should allow Assistant to view VIP document")
    void shouldAllowAssistantToViewVipDocument() {
      when(documentRepository.findById(vipDocument.getId())).thenReturn(Optional.of(vipDocument));
      when(userRepository.findByGmail("assistant@gmail.com"))
          .thenReturn(Optional.of(assistantUser));

      DocumentResponse response =
          documentService.getDocumentById(vipDocument.getId(), "assistant@gmail.com");

      assertThat(response).isNotNull();
      assertThat(response.title()).isEqualTo(vipDocument.getTitle());
    }

    @Test
    @DisplayName("Should throw DOCUMENT_NOT_FOUND when document does not exist")
    void shouldThrowWhenDocumentNotFound() {
      UUID missingId = UUID.randomUUID();
      when(documentRepository.findById(missingId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> documentService.getDocumentById(missingId, "learner@gmail.com"))
          .isInstanceOf(DocumentException.class)
          .satisfies(
              ex ->
                  assertThat(((DocumentException) ex).getCode())
                      .isEqualTo(DocumentErrorCode.DOCUMENT_NOT_FOUND.code()));
    }
  }

  @Nested
  @DisplayName("Download Document Tests")
  class DownloadDocumentTests {

    @Test
    @DisplayName("Should allow normal user to download public document and increment count")
    void shouldAllowNormalUserToDownloadPublicDocument() {
      Document updatedDoc =
          Document.builder()
              .title(publicDocument.getTitle())
              .docType(publicDocument.getDocType())
              .fileType(publicDocument.getFileType())
              .fileUrl(publicDocument.getFileUrl())
              .numPages(publicDocument.getNumPages())
              .downloadCount(6)
              .accessTier(publicDocument.getAccessTier())
              .documentBadges(publicDocument.getDocumentBadges())
              .build();
      updatedDoc.setId(publicDocument.getId());

      when(documentRepository.findById(publicDocument.getId()))
          .thenReturn(Optional.of(publicDocument), Optional.of(updatedDoc));

      DocumentDownloadResponse response =
          documentService.downloadDocument(publicDocument.getId(), "learner@gmail.com");

      assertThat(response).isNotNull();
      assertThat(response.downloadUrl()).isEqualTo(publicDocument.getFileUrl());
      assertThat(response.downloadCount()).isEqualTo(6);
      verify(documentRepository).incrementDownloadCount(publicDocument.getId());
    }

    @Test
    @DisplayName("Should throw VIP_ONLY when normal user downloads VIP document")
    void shouldBlockNormalUserFromDownloadingVipDocument() {
      when(documentRepository.findById(vipDocument.getId())).thenReturn(Optional.of(vipDocument));
      when(userRepository.findByGmail("learner@gmail.com")).thenReturn(Optional.of(normalUser));

      assertThatThrownBy(
              () -> documentService.downloadDocument(vipDocument.getId(), "learner@gmail.com"))
          .isInstanceOf(DocumentException.class)
          .satisfies(
              ex ->
                  assertThat(((DocumentException) ex).getCode())
                      .isEqualTo(DocumentErrorCode.VIP_ONLY.code()));
    }

    @Test
    @DisplayName("Should allow VIP user to download VIP document and increment count")
    void shouldAllowVipUserToDownloadVipDocument() {
      Document updatedDoc =
          Document.builder()
              .title(vipDocument.getTitle())
              .docType(vipDocument.getDocType())
              .fileType(vipDocument.getFileType())
              .fileUrl(vipDocument.getFileUrl())
              .numPages(vipDocument.getNumPages())
              .downloadCount(3)
              .accessTier(vipDocument.getAccessTier())
              .documentBadges(vipDocument.getDocumentBadges())
              .build();
      updatedDoc.setId(vipDocument.getId());

      when(documentRepository.findById(vipDocument.getId()))
          .thenReturn(Optional.of(vipDocument), Optional.of(updatedDoc));
      when(userRepository.findByGmail("vip@gmail.com")).thenReturn(Optional.of(vipUser));

      DocumentDownloadResponse response =
          documentService.downloadDocument(vipDocument.getId(), "vip@gmail.com");

      assertThat(response).isNotNull();
      assertThat(response.downloadUrl()).isEqualTo(vipDocument.getFileUrl());
      assertThat(response.downloadCount()).isEqualTo(3);
      verify(documentRepository).incrementDownloadCount(vipDocument.getId());
    }

    @Test
    @DisplayName("Should allow Assistant to download VIP document")
    void shouldAllowAssistantToDownloadVipDocument() {
      Document updatedDoc =
          Document.builder()
              .title(vipDocument.getTitle())
              .docType(vipDocument.getDocType())
              .fileType(vipDocument.getFileType())
              .fileUrl(vipDocument.getFileUrl())
              .numPages(vipDocument.getNumPages())
              .downloadCount(3)
              .accessTier(vipDocument.getAccessTier())
              .documentBadges(vipDocument.getDocumentBadges())
              .build();
      updatedDoc.setId(vipDocument.getId());

      when(documentRepository.findById(vipDocument.getId()))
          .thenReturn(Optional.of(vipDocument), Optional.of(updatedDoc));
      when(userRepository.findByGmail("assistant@gmail.com"))
          .thenReturn(Optional.of(assistantUser));

      DocumentDownloadResponse response =
          documentService.downloadDocument(vipDocument.getId(), "assistant@gmail.com");

      assertThat(response).isNotNull();
      assertThat(response.downloadCount()).isEqualTo(3);
    }
  }

  @Nested
  @DisplayName("List, Update & Delete Tests")
  class ListUpdateDeleteTests {

    @Test
    @DisplayName("Should list documents with pagination")
    void shouldListDocuments() {
      Pageable pageable = PageRequest.of(0, 10);
      Page<Document> page = new PageImpl<>(List.of(publicDocument), pageable, 1);
      when(documentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

      Page<DocumentResponse> result =
          documentService.listDocuments(null, null, null, null, pageable);

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().get(0).title()).isEqualTo(publicDocument.getTitle());
    }

    @Test
    @DisplayName("Should list documents for guest with restricted fields and pagination")
    void shouldListDocumentsForGuest() {
      Pageable pageable = PageRequest.of(0, 10);
      Page<Document> page = new PageImpl<>(List.of(publicDocument), pageable, 1);
      when(documentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

      Page<DocumentGuestResponse> result =
          documentService.listDocumentsForGuest(null, null, null, pageable);

      assertThat(result.getContent()).hasSize(1);
      DocumentGuestResponse guestDoc = result.getContent().get(0);
      assertThat(guestDoc.id()).isEqualTo(publicDocument.getId());
      assertThat(guestDoc.title()).isEqualTo(publicDocument.getTitle());
      assertThat(guestDoc.description()).isEqualTo(publicDocument.getDescription());
      assertThat(guestDoc.numPages()).isEqualTo(publicDocument.getNumPages());
      assertThat(guestDoc.downloadCount()).isEqualTo(publicDocument.getDownloadCount());
      assertThat(guestDoc.badges()).isNotNull();
    }

    @Test
    @DisplayName("Should update document metadata and badges")
    void shouldUpdateDocument() {
      Badge newBadge = Badge.builder().name("Công thức").build();
      newBadge.setId(UUID.randomUUID());

      UpdateDocumentRequest updateRequest =
          new UpdateDocumentRequest(
              null,
              "Updated Title",
              DocType.EXERCISE,
              null,
              20,
              "Updated Desc",
              null,
              AccessTier.VIP,
              List.of(newBadge.getId()));

      when(documentRepository.findById(publicDocument.getId()))
          .thenReturn(Optional.of(publicDocument));
      when(badgeRepository.findAllById(List.of(newBadge.getId()))).thenReturn(List.of(newBadge));
      when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

      DocumentResponse response =
          documentService.updateDocument(publicDocument.getId(), updateRequest);

      assertThat(response.title()).isEqualTo("Updated Title");
      assertThat(response.docType()).isEqualTo(DocType.EXERCISE);
      assertThat(response.numPages()).isEqualTo(20);
      assertThat(response.accessTier()).isEqualTo(AccessTier.VIP);
      assertThat(response.badges()).hasSize(1);
      assertThat(response.badges().get(0).name()).isEqualTo("Công thức");
    }

    @Test
    @DisplayName("Should update document file and auto-detect fileType, and delete old file")
    void shouldUpdateDocumentWithFileAndAutoDetectFileType() {
      MockMultipartFile xlsxFile =
          new MockMultipartFile(
              "file", "report.xlsx", "application/vnd.ms-excel", "content".getBytes());
      UpdateDocumentRequest updateRequest =
          new UpdateDocumentRequest(xlsxFile, null, null, null, null, null, null, null, null);

      when(documentRepository.findById(publicDocument.getId()))
          .thenReturn(Optional.of(publicDocument));
      when(fileService.uploadDocumentFile(xlsxFile))
          .thenReturn(
              new UploadDocumentResult(100L, "documents/report.xlsx", "https://s3/report.xlsx"));
      when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

      DocumentResponse response =
          documentService.updateDocument(publicDocument.getId(), updateRequest);

      assertThat(response).isNotNull();
      verify(documentRepository).save(documentCaptor.capture());
      assertThat(documentCaptor.getValue().getFileType()).isEqualTo(DocumentFileType.XLSX);
      assertThat(documentCaptor.getValue().getFileUrl()).isEqualTo("https://s3/report.xlsx");
      verify(fileService).deleteFile("https://s3.example.com/grammar.pdf");
    }

    @Test
    @DisplayName("Should rollback S3 upload if update fails after new file upload")
    void shouldRollbackS3UploadWhenUpdateFailsWithNewFile() {
      MockMultipartFile xlsxFile =
          new MockMultipartFile(
              "file", "report.xlsx", "application/vnd.ms-excel", "content".getBytes());
      UpdateDocumentRequest updateRequest =
          new UpdateDocumentRequest(xlsxFile, null, null, null, null, null, null, null, null);

      when(documentRepository.findById(publicDocument.getId()))
          .thenReturn(Optional.of(publicDocument));
      when(fileService.uploadDocumentFile(xlsxFile))
          .thenReturn(
              new UploadDocumentResult(100L, "documents/report.xlsx", "https://s3/report.xlsx"));
      when(documentRepository.save(any(Document.class)))
          .thenThrow(new RuntimeException("Database error during update"));

      assertThatThrownBy(
              () -> documentService.updateDocument(publicDocument.getId(), updateRequest))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Database error during update");

      verify(fileService).deleteFile("documents/report.xlsx");
    }

    @Test
    @DisplayName("Should delete document from repository and delete file from S3")
    void shouldDeleteDocument() {
      when(documentRepository.findById(publicDocument.getId()))
          .thenReturn(Optional.of(publicDocument));

      documentService.deleteDocument(publicDocument.getId());

      verify(documentRepository).delete(publicDocument);
      verify(fileService).deleteFile("https://s3.example.com/grammar.pdf");
    }
  }
}
