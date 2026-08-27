package studyweb.cus.repository.course;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import studyweb.cus.entity.progress.UserSubjectProgress;

@Repository
public interface UserSubjectProgressRepository extends JpaRepository<UserSubjectProgress, UUID> {
  Optional<UserSubjectProgress> findByUserIdAndSubjectId(UUID userId, UUID subjectId);

  List<UserSubjectProgress> findByUserIdAndSubjectIdIn(UUID userId, Collection<UUID> subjectIds);

  @Query(
      """
                SELECT COALESCE(AVG(s.progressPercent), 0)
                FROM UserSubjectProgress s
                WHERE s.user.id = :userId
                  AND s.subject.course.id = :courseId
            """)
  Double avgSubjectPercentByUserIdAndCourseId(
      @Param("userId") UUID userId, @Param("courseId") UUID courseId);
}
