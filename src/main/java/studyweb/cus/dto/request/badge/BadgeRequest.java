package studyweb.cus.dto.request.badge;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BadgeRequest(
    @Schema(description = "Badge name", example = "Toán")
        @NotBlank(message = "Badge name is required")
        @Size(max = 255, message = "Badge name must not exceed 255 characters")
        String name) {}
