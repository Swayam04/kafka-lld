package message.request;

import util.Struct;


public record RequestMessage(Struct requestHeader, Struct requestBody) {

    public RequestMessage(Struct requestHeader) {
        this(requestHeader, null);
    }

}
