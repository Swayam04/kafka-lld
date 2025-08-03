package schema;

public record SchemaSet(
        Schema requestHeaderSchema,
        Schema requestBodySchema,
        Schema responseHeaderSchema,
        Schema responseBodySchema
) {}
