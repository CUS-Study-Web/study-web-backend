package studyweb.cus.service.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import studyweb.cus.config.S3Properties;
import studyweb.cus.dto.response.document.UploadDocumentResult;
import studyweb.cus.exception.file.FileErrorCode;
import studyweb.cus.exception.file.FileException;
import studyweb.cus.service.file.impl.FileServiceImpl;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

  @Mock
  private S3Client s3Client;
  @Mock
  private S3Presigner s3Presigner;

  private final S3Properties properties = new S3Properties();

  private FileServiceImpl fileService;

  @BeforeEach
  void setUp() {
    properties.setEndpoint("https://minio.test.invalid:9000");
    properties.setBucket("bucket-vmt");
    fileService = new FileServiceImpl(s3Client, s3Presigner, properties);
  }

  private void stubPutObject() {
    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .thenReturn(PutObjectResponse.builder().build());
  }

  private MockMultipartFile file(String name, String contentType, int... content) {
    byte[] bytes = new byte[content.length];
    for (int i = 0; i < content.length; i++) {
      bytes[i] = (byte) content[i];
    }
    return new MockMultipartFile("file", name, contentType, bytes);
  }

  @Test
  void uploadDocumentFileReturnsSizeAndFileUrl() {
    stubPutObject();

    UploadDocumentResult result = fileService.uploadDocumentFile(file("lesson.pdf", "application/pdf", 1, 2, 3));

    assertThat(result.fileSize()).isEqualTo(3L);
    assertThat(result.fileKey()).startsWith("documents/").endsWith(".pdf");
    assertThat(result.fileUrl())
        .isEqualTo("https://minio.test.invalid:9000/bucket-vmt/" + result.fileKey());

    ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
    ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
    verify(s3Client).putObject(captor.capture(), bodyCaptor.capture());
    assertThat(captor.getValue().bucket()).isEqualTo("bucket-vmt");
    assertThat(captor.getValue().key()).startsWith("documents/").endsWith(".pdf");
    assertThat(captor.getValue().contentType()).isEqualTo("application/pdf");
    assertThat(bodyCaptor.getValue().contentLength()).isEqualTo(3L);
  }

  @Test
  void uploadAvatarFileUsesAvatarFolderAndImageExtensions() {
    stubPutObject();

    UploadDocumentResult result = fileService.uploadAvatarFile(file("avatar.png", "image/png", 9));

    assertThat(result.fileKey()).startsWith("avatars/").endsWith(".png");
    assertThat(result.fileUrl())
        .isEqualTo("https://minio.test.invalid:9000/bucket-vmt/" + result.fileKey());

    assertThatThrownBy(() -> fileService.uploadAvatarFile(file("avatar.pdf", "application/pdf", 1)))
        .isInstanceOf(FileException.class)
        .hasMessage(FileErrorCode.FILE_EXTENSION_NOT_ALLOWED.message());
  }

  @Test
  void uploadExerciseFileUsesExerciseFolderAndAllowedExtensions() {
    stubPutObject();

    UploadDocumentResult result = fileService.uploadExerciseFile(file("exercise.pdf", "application/pdf", 1, 2));

    assertThat(result.fileKey()).startsWith("exercises/").endsWith(".pdf");
    assertThat(result.fileUrl())
        .isEqualTo("https://minio.test.invalid:9000/bucket-vmt/" + result.fileKey());
  }

  @Test
  void uploadExamFileUsesExamFolderAndAllowedExtensions() {
    stubPutObject();

    UploadDocumentResult result = fileService.uploadExamFile(file("exam.pdf", "application/pdf", 1, 2));

    assertThat(result.fileKey()).startsWith("exams/").endsWith(".pdf");
    assertThat(result.fileUrl())
        .isEqualTo("https://minio.test.invalid:9000/bucket-vmt/" + result.fileKey());
  }

  @Test
  void emptyFileIsRejected() {
    MultipartFile empty = file("empty.pdf", "application/pdf");

    assertThatThrownBy(() -> fileService.uploadDocumentFile(empty))
        .isInstanceOf(FileException.class)
        .hasMessage(FileErrorCode.FILE_EMPTY.message());
  }

  @Test
  void disallowedExtensionIsRejected() {
    MultipartFile virus = file("virus.exe", "application/octet-stream", 1);

    assertThatThrownBy(() -> fileService.uploadDocumentFile(virus))
        .isInstanceOf(FileException.class)
        .hasMessage(FileErrorCode.FILE_EXTENSION_NOT_ALLOWED.message());
  }

  @Test
  void missingExtensionIsRejected() {
    MultipartFile noExtension = file("no_extension_file", "application/octet-stream", 1);

    assertThatThrownBy(() -> fileService.uploadDocumentFile(noExtension))
        .isInstanceOf(FileException.class)
        .hasMessage(FileErrorCode.FILE_EXTENSION_NOT_ALLOWED.message());
  }

  @Test
  void uploadMultipleDocumentsEmptyReturnsEmpty() {
    assertThat(fileService.uploadMultipleDocuments(List.of())).isEmpty();
  }

  @Test
  void uploadMultipleDocumentsUploadsAllFiles() {
    stubPutObject();

    List<UploadDocumentResult> results = fileService.uploadMultipleDocuments(
        List.of(file("a.pdf", "application/pdf", 1), file("b.docx", "application/docx", 2)));

    assertThat(results).hasSize(2);
    assertThat(results).allMatch(r -> r.fileUrl().equals("https://minio.test.invalid:9000/bucket-vmt/" + r.fileKey()));
    verify(s3Client, org.mockito.Mockito.times(2))
        .putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Test
  void uploadMultipleDocumentsMixedFailsOnInvalidFile() {
    stubPutObject();

    assertThatThrownBy(
        () -> fileService.uploadMultipleDocuments(
            List.of(
                file("a.pdf", "application/pdf", 1),
                file("bad.exe", "application/octet-stream", 1))))
        .isInstanceOf(FileException.class)
        .hasMessage(FileErrorCode.FILE_EXTENSION_NOT_ALLOWED.message());
  }

  @Test
  void buildFileUrlReturnsCorrectUrl() {
    String url = fileService.buildFileUrl("documents/test.pdf");
    assertThat(url).isEqualTo("https://minio.test.invalid:9000/bucket-vmt/documents/test.pdf");
  }

  @Test
  void deleteFileDeletesObjectFromS3() {
    fileService.deleteFile("documents/test.pdf");

    ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
    verify(s3Client).deleteObject(captor.capture());
    assertThat(captor.getValue().bucket()).isEqualTo("bucket-vmt");
    assertThat(captor.getValue().key()).isEqualTo("documents/test.pdf");
  }

  @Test
  void deleteFileWithNullOrBlankKeyDoesNothing() {
    fileService.deleteFile(null);
    fileService.deleteFile("   ");

    verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
  }
}
