package iot.backend.iotmonitoring.dto.mqtt;

import iot.backend.iotmonitoring.enums.DeviceStatus;
import jakarta.validation.constraints.NotNull;

public record MqttDeviceStatusMessage(
        @NotNull Long device,
        @NotNull DeviceStatus status
) {

    public Long deviceId() {
        return device;
    }
}
