package iot.backend.iotmonitoring.dto.response;

public record AuthenticatedUserResponse(
        Long id,
        String username,
        String fullName
) {
}
