package iot.backend.iotmonitoring.dto.request;

import iot.backend.iotmonitoring.enums.ActionStatus;
import iot.backend.iotmonitoring.enums.DeviceAction;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceActionHistoryFilterRequest {

    private Long deviceId;
    private DeviceAction action;
    private ActionStatus status;

    private String time;

    @Min(0)
    private int page = 0;

    @Min(1)
    @Max(100)
    private int size = 10;

    private String sortBy = "time";
    private String sortDirection = "desc";
}
