package studyweb.cus.dto.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import studyweb.cus.enums.Gender;

@Schema(description = "Request body for updating user profile information")
public record UpdateProfileRequest(
    @Size(max = 150, message = "Name must not exceed 150 characters")
    String name,

    @Pattern(
        regexp = "^$|^0[35789]\\d{8}$",
        message = "Phone number must be blank or a valid 10-digit Vietnamese phone number")
    String phone,

    @Past(message = "Birth date must be in the past")
    LocalDate birth,

    Gender gender,

    @Size(max = 150, message = "School must not exceed 150 characters")
    String school) {}
