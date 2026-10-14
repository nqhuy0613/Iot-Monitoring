package iot.backend.iotmonitoring.service.websocket;

import iot.backend.iotmonitoring.dto.response.DeviceControlStatusUpdateResponse;
import iot.backend.iotmonitoring.event.DeviceControlStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class DeviceControlStatusWebSocketPublisher {

    public static final String DEVICE_STATUS_TOPIC = "/topic/devices/status";

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void broadcastDeviceControlStatus(DeviceControlStatusChangedEvent event) {
        messagingTemplate.convertAndSend(
                DEVICE_STATUS_TOPIC,
                new DeviceControlStatusUpdateResponse(event.deviceId(), event.status())
        );
    }
}
