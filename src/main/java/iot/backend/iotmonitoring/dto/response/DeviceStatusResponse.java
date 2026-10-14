package iot.backend.iotmonitoring.dto.response;

import iot.backend.iotmonitoring.enums.DeviceStatus;

public record DeviceStatusResponse(
        Long deviceId,
        DeviceStatus status
) {
}
