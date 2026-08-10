package studyweb.cus.service.user;

import studyweb.cus.dto.request.auth.ChangePasswordRequest;
import studyweb.cus.dto.request.auth.RegisterRequest;
import studyweb.cus.dto.response.auth.UserResponse;
import studyweb.cus.entity.user.User;

public interface UserService {

  User createUser(RegisterRequest request);

  UserResponse getCurrentUser(String email);

  void changePassword(String email, ChangePasswordRequest request);
}
