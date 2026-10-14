package iot.backend.iotmonitoring.service.mqtt;

import iot.backend.iotmonitoring.dto.mqtt.MqttSensorDataMessage;
import iot.backend.iotmonitoring.service.sensor.SensorDataIngestionService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true", matchIfMissing = true)
public class MqttSensorDataMessageHandler {

    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final SensorDataIngestionService ingestionService;

    @ServiceActivator(inputChannel = "mqttSensorDataInputChannel")
    public void handleSensorDataMessage(Message<?> mqttMessage) {
        String payload = convertPayloadToText(mqttMessage.getPayload());
        MqttSensorDataMessage sensorDataMessage;

        try {
            sensorDataMessage = objectMapper.readValue(
                    payload,
                    MqttSensorDataMessage.class
            );
            validateSensorDataMessage(sensorDataMessage);
        } catch (JacksonException | ConstraintViolationException exception) {
            log.warn("Ignoring invalid MQTT sensor payload: {}", payload, exception);
            return;
        }

        ingestionService.ingestSensorData(sensorDataMessage);
    }

    private void validateSensorDataMessage(MqttSensorDataMessage sensorDataMessage) {
        Set<ConstraintViolation<MqttSensorDataMessage>> violations = validator.validate(sensorDataMessage);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private String convertPayloadToText(Object payload) {
        if (payload instanceof byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        return String.valueOf(payload);
    }
}
