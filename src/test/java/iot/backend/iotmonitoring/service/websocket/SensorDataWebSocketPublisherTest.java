package iot.backend.iotmonitoring.service.websocket;

import iot.backend.iotmonitoring.dto.response.LatestSensorDataResponse;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SensorDataWebSocketPublisherTest {

    @Test
    void publishesLatestDataToSensorTopic() {
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        SensorDataWebSocketPublisher publisher =
                new SensorDataWebSocketPublisher(messagingTemplate);
        LocalDateTime createdAt = LocalDateTime.of(2026, 10, 11, 8, 30);
        LatestSensorDataResponse response =
                new LatestSensorDataResponse(28.5, 74.0, 320.0, createdAt);

        publisher.publishSensorData(response);

        verify(messagingTemplate).convertAndSend(
                SensorDataWebSocketPublisher.SENSOR_DATA_TOPIC,
                response
        );
    }
}
