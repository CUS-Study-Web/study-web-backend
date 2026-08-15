package studyweb.cus.dto.response.auth;

public record AuthResponse(String accessToken, String refreshToken, UserResponse user) {}
