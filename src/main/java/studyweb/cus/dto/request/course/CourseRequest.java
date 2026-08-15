package studyweb.cus.dto.request.course;

import jakarta.validation.constraints.NotBlank;

public record CourseRequest(
    @NotBlank(message = "Course title is required") String title,
    String subtitle,
    String badgeTitle,
    String description,
    String thumbnailUrl) {}
