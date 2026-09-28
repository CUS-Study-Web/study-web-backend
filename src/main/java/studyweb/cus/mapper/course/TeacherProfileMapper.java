package studyweb.cus.mapper.course;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import studyweb.cus.dto.request.course.TeacherProfileRequest;
import studyweb.cus.dto.response.course.TeacherProfileResponse;
import studyweb.cus.entity.course.TeacherProfile;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeacherProfileMapper {

    TeacherProfileResponse toTeacherProfileResponse(TeacherProfile entity);

    TeacherProfile toTeacherProfile(TeacherProfileRequest dto);

    void updateTeacherProfileFromDto(TeacherProfileRequest dto, @MappingTarget TeacherProfile entity);
}
