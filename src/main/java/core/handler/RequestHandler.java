package core.handler;

import message.request.RequestInfo;
import message.response.ResponseMessage;
import schema.VersionRange;

public interface RequestHandler {

    short apiKey();
    VersionRange supportedVersions();
    ResponseMessage handleRequest(RequestInfo requestInfo);

}
