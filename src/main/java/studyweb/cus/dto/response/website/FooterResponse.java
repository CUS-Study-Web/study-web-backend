package studyweb.cus.dto.response.website;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record FooterResponse(
    UUID id,
    String companyName,
    String address,
    String facebookUrl,
    String instagramUrl,
    String youtubeUrl,
    String tiktokUrl,
    String phone,
    String email,
    String website,
    String workingHours,
    String copyrightText,
    String privacyUrl,
    String termsUrl,
    List<FooterLinkResponse> links,
    String updatedByEmail,
    LocalDateTime updatedAt
) {}
