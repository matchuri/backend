package matchuri.backend.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.common.CommonErrorCode;
import matchuri.backend.global.api.ApiResponse;
import matchuri.backend.global.api.ErrorResponse;
import matchuri.backend.global.api.ValidationErrorDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RequestValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleRequestValidationException(RequestValidationException exception) {
        return errorResponse(exception.getErrorCode(), List.of(exception.getDetail()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request
    ) {
        log.warn("Business exception: path={}, code={}", request.getRequestURI(), exception.getErrorCode().getCode());
        return errorResponse(exception.getErrorCode());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            AuthenticationException exception,
            HttpServletRequest request
    ) {
        log.warn("Authentication exception: path={}, code={}", request.getRequestURI(), exception.getErrorCode().getCode());
        return errorResponse(exception.getErrorCode());
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthorizationException(
            AuthorizationException exception,
            HttpServletRequest request
    ) {
        log.warn("Authorization exception: path={}, code={}", request.getRequestURI(), exception.getErrorCode().getCode());
        return errorResponse(exception.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        List<ValidationErrorDetail> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toBodyValidationDetail)
                .toList();

        return errorResponse(CommonErrorCode.INVALID_BODY_FIELD, details);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException exception) {
        List<ValidationErrorDetail> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new ValidationErrorDetail("QUERY", fieldError.getField(), resolveReason(fieldError)))
                .toList();

        return errorResponse(CommonErrorCode.INVALID_QUERY_PARAMETER, details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException exception) {
        List<ValidationErrorDetail> details = exception.getConstraintViolations()
                .stream()
                .map(this::toConstraintViolationDetail)
                .toList();

        return errorResponse(CommonErrorCode.INVALID_QUERY_PARAMETER, details);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        String source = isPathVariable(exception) ? "PATH" : "QUERY";
        CommonErrorCode errorCode = isPathVariable(exception)
                ? CommonErrorCode.INVALID_PATH_VARIABLE
                : CommonErrorCode.INVALID_QUERY_PARAMETER;

        ValidationErrorDetail detail = new ValidationErrorDetail(
                source,
                exception.getName(),
                Objects.requireNonNullElse(exception.getMostSpecificCause().getMessage(), "타입 변환에 실패했습니다.")
        );

        return errorResponse(errorCode, List.of(detail));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(MissingServletRequestParameterException exception) {
        ValidationErrorDetail detail = new ValidationErrorDetail("QUERY", exception.getParameterName(), "필수 파라미터가 누락되었습니다.");
        return errorResponse(CommonErrorCode.INVALID_QUERY_PARAMETER, List.of(detail));
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingPathVariable(MissingPathVariableException exception) {
        ValidationErrorDetail detail = new ValidationErrorDetail("PATH", exception.getVariableName(), "필수 경로 변수가 누락되었습니다.");
        return errorResponse(CommonErrorCode.INVALID_PATH_VARIABLE, List.of(detail));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException exception) {
        return errorResponse(CommonErrorCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException exception) {
        return errorResponse(CommonErrorCode.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(
            NoResourceFoundException exception,
            HttpServletRequest request
    ) {
        log.debug("No resource found: path={}", request.getRequestURI(), exception);
        return errorResponse(CommonErrorCode.RESOURCE_NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception, HttpServletRequest request) {
        log.error("Unexpected exception: path={}", request.getRequestURI(), exception);
        return errorResponse(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResponse<Void>> errorResponse(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.failure(errorCode));
    }

    private ResponseEntity<ApiResponse<Void>> errorResponse(ErrorCode errorCode, List<ValidationErrorDetail> details) {
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.failure(ErrorResponse.of(errorCode, details)));
    }

    private ValidationErrorDetail toBodyValidationDetail(FieldError fieldError) {
        return new ValidationErrorDetail("BODY", fieldError.getField(), resolveReason(fieldError));
    }

    private ValidationErrorDetail toConstraintViolationDetail(ConstraintViolation<?> violation) {
        String propertyPath = violation.getPropertyPath().toString();
        int lastDotIndex = propertyPath.lastIndexOf('.');
        String field = lastDotIndex >= 0 ? propertyPath.substring(lastDotIndex + 1) : propertyPath;

        return new ValidationErrorDetail("QUERY", field, violation.getMessage());
    }

    private String resolveReason(FieldError fieldError) {
        return Objects.requireNonNullElse(fieldError.getDefaultMessage(), "요청 값이 올바르지 않습니다.");
    }

    private boolean isPathVariable(MethodArgumentTypeMismatchException exception) {
        return exception.getParameter() != null && exception.getParameter().hasParameterAnnotation(PathVariable.class);
    }
}
