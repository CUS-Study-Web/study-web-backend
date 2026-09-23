package studyweb.cus.service.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import studyweb.cus.dto.request.content.RequestVipFormContentRequest;
import studyweb.cus.dto.response.content.RequestVipFormContentResponse;
import studyweb.cus.dto.response.document.UploadDocumentResult;
import studyweb.cus.entity.content.RequestVipFormContent;
import studyweb.cus.entity.user.User;
import studyweb.cus.mapper.content.RequestVipFormContentMapper;
import studyweb.cus.repository.content.RequestVipFormContentRepository;
import studyweb.cus.repository.user.UserRepository;
import studyweb.cus.service.content.impl.RequestVipFormContentServiceImpl;
import studyweb.cus.service.file.FileService;

@ExtendWith(MockitoExtension.class)
class RequestVipFormContentServiceImplTest {

  @Mock private RequestVipFormContentRepository requestVipFormContentRepository;
  @Mock private UserRepository userRepository;
  @Mock private FileService fileService;

  private final RequestVipFormContentMapper requestVipFormContentMapper =
      Mappers.getMapper(RequestVipFormContentMapper.class);

  private RequestVipFormContentService requestVipFormContentService;

  @BeforeEach
  void setUp() {
    requestVipFormContentService =
        new RequestVipFormContentServiceImpl(
            requestVipFormContentRepository, requestVipFormContentMapper, userRepository, fileService);
  }

  @Test
  @DisplayName("getContent returns latest content when present")
  void getContent_existingContent() {
    RequestVipFormContent content =
        RequestVipFormContent.builder()
            .formTitle("Đăng ký VIP")
            .hotline("0901234567")
            .bankName("MB Bank")
            .accountNumber("123456")
            .build();
    content.setId(UUID.randomUUID());

    when(requestVipFormContentRepository.findFirstByOrderByCreatedAtDesc())
        .thenReturn(Optional.of(content));

    RequestVipFormContentResponse response = requestVipFormContentService.getContent();

    assertThat(response).isNotNull();
    assertThat(response.formTitle()).isEqualTo("Đăng ký VIP");
    assertThat(response.bankName()).isEqualTo("MB Bank");
  }

  @Test
  @DisplayName("updateContent updates fields and sets updatedBy")
  void updateContent_success() {
    RequestVipFormContent existing =
        RequestVipFormContent.builder().formTitle("Old Title").build();
    existing.setId(UUID.randomUUID());

    UUID adminId = UUID.randomUUID();
    User admin = User.builder().gmail("admin@studyweb.edu").build();
    admin.setId(adminId);

    when(requestVipFormContentRepository.findFirstByOrderByCreatedAtDesc())
        .thenReturn(Optional.of(existing));
    when(userRepository.findByGmail("admin@studyweb.edu")).thenReturn(Optional.of(admin));
    when(requestVipFormContentRepository.save(any(RequestVipFormContent.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    RequestVipFormContentRequest request =
        new RequestVipFormContentRequest(
            "New VIP Form Title",
            "New Description",
            "0988776655",
            "https://facebook.com/new",
            "Vietcombank",
            "NGUYEN VAN A",
            "987654321",
            "VIP CK");

    RequestVipFormContentResponse response =
        requestVipFormContentService.updateContent(request, "admin@studyweb.edu");

    assertThat(response).isNotNull();
    assertThat(response.formTitle()).isEqualTo("New VIP Form Title");
    assertThat(response.bankName()).isEqualTo("Vietcombank");
    assertThat(response.updatedBy()).isEqualTo(adminId);

    verify(requestVipFormContentRepository).save(any(RequestVipFormContent.class));
  }

  @Test
  @DisplayName("updateContent with QR file uploads to FileService and deletes old QR")
  void updateContent_withQrFile_uploadsAndDeletesOld() {
    String oldQrUrl = "https://s3.example.com/avatars/old-qr.png";
    RequestVipFormContent existing =
        RequestVipFormContent.builder()
            .formTitle("Old Title")
            .accountHolderQrUrl(oldQrUrl)
            .build();
    existing.setId(UUID.randomUUID());

    when(requestVipFormContentRepository.findFirstByOrderByCreatedAtDesc())
        .thenReturn(Optional.of(existing));
    when(requestVipFormContentRepository.save(any(RequestVipFormContent.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    MockMultipartFile qrFile =
        new MockMultipartFile(
            "accountHolderQr", "qr.png", "image/png", new byte[] {1, 2, 3});
    UploadDocumentResult uploadResult =
        new UploadDocumentResult(3L, "avatars/new-qr.png", "https://s3.example.com/avatars/new-qr.png");
    when(fileService.uploadAvatarFile(qrFile)).thenReturn(uploadResult);

    RequestVipFormContentRequest request =
        new RequestVipFormContentRequest(
            "VIP Title", null, null, null, null, null, null, null, qrFile);

    RequestVipFormContentResponse response =
        requestVipFormContentService.updateContent(request, null);

    assertThat(response).isNotNull();
    assertThat(response.accountHolderQrUrl()).isEqualTo("https://s3.example.com/avatars/new-qr.png");

    verify(fileService).uploadAvatarFile(qrFile);
    verify(fileService).deleteFile(oldQrUrl);
  }
}
