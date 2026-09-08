package studyweb.cus.dto.request.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

public record LeaderboardRequest(@NotBlank(message = "Field cannot be empty") String studentName,
        @NotNull(message = "Field is required") UUID courseId, String achievement,
        MultipartFile avatarImage, @NotNull(message = "Field is required") BigDecimal sumScore) {
}