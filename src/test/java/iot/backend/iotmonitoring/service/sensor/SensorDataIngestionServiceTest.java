package iot.backend.iotmonitoring.service.sensor;

import iot.backend.iotmonitoring.dto.mqtt.MqttSensorDataMessage;
import iot.backend.iotmonitoring.dto.response.LatestSensorDataResponse;
import iot.backend.iotmonitoring.entity.Sensor;
import iot.backend.iotmonitoring.entity.SensorData;
import iot.backend.iotmonitoring.enums.SensorType;
import iot.backend.iotmonitoring.repository.SensorDataRepository;
import iot.backend.iotmonitoring.repository.SensorRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SensorDataIngestionServiceTest {

    private final SensorRepository sensorRepository = mock(SensorRepository.class);
    private final SensorDataRepository sensorDataRepository = mock(SensorDataRepository.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final SensorDataIngestionService service = new SensorDataIngestionService(
            sensorRepository,
            sensorDataRepository,
            eventPublisher
    );

    @Test
    void savesAllSensorValuesAndPublishesLatestData() {
        Sensor temperatureSensor = sensor(SensorType.TEMPERATURE);
        Sensor humiditySensor = sensor(SensorType.HUMIDITY);
        Sensor lightSensor = sensor(SensorType.LIGHT);

        when(sensorRepository.findByType(SensorType.TEMPERATURE))
                .thenReturn(Optional.of(temperatureSensor));
        when(sensorRepository.findByType(SensorType.HUMIDITY))
                .thenReturn(Optional.of(humiditySensor));
        when(sensorRepository.findByType(SensorType.LIGHT))
                .thenReturn(Optional.of(lightSensor));

        service.ingestSensorData(new MqttSensorDataMessage(
                28.5,
                74.0,
                320.0
        ));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SensorData>> sensorDataCaptor = ArgumentCaptor.forClass(List.class);
        verify(sensorDataRepository).saveAll(sensorDataCaptor.capture());

        List<SensorData> savedData = sensorDataCaptor.getValue();
        assertEquals(3, savedData.size());
        assertEquals(temperatureSensor, savedData.get(0).getSensor());
        assertEquals(28.5, savedData.get(0).getValue());
        assertEquals(humiditySensor, savedData.get(1).getSensor());
        assertEquals(74.0, savedData.get(1).getValue());
        assertEquals(lightSensor, savedData.get(2).getSensor());
        assertEquals(320.0, savedData.get(2).getValue());
        LocalDateTime backendCreatedAt = savedData.get(0).getCreatedAt();
        assertEquals(backendCreatedAt, savedData.get(1).getCreatedAt());
        assertEquals(backendCreatedAt, savedData.get(2).getCreatedAt());

        ArgumentCaptor<LatestSensorDataResponse> eventCaptor =
                ArgumentCaptor.forClass(LatestSensorDataResponse.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(28.5, eventCaptor.getValue().getTemperature());
        assertEquals(74.0, eventCaptor.getValue().getHumidity());
        assertEquals(320.0, eventCaptor.getValue().getLight());
        assertEquals(backendCreatedAt, eventCaptor.getValue().getCreatedAt());
    }

    private Sensor sensor(SensorType type) {
        Sensor sensor = new Sensor();
        sensor.setType(type);
        return sensor;
    }
}
