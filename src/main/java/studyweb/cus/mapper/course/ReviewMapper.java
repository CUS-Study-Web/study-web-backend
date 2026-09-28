package studyweb.cus.mapper.course;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import studyweb.cus.dto.request.course.ReviewRequest;
import studyweb.cus.dto.response.course.ReviewResponse;
import studyweb.cus.entity.course.Review;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ReviewMapper {
    @Mapping(target = "courseId", source = "course.id")
    ReviewResponse toReviewResponse(Review review);

    @Mapping(target = "course", ignore = true)
    Review toReview(ReviewRequest dto);

    @Mapping(target = "course", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateReviewFromDto(ReviewRequest dto, @MappingTarget Review entity);
}
