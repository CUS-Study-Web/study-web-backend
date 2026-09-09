package studyweb.cus.dto.request.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;
import studyweb.cus.enums.CtaTarget;

public record UpdateHomepageRequest(
    @Size(max = 255, message = "Badge title must not exceed 255 characters") String badgeTitle,
    @Size(max = 255, message = "Headline 1 must not exceed 255 characters") String headline1,
    @Size(max = 255, message = "Headline 2 must not exceed 255 characters") String headline2,
    @Size(max = 100, message = "Description must not exceed 100 characters") String description,
    @Size(max = 100, message = "CTA button 1 name must not exceed 100 characters")
        String ctaBtn1Name,
    CtaTarget ctaBtn1Target,
    @Size(max = 100, message = "CTA button 2 name must not exceed 100 characters")
        String ctaBtn2Name,
    CtaTarget ctaBtn2Target,
    @Size(max = 50, message = "Stat 1 number must not exceed 50 characters") String stat1Number,
    @Size(max = 255, message = "Stat 1 description must not exceed 255 characters")
        String stat1Desc,
    @Size(max = 50, message = "Stat 2 number must not exceed 50 characters") String stat2Number,
    @Size(max = 255, message = "Stat 2 description must not exceed 255 characters")
        String stat2Desc,
    @Size(max = 50, message = "Student stat description must not exceed 50 characters")
        String studentStatsDesc,
    @Schema(description = "Main cover image file (optional)", format = "binary")
        MultipartFile mainImage,
    @Schema(description = "Student avatar 1 file (optional)", format = "binary")
        MultipartFile student1Avatar,
    @Schema(description = "Student avatar 2 file (optional)", format = "binary")
        MultipartFile student2Avatar,
    @Schema(description = "Student avatar 3 file (optional)", format = "binary")
        MultipartFile student3Avatar) {}
