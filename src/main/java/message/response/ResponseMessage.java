package message.response;

import lombok.Getter;
import lombok.Setter;
import message.request.RequestMessage;
import message.request.header.RequestHeader;
import message.response.header.ResponseHeader;
import message.response.header.ResponseHeaderV0;

import java.nio.ByteBuffer;

@Getter
@Setter
public class ResponseMessage {

    private int messageSize;
    private ResponseHeader responseHeader;

    public ResponseMessage(RequestMessage requestMessage) {
        this.responseHeader = new ResponseHeaderV0(requestMessage);
        this.messageSize = getResponseMessageSize();
    }

    private int getResponseMessageSize() {
        return 4 + responseHeader.getBytes().length;
    }

    public byte[] getMessage() {
        ByteBuffer buffer = ByteBuffer.allocate(512);
        buffer.putInt(this.messageSize);
        buffer.put(responseHeader.getBytes());
        return buffer.array();
    }
}
