package schema;

import lombok.Getter;
import util.Struct;

@Getter
public enum DataType {

    BOOLEAN("BOOLEAN", Boolean.class),
    INT8("INT8", Byte.class),
    INT16("INT16", Short.class),
    INT32("INT32", Integer.class),
    INT64("INT64", Long.class),
    VARINT("VARINT", Long.class),
    UNSIGNED_VARINT("UNSIGNED_VARINT", Long.class),
    STRING("STRING", String.class),
    COMPACT_STRING("COMPACT_STRING", String.class),
    NULLABLE_STRING("NULLABLE_STRING", String.class),
    COMPACT_NULLABLE_STRING("COMPACT_NULLABLE_STRING", String.class),
    FLOAT64("FLOAT64", Double.class),
    ARRAY("ARRAY", Object[].class),
    COMPACT_ARRAY("COMPACT_ARRAY", Object[].class),
    STRUCT("STRUCT", Struct.class);


    private final String name;
    private final Class<?> javaType;

    DataType(String name, Class<?> javaType) {
        this.name = name;
        this.javaType = javaType;
    }

}
