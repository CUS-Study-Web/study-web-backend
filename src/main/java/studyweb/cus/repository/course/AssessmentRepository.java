package studyweb.cus.repository.course;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import studyweb.cus.entity.course.Assessment;
import studyweb.cus.enums.AssessmentStatus;
import studyweb.cus.enums.AssessmentType;
import studyweb.cus.exception.assessment.AssessmentErrorCode;
import studyweb.cus.exception.assessment.AssessmentException;

public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {

    Page<Assessment> findByCourseIdAndAssessmentTypeAndDeletedAtIsNull(
            UUID courseId, AssessmentType assessmentType, Pageable pageable);

    long countByCourseIdAndAssessmentTypeAndDeletedAtIsNullAndAccessIn(
            UUID courseId, AssessmentType assessmentType, java.util.Collection<studyweb.cus.enums.AccessTier> accessTiers);

    long countByCourseIdAndAssessmentTypeAndDeletedAtIsNull(
            UUID courseId, AssessmentType assessmentType);

    long countBySubjectIdAndDeletedAtIsNullAndAssessmentTypeAndAccessIn(
            UUID subjectId,
            AssessmentType assessmentType,
            java.util.Collection<studyweb.cus.enums.AccessTier> accessTiers);

    Page<Assessment> findBySubjectIdAndAssessmentTypeAndDeletedAtIsNull(
            UUID subjectId, AssessmentType assessmentType, Pageable pageable);

    Optional<Assessment> findByIdAndDeletedAtIsNull(UUID id);

    List<Assessment> findByStatusAndCreatedAtBefore(
            AssessmentStatus status, LocalDateTime createdAt);

    default Assessment requireAssessment(UUID id) {
        return findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AssessmentException(
                        AssessmentErrorCode.ASSESSMENT_NOT_FOUND));
    }
}
