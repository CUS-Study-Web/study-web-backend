package study_web.cus.dto.response.auth;

import java.time.LocalDate;
import java.util.UUID;
import study_web.cus.enums.Gender;
import study_web.cus.enums.Role;

public record UserResponse(UUID id, String gmail, String name, String phone, LocalDate birth, Gender gender,
        String school, Role role) {
}
