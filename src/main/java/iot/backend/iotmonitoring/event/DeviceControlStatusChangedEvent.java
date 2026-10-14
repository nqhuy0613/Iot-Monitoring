package iot.backend.iotmonitoring.event;

import iot.backend.iotmonitoring.enums.ActionStatus;

public record DeviceControlStatusChangedEvent(
        Long deviceId,
        ActionStatus status
) {
}
