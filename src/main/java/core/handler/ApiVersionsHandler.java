package core.handler;

import exceptions.ApiException;
import exceptions.UnsupportedVersionException;
import lombok.extern.slf4j.Slf4j;
import message.request.RequestInfo;
import message.request.RequestMessage;
import message.response.ResponseMessage;
import schema.ApiVersionsApiSchema;
import schema.SchemaSet;
import schema.VersionRange;
import util.Parser;

@Slf4j
public class ApiVersionsHandler implements RequestHandler {
    private static final ApiVersionsApiSchema apiVersionsSchema = new ApiVersionsApiSchema();
    private SchemaSet schemaSet;

    @Override
    public short apiKey() {
        return apiVersionsSchema.apiKey();
    }

    @Override
    public VersionRange supportedVersions() {
        return  apiVersionsSchema.versionRange();
    }

    @Override
    public ResponseMessage handleRequest(RequestInfo requestInfo) {
        try {
            RequestMessage request = validateAndParseRequest(requestInfo);
            return null;
        } catch (ApiException e) {
            log.error("Failed to handle ApiVersions request: {}", e.getMessage());
            return null;
        }
    }

    private RequestMessage validateAndParseRequest(RequestInfo requestInfo) throws ApiException {
        if(!supportedVersions().contains(requestInfo.requestApiVersion())) {
            throw new UnsupportedVersionException();
        }
        this.schemaSet = apiVersionsSchema.forVersion(requestInfo.requestApiVersion());
        return Parser.parseMessage(requestInfo, schemaSet.requestHeaderSchema(), schemaSet.requestBodySchema());
    }
}
