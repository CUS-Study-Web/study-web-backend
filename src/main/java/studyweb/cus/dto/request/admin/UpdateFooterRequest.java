package studyweb.cus.dto.request.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateFooterRequest(
    @Size(max = 255, message = "Company name must not exceed 255 characters")
    String companyName,

    @Size(max = 100, message = "Address name must not exceed 100 characters")
    String address,

    @Size(max = 500, message = "Facebook URL must not exceed 500 characters")
    String facebookUrl,

    @Size(max = 500, message = "Instagram URL must not exceed 500 characters")
    String instagramUrl,

    @Size(max = 500, message = "YouTube URL must not exceed 500 characters")
    String youtubeUrl,

    @Size(max = 500, message = "TikTok URL must not exceed 500 characters")
    String tiktokUrl,

    @Size(max = 50, message = "Phone must not exceed 50 characters")
    String phone,

    @Size(max = 150, message = "Email must not exceed 150 characters")
    String email,

    @Size(max = 255, message = "Website must not exceed 255 characters")
    String website,

    @Size(max = 100, message = "Working hours must not exceed 100 characters")
    String workingHours,

    @Size(max = 255, message = "Copyright text must not exceed 255 characters")
    String copyrightText,

    @Size(max = 500, message = "Privacy URL must not exceed 500 characters")
    String privacyUrl,

    @Size(max = 500, message = "Terms URL must not exceed 500 characters")
    String termsUrl,

    @Valid
    List<FooterLinkItemRequest> links
) {}
