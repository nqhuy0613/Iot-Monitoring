package iot.backend.iotmonitoring.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SensorHistoryFilterRequest {

    private String searchBy;
    private String value;

    @Min(0)
    private int page = 0;

    @Min(1)
    @Max(100)
    private int size = 10;

    private String sortBy = "time";
    private String sortDirection = "desc";
}
