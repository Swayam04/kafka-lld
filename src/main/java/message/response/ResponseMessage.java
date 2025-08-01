package message;

import java.nio.ByteBuffer;

public class ResponseMessage {

    public static byte[] getMessage() {
        ByteBuffer buffer = ByteBuffer.allocate(512);
        int messageSize = 0;
        buffer.putInt(messageSize);
        int correlationId = 7;
        buffer.putInt(correlationId);
        return buffer.array();
    }
}
