package studyweb.cus.repository.course;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import studyweb.cus.entity.course.Subject;

public interface SubjectRepository extends JpaRepository<Subject, UUID> {

  List<Subject> findByCourseIdAndDeletedAtIsNull(UUID courseId);

  long countByCourseIdAndDeletedAtIsNull(UUID courseId);

  Optional<Subject> findByIdAndCourseIdAndDeletedAtIsNull(UUID id, UUID courseId);
}
