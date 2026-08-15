package studyweb.cus.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import studyweb.cus.dto.base.ErrorResponse;
import studyweb.cus.exception.system.SystemErrorCode;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(BaseException.class)
  public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
    log.warn("Base exception: {} - {}", ex.getCode(), ex.getMessage());
    ErrorResponse response =
        new ErrorResponse(ex.getHttpStatus().value(), ex.getMessage(), ex.getCode());
    return ResponseEntity.status(ex.getHttpStatus()).body(response);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(error -> errors.put(((FieldError) error).getField(), error.getDefaultMessage()));

    String firstError =
        errors.values().stream().findFirst().orElse(SystemErrorCode.VALIDATION_ERROR.message());
    ErrorResponse response =
        new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(), firstError, SystemErrorCode.VALIDATION_ERROR.code());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(BindException.class)
  public ResponseEntity<ErrorResponse> handleBindException(BindException ex) {
    log.warn("Binding validation failed: {}", ex.getMessage());
    ErrorResponse response =
        new ErrorResponse(
            SystemErrorCode.VALIDATION_ERROR.httpStatus().value(),
            SystemErrorCode.VALIDATION_ERROR.message(),
            SystemErrorCode.VALIDATION_ERROR.code());
    return ResponseEntity.status(SystemErrorCode.VALIDATION_ERROR.httpStatus()).body(response);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex) {
    log.warn("Method not supported: {} for {}", ex.getMethod(), ex.getMessage());
    ErrorResponse response =
        new ErrorResponse(
            SystemErrorCode.METHOD_NOT_ALLOWED.httpStatus().value(),
            SystemErrorCode.METHOD_NOT_ALLOWED.message(),
            SystemErrorCode.METHOD_NOT_ALLOWED.code());
    return ResponseEntity.status(SystemErrorCode.METHOD_NOT_ALLOWED.httpStatus()).body(response);
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(NoHandlerFoundException ex) {
    log.warn("No handler found: {} {}", ex.getHttpMethod(), ex.getRequestURL());
    ErrorResponse response =
        new ErrorResponse(
            SystemErrorCode.RESOURCE_NOT_FOUND.httpStatus().value(),
            SystemErrorCode.RESOURCE_NOT_FOUND.message(),
            SystemErrorCode.RESOURCE_NOT_FOUND.code());
    return ResponseEntity.status(SystemErrorCode.RESOURCE_NOT_FOUND.httpStatus()).body(response);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
    log.warn("No resource found: {}", ex.getResourcePath());
    ErrorResponse response =
        new ErrorResponse(
            SystemErrorCode.RESOURCE_NOT_FOUND.httpStatus().value(),
            SystemErrorCode.RESOURCE_NOT_FOUND.message(),
            SystemErrorCode.RESOURCE_NOT_FOUND.code());
    return ResponseEntity.status(SystemErrorCode.RESOURCE_NOT_FOUND.httpStatus()).body(response);
  }

  @ExceptionHandler(DataAccessException.class)
  public ResponseEntity<ErrorResponse> handleDatabaseException(DataAccessException ex) {
    log.error("Database error: ", ex);
    ErrorResponse response =
        new ErrorResponse(
            SystemErrorCode.DATABASE_ERROR.httpStatus().value(),
            SystemErrorCode.DATABASE_ERROR.message(),
            SystemErrorCode.DATABASE_ERROR.code());
    return ResponseEntity.status(SystemErrorCode.DATABASE_ERROR.httpStatus()).body(response);
  }

  @ExceptionHandler(MultipartException.class)
  public ResponseEntity<ErrorResponse> handleMultipartException(MultipartException ex) {
    log.warn("Multipart parsing error: {}", ex.getMessage());
    ErrorResponse response =
        new ErrorResponse(
            SystemErrorCode.INVALID_MULTIPART.httpStatus().value(),
            SystemErrorCode.INVALID_MULTIPART.message(),
            SystemErrorCode.INVALID_MULTIPART.code());
    return ResponseEntity.status(SystemErrorCode.INVALID_MULTIPART.httpStatus()).body(response);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
    log.warn("Access denied: {}", ex.getMessage());
    ErrorResponse response =
        new ErrorResponse(
            SystemErrorCode.FORBIDDEN.httpStatus().value(),
            SystemErrorCode.FORBIDDEN.message(),
            SystemErrorCode.FORBIDDEN.code());
    return ResponseEntity.status(SystemErrorCode.FORBIDDEN.httpStatus()).body(response);
  }

  @ExceptionHandler(MissingServletRequestPartException.class)
  public ResponseEntity<ErrorResponse> handleMissingPart(MissingServletRequestPartException ex) {
    log.warn("Missing request part: {}", ex.getRequestPartName());
    ErrorResponse response =
        new ErrorResponse(
            SystemErrorCode.INVALID_MULTIPART.httpStatus().value(),
            SystemErrorCode.INVALID_MULTIPART.message(),
            SystemErrorCode.INVALID_MULTIPART.code());
    return ResponseEntity.status(SystemErrorCode.INVALID_MULTIPART.httpStatus()).body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
    log.error("Unexpected error: ", ex);
    ErrorResponse response =
        new ErrorResponse(
            SystemErrorCode.INTERNAL_ERROR.httpStatus().value(),
            SystemErrorCode.INTERNAL_ERROR.message(),
            SystemErrorCode.INTERNAL_ERROR.code());
    return ResponseEntity.status(SystemErrorCode.INTERNAL_ERROR.httpStatus()).body(response);
  }
}
