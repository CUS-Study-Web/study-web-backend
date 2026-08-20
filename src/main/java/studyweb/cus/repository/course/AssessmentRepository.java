package studyweb.cus.repository.course;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import studyweb.cus.entity.course.Assessment;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {

  @Query(
      """
      SELECT a.uploadedBy.id, COUNT(a)
      FROM Assessment a
      WHERE a.uploadedBy.id IN :assistantIds
        AND a.deletedAt IS NULL
      GROUP BY a.uploadedBy.id
      """)
  List<Object[]> countExamsByAssistantIds(@Param("assistantIds") List<UUID> assistantIds);
}
