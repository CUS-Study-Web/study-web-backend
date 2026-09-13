package studyweb.cus.dto.response.content;

import java.util.List;

public record FooterContentResponse(
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
    List<FooterLinkItemResponse> links) {}
