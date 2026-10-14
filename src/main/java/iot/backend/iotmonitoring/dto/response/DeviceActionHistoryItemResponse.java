package iot.backend.iotmonitoring.dto.response;

import iot.backend.iotmonitoring.enums.ActionStatus;
import iot.backend.iotmonitoring.enums.DeviceAction;

import java.time.LocalDateTime;

public record DeviceActionHistoryItemResponse(
        Long id,
        String userName,
        String deviceName,
        DeviceAction action,
        ActionStatus status,
        LocalDateTime createdAt
) {
}
