package iot.backend.iotmonitoring.service.mqtt;

import iot.backend.iotmonitoring.dto.mqtt.MqttDeviceStatusMessage;
import iot.backend.iotmonitoring.service.device.DeviceStatusConfirmationService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true", matchIfMissing = true)
public class MqttDeviceStatusMessageHandler {

    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final DeviceStatusConfirmationService deviceStatusConfirmationService;

    @ServiceActivator(inputChannel = "mqttDeviceStatusInputChannel")
    public void handleDeviceStatusMessage(Message<?> mqttMessage) {
        String payload = convertPayloadToText(mqttMessage.getPayload());
        MqttDeviceStatusMessage statusMessage;

        try {
            statusMessage = objectMapper.readValue(payload, MqttDeviceStatusMessage.class);
            validateDeviceStatusMessage(statusMessage);
        } catch (JacksonException | ConstraintViolationException exception) {
            log.warn("Ignoring invalid MQTT device status payload: {}", payload, exception);
            return;
        }

        deviceStatusConfirmationService.confirmDeviceStatus(statusMessage);
    }

    private void validateDeviceStatusMessage(MqttDeviceStatusMessage statusMessage) {
        Set<ConstraintViolation<MqttDeviceStatusMessage>> violations =
                validator.validate(statusMessage);
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
