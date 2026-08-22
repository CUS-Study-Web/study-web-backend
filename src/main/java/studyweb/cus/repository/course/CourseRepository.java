package studyweb.cus.repository.course;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import studyweb.cus.entity.course.Course;
import studyweb.cus.exception.course.CourseErrorCode;
import studyweb.cus.exception.course.CourseException;

public interface CourseRepository extends JpaRepository<Course, UUID> {

  Page<Course> findByDeletedAtIsNull(Pageable pageable);

  Optional<Course> findByIdAndDeletedAtIsNull(UUID id);

  default Course requireCourse(UUID id) {
    return findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new CourseException(CourseErrorCode.COURSE_NOT_FOUND));
  }
}
