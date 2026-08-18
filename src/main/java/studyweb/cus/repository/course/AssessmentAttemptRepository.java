package studyweb.cus.repository.course;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import studyweb.cus.entity.course.AssessmentAttempt;

public interface AssessmentAttemptRepository extends JpaRepository<AssessmentAttempt, UUID> {

  Page<AssessmentAttempt> findByUserIdAndExamIdOrderByAttemptNumberDesc(
      UUID userId, UUID examId, Pageable pageable);

  int countByUserIdAndExamId(UUID userId, UUID examId);
}
