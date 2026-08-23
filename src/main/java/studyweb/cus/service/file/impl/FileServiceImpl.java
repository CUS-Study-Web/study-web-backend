package studyweb.cus.service.file.impl;

import static studyweb.cus.constant.FileConstants.AVATAR_EXTENSIONS;
import static studyweb.cus.constant.FileConstants.DOCUMENT_EXTENSIONS;
import static studyweb.cus.constant.FileConstants.EXAM_EXTENSIONS;
import static studyweb.cus.constant.FileConstants.EXERCISE_EXTENSIONS;
import static studyweb.cus.constant.FileConstants.FOLDER_AVATARS;
import static studyweb.cus.constant.FileConstants.FOLDER_DOCUMENTS;
import static studyweb.cus.constant.FileConstants.FOLDER_EXAMS;
import static studyweb.cus.constant.FileConstants.FOLDER_EXERCISES;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import studyweb.cus.config.S3Properties;
import studyweb.cus.dto.response.document.UploadDocumentResult;
import studyweb.cus.exception.file.FileErrorCode;
import studyweb.cus.exception.file.FileException;
import studyweb.cus.service.file.FileService;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final S3Properties s3Properties;

  @Override
  public UploadDocumentResult uploadDocumentFile(MultipartFile file) {
    return upload(file, FOLDER_DOCUMENTS, DOCUMENT_EXTENSIONS);
  }

  @Override
  public UploadDocumentResult uploadAvatarFile(MultipartFile file) {
    return upload(file, FOLDER_AVATARS, AVATAR_EXTENSIONS);
  }

  @Override
  public UploadDocumentResult uploadExerciseFile(MultipartFile file) {
    return upload(file, FOLDER_EXERCISES, EXERCISE_EXTENSIONS);
  }

  @Override
  public UploadDocumentResult uploadExamFile(MultipartFile file) {
    return upload(file, FOLDER_EXAMS, EXAM_EXTENSIONS);
  }

  @Override
  public List<UploadDocumentResult> uploadMultipleDocuments(List<MultipartFile> files) {
    return files.stream().map(this::uploadDocumentFile).toList();
  }

  @Override
  public List<UploadDocumentResult> uploadMultipleExercises(List<MultipartFile> files) {
    return files.stream().map(this::uploadExerciseFile).toList();
  }

  @Override
  public List<UploadDocumentResult> uploadMultipleExams(List<MultipartFile> files) {
    return files.stream().map(this::uploadExamFile).toList();
  }

  private UploadDocumentResult upload(
      MultipartFile file, String folder, Set<String> allowedExtensions) {
    if (file.isEmpty()) {
      throw new FileException(FileErrorCode.FILE_EMPTY);
    }
    String extension = extensionOf(file.getOriginalFilename());
    if (extension == null || !allowedExtensions.contains(extension)) {
      throw new FileException(FileErrorCode.FILE_EXTENSION_NOT_ALLOWED);
    }

    String objectName = folder + UUID.randomUUID() + "." + extension;
    try {
      s3Client.putObject(
          PutObjectRequest.builder()
              .bucket(s3Properties.getBucket())
              .key(objectName)
              .contentType(
                  file.getContentType() == null
                      ? "application/octet-stream"
                      : file.getContentType())
              .build(),
          RequestBody.fromBytes(file.getBytes()));
    } catch (Exception e) {
      log.error("Failed to upload file {} to S3", objectName, e);
      throw new FileException(FileErrorCode.UPLOAD_FAILED);
    }

    String fileUrl = generatePresignedUrl(objectName);
    log.info("Uploaded file {} ({} bytes)", objectName, file.getSize());
    return new UploadDocumentResult(file.getSize(), objectName, fileUrl);
  }

  @Override
  public String generatePresignedUrl(String objectKey) {
    try {
      GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(s3Properties.getBucket()).key(objectKey)
          .build();
      GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
          .getObjectRequest(getObjectRequest)
          .signatureDuration(Duration.ofDays(7)).build();
      PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
      return presigned.url().toString();
    } catch (Exception e) {
      log.error("Failed to generate presigned URL for {}", objectKey, e);
      throw new FileException(FileErrorCode.UPLOAD_FAILED);
    }
  }

  private String extensionOf(String fileName) {
    if (fileName == null) {
      return null;
    }
    int dot = fileName.lastIndexOf('.');
    if (dot < 0 || dot == fileName.length() - 1) {
      return null;
    }
    return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
  }

  @Override
  public void deleteFile(String fileKey) {
    if (fileKey == null || fileKey.isBlank()) {
      return;
    }
    try {
      s3Client.deleteObject(DeleteObjectRequest.builder()
          .bucket(s3Properties.getBucket())
          .key(fileKey)
          .build());
      log.info("Deleted file from S3: {}", fileKey);
    } catch (Exception e) {
      log.error("Failed to delete file {} from S3", fileKey, e);
    }
  }
}
