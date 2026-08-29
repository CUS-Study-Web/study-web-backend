package studyweb.cus.dto.request.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import studyweb.cus.enums.UserTier;

public record UpdateAccountRequest(
    String name,
    @Email(message = "Invalid email format") String gmail,
    LocalDate startDate,
    LocalDate endDate,
    String note,
    UserTier tier,
    @Pattern(
            regexp = "^$|^.{8,}$",
            message = "Password must be blank or at least 8 characters long")
        String password) {}
