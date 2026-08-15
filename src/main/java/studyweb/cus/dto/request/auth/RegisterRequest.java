package studyweb.cus.dto.request.auth;

import java.time.LocalDate;
import studyweb.cus.enums.Gender;

public record RegisterRequest(
    String gmail,
    String name,
    String phone,
    LocalDate birth,
    Gender gender,
    String school,
    String password) {}
