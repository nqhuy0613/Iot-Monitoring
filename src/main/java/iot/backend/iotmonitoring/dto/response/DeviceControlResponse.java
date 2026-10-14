package iot.backend.iotmonitoring.dto.response;

import iot.backend.iotmonitoring.enums.ActionStatus;
import iot.backend.iotmonitoring.enums.DeviceAction;

public record DeviceControlResponse(
        Long deviceId,
        DeviceAction action,
        ActionStatus status
) {
}
