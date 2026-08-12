package studyweb.cus.dto.response.auth;

import java.time.LocalDate;
import java.util.UUID;
import studyweb.cus.enums.Gender;

public record UserResponse(
        UUID id,
        String gmail,
        String name,
        String phone,
        LocalDate birth,
        Gender gender,
        String school) {
}
