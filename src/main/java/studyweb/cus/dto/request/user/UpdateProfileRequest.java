package studyweb.cus.dto.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import studyweb.cus.enums.Gender;

@Schema(description = "Request body for updating user profile information")
public record UpdateProfileRequest(
    @Size(max = 150, message = "Name must not exceed 150 characters")
    String name,

    @Pattern(regexp = "^$|^\\d{10}$", message = "Phone number must be blank or exactly 10 digits")
    String phone,

    LocalDate birth,

    Gender gender,

    @Size(max = 150, message = "School must not exceed 150 characters")
    String school) {}
