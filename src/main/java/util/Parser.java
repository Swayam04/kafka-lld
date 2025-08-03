package util;

import exceptions.InvalidRequestException;
import message.request.RequestInfo;
import message.request.RequestMessage;
import schema.DataType;
import schema.Field;
import schema.HeaderSchema;
import schema.Schema;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

/**
 * A parser for decoding raw byte payloads into structured request messages based on a defined schema.
 * This class includes comprehensive error handling to validate the incoming data stream.
 */
public class Parser {

    private static final int MAX_REASONABLE_SIZE = 8192;

    /**
     * Parses the common request headers from a raw byte array.
     *
     * @param rawPayload The byte array containing the request.
     * @return A RequestInfo object with common header data and the remaining payload.
     * @throws InvalidRequestException if the payload is malformed or incomplete.
     */
    public static RequestInfo parseCommons(byte[] rawPayload) {
        if (rawPayload.length < 4) {
            throw new InvalidRequestException("Request is too short to contain message size. Minimum 4 bytes required.");
        }
        ByteBuffer buffer = ByteBuffer.wrap(rawPayload);
        int messageSize = buffer.getInt();

        if (rawPayload.length - 4 != messageSize) {
            throw new InvalidRequestException(
                    "Message size mismatch. Expected " + messageSize + " bytes, but found " + (rawPayload.length - 4) + " bytes."
            );
        }

        ensureRemaining(buffer, 8, "request headers (apiKey, apiVersion, correlationId)");
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

    /**
     * Parses the remaining request bytes into a structured message.
     *
     * @param requestInfo  The request info containing the remaining bytes.
     * @param headerSchema The schema for the request header.
     * @param bodySchema   The schema for the request body.
     * @return A fully parsed RequestMessage.
     * @throws InvalidRequestException if the payload does not conform to the schema or has trailing data.
     */
    public static RequestMessage parseMessage(RequestInfo requestInfo, Schema headerSchema, Schema bodySchema) {
        ByteBuffer buffer = ByteBuffer.wrap(requestInfo.remainingRequest());
        Struct header = parseHeader(headerSchema, buffer, requestInfo);
        Struct body = parseBody(bodySchema, buffer, requestInfo.requestApiVersion());

        if (buffer.hasRemaining()) {
            throw new InvalidRequestException("Request has " + buffer.remaining() + " trailing bytes that were not read.");
        }

        return new RequestMessage(header, body);
    }

    /**
     * Parses the request body from the buffer according to the given schema.
     *
     * @param bodySchema The schema for the request body.
     * @param buffer     The byte buffer containing the body data.
     * @param apiVersion The API version of the request.
     * @return A Struct representing the parsed body.
     * @throws InvalidRequestException if the data is malformed.
     */
    public static Struct parseBody(Schema bodySchema, ByteBuffer buffer, short apiVersion) {
        Struct body = new Struct();
        for (Field field : bodySchema.fields()) {
            if (field.tag().isEmpty() && field.validVersions().contains(apiVersion)) {
                parseField(field, buffer, body, apiVersion);
            }
        }
        parseTaggedFields(bodySchema, apiVersion, buffer, body);
        return body;
    }

    private static Struct parseHeader(Schema headerSchema, ByteBuffer buffer, RequestInfo requestInfo) {
        Struct header = new Struct();
        header.set("request_api_key", DataType.INT16, requestInfo.requestApiKey());
        header.set("request_api_version", DataType.INT16, requestInfo.requestApiVersion());
        header.set("correlation_id", DataType.INT32, requestInfo.correlationId());
        header.set("client_id", DataType.NULLABLE_STRING, requestInfo.clientId().orElse(null));

        if (headerSchema == HeaderSchema.REQUEST_HEADER_V2) {
            parseTaggedFields(headerSchema, requestInfo.requestApiVersion(), buffer, header);
        }
        return header;
    }

    private static void parseTaggedFields(Schema schema, short apiVersion, ByteBuffer buffer, Struct struct) {
        int numTaggedFields = parseUnsignedVariableInt(buffer).intValue();
        if (numTaggedFields < 0) {
            throw new InvalidRequestException("Invalid number of tagged fields: " + numTaggedFields);
        }

        for (int i = 0; i < numTaggedFields; i++) {
            int tag = parseUnsignedVariableInt(buffer).intValue();
            int size = parseUnsignedVariableInt(buffer).intValue();

            if (size < 0) {
                throw new InvalidRequestException("Invalid size for tagged field " + tag + ": " + size);
            }
            ensureRemaining(buffer, size, "tagged field " + tag);

            ByteBuffer fieldBuffer = buffer.slice();
            fieldBuffer.limit(size);

            Optional<Field> optionalField = Arrays.stream(schema.fields())
                    .filter(f -> f.tag().isPresent() && f.tag().get() == tag)
                    .findFirst();

            if (optionalField.isPresent() && optionalField.get().validVersions().contains(apiVersion)) {
                parseField(optionalField.get(), fieldBuffer, struct, apiVersion);
                if (fieldBuffer.hasRemaining()) {
                    throw new InvalidRequestException(
                            "Tagged field " + tag + " has " + fieldBuffer.remaining() + " trailing bytes that were not read."
                    );
                }
            }
            buffer.position(buffer.position() + size);
        }
    }

    private static void parseField(Field field, ByteBuffer buffer, Struct struct, short apiVersion) {
        Object value = getValue(field, buffer, apiVersion);
        struct.set(field.name(), field.type(), value);
    }

    private static Object getValue(Field field, ByteBuffer buffer, short apiVersion) {
        return switch (field.type()) {
            case INT8 -> {
                ensureRemaining(buffer, 1, "INT8");
                yield buffer.get();
            }
            case INT16 -> {
                ensureRemaining(buffer, 2, "INT16");
                yield buffer.getShort();
            }
            case INT32 -> {
                ensureRemaining(buffer, 4, "INT32");
                yield buffer.getInt();
            }
            case INT64 -> {
                ensureRemaining(buffer, 8, "INT64");
                yield buffer.getLong();
            }
            case VARINT -> parseVariableInt(buffer);
            case UNSIGNED_VARINT -> parseUnsignedVariableInt(buffer);
            case FLOAT64 -> {
                ensureRemaining(buffer, 8, "FLOAT64");
                yield buffer.getDouble();
            }
            case BOOLEAN -> {
                ensureRemaining(buffer, 1, "BOOLEAN");
                yield buffer.get() != 0;
            }
            case STRING -> parseString(buffer);
            case COMPACT_STRING -> parseCompactString(buffer);
            case NULLABLE_STRING -> parseNullableString(buffer);
            case COMPACT_NULLABLE_STRING -> parseCompactNullableString(buffer);
            case ARRAY -> parseArray(buffer, field.nestedSchema(), apiVersion);
            case COMPACT_ARRAY -> parseCompactArray(buffer, field.nestedSchema(), apiVersion);
            case STRUCT -> parseStruct(field.nestedSchema(), buffer, apiVersion);
        };
    }

    private static Struct parseStruct(Schema schema, ByteBuffer buffer, short apiVersion) {
        Struct struct = new Struct();
        for (Field field : schema.fields()) {
            if (field.validVersions().contains(apiVersion)) {
                parseField(field, buffer, struct, apiVersion);
            }
        }
        parseTaggedFields(schema, apiVersion, buffer, struct);
        return struct;
    }

    private static String readString(ByteBuffer buffer, int length) {
        if (length < 0) {
            throw new InvalidRequestException("Invalid string length: " + length);
        }
        if (length > MAX_REASONABLE_SIZE) {
            throw new InvalidRequestException("String length " + length + " exceeds maximum reasonable size of " + MAX_REASONABLE_SIZE);
        }
        ensureRemaining(buffer, length, "string of length " + length);
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static String parseString(ByteBuffer buffer) {
        ensureRemaining(buffer, 2, "string length");
        short length = buffer.getShort();
        return readString(buffer, length);
    }

    public static Optional<String> parseNullableString(ByteBuffer buffer) {
        ensureRemaining(buffer, 2, "nullable string length");
        short length = buffer.getShort();
        if (length == -1) {
            return Optional.empty();
        }
        return Optional.of(readString(buffer, length));
    }

    public static String parseCompactString(ByteBuffer buffer) {
        int length = parseUnsignedVariableInt(buffer).intValue() - 1;
        return readString(buffer, length);
    }

    public static Optional<String> parseCompactNullableString(ByteBuffer buffer) {
        int length= parseUnsignedVariableInt(buffer).intValue() - 1;
        if (length == -1) return Optional.empty();
        return Optional.of(readString(buffer, length));
    }

    private static Integer parseVariableInt(ByteBuffer buffer) {
        Long value = parseUnsignedVariableInt(buffer);
        return decodeZigZag32(value);
    }

    private static Long parseUnsignedVariableInt(ByteBuffer buffer) {
        long value = 0L;
        int shift = 0;
        byte b;
        for (int i = 0; i < 10; i++) {
            if (!buffer.hasRemaining()) {
                throw new InvalidRequestException("Malformed varint: insufficient bytes.");
            }
            b = buffer.get();
            value |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return value;
            }
            shift += 7;
            if (shift > 63) {
                throw new InvalidRequestException("Malformed varint: value is too long.");
            }
        }
        throw new InvalidRequestException("Malformed varint: value is too long.");
    }

    private static Integer decodeZigZag32(Long value) {
        return (value.intValue() >>> 1) ^ -(value.intValue() & 1);
    }

    private static Object[] parseArray(ByteBuffer buffer, Schema elementSchema, short apiVersion) {
        ensureRemaining(buffer, 4, "array length");
        int length = buffer.getInt();
        return readArray(buffer, elementSchema, apiVersion, length);
    }

    private static Object[] parseCompactArray(ByteBuffer buffer, Schema elementSchema, short apiVersion) {
        int length = parseUnsignedVariableInt(buffer).intValue() - 1;
        return readArray(buffer, elementSchema, apiVersion, length);
    }

    private static Object[] readArray(ByteBuffer buffer, Schema elementSchema, short apiVersion, int length) {
        if (length == -1) {
            return null;
        }
        if (length < 0) {
            throw new InvalidRequestException("Invalid array length: " + length);
        }
        if (length > MAX_REASONABLE_SIZE) {
            throw new InvalidRequestException("Array length " + length + " exceeds maximum reasonable size of " + MAX_REASONABLE_SIZE);
        }
        Object[] array = new Object[length];
        for (int i = 0; i < length; i++) {
            try {
                array[i] = parseElement(elementSchema, buffer, apiVersion);
            } catch (InvalidRequestException e) {
                throw new InvalidRequestException("Error parsing element " + i + " of array: " + e.getMessage());
            }
        }
        return array;
    }

    private static Object parseElement(Schema elementSchema, ByteBuffer buffer, short apiVersion) {
        if (elementSchema.fields().length == 1) {
            Field primitiveField = elementSchema.fields()[0];
            return getValue(primitiveField, buffer, apiVersion);
        } else {
            return parseStruct(elementSchema, buffer, apiVersion);
        }
    }

    /**
     * Helper method to ensure the buffer has enough remaining bytes to read.
     *
     * @param buffer The ByteBuffer to check.
     * @param bytes  The number of bytes required.
     * @param fieldName A descriptive name of the field being read, for error reporting.
     * @throws InvalidRequestException if the buffer is exhausted.
     */
    private static void ensureRemaining(ByteBuffer buffer, int bytes, String fieldName) {
        if (buffer.remaining() < bytes) {
            throw new InvalidRequestException(
                    "Buffer exhausted while trying to read " + fieldName + ". Expected " +
                            bytes + " bytes, but only " + buffer.remaining() + " are available."
            );
        }
    }
}
