package iot.backend.iotmonitoring.service.websocket;

import iot.backend.iotmonitoring.dto.response.LatestSensorDataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SensorDataWebSocketPublisher {

    public static final String SENSOR_DATA_TOPIC = "/topic/sensor-data";

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishSensorData(LatestSensorDataResponse sensorData) {
        messagingTemplate.convertAndSend(SENSOR_DATA_TOPIC, sensorData);
    }
}
