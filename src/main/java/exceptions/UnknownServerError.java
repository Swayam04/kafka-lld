package exceptions;

public class UnknownServerError extends ApiException {

    public UnknownServerError() {
        super(ErrorCode.UNKNOWN_SERVER_ERROR);
    }

}
