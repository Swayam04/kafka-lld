package message.response.header;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class ResponseHeader {

    protected int correlationId;

    public ResponseHeader(int correlationId) {
        this.correlationId = correlationId;
    }

    public abstract byte[] getBytes();
}
