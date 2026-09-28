package studyweb.cus.dto.request.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

public record ReviewRequest(@NotBlank(message = "Field cannot be empty") String studentName,
        @NotNull(message = "Field is required") UUID courseId,
        @NotBlank(message = "Field cannot be empty") String timeText,
        @NotBlank(message = "Field cannot be empty") String comment,
        MultipartFile avatarImage) {
}