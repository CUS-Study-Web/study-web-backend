package studyweb.cus.service.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
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
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import studyweb.cus.config.S3Properties;
import studyweb.cus.dto.response.document.UploadDocumentResult;
import studyweb.cus.exception.file.FileErrorCode;
import studyweb.cus.exception.file.FileException;
import studyweb.cus.service.file.impl.FileServiceImpl;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

  private static final String SIGNED_URL = "https://minio.test.invalid:9000/bucket-vmt/signed?sig";

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

  private void stubPresignedUrl() throws Exception {
    PresignedGetObjectRequest presigned = org.mockito.Mockito.mock(PresignedGetObjectRequest.class);
    when(presigned.url()).thenReturn(URI.create(SIGNED_URL).toURL());
    when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presigned);
  }

  private MockMultipartFile file(String name, String contentType, int... content) {
    byte[] bytes = new byte[content.length];
    for (int i = 0; i < content.length; i++) {
      bytes[i] = (byte) content[i];
    }
    return new MockMultipartFile("file", name, contentType, bytes);
  }

  @Test
  void uploadDocumentFileReturnsSizeAndPresignedUrl() throws Exception {
    stubPutObject();
    stubPresignedUrl();

    UploadDocumentResult result = fileService.uploadDocumentFile(file("lesson.pdf", "application/pdf", 1, 2, 3));

    assertThat(result.fileSize()).isEqualTo(3L);
    assertThat(result.fileUrl()).isEqualTo(SIGNED_URL);

    ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
    ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
    verify(s3Client).putObject(captor.capture(), bodyCaptor.capture());
    assertThat(captor.getValue().bucket()).isEqualTo("bucket-vmt");
    assertThat(captor.getValue().key()).startsWith("documents/").endsWith(".pdf");
    assertThat(captor.getValue().contentType()).isEqualTo("application/pdf");
    assertThat(bodyCaptor.getValue().contentLength()).isEqualTo(3L);

    ArgumentCaptor<GetObjectPresignRequest> urlCaptor = ArgumentCaptor.forClass(GetObjectPresignRequest.class);
    verify(s3Presigner).presignGetObject(urlCaptor.capture());
    GetObjectRequest getRequest = urlCaptor.getValue().getObjectRequest();
    assertThat(getRequest.bucket()).isEqualTo("bucket-vmt");
    assertThat(getRequest.key()).startsWith("documents/").endsWith(".pdf");
  }

  @Test
  void uploadAvatarFileUsesAvatarFolderAndImageExtensions() throws Exception {
    stubPutObject();
    stubPresignedUrl();

    UploadDocumentResult result = fileService.uploadAvatarFile(file("avatar.png", "image/png", 9));

    assertThat(result.fileUrl()).isEqualTo(SIGNED_URL);

    assertThatThrownBy(() -> fileService.uploadAvatarFile(file("avatar.pdf", "application/pdf", 1)))
        .isInstanceOf(FileException.class)
        .hasMessage(FileErrorCode.FILE_EXTENSION_NOT_ALLOWED.message());
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
  void uploadMultipleDocumentsUploadsAllFiles() throws Exception {
    stubPutObject();
    stubPresignedUrl();

    List<UploadDocumentResult> results = fileService.uploadMultipleDocuments(
        List.of(file("a.pdf", "application/pdf", 1), file("b.docx", "application/docx", 2)));

    assertThat(results).hasSize(2);
    assertThat(results).allMatch(r -> r.fileUrl().equals(SIGNED_URL));
    verify(s3Client, org.mockito.Mockito.times(2))
        .putObject(any(PutObjectRequest.class), any(RequestBody.class));
    verify(s3Presigner, org.mockito.Mockito.times(2))
        .presignGetObject(any(GetObjectPresignRequest.class));
  }

  @Test
  void uploadMultipleDocumentsMixedFailsOnInvalidFile() throws Exception {
    stubPutObject();
    stubPresignedUrl();

    assertThatThrownBy(
        () -> fileService.uploadMultipleDocuments(
            List.of(
                file("a.pdf", "application/pdf", 1),
                file("bad.exe", "application/octet-stream", 1))))
        .isInstanceOf(FileException.class)
        .hasMessage(FileErrorCode.FILE_EXTENSION_NOT_ALLOWED.message());
  }
}
