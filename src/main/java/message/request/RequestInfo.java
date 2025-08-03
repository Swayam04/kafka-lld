package message.request;

import lombok.Builder;

import java.util.Optional;

@Builder
public record RequestInfo(Integer messageSize, Short requestApiKey, Short requestApiVersion, Integer correlationId, Optional<String> clientId, byte[] remainingRequest) {
}
