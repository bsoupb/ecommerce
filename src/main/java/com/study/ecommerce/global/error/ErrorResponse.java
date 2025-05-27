package com.study.ecommerce.global.error;

// 전역적인 에러 관리
// SpringBoot 에서 예외가 발생했을 때 클라이언트에게 전달하는 에러 응답 형식을 정의한 DTO
// 예외가 발생했을 때 일관된 에러 응답 포맷을 만들어 클라이언트에게 전달하는 역할
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 클라이언트에게 에러 발생 시 일관되 형태로 응답을 주기 위한 DTO 클래스
// 예외 발생 시 이 클래스를 리턴하여 클라이언트가 에러를 명확히 이해하도록 함
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorResponse {
    private LocalDateTime timestamp = LocalDateTime.now();      // 에러 발생 시간
    private String message;
    private int status;
    private String code;
    private List<FieldError> errors;

    public ErrorResponse(final ErrorCode code, final List<FieldError> errors) {
        this.message = code.getMessage();
        this.status = code.getStatus();
        this.code = code.getCode();
        this.errors = errors;
    }

    public ErrorResponse(final ErrorCode code) {
        this.message = code.getMessage();
        this.status = code.getStatus();
        this.code = code.getCode();
        this.errors = new ArrayList<>();
    }
    
    // BindingResult
    // : 검증 오류 처리 방법
    // : 검증 오류를 보관하는 객체
    // -> 검증 오류가 발생하면 BindingResult 객체에 보관

    // 정적 팩토리 메서드

    // FieldError.of(bindingResult): BindingResult의 필드 오류 정보를 FieldError 리스트로 변환
    public static ErrorResponse of(final ErrorCode code, final BindingResult bindingResult) {
        return new ErrorResponse(code, FieldError.of(bindingResult));
    }

    // of: 팩토리 메서드의 파라미터로 넘어온 값들을 검증하여 인스턴스를 생성할 때 사용
    public static ErrorResponse of(final ErrorCode code) {
        return new ErrorResponse(code);
    }

    public static ErrorResponse of(final ErrorCode code, final List<FieldError> errors) {
        return new ErrorResponse(code, errors);
    }

    // MethodArgumentTypeMismatchException: URL 파라미터의 타입이 잘못된 경우 발생하는 예외
    public static ErrorResponse of(MethodArgumentTypeMismatchException e) {
        final String value = e.getValue() == null ? "" : e.getValue().toString();
        final List<FieldError> errors = FieldError.of(e.getName(), value, e.getErrorCode());
        return new ErrorResponse(ErrorCode.INVALID_TYPE_VALUE, errors);
    }

    // 내부 정적 클래스
    // 하나의 필드에 대한 에러 정보를 담는 객체
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class FieldError {
        private String field;
        private String value;
        private String reason;

        // 불변 클래스
        public FieldError(final String field, final String value, final String reason) {
            this.field = field;
            this.value = value;
            this.reason = reason;
        }

        // 정적 팩토리 메서드
        public static List<FieldError> of(final String field, final String value, final String reason) {
            List<FieldError> fieldErrors = new ArrayList<>();
            fieldErrors.add(new FieldError(field, value, reason));
            return fieldErrors;
        }

        public static List<FieldError> of(final BindingResult bindingResult) {
            final List<org.springframework.validation.FieldError> fieldErrors = bindingResult.getFieldErrors();
            return fieldErrors.stream()
                    .map(error -> new FieldError(
                            error.getField(),
                            error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                            error.getDefaultMessage()
                    ))
                    .toList();
        }
    }
}
