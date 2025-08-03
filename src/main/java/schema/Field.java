package schema;

import java.util.Optional;

public record Field(String name, DataType type, VersionRange validVersions, Optional<Integer> tag, Schema nestedSchema) {

    public Field(String name, DataType type, String versions) {
        this(name, type, parseVersions(versions), Optional.empty(), null);
    }

    public Field(String name, DataType type, String versions, Schema nestedSchema) {
        this(name, type, parseVersions(versions), Optional.empty(), nestedSchema);
    }

    public Field(String name, DataType type, String versions, int tag) {
        this(name, type, parseVersions(versions), Optional.of(tag), null);
    }

    private  static VersionRange parseVersions(String versions) {
        if (versions.contains("+")) {
            return VersionRange.since(Short.parseShort(versions.replace("+", "")));
        } else if (versions.contains("-")) {
            return VersionRange.of(
                    Short.parseShort(versions.split("-")[0]),
                    Short.parseShort(versions.split("-")[1])
            );
        }
        throw new IllegalArgumentException("Invalid versions specified: " + versions);
    }

}
