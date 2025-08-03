package core.handler;

import core.handler.api_versions.ApiVersionInfo;
import message.request.RequestInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestHandlerFactory {
    private static final Map<Short, RequestHandler> handlers = new HashMap<>();

    public static RequestHandler getRequestHandler(RequestInfo requestInfo) {
        return handlers.get(requestInfo.requestApiKey());
    }

    public static List<ApiVersionInfo> getSupportedApiVersions() {
        return handlers.values().stream()
                .map(handler -> new ApiVersionInfo(
                        handler.apiKey(),
                        handler.supportedVersions().min(),
                        handler.supportedVersions().max()))
                .toList();
    }

}
