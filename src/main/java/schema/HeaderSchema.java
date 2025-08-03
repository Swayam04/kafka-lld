package schema;

public class HeaderSchema {

    public static final Schema REQUEST_HEADER_V1 = new Schema(
            new Field("request_api_key", DataType.INT16, "1-2"),
            new Field("request_api_version", DataType.INT16, "1-2"),
            new Field("correlation_id", DataType.INT32, "1-2"),
            new Field("client_id", DataType.STRING, "1-2")
    );

    public static final Schema REQUEST_HEADER_V2 = new Schema(
            new Field("request_api_key", DataType.INT16, "1-2"),
            new Field("request_api_version", DataType.INT16, "1-2"),
            new Field("correlation_id", DataType.INT32, "1-2"),
            new Field("client_id", DataType.COMPACT_STRING, "1-2")
    );

    public static final Schema RESPONSE_HEADER_V0 = new Schema(
            new Field("correlation_id", DataType.INT32, "0-1")
    );

    public static final Schema RESPONSE_HEADER_V1 = new Schema(
            new Field("correlation_id", DataType.INT32, "0-1")
    );

}
