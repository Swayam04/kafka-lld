package exceptions;

public class RequestTimedOutException extends  ApiException {

    public RequestTimedOutException() {
        super(ErrorCode.REQUEST_TIMED_OUT);
    }

}
