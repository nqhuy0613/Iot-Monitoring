package iot.backend.iotmonitoring.dto.response;

public record LoginResponse(
        String token,
        AuthenticatedUserResponse user
) {
}
