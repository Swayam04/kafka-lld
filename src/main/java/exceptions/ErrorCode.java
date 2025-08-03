package exceptions;

import lombok.Getter;

@Getter
public enum ErrorCode {
    NONE(0, "No error."),
    UNKNOWN_SERVER_ERROR(-1, "The server experienced an unexpected error."),
    REQUEST_TIMED_OUT(7, "The request timed out."),
    UNSUPPORTED_VERSION(35, "The version of API is not supported."),
    INVALID_REQUEST(42, "The request is invalid.");

    private final short code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = (short) code;
        this.message = message;
    }
}
