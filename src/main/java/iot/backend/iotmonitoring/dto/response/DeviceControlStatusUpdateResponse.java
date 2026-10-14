package iot.backend.iotmonitoring.dto.response;

import iot.backend.iotmonitoring.enums.ActionStatus;

public record DeviceControlStatusUpdateResponse(
        Long deviceId,
        ActionStatus status
) {
}
