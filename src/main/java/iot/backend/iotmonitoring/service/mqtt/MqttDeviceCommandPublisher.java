package iot.backend.iotmonitoring.service.mqtt;

import iot.backend.iotmonitoring.dto.mqtt.MqttDeviceCommandMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true", matchIfMissing = true)
public class MqttDeviceCommandPublisher {

    private final MessageChannel mqttDeviceCommandOutputChannel;

    @Value("${mqtt.device-command-topic}")
    private String commandTopic;

    public MqttDeviceCommandPublisher(
            @Qualifier("mqttDeviceCommandOutputChannel") MessageChannel mqttDeviceCommandOutputChannel
    ) {
        this.mqttDeviceCommandOutputChannel = mqttDeviceCommandOutputChannel;
    }

    public void publishDeviceCommand(MqttDeviceCommandMessage command) {
        Message<String> message = MessageBuilder.withPayload(command.toPayload())
                .setHeader(MqttHeaders.TOPIC, commandTopic)
                .build();

        if (!mqttDeviceCommandOutputChannel.send(message)) {
            throw new IllegalStateException("Cannot publish MQTT device command");
        }
    }
}
