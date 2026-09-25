package studyweb.cus.dto.request.content;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

public record RequestVipFormContentRequest(
    @Schema(description = "Form title", example = "Đăng ký Nâng cấp Tài khoản VIP")
        String formTitle,
    @Schema(
            description = "Form description or instructions",
            example = "Vui lòng điền thông tin và chuyển khoản...")
        String description,
    @Schema(description = "Support hotline", example = "0901234567") String hotline,
    @Schema(description = "Support fanpage URL", example = "https://facebook.com/studyweb")
        String fanpageLink,
    @Schema(description = "Bank name", example = "MB Bank") String bankName,
    @Schema(description = "Account holder name", example = "CONG TY TNHH STUDYWEB")
        String accountHolder,
    @Schema(description = "Bank account number", example = "999988886666") String accountNumber,
    @Schema(description = "Transfer content syntax", example = "VIP [EMAIL] [SDT]")
        String transferContent,
    @Schema(description = "Account holder QR code image file (optional)", format = "binary")
        MultipartFile accountHolderQr) {

  public RequestVipFormContentRequest(
      String formTitle,
      String description,
      String hotline,
      String fanpageLink,
      String bankName,
      String accountHolder,
      String accountNumber,
      String transferContent) {
    this(
        formTitle,
        description,
        hotline,
        fanpageLink,
        bankName,
        accountHolder,
        accountNumber,
        transferContent,
        null);
  }
}
