package study_web.cus.dto.response.auth;

import java.time.LocalDate;
import java.util.UUID;
import study_web.cus.enums.Gender;

public record UserResponse(
    UUID id,
    String gmail,
    String name,
    String phone,
    LocalDate birth,
    Gender gender,
    String school) {}
