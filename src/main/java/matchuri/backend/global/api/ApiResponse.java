package matchuri.backend.global.api;

import matchuri.backend.global.exception.ErrorCode;

public record ApiResponse<T>(
        boolean success,
        T data,
        ErrorResponse error
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Void> successWithoutData() {
        return new ApiResponse<>(true, null, null);
    }

    public static <T> ApiResponse<T> failure(ErrorResponse error) {
        return new ApiResponse<>(false, null, error);
    }

    public static <T> ApiResponse<T> failure(ErrorCode errorCode) {
        return failure(ErrorResponse.of(errorCode));
    }

    public static <T> ApiResponse<T> failure(ErrorCode errorCode, Object... args) {
        return failure(ErrorResponse.of(errorCode, args));
    }

    public static <T> ApiResponse<T> failure(String code, String message) {
        return failure(ErrorResponse.of(400, code, message));
    }
}
