package message.request;

import lombok.Getter;
import lombok.Setter;
import message.request.header.RequestHeader;

@Getter
@Setter
public class RequestMessage {

    private int messageSize;
    private RequestHeader requestHeader;
}
