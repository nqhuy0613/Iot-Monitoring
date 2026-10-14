package iot.backend.iotmonitoring.service.mqtt;

import iot.backend.iotmonitoring.dto.mqtt.MqttDeviceCommandMessage;
import iot.backend.iotmonitoring.enums.DeviceAction;
import org.junit.jupiter.api.Test;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MqttDeviceCommandPublisherTest {

    @Test
    void publishesCommandAsDeviceIdAndAction() {
        CapturingMessageChannel outputChannel = new CapturingMessageChannel();
        MqttDeviceCommandPublisher publisher = new MqttDeviceCommandPublisher(outputChannel);
        ReflectionTestUtils.setField(publisher, "commandTopic", "iot/devices/command");

        publisher.publishDeviceCommand(new MqttDeviceCommandMessage(1L, DeviceAction.ON));

        assertEquals("1:ON", outputChannel.message.getPayload());
        assertEquals(
                "iot/devices/command",
                outputChannel.message.getHeaders().get(MqttHeaders.TOPIC)
        );
    }

    private static class CapturingMessageChannel implements MessageChannel {

        private Message<?> message;

        @Override
        public boolean send(Message<?> message) {
            this.message = message;
            return true;
        }

        @Override
        public boolean send(Message<?> message, long timeout) {
            return send(message);
        }
    }
}
