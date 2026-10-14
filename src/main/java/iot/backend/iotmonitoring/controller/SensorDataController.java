package iot.backend.iotmonitoring.controller;

import iot.backend.iotmonitoring.dto.request.SensorHistoryFilterRequest;
import iot.backend.iotmonitoring.dto.response.LatestSensorDataResponse;
import iot.backend.iotmonitoring.dto.response.SensorChartResponse;
import iot.backend.iotmonitoring.dto.response.SensorHistoryPageResponse;
import jakarta.validation.Valid;
import iot.backend.iotmonitoring.service.sensor.SensorDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sensor-data")
@RequiredArgsConstructor
@Validated
public class SensorDataController {

    private final SensorDataService sensorDataService;

    @GetMapping("/latest")
    public ResponseEntity<LatestSensorDataResponse> getLatestSensorData() {

        LatestSensorDataResponse latestSensorDataResponse = sensorDataService.getLatestSensorData();

        return ResponseEntity.ok(
                latestSensorDataResponse
        );

    }

    @GetMapping("/chart")
    public ResponseEntity<SensorChartResponse> getChartData() {
        return ResponseEntity.ok(sensorDataService.getChartData());
    }

    @GetMapping("/history")
    public ResponseEntity<SensorHistoryPageResponse> getSensorDataHistory(
            @Valid @ModelAttribute SensorHistoryFilterRequest filter
    ) {
        return ResponseEntity.ok(sensorDataService.getSensorDataHistory(filter));
    }

}
