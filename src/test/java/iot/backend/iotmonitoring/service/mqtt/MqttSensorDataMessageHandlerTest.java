package iot.backend.iotmonitoring.service.mqtt;

import iot.backend.iotmonitoring.dto.mqtt.MqttSensorDataMessage;
import iot.backend.iotmonitoring.service.sensor.SensorDataIngestionService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.support.GenericMessage;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class MqttSensorDataMessageHandlerTest {

    private final SensorDataIngestionService ingestionService =
            mock(SensorDataIngestionService.class);
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final MqttSensorDataMessageHandler handler = new MqttSensorDataMessageHandler(
            new ObjectMapper(),
            validator,
            ingestionService
    );

    @Test
    void parsesAndForwardsValidJsonPayload() {
        handler.handleSensorDataMessage(new GenericMessage<>("""
                {"temperature":28.5,"humidity":74.0,"light":320.0}
                """));

        verify(ingestionService).ingestSensorData(
                new MqttSensorDataMessage(
                        28.5,
                        74.0,
                        320.0
                )
        );
    }

    @Test
    void ignoresInvalidJsonPayload() {
        handler.handleSensorDataMessage(new GenericMessage<>("invalid-json"));

        verify(ingestionService, never()).ingestSensorData(any());
    }

    @Test
    void propagatesPersistenceFailureInsteadOfSilentlyLosingMessage() {
        doThrow(new IllegalStateException("Database unavailable"))
                .when(ingestionService)
                .ingestSensorData(any());

        assertThrows(
                IllegalStateException.class,
                () -> handler.handleSensorDataMessage(new GenericMessage<>("""
                        {"temperature":28.5,"humidity":74.0,"light":320.0}
                        """))
        );
    }
}
