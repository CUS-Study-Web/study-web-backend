package studyweb.cus.service.file;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import studyweb.cus.dto.UploadDocumentResult;

public interface FileService {

  UploadDocumentResult uploadDocumentFile(MultipartFile file);

  UploadDocumentResult uploadAvatarFile(MultipartFile file);

  UploadDocumentResult uploadExerciseFile(MultipartFile file);

  UploadDocumentResult uploadExamFile(MultipartFile file);

  List<UploadDocumentResult> uploadMultipleDocuments(List<MultipartFile> files);

  List<UploadDocumentResult> uploadMultipleExercises(List<MultipartFile> files);

  List<UploadDocumentResult> uploadMultipleExams(List<MultipartFile> files);
}
