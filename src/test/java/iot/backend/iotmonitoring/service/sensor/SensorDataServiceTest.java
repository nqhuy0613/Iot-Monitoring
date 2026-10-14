package iot.backend.iotmonitoring.service.sensor;

import iot.backend.iotmonitoring.dto.response.LatestSensorDataResponse;
import iot.backend.iotmonitoring.enums.SensorType;
import iot.backend.iotmonitoring.exception.ApplicationException;
import iot.backend.iotmonitoring.repository.SensorDataRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SensorDataServiceTest {

    private final SensorDataRepository sensorDataRepository = mock(SensorDataRepository.class);
    private final SensorDataService sensorDataService = new SensorDataService(sensorDataRepository);

    @Test
    void getLatestSensorDataReturnsLatestValueOfEverySensorType() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 10, 11, 8, 30);
        when(sensorDataRepository.findLatestValueBySensorType(SensorType.TEMPERATURE))
                .thenReturn(Optional.of(28.5));
        when(sensorDataRepository.findLatestValueBySensorType(SensorType.HUMIDITY))
                .thenReturn(Optional.of(74.0));
        when(sensorDataRepository.findLatestValueBySensorType(SensorType.LIGHT))
                .thenReturn(Optional.of(320.0));
        when(sensorDataRepository.findLatestCreatedAt())
                .thenReturn(Optional.of(createdAt));

        LatestSensorDataResponse response = sensorDataService.getLatestSensorData();

        assertEquals(28.5, response.getTemperature());
        assertEquals(74.0, response.getHumidity());
        assertEquals(320.0, response.getLight());
        assertEquals(createdAt, response.getCreatedAt());
        verify(sensorDataRepository).findLatestValueBySensorType(SensorType.TEMPERATURE);
        verify(sensorDataRepository).findLatestValueBySensorType(SensorType.HUMIDITY);
        verify(sensorDataRepository).findLatestValueBySensorType(SensorType.LIGHT);
    }

    @Test
    void getLatestSensorDataThrowsWhenOneSensorTypeHasNoData() {
        when(sensorDataRepository.findLatestValueBySensorType(SensorType.TEMPERATURE))
                .thenReturn(Optional.of(28.5));
        when(sensorDataRepository.findLatestValueBySensorType(SensorType.HUMIDITY))
                .thenReturn(Optional.empty());
        when(sensorDataRepository.findLatestValueBySensorType(SensorType.LIGHT))
                .thenReturn(Optional.empty());

        assertThrows(
                ApplicationException.class,
                sensorDataService::getLatestSensorData
        );
    }
}
