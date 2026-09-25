package studyweb.cus.service.registration.impl;

import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studyweb.cus.dto.request.registration.RegisterFormRequest;
import studyweb.cus.dto.response.registration.RegisterFormResponse;
import studyweb.cus.entity.registration.RegisterForm;
import studyweb.cus.exception.system.SystemErrorCode;
import studyweb.cus.exception.system.SystemException;
import studyweb.cus.mapper.registration.RegisterFormMapper;
import studyweb.cus.repository.registration.RegisterFormRepository;
import studyweb.cus.service.registration.RegisterFormService;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterFormServiceImpl implements RegisterFormService {

  private final RegisterFormRepository registerFormRepository;
  private final RegisterFormMapper registerFormMapper;

  @Override
  @Transactional
  public RegisterFormResponse createRegisterForm(RegisterFormRequest request) {
    log.info("Processing offline exam registration form for email: {}", request.email());
    RegisterForm entity = registerFormMapper.toEntity(request);
    RegisterForm saved = registerFormRepository.save(entity);
    log.info("Offline exam registration form saved successfully with id: {}", saved.getId());
    return registerFormMapper.toResponse(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<RegisterFormResponse> listRegisterForms(
      LocalDate date, String search, Pageable pageable) {
    log.info("Fetching register forms: date={}, search='{}', pageable={}", date, search, pageable);
    return registerFormRepository
        .searchRegisterForms(date, search, pageable)
        .map(registerFormMapper::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public long countRegisterForms(LocalDate date, String search) {
    log.info("Counting register forms: date={}, search='{}'", date, search);
    return registerFormRepository.countRegisterForms(date, search);
  }

  @Override
  @Transactional(readOnly = true)
  public RegisterFormResponse getRegisterFormById(UUID id) {
    log.info("Fetching register form with id: {}", id);
    RegisterForm form =
        registerFormRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new SystemException(
                        SystemErrorCode.RESOURCE_NOT_FOUND, "Register form not found"));
    return registerFormMapper.toResponse(form);
  }
}
