package studyweb.cus.service.registration;

import studyweb.cus.dto.request.registration.RegisterFormRequest;
import studyweb.cus.dto.response.registration.RegisterFormResponse;

public interface RegisterFormService {

  RegisterFormResponse createRegisterForm(RegisterFormRequest request);
}
