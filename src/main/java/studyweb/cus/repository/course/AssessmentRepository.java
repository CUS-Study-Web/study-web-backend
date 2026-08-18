package studyweb.cus.repository.course;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import studyweb.cus.entity.course.Assessment;
import studyweb.cus.enums.AssessmentType;

public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {

  Page<Assessment> findByCourseIdAndAssessmentTypeAndDeletedAtIsNull(
      UUID courseId, AssessmentType assessmentType, Pageable pageable);

  Page<Assessment> findBySubjectIdAndAssessmentTypeAndDeletedAtIsNull(
      UUID subjectId, AssessmentType assessmentType, Pageable pageable);

  Optional<Assessment> findByIdAndDeletedAtIsNull(UUID id);
}
