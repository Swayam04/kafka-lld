package core.handler;

import core.handler.api_versions.ApiVersionInfo;
import core.handler.api_versions.ApiVersionsHandler;
import exceptions.InvalidRequestException;
import message.request.RequestInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestHandlerFactory {
    private static final Map<Short, RequestHandler> handlers = new HashMap<>();

    static {
        registerHandler(new ApiVersionsHandler());
    }

    private static void registerHandler(RequestHandler requestHandler) {
        if(!handlers.containsKey(requestHandler.apiKey())) {
            handlers.put(requestHandler.apiKey(), requestHandler);
        }
    }

    public static RequestHandler getRequestHandler(RequestInfo requestInfo) {
        if (handlers.containsKey(requestInfo.requestApiKey())) {
            return handlers.get(requestInfo.requestApiKey());
        } else {
            throw new InvalidRequestException();
        }
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
