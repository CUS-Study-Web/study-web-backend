package studyweb.cus.dto.request.course;

import jakarta.validation.constraints.NotBlank;

import org.springframework.web.multipart.MultipartFile;

public record TeacherProfileRequest(@NotBlank(message = "Field cannot be empty") String name,
                @NotBlank(message = "Field cannot be empty") String description, MultipartFile avatarImage,
                @NotBlank(message = "Field cannot be empty") String subject) {
}