package iot.backend.iotmonitoring.event;

import iot.backend.iotmonitoring.enums.DeviceAction;

public record DeviceControlRequestedEvent(
        Long actionHistoryId,
        Long deviceId,
        DeviceAction action
) {
}
