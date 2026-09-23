package studyweb.cus.service.registration;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import studyweb.cus.dto.request.registration.RegisterFormRequest;
import studyweb.cus.dto.response.registration.RegisterFormResponse;

public interface RegisterFormService {

  RegisterFormResponse createRegisterForm(RegisterFormRequest request);

  Page<RegisterFormResponse> listRegisterForms(String search, Pageable pageable);

  RegisterFormResponse getRegisterFormById(UUID id);
}
