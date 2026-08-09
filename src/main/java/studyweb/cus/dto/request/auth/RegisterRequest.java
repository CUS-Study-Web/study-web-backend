package study_web.cus.dto.request.auth;

import java.time.LocalDate;
import study_web.cus.enums.Gender;

public record RegisterRequest(
    String gmail,
    String name,
    String phone,
    LocalDate birth,
    Gender gender,
    String school,
    String password) {}
