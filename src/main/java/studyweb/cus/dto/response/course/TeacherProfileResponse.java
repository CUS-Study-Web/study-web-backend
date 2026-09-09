package studyweb.cus.dto.response.course;

import java.util.UUID;

public record TeacherProfileResponse(UUID id, String name, String description,
        String avatarUrl, String subject) {
}