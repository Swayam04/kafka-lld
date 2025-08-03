package util;

import exceptions.InvalidRequestException;
import message.request.RequestInfo;
import message.request.RequestMessage;
import schema.DataType;
import schema.Field;
import schema.HeaderSchema;
import schema.Schema;

import java.nio.ByteBuffer;
import java.util.Optional;

public class Parser {

    public static RequestInfo parseCommons(byte[] rawPayload) {
        ByteBuffer buffer = ByteBuffer.wrap(rawPayload);
        int messageSize = buffer.getInt();
        short requestApiKey = buffer.getShort();
        short requestApiVersion = buffer.getShort();
        int correlationId = buffer.getInt();
        Optional<String> clientId = parseNullableString(buffer);

        byte[] remainingBytes = new byte[buffer.remaining()];
        buffer.get(remainingBytes);

        return RequestInfo.builder()
                .messageSize(messageSize)
                .requestApiKey(requestApiKey)
                .requestApiVersion(requestApiVersion)
                .correlationId(correlationId)
                .clientId(clientId)
                .remainingRequest(remainingBytes)
                .build();
    }

    public static RequestMessage parseMessage(RequestInfo requestInfo, Schema headerSchema, Schema bodySchema) {
        ByteBuffer buffer = ByteBuffer.wrap(requestInfo.remainingRequest());
        Struct header = parseHeader(headerSchema, buffer, requestInfo);
        Struct body = new Struct();
        return new RequestMessage(header, body);
    }

    private static Struct parseHeader(Schema headerSchema, ByteBuffer buffer, RequestInfo requestInfo) {
        Struct header = new Struct();
        header.set("request_api_key", DataType.INT16, requestInfo.requestApiKey());
        header.set("request_api_version", DataType.INT16, requestInfo.requestApiVersion());
        header.set("correlation_id", DataType.INT32, requestInfo.correlationId());
        header.set("client_id", DataType.COMPACT_STRING, requestInfo.clientId().orElse(null));

        if (headerSchema == HeaderSchema.REQUEST_HEADER_V2) {
            parseTaggedFields(headerSchema, header, buffer);
        }
        return header;
    }

    private static void parseTaggedFields(Schema schema, Struct struct, ByteBuffer buffer) {

    }


    private static Optional<String> parseNullableString(ByteBuffer buffer) {
        short length = buffer.getShort();
        if (length == -1) {
            return Optional.empty();
        }
        if (length < 0 || length > buffer.remaining()) {
            throw new InvalidRequestException("Invalid string length: " + length);
        }
        byte[] stringBytes = new byte[length];
        buffer.get(stringBytes);
        return Optional.of(new String(stringBytes));
    }

}
