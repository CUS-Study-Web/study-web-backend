package studyweb.cus.service.course;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import studyweb.cus.dto.request.course.TeacherProfileRequest;
import studyweb.cus.dto.response.course.TeacherProfileResponse;

public interface TeacherProfileService {
    Page<TeacherProfileResponse> getTeachers(Pageable pageable);

    TeacherProfileResponse createTeacher(TeacherProfileRequest request);

    TeacherProfileResponse updateTeacher(UUID id, TeacherProfileRequest request);

    void deleteTeacher(UUID id);
}
