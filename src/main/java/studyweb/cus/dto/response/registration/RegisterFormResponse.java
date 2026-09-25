package studyweb.cus.dto.response.registration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RegisterFormResponse(
    UUID id,
    String name,
    String phoneNumer,
    String email,
    String subject,
    String note,
    LocalDate registeredDate,
    LocalDateTime createdAt) {}
