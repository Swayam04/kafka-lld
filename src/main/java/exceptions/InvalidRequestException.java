package exceptions;

public class InvalidRequestException extends ApiException {

    public InvalidRequestException() {
        super(ErrorCode.INVALID_REQUEST);
    }

    public InvalidRequestException(String message) {
        super(ErrorCode.INVALID_REQUEST, message);
    }

}
