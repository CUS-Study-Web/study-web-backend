package study_web.cus.service.user;

import study_web.cus.dto.request.auth.ChangePasswordRequest;
import study_web.cus.dto.request.auth.RegisterRequest;
import study_web.cus.dto.response.auth.UserResponse;
import study_web.cus.entity.user.User;

public interface UserService {

    User createUser(RegisterRequest request);

    UserResponse getCurrentUser(String email);

    void changePassword(String email, ChangePasswordRequest request);
}
