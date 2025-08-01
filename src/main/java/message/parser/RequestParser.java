package message.parser;

import message.request.RequestMessage;
import message.request.header.RequestHeader;
import message.request.header.RequestHeaderV2;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class RequestParser {

    public static RequestMessage parseRequest(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setMessageSize(buffer.getInt());
        requestMessage.setRequestHeader(parseRequestHeader(buffer));
        return requestMessage;
    }

    private static RequestHeader parseRequestHeader(ByteBuffer buffer) {
        RequestHeaderV2 requestHeader = new RequestHeaderV2();
        requestHeader.setRequestApiKey(buffer.getShort());
        requestHeader.setRequestApiVersion(buffer.getShort());
        requestHeader.setCorrelationId(buffer.getInt());

        int clientIdLength = buffer.getShort();
        if (clientIdLength >= 0) {
            byte[] clientIdBytes = new byte[clientIdLength];
            try {
                buffer.get(clientIdBytes);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Client ID length exceeds buffer size", e);
            }
            requestHeader.setClientId(new String(clientIdBytes, StandardCharsets.UTF_8));
        }
        int tagBufferLength = ((int) readVarint(buffer)) - 1;
        if (tagBufferLength >= 0) {
            byte[] tagBuffer = new byte[tagBufferLength];
            try {
                buffer.get(tagBuffer);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Tag buffer length exceeds buffer size", e);
            }
            requestHeader.setTagBuffer(tagBuffer);
        }
        return requestHeader;
    }

    private static long readVarint(ByteBuffer buffer) {
        long result = 0;
        int shift = 0;
        byte currentByte;
        for (int i = 0; i < 5; i++) { // 32-bit VarInt is at most 5 bytes
            currentByte = buffer.get();
            result |= (long) (currentByte & 0x7F) << shift;
            if ((currentByte & 0x80) == 0) {
                return result;
            }
            shift += 7;
        }
        throw new IllegalArgumentException("Malformed VarInt");
    }

}
