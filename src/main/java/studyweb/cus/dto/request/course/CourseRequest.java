package studyweb.cus.dto.request.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record CourseRequest(
    @NotBlank(message = "Course title is required")
        @Size(max = 255, message = "Course title must not exceed 255 characters")
        String title,
    @Size(max = 255, message = "Subtitle must not exceed 255 characters") String subtitle,
    @Size(max = 255, message = "Badge title must not exceed 255 characters") String badgeTitle,
    @Size(max = 255, message = "Description must not exceed 255 characters") String description,
    @Schema(description = "Thumbnail image", format = "binary") MultipartFile thumbnailImage) {}
