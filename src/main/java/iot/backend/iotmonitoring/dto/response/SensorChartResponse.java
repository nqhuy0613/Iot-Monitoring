package iot.backend.iotmonitoring.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SensorChartResponse {

    private List<SensorChartPointResponse> temperature;

    private List<SensorChartPointResponse> humidity;

    private List<SensorChartPointResponse> light;
}
