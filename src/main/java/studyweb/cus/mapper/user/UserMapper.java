package studyweb.cus.mapper.user;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import studyweb.cus.dto.response.auth.UserResponse;
import studyweb.cus.dto.response.user.UserProfileResponse;
import studyweb.cus.entity.user.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

  UserResponse toUserResponse(User user);

  UserProfileResponse toUserProfileResponse(User user);
}
