package iot.backend.iotmonitoring.dto.response;

import iot.backend.iotmonitoring.enums.SensorType;

import java.time.LocalDateTime;

public record SensorHistoryItemResponse(
        Long id,
        SensorType sensorType,
        Double value,
        LocalDateTime createdAt
) {
}
