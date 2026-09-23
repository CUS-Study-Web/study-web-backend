package studyweb.cus.controller.registration;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studyweb.cus.controller.AbstractBaseController;
import studyweb.cus.dto.base.SingleResponse;
import studyweb.cus.dto.request.registration.RegisterFormRequest;
import studyweb.cus.dto.response.registration.RegisterFormResponse;
import studyweb.cus.service.registration.RegisterFormService;

@RestController
@RequestMapping("/api/register-forms")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Register Form", description = "Endpoints for offline examination registration forms")
public class RegisterFormController extends AbstractBaseController {

  private final RegisterFormService registerFormService;

  @PostMapping({"", "/guest"})
  @Operation(
      summary = "Register form for offline examination",
      description = "Allow guests to submit registration form to examine offline without authentication")
  public ResponseEntity<SingleResponse<RegisterFormResponse>> registerForm(
      @Valid @RequestBody RegisterFormRequest request) {
    log.info("[POST /api/register-forms] Registering form for: {}", request.email());
    RegisterFormResponse response = registerFormService.createRegisterForm(request);
    return successSingle(response, "Registration form submitted successfully!");
  }
}
