package study_web.cus.mapper.user;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import study_web.cus.dto.response.auth.UserResponse;
import study_web.cus.entity.user.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserResponse toUserResponse(User user);
}
