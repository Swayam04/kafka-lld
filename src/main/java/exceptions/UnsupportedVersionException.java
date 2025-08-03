package exceptions;

public class UnsupportedVersionException extends ApiException {

    public UnsupportedVersionException() {
        super(ErrorCode.UNSUPPORTED_VERSION);
    }

    public UnsupportedVersionException(String message) {
        super(ErrorCode.UNSUPPORTED_VERSION, message);
    }

}
