package iot.backend.iotmonitoring.dto.request;

import iot.backend.iotmonitoring.enums.DeviceAction;
import jakarta.validation.constraints.NotNull;

public record DeviceControlRequest(
        @NotNull DeviceAction action
) {
}
