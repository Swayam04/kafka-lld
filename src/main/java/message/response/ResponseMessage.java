package message.response;

import util.Struct;

public record ResponseMessage(Struct responseHeader, Struct responseBody) {

}
