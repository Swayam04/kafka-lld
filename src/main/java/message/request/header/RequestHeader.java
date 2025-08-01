package message.request.header;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class RequestHeader {

    protected short requestApiKey;
    protected short requestApiVersion;
    protected int correlationId;

}
