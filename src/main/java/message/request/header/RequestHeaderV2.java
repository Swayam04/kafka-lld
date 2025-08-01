package message.request.header;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestHeaderV2 extends RequestHeader {

    private String clientId;
    private byte[] tagBuffer;

}
