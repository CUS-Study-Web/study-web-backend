package studyweb.cus.dto.request.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateAssistantRequest(
    @NotBlank(message = "Name is required") String name,
    @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String gmail,
    @Pattern(regexp = "^$|^\\d{10}$", message = "Phone number must be blank or exactly 10 digits")
        String phone,
    @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must contain 8 characters") String password) {}
