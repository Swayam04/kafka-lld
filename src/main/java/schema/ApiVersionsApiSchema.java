package schema;

import java.util.*;

public class ApiVersionsApiSchema implements ApiSchema {
    private final Map<Short, SchemaSet> versionedSchemas = new HashMap<>();

    private static final Schema API_VERSIONS_API_KEY = new Schema(
            new Field("api_key", DataType.INT16, "0+"),
            new Field("min_version", DataType.INT16, "0+"),
            new Field("max_version", DataType.INT16, "0+")
    );

    private static final Schema API_VERSIONS_REQUEST_BODY = new Schema(
            new Field("client_software_name", DataType.COMPACT_STRING, "3+"),
            new Field("client_software_version", DataType.COMPACT_STRING, "3+")
    );

    private static final Schema API_VERSIONS_RESPONSE_BODY = new Schema(
            new Field("error_code", DataType.INT16, "0+"),
            new Field("api_keys", DataType.ARRAY, "0+",  API_VERSIONS_API_KEY),
            new Field("throttle_time_ms", DataType.INT32, "1+")
    );

    public ApiVersionsApiSchema() {
        for (short v = 0; v <= 2; v++) {
            versionedSchemas.put(v, new SchemaSet(
                    HeaderSchema.REQUEST_HEADER_V1,
                    API_VERSIONS_REQUEST_BODY,
                    HeaderSchema.RESPONSE_HEADER_V0,
                    API_VERSIONS_RESPONSE_BODY
            ));
        }

        for (short v = 3; v <= 4; v++) {
            versionedSchemas.put((short) 3, new SchemaSet(
                    HeaderSchema.REQUEST_HEADER_V2,
                    API_VERSIONS_REQUEST_BODY,
                    HeaderSchema.RESPONSE_HEADER_V0,
                    API_VERSIONS_RESPONSE_BODY
            ));
        }

    }

    @Override
    public short apiKey() {
        return 18;
    }

    public VersionRange versionRange() {
        Set<Short> versions = versionedSchemas.keySet();
        return VersionRange.of(Collections.min(versions), Collections.max(versions));
    }

    @Override
    public SchemaSet forVersion(short apiVersion) {
        if (versionedSchemas.containsKey(apiVersion)) {
            return versionedSchemas.get(apiVersion);
        } else {
            throw new IllegalArgumentException("Unsupported API version: " + apiVersion);
        }
    }
}
