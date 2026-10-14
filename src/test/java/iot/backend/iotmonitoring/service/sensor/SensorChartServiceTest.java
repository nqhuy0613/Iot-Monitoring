package iot.backend.iotmonitoring.service.sensor;

import iot.backend.iotmonitoring.dto.response.SensorChartPointResponse;
import iot.backend.iotmonitoring.dto.response.SensorChartResponse;
import iot.backend.iotmonitoring.entity.SensorData;
import iot.backend.iotmonitoring.enums.SensorType;
import iot.backend.iotmonitoring.repository.SensorDataRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SensorChartServiceTest {

    private final SensorDataRepository sensorDataRepository = mock(SensorDataRepository.class);
    private final SensorDataService sensorDataService = new SensorDataService(sensorDataRepository);

    @Test
    void returnsLatestFifteenValuesOfEachTypeInChronologicalOrder() {
        LocalDateTime olderTime = LocalDateTime.of(2026, 10, 11, 8, 30);
        LocalDateTime newerTime = LocalDateTime.of(2026, 10, 11, 8, 31);

        when(sensorDataRepository.findLatestBySensorType(
                SensorType.TEMPERATURE,
                PageRequest.of(0, 15)
        )).thenReturn(List.of(
                sensorData(29.0, newerTime),
                sensorData(28.5, olderTime)
        ));
        when(sensorDataRepository.findLatestBySensorType(
                SensorType.HUMIDITY,
                PageRequest.of(0, 15)
        )).thenReturn(List.of(sensorData(74.0, newerTime)));
        when(sensorDataRepository.findLatestBySensorType(
                SensorType.LIGHT,
                PageRequest.of(0, 15)
        )).thenReturn(List.of(sensorData(320.0, newerTime)));

        SensorChartResponse response = sensorDataService.getChartData();

        assertEquals(2, response.getTemperature().size());
        assertPoint(response.getTemperature().get(0), olderTime, 28.5);
        assertPoint(response.getTemperature().get(1), newerTime, 29.0);
        assertPoint(response.getHumidity().get(0), newerTime, 74.0);
        assertPoint(response.getLight().get(0), newerTime, 320.0);

        verify(sensorDataRepository).findLatestBySensorType(
                SensorType.TEMPERATURE,
                PageRequest.of(0, 15)
        );
        verify(sensorDataRepository).findLatestBySensorType(
                SensorType.HUMIDITY,
                PageRequest.of(0, 15)
        );
        verify(sensorDataRepository).findLatestBySensorType(
                SensorType.LIGHT,
                PageRequest.of(0, 15)
        );
    }

    private SensorData sensorData(Double value, LocalDateTime createdAt) {
        SensorData sensorData = new SensorData();
        sensorData.setValue(value);
        sensorData.setCreatedAt(createdAt);
        return sensorData;
    }

    private void assertPoint(
            SensorChartPointResponse point,
            LocalDateTime expectedTime,
            Double expectedValue
    ) {
        assertEquals(expectedTime, point.getTime());
        assertEquals(expectedValue, point.getValue());
    }
}
