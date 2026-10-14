package iot.backend.iotmonitoring.service.sensor;

import iot.backend.iotmonitoring.dto.mqtt.MqttSensorDataMessage;
import iot.backend.iotmonitoring.dto.response.LatestSensorDataResponse;
import iot.backend.iotmonitoring.entity.Sensor;
import iot.backend.iotmonitoring.entity.SensorData;
import iot.backend.iotmonitoring.enums.SensorType;
import iot.backend.iotmonitoring.exception.ApplicationException;
import iot.backend.iotmonitoring.repository.SensorDataRepository;
import iot.backend.iotmonitoring.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SensorDataIngestionService {

    private final SensorRepository sensorRepository;
    private final SensorDataRepository sensorDataRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void ingestSensorData(MqttSensorDataMessage message) {
        LocalDateTime createdAt = LocalDateTime.now().withNano(0);
        Sensor temperatureSensor = findSensorByType(SensorType.TEMPERATURE);
        Sensor humiditySensor = findSensorByType(SensorType.HUMIDITY);
        Sensor lightSensor = findSensorByType(SensorType.LIGHT);

        sensorDataRepository.saveAll(List.of(
                createSensorDataEntity(temperatureSensor, message.temperature(), createdAt),
                createSensorDataEntity(humiditySensor, message.humidity(), createdAt),
                createSensorDataEntity(lightSensor, message.light(), createdAt)
        ));

        eventPublisher.publishEvent(new LatestSensorDataResponse(
                message.temperature(),
                message.humidity(),
                message.light(),
                createdAt
        ));
    }

    private Sensor findSensorByType(SensorType type) {
        return sensorRepository.findByType(type)
                .orElseThrow(() -> new ApplicationException("Sensor not found: " + type));
    }

    private SensorData createSensorDataEntity(
            Sensor sensor,
            Double value,
            LocalDateTime createdAt
    ) {
        SensorData sensorData = new SensorData();
        sensorData.setSensor(sensor);
        sensorData.setValue(value);
        sensorData.setCreatedAt(createdAt);
        return sensorData;
    }
}
