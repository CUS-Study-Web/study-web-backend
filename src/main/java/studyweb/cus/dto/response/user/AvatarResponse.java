package studyweb.cus.dto.response.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing the updated avatar URL")
public record AvatarResponse(
    @Schema(description = "URL of the uploaded avatar image", example = "https://storage.googleapis.com/.../avatar.png")
    String avatarUrl) {}
