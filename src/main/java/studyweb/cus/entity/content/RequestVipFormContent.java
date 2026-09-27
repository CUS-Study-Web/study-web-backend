package studyweb.cus.entity.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import studyweb.cus.entity.AbstractBaseEntity;
import studyweb.cus.entity.user.User;

@Entity
@Table(name = "request_vip_form_content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestVipFormContent extends AbstractBaseEntity {

  @Column(name = "form_title")
  private String formTitle;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "hotline", length = 50)
  private String hotline;

  @Column(name = "fanpage_link", columnDefinition = "TEXT")
  private String fanpageLink;

  @Column(name = "bank_name")
  private String bankName;

  @Column(name = "account_holder")
  private String accountHolder;

  @Column(name = "account_number", length = 100)
  private String accountNumber;

  @Column(name = "transfer_content")
  private String transferContent;

  @Column(name = "account_holder_qr_url", length = 500)
  private String accountHolderQrUrl;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "updated_by")
  private User updatedBy;
}
