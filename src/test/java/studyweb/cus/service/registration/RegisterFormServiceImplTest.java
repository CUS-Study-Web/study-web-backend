package studyweb.cus.service.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import studyweb.cus.dto.request.registration.RegisterFormRequest;
import studyweb.cus.dto.response.registration.RegisterFormResponse;
import studyweb.cus.entity.registration.RegisterForm;
import studyweb.cus.exception.system.SystemException;
import studyweb.cus.mapper.registration.RegisterFormMapper;
import studyweb.cus.repository.registration.RegisterFormRepository;
import studyweb.cus.service.registration.impl.RegisterFormServiceImpl;

@ExtendWith(MockitoExtension.class)
class RegisterFormServiceImplTest {

  @Mock private RegisterFormRepository registerFormRepository;

  private final RegisterFormMapper registerFormMapper =
      Mappers.getMapper(RegisterFormMapper.class);

  private RegisterFormService registerFormService;

  @BeforeEach
  void setUp() {
    registerFormService = new RegisterFormServiceImpl(registerFormRepository, registerFormMapper);
  }

  @Test
  @DisplayName("createRegisterForm successfully saves form with specified registeredDate")
  void createRegisterForm_withSpecifiedDate() {
    LocalDate customDate = LocalDate.of(2026, 10, 15);
    RegisterFormRequest request =
        new RegisterFormRequest(
            "Nguyen Van A",
            "0987654321",
            "nguyenvana@example.com",
            "Toán học",
            "Đăng ký thi offline ca sáng",
            customDate);

    UUID generatedId = UUID.randomUUID();
    when(registerFormRepository.save(any(RegisterForm.class)))
        .thenAnswer(
            invocation -> {
              RegisterForm form = invocation.getArgument(0);
              form.setId(generatedId);
              return form;
            });

    RegisterFormResponse response = registerFormService.createRegisterForm(request);

    assertThat(response).isNotNull();
    assertThat(response.id()).isEqualTo(generatedId);
    assertThat(response.name()).isEqualTo("Nguyen Van A");
    assertThat(response.phoneNumer()).isEqualTo("0987654321");
    assertThat(response.email()).isEqualTo("nguyenvana@example.com");
    assertThat(response.subject()).isEqualTo("Toán học");
    assertThat(response.note()).isEqualTo("Đăng ký thi offline ca sáng");
    assertThat(response.registeredDate()).isEqualTo(customDate);

    verify(registerFormRepository).save(any(RegisterForm.class));
  }

  @Test
  @DisplayName("createRegisterForm defaults registeredDate to today when null")
  void createRegisterForm_defaultsRegisteredDate() {
    RegisterFormRequest request =
        new RegisterFormRequest(
            "Tran Thi B",
            "0912345678",
            "tranthib@example.com",
            "Vật lý",
            null,
            null);

    UUID generatedId = UUID.randomUUID();
    when(registerFormRepository.save(any(RegisterForm.class)))
        .thenAnswer(
            invocation -> {
              RegisterForm form = invocation.getArgument(0);
              form.setId(generatedId);
              return form;
            });

    RegisterFormResponse response = registerFormService.createRegisterForm(request);

    assertThat(response).isNotNull();
    assertThat(response.id()).isEqualTo(generatedId);
    assertThat(response.name()).isEqualTo("Tran Thi B");
    assertThat(response.registeredDate()).isEqualTo(LocalDate.now());

    verify(registerFormRepository).save(any(RegisterForm.class));
  }

  @Test
  @DisplayName("listRegisterForms returns paged list of register forms")
  void listRegisterForms_success() {
    Pageable pageable = PageRequest.of(0, 10);
    RegisterForm form =
        RegisterForm.builder()
            .name("Nguyen Van A")
            .phoneNumer("0987654321")
            .email("nguyenvana@example.com")
            .subject("Toán học")
            .registeredDate(LocalDate.now())
            .build();
    form.setId(UUID.randomUUID());

    when(registerFormRepository.searchRegisterForms(eq("Toán"), eq(pageable)))
        .thenReturn(new PageImpl<>(List.of(form), pageable, 1));

    Page<RegisterFormResponse> result = registerFormService.listRegisterForms("Toán", pageable);

    assertThat(result).isNotNull();
    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getContent().getFirst().name()).isEqualTo("Nguyen Van A");

    verify(registerFormRepository).searchRegisterForms("Toán", pageable);
  }

  @Test
  @DisplayName("getRegisterFormById returns form when found")
  void getRegisterFormById_found() {
    UUID id = UUID.randomUUID();
    RegisterForm form =
        RegisterForm.builder()
            .name("Nguyen Van A")
            .phoneNumer("0987654321")
            .email("nguyenvana@example.com")
            .subject("Toán học")
            .registeredDate(LocalDate.now())
            .build();
    form.setId(id);

    when(registerFormRepository.findById(id)).thenReturn(Optional.of(form));

    RegisterFormResponse response = registerFormService.getRegisterFormById(id);

    assertThat(response).isNotNull();
    assertThat(response.id()).isEqualTo(id);
    assertThat(response.name()).isEqualTo("Nguyen Van A");
  }

  @Test
  @DisplayName("getRegisterFormById throws SystemException when not found")
  void getRegisterFormById_notFound() {
    UUID id = UUID.randomUUID();
    when(registerFormRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> registerFormService.getRegisterFormById(id))
        .isInstanceOf(SystemException.class)
        .hasMessageContaining("Register form not found");
  }
}
