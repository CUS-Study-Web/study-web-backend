package studyweb.cus.service.file.impl;

import static studyweb.cus.constant.FileConstants.AVATAR_EXTENSIONS;
import static studyweb.cus.constant.FileConstants.DOCUMENT_EXTENSIONS;
import static studyweb.cus.constant.FileConstants.EXAM_EXTENSIONS;
import static studyweb.cus.constant.FileConstants.EXERCISE_EXTENSIONS;
import static studyweb.cus.constant.FileConstants.FOLDER_AVATARS;
import static studyweb.cus.constant.FileConstants.FOLDER_DOCUMENTS;
import static studyweb.cus.constant.FileConstants.FOLDER_EXAMS;
import static studyweb.cus.constant.FileConstants.FOLDER_EXERCISES;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import studyweb.cus.config.MinioProperties;
import studyweb.cus.dto.UploadDocumentResult;
import studyweb.cus.exception.file.FileErrorCode;
import studyweb.cus.exception.file.FileException;
import studyweb.cus.service.file.FileService;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

  private final MinioClient minioClient;
  private final MinioProperties minioProperties;

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
    try (InputStream inputStream = file.getInputStream()) {
      minioClient.putObject(
          PutObjectArgs.builder().bucket(minioProperties.getBucket()).object(objectName).stream(
              inputStream, file.getSize(), -1)
              .contentType(
                  file.getContentType() == null
                      ? "application/octet-stream"
                      : file.getContentType())
              .build());
    } catch (Exception e) {
      log.error("Failed to upload file {} to MinIO", objectName, e);
      throw new FileException(FileErrorCode.UPLOAD_FAILED);
    }

    String fileUrl = presignedUrl(objectName);
    log.info("Uploaded file {} ({} bytes)", objectName, file.getSize());
    return new UploadDocumentResult(file.getSize(), fileUrl);
  }

  private String presignedUrl(String objectName) {
    try {
      return minioClient.getPresignedObjectUrl(
          GetPresignedObjectUrlArgs.builder()
              .method(Method.GET)
              .bucket(minioProperties.getBucket())
              .object(objectName)
              .build());
    } catch (Exception e) {
      log.error("Failed to generate presigned URL for {}", objectName, e);
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
}
