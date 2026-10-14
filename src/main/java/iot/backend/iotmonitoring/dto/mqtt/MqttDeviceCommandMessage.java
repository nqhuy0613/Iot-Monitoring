package iot.backend.iotmonitoring.dto.mqtt;

import iot.backend.iotmonitoring.enums.DeviceAction;

public record MqttDeviceCommandMessage(
        Long deviceId,
        DeviceAction action
) {

    public String toPayload() {
        return deviceId + ":" + action.name();
    }
}
