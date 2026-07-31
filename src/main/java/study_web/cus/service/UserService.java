package study_web.cus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study_web.cus.dto.request.auth.RegisterRequest;
import study_web.cus.entity.user.User;
import study_web.cus.enums.Role;
import study_web.cus.exception.auth.AuthErrorCode;
import study_web.cus.exception.auth.AuthException;
import study_web.cus.repository.user.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(RegisterRequest request) {
        if (request.password().length() < MIN_PASSWORD_LENGTH) {
            throw new AuthException(AuthErrorCode.INVALID_PASSWORD);
        }
        if (userRepository.existsByGmail(request.gmail())) {
            throw new AuthException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .gmail(request.gmail())
                .name(request.name())
                .phone(request.phone())
                .birth(request.birth())
                .gender(request.gender())
                .school(request.school())
                .role(Role.LEARNER)
                .password(passwordEncoder.encode(request.password()))
                .build();
        return userRepository.save(user);
    }
}
