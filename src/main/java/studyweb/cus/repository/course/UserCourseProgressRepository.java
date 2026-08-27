package studyweb.cus.repository.course;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import studyweb.cus.entity.progress.UserCourseProgress;

public interface UserCourseProgressRepository extends JpaRepository<UserCourseProgress, UUID> {
    Optional<UserCourseProgress> findByUserIdAndCourseId(UUID userId, UUID courseId);
}
