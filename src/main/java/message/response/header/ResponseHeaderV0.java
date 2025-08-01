package message.response.header;

import lombok.Getter;
import lombok.Setter;
import message.request.RequestMessage;

import java.nio.ByteBuffer;

@Getter
@Setter
public class ResponseHeaderV0 extends ResponseHeader {

    public ResponseHeaderV0(RequestMessage requestMessage) {
        super(requestMessage.getRequestHeader().getCorrelationId());
    }

    @Override
    public  byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(this.correlationId);
        return buffer.array();
    }

}
