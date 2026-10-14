package iot.backend.iotmonitoring.service.device;

import iot.backend.iotmonitoring.dto.mqtt.MqttDeviceCommandMessage;
import iot.backend.iotmonitoring.event.DeviceControlRequestedEvent;
import iot.backend.iotmonitoring.service.mqtt.MqttDeviceCommandPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true", matchIfMissing = true)
public class DeviceControlRequestListener {

    private final MqttDeviceCommandPublisher commandPublisher;
    private final DeviceControlTimeoutService timeoutService;
    private final ScheduledExecutorService deviceControlTimeoutScheduler;

    @Value("${device.control-timeout-seconds:5}")
    private long timeoutSeconds;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishCommandAfterCommit(DeviceControlRequestedEvent event) {
        try {
            commandPublisher.publishDeviceCommand(new MqttDeviceCommandMessage(
                    event.deviceId(),
                    event.action()
            ));

            deviceControlTimeoutScheduler.schedule(
                    () -> timeoutService.markActionAsFailedIfPending(event.actionHistoryId()),
                    timeoutSeconds,
                    TimeUnit.SECONDS
            );
        } catch (RuntimeException exception) {
            timeoutService.markActionAsFailedIfPending(event.actionHistoryId());
            throw exception;
        }
    }
}
