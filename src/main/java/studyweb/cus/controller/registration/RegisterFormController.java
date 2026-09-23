package studyweb.cus.controller.registration;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import studyweb.cus.controller.AbstractBaseController;
import studyweb.cus.dto.base.PageResponse;
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

  @PostMapping("/guest")
  @Operation(
      summary = "Register form for offline examination",
      description =
          "Allow guests to submit registration form to examine offline without authentication")
  public ResponseEntity<SingleResponse<RegisterFormResponse>> registerForm(
      @Valid @RequestBody RegisterFormRequest request) {
    log.info("[POST /api/register-forms/guest] Registering form for: {}", request.email());
    RegisterFormResponse response = registerFormService.createRegisterForm(request);
    return successSingle(response, "Registration form submitted successfully!");
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'ASSISTANT')")
  @Operation(
      summary = "List register forms",
      description =
          "List all offline exam registration forms with optional search and pagination (Admin and Assistant only)")
  public ResponseEntity<PageResponse<RegisterFormResponse>> listRegisterForms(
      @RequestParam(required = false) String search,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    log.info(
        "[GET /api/register-forms] search='{}', page={}, size={}",
        search,
        pageable.getPageNumber(),
        pageable.getPageSize());
    return paging(
        registerFormService.listRegisterForms(search, pageable),
        "Register forms fetched successfully!");
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'ASSISTANT')")
  @Operation(
      summary = "Get register form details",
      description = "Get offline exam registration form by ID (Admin and Assistant only)")
  public ResponseEntity<SingleResponse<RegisterFormResponse>> getRegisterForm(
      @PathVariable UUID id) {
    log.info("[GET /api/register-forms/{}] Fetching register form details", id);
    RegisterFormResponse response = registerFormService.getRegisterFormById(id);
    return successSingle(response, "Register form fetched successfully!");
  }
}
