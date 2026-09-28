package studyweb.cus.service.user;

import org.springframework.web.multipart.MultipartFile;
import studyweb.cus.dto.request.auth.ChangePasswordRequest;
import studyweb.cus.dto.request.auth.RegisterRequest;
import studyweb.cus.dto.request.user.UpdateProfileRequest;
import studyweb.cus.dto.request.user.VipSubscriptionRequest;
import studyweb.cus.dto.response.auth.UserResponse;
import studyweb.cus.dto.response.user.AvatarResponse;
import studyweb.cus.dto.response.user.UserProfileResponse;
import studyweb.cus.dto.response.user.VipInfoResponse;
import studyweb.cus.entity.user.User;

public interface UserService {

  User createUser(RegisterRequest request);

  UserResponse getCurrentUser(String email);

  UserProfileResponse getProfile(String email);

  UserProfileResponse updateProfile(String email, UpdateProfileRequest request);

  AvatarResponse uploadAvatar(String email, MultipartFile file);

  void changePassword(String email, ChangePasswordRequest request);

  void createVipRequest(String email, VipSubscriptionRequest request, boolean isRenewal);

  VipInfoResponse getVipInfo(String email);
}
