package studyweb.cus.repository.course;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import studyweb.cus.entity.progress.UserLessonProgress;

@Repository
public interface UserLessonProgressRepository extends JpaRepository<UserLessonProgress, UUID> {
    Optional<UserLessonProgress> findByUserIdAndLessonId(UUID userId, UUID lessonId);

    long countByUserIdAndLesson_Subject_IdAndLesson_DeletedAtIsNullAndIsClickedTrue(UUID userId, UUID subjectId);

    List<UserLessonProgress> findByUserIdAndLessonIdIn(UUID userId, Collection<UUID> lessonIds);
}
