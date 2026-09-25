package studyweb.cus.entity.registration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import studyweb.cus.entity.AbstractBaseEntity;

@Entity
@Table(
    name = "register_forms",
    indexes = {
      @Index(name = "idx_register_forms_email", columnList = "email"),
      @Index(name = "idx_register_forms_registered_date", columnList = "registered_date"),
      @Index(name = "idx_register_forms_created_at", columnList = "created_at")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterForm extends AbstractBaseEntity {

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "phone_numer", nullable = false, length = 50)
  private String phoneNumer;

  @Column(name = "email", nullable = false)
  private String email;

  @Column(name = "subject")
  private String subject;

  @Column(name = "note", columnDefinition = "TEXT")
  private String note;

  @Column(name = "registered_date", nullable = false)
  @Builder.Default
  private LocalDate registeredDate = LocalDate.now();
}
