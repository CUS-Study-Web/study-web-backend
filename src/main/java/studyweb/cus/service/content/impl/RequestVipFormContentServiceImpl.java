package studyweb.cus.service.content.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studyweb.cus.dto.request.content.RequestVipFormContentRequest;
import studyweb.cus.dto.response.content.RequestVipFormContentResponse;
import studyweb.cus.dto.response.document.UploadDocumentResult;
import studyweb.cus.entity.content.RequestVipFormContent;
import studyweb.cus.entity.user.User;
import studyweb.cus.mapper.content.RequestVipFormContentMapper;
import studyweb.cus.repository.content.RequestVipFormContentRepository;
import studyweb.cus.repository.user.UserRepository;
import studyweb.cus.service.content.RequestVipFormContentService;
import studyweb.cus.service.file.FileService;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestVipFormContentServiceImpl implements RequestVipFormContentService {

  private final RequestVipFormContentRepository requestVipFormContentRepository;
  private final RequestVipFormContentMapper requestVipFormContentMapper;
  private final UserRepository userRepository;
  private final FileService fileService;

  @Override
  @Transactional(readOnly = true)
  public RequestVipFormContentResponse getContent() {
    log.info("Fetching VIP request form content");
    RequestVipFormContent content = getOrCreateContent();
    return requestVipFormContentMapper.toResponse(content);
  }

  @Override
  @Transactional
  public RequestVipFormContentResponse updateContent(
      RequestVipFormContentRequest request, String updaterEmail) {
    log.info("Updating VIP request form content by: {}", updaterEmail);
    RequestVipFormContent content = getOrCreateContent();
    String oldQrUrl = content.getAccountHolderQrUrl();
    String newUploadedKey = null;

    requestVipFormContentMapper.updateEntityFromRequest(request, content);

    RequestVipFormContent reqVipFormContentSaved;
    try {
      if (request.accountHolderQr() != null && !request.accountHolderQr().isEmpty()) {
        UploadDocumentResult uploadResult = fileService.uploadQrFile(request.accountHolderQr());
        newUploadedKey = uploadResult.fileKey();
        content.setAccountHolderQrUrl(uploadResult.fileUrl());
      }

      if (updaterEmail != null && !updaterEmail.isBlank()) {
        userRepository.findByGmail(updaterEmail).ifPresent(content::setUpdatedBy);
      }

     reqVipFormContentSaved = requestVipFormContentRepository.save(content);
    } catch (Exception ex) {
      if (newUploadedKey != null) {
        try {
          fileService.deleteFile(newUploadedKey);
        } catch (Exception deleteEx) {
          log.error(
              "Failed to delete newly uploaded QR file {} after database error",
              newUploadedKey,
              deleteEx);
        }
      }
      throw ex;
    }

    if (newUploadedKey != null && oldQrUrl != null && !oldQrUrl.isBlank()) {
      try {
        fileService.deleteFile(oldQrUrl);
      } catch (Exception e) {
        log.warn("Failed to delete old QR code file: {}", oldQrUrl, e);
      }
    }

    log.info("VIP request form content updated successfully");
    return requestVipFormContentMapper.toResponse(reqVipFormContentSaved);
  }

  private RequestVipFormContent getOrCreateContent() {
    return requestVipFormContentRepository
        .findFirstByOrderByCreatedAtDesc()
        .orElseGet(
            () ->
                requestVipFormContentRepository.save(
                    RequestVipFormContent.builder()
                        .formTitle("Đăng ký Nâng cấp Tài khoản VIP")
                        .description("Vui lòng điền thông tin và chuyển khoản theo hướng dẫn.")
                        .build()));
  }
}
