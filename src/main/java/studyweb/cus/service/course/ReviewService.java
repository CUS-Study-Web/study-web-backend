package studyweb.cus.service.course;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import studyweb.cus.dto.request.course.ReviewRequest;
import studyweb.cus.dto.response.course.ReviewResponse;

public interface ReviewService {
    Page<ReviewResponse> getReviews(UUID courseId, Pageable pageable);
    ReviewResponse createReview(ReviewRequest request);
    ReviewResponse updateReview(UUID id, ReviewRequest request);
    void deleteReview(UUID id);
}
