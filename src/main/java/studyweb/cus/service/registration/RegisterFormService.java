package studyweb.cus.service.registration;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import studyweb.cus.dto.request.registration.RegisterFormRequest;
import studyweb.cus.dto.response.registration.RegisterFormResponse;

public interface RegisterFormService {

  RegisterFormResponse createRegisterForm(RegisterFormRequest request);

  Page<RegisterFormResponse> listRegisterForms(LocalDate date, String search, Pageable pageable);

  long countRegisterForms(LocalDate date, String search);

  RegisterFormResponse getRegisterFormById(UUID id);
}
