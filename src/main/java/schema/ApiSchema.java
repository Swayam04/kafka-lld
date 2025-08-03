package schema;

public interface ApiSchema {

    short apiKey();
    SchemaSet forVersion(short apiVersion);
    VersionRange versionRange();

}
