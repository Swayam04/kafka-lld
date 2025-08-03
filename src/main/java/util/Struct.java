package util;

import schema.DataType;

import java.util.HashMap;
import java.util.Map;

public class Struct {

    private final Map<String, Object> values;

    public Struct() {
        values = new HashMap<>();
    }

    public void set(String fieldName, DataType type, Object value) {
        if (type == DataType.ARRAY || type == DataType.COMPACT_ARRAY) {
            if (!value.getClass().isArray()) {
                throw new IllegalArgumentException("Value for array field '" + fieldName + "' must be an array.");
            }
        } else if (value != null && !type.getJavaType().isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException("Value for field '" + fieldName + "' is of type " +
                    value.getClass().getSimpleName() + " but expected " + type.getJavaType().getSimpleName());
        }
        values.put(fieldName, value);
    }

    public <T> T get(String fieldName, Class<T> expectedType) {
        Object value = values.get(fieldName);
        if (value == null) {
            return null;
        }
        if (!expectedType.isInstance(value)) {
            throw new ClassCastException("Value for field '" + fieldName + "' is of type " +
                    value.getClass().getSimpleName() + " but expected " + expectedType.getSimpleName());
        }
        return expectedType.cast(value);
    }

    public Short getShort(String fieldName) {
        return get(fieldName, Short.class);
    }

    public Byte getByte(String fieldName) {
        return get(fieldName, Byte.class);
    }

    public Integer getInt(String fieldName) {
        return get(fieldName, Integer.class);
    }

    public Boolean getBoolean(String fieldName) {
        return get(fieldName, Boolean.class);
    }

    public Long getLong(String fieldName) {
        return get(fieldName, Long.class);
    }

    public String getString(String fieldName) {
        return get(fieldName, String.class);
    }

    public Object[] getArray(String fieldName) {
        return get(fieldName, Object[].class);
    }

}
