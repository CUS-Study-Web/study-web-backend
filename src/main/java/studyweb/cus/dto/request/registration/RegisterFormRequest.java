package studyweb.cus.dto.request.registration;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record RegisterFormRequest(
    @Schema(description = "Full name of the registrant", example = "Nguyen Van A")
        @NotBlank(message = "Name is required")
        String name,
    @Schema(description = "Phone number of the registrant", example = "0987654321")
        @NotBlank(message = "Phone number is required")
        @Pattern(
            regexp = "^(?:\\+84|0)[35789]\\d{8}$",
            message = "Phone number must be a valid Vietnamese phone number (e.g. 0987654321)")
        @JsonAlias({"phoneNumber", "phone", "phone_number"})
        String phoneNumer,
    @Schema(description = "Email address of the registrant", example = "nguyenvana@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,
    @Schema(description = "Subject for offline examination", example = "Toán học")
        String subject,
    @Schema(description = "Additional notes", example = "Đăng ký thi ca sáng thứ 7")
        String note,
    @Schema(description = "Registered date (defaults to current date if omitted)", example = "2026-09-23")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @JsonAlias({"registeredDate", "registered_date"})
        LocalDate registeredDate) {}
