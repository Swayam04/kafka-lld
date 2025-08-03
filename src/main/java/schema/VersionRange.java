package schema;

public record VersionRange(short min, short max) {
    public static VersionRange of(short min, short max) { return new VersionRange(min, max); }
    public static VersionRange since(short min) { return new VersionRange(min, Short.MAX_VALUE); }
    public boolean contains(short version) { return version >= min && version <= max; }
}
