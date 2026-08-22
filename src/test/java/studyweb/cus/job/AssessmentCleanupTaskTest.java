package studyweb.cus.job;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import studyweb.cus.entity.course.Assessment;
import studyweb.cus.enums.AssessmentStatus;
import studyweb.cus.repository.course.AssessmentRepository;
import studyweb.cus.service.file.FileService;

@ExtendWith(MockitoExtension.class)
class AssessmentCleanupTaskTest {

  @Mock private AssessmentRepository assessmentRepository;

  @Mock private FileService fileService;

  @InjectMocks private AssessmentCleanupTask task;

  @Test
  void cleanupOrphanAssessments_withNoOrphans_doesNothing() {
    when(assessmentRepository.findByStatusAndCreatedAtBefore(
            eq(AssessmentStatus.PENDING_UPLOAD), any()))
        .thenReturn(List.of());

    task.cleanupOrphanAssessments();

    verify(fileService, never()).deleteFile(any());
    verify(assessmentRepository, never()).delete(any());
  }

  @Test
  void cleanupOrphanAssessments_withOrphans_deletesFromS3AndDb() {
    Assessment a1 = new Assessment();
    a1.setId(UUID.randomUUID());
    a1.setFileKey("key1");

    Assessment a2 = new Assessment();
    a2.setId(UUID.randomUUID());
    a2.setFileKey("key2");

    when(assessmentRepository.findByStatusAndCreatedAtBefore(
            eq(AssessmentStatus.PENDING_UPLOAD), any()))
        .thenReturn(List.of(a1, a2));

    task.cleanupOrphanAssessments();

    verify(fileService).deleteFile("key1");
    verify(fileService).deleteFile("key2");
    verify(assessmentRepository).delete(a1);
    verify(assessmentRepository).delete(a2);
  }

  @Test
  void cleanupOrphanAssessments_whenS3Fails_catchesExceptionAndSkipsDbDeletion() {
    Assessment a1 = new Assessment();
    a1.setId(UUID.randomUUID());
    a1.setFileKey("key1");

    Assessment a2 = new Assessment();
    a2.setId(UUID.randomUUID());
    a2.setFileKey("key2");

    when(assessmentRepository.findByStatusAndCreatedAtBefore(
            eq(AssessmentStatus.PENDING_UPLOAD), any()))
        .thenReturn(List.of(a1, a2));

    doThrow(new RuntimeException("S3 Error")).when(fileService).deleteFile("key1");

    task.cleanupOrphanAssessments();

    verify(fileService).deleteFile("key1");
    verify(fileService).deleteFile("key2");

    // a1 shouldn't be deleted from DB because S3 deletion failed
    verify(assessmentRepository, never()).delete(a1);
    // a2 should be deleted normally
    verify(assessmentRepository).delete(a2);
  }

  @Test
  void cleanupOrphanAssessments_withNullUrl_deletesOnlyDb() {
    Assessment a1 = new Assessment();
    a1.setId(UUID.randomUUID());
    a1.setFileKey(null); // File upload failed completely, so no S3 URL

    when(assessmentRepository.findByStatusAndCreatedAtBefore(
            eq(AssessmentStatus.PENDING_UPLOAD), any()))
        .thenReturn(List.of(a1));

    task.cleanupOrphanAssessments();

    verify(fileService, never()).deleteFile(any());
    verify(assessmentRepository).delete(a1);
  }
}
