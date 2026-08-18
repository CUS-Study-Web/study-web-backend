package studyweb.cus.repository.course;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import studyweb.cus.entity.course.AssessmentAttempt;

public interface AssessmentAttemptRepository extends JpaRepository<AssessmentAttempt, UUID> {
  @Query(
      """
      SELECT aa FROM AssessmentAttempt aa
      JOIN FETCH aa.exam a
      WHERE aa.user.id IN :userIds
        AND aa.score IS NOT NULL
      """)
  List<AssessmentAttempt> findAllByUserIdsWithExam(@Param("userIds") List<UUID> userIds);
}
