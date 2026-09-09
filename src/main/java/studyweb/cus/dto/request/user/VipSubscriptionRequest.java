package studyweb.cus.dto.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

public record VipSubscriptionRequest(
    @NotBlank(message = "Name is required") String name,
    @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
    @NotNull(message = "Birth date is required")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate birth,
    @NotBlank(message = "Phone number is required") String phone,
    @Schema(description = "Payment evidence image", format = "binary")
        @NotNull(message = "Evidence image is required")
        MultipartFile evidence,
    String note) {}
