package iot.backend.iotmonitoring.dto.mqtt;

import jakarta.validation.constraints.NotNull;

public record MqttSensorDataMessage(
        @NotNull Double temperature,
        @NotNull Double humidity,
        @NotNull Double light
) {
}
