package studyweb.cus.repository.course;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import studyweb.cus.entity.course.Review;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    
    @Query("SELECT r FROM Review r WHERE r.course.id = :courseId")
    Page<Review> findByCourseId(@Param("courseId") UUID courseId, Pageable pageable);
}
