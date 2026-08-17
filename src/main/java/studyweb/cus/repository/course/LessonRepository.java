package studyweb.cus.repository.course;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import studyweb.cus.entity.course.Lesson;
import studyweb.cus.enums.AccessTier;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {

  Page<Lesson> findBySubjectIdAndDeletedAtIsNullAndAccessIn(
      UUID subjectId, Collection<AccessTier> accessTiers, Pageable pageable);

  long countBySubjectIdAndDeletedAtIsNull(UUID subjectId);

  Optional<Lesson> findByIdAndSubjectIdAndDeletedAtIsNull(UUID id, UUID subjectId);
}
