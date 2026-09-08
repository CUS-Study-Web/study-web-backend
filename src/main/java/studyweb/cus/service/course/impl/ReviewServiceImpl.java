package studyweb.cus.service.course.impl;

import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studyweb.cus.dto.request.course.ReviewRequest;
import studyweb.cus.dto.response.course.ReviewResponse;
import studyweb.cus.entity.course.Course;
import studyweb.cus.entity.course.Review;
import studyweb.cus.mapper.course.ReviewMapper;
import studyweb.cus.repository.course.CourseRepository;
import studyweb.cus.repository.course.ReviewRepository;
import studyweb.cus.service.course.ReviewService;

import studyweb.cus.service.file.FileService;
import studyweb.cus.dto.response.document.UploadDocumentResult;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final CourseRepository courseRepository;
    private final ReviewMapper reviewMapper;
    private final FileService fileService;

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviews(UUID courseId, Pageable pageable) {
        log.info("Fetching reviews with courseId: {}", courseId);
        if (courseId != null) {
            return reviewRepository.findByCourseId(courseId, pageable).map(reviewMapper::toReviewResponse);
        }
        return reviewRepository.findAll(pageable).map(reviewMapper::toReviewResponse);
    }

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {
        log.info("Creating a new review for course ID: {}", request.courseId());
        Review review = reviewMapper.toReview(request);
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
        review.setCourse(course);
        
        if (request.avatarImage() != null && !request.avatarImage().isEmpty()) {
            log.info("Uploading avatar image for new review");
            UploadDocumentResult uploadResult = fileService.uploadAvatarFile(request.avatarImage());
            review.setAvatarUrl(uploadResult.fileUrl());
        }
        
        Review saved = reviewRepository.save(review);
        log.info("Successfully created review with ID: {}", saved.getId());
        return reviewMapper.toReviewResponse(saved);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(UUID id, ReviewRequest request) {
        log.info("Updating review with ID: {}", id);
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));
        
        if (request.studentName() != null && !request.studentName().isBlank()) {
            review.setStudentName(request.studentName());
        }
        if (request.timeText() != null && !request.timeText().isBlank()) {
            review.setTimeText(request.timeText());
        }
        if (request.comment() != null && !request.comment().isBlank()) {
            review.setComment(request.comment());
        }
        if (request.courseId() != null && !request.courseId().equals(review.getCourse().getId())) {
            log.info("Updating course reference for review ID: {}", id);
            Course course = courseRepository.findById(request.courseId())
                    .orElseThrow(() -> new EntityNotFoundException("Course not found"));
            review.setCourse(course);
        }
        
        if (request.avatarImage() != null && !request.avatarImage().isEmpty()) {
            log.info("Uploading new avatar image for review ID: {}", id);
            UploadDocumentResult uploadResult = fileService.uploadAvatarFile(request.avatarImage());
            review.setAvatarUrl(uploadResult.fileUrl());
        }
        
        Review saved = reviewRepository.save(review);
        log.info("Successfully updated review with ID: {}", id);
        return reviewMapper.toReviewResponse(saved);
    }

    @Override
    @Transactional
    public void deleteReview(UUID id) {
        log.info("Deleting review with ID: {}", id);
        if (!reviewRepository.existsById(id)) {
            throw new EntityNotFoundException("Review not found");
        }
        reviewRepository.deleteById(id);
        log.info("Successfully deleted review with ID: {}", id);
    }
}
