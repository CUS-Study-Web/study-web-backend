package studyweb.cus.service.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import studyweb.cus.config.MinioProperties;
import studyweb.cus.dto.UploadDocumentResult;
import studyweb.cus.exception.file.FileErrorCode;
import studyweb.cus.exception.file.FileException;
import studyweb.cus.service.file.impl.FileServiceImpl;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

  @Mock private MinioClient minioClient;

  private final MinioProperties properties = new MinioProperties();

  private FileServiceImpl fileService;

  @BeforeEach
  void setUp() {
    properties.setEndpoint("https://minio1.webtui.vn:9001");
    properties.setBucket("bucket-vmt");
    fileService = new FileServiceImpl(minioClient, properties);
  }

  private void stubPutObject() throws Exception {
    doReturn(null).when(minioClient).putObject(any(PutObjectArgs.class));
  }

  private MockMultipartFile file(String name, String contentType, int... content) {
    byte[] bytes = new byte[content.length];
    for (int i = 0; i < content.length; i++) {
      bytes[i] = (byte) content[i];
    }
    return new MockMultipartFile("file", name, contentType, bytes);
  }

  @Test
  void uploadDocumentFileReturnsSizeAndPublicUrl() throws Exception {
    stubPutObject();

    UploadDocumentResult result =
        fileService.uploadDocumentFile(file("lesson.pdf", "application/pdf", 1, 2, 3));

    assertThat(result.fileSize()).isEqualTo(3L);
    assertThat(result.fileUrl())
        .startsWith("https://minio1.webtui.vn:9001/bucket-vmt/documents/")
        .endsWith(".pdf");

    ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
    verify(minioClient).putObject(captor.capture());
    assertThat(captor.getValue().bucket()).isEqualTo("bucket-vmt");
    assertThat(captor.getValue().object()).startsWith("documents/").endsWith(".pdf");
    assertThat(captor.getValue().objectSize()).isEqualTo(3L);
  }

  @Test
  void uploadAvatarFileUsesAvatarFolderAndImageExtensions() throws Exception {
    stubPutObject();

    UploadDocumentResult result = fileService.uploadAvatarFile(file("avatar.png", "image/png", 9));

    assertThat(result.fileUrl()).startsWith("https://minio1.webtui.vn:9001/bucket-vmt/avatars/");

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
  void uploadMultipleDocumentsEmptyReturnsEmpty() {
    assertThat(fileService.uploadMultipleDocuments(List.of())).isEmpty();
  }

  @Test
  void uploadMultipleDocumentsUploadsAllFiles() throws Exception {
    stubPutObject();

    List<UploadDocumentResult> results =
        fileService.uploadMultipleDocuments(
            List.of(file("a.pdf", "application/pdf", 1), file("b.docx", "application/docx", 2)));

    assertThat(results).hasSize(2);
    assertThat(results)
        .allMatch(
            r -> r.fileUrl().startsWith("https://minio1.webtui.vn:9001/bucket-vmt/documents/"));
    verify(minioClient, org.mockito.Mockito.times(2)).putObject(any(PutObjectArgs.class));
  }

  @Test
  void uploadMultipleDocumentsMixedFailsOnInvalidFile() throws Exception {
    stubPutObject();

    assertThatThrownBy(
            () ->
                fileService.uploadMultipleDocuments(
                    List.of(
                        file("a.pdf", "application/pdf", 1),
                        file("bad.exe", "application/octet-stream", 1))))
        .isInstanceOf(FileException.class)
        .hasMessage(FileErrorCode.FILE_EXTENSION_NOT_ALLOWED.message());
  }
}
