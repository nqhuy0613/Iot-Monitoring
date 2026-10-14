package iot.backend.iotmonitoring.service.device;

import iot.backend.iotmonitoring.dto.mqtt.MqttDeviceStatusMessage;
import iot.backend.iotmonitoring.entity.ActionHistory;
import iot.backend.iotmonitoring.entity.Device;
import iot.backend.iotmonitoring.enums.ActionStatus;
import iot.backend.iotmonitoring.event.DeviceControlStatusChangedEvent;
import iot.backend.iotmonitoring.repository.ActionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceStatusConfirmationService {

    private final ActionHistoryRepository actionHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void confirmDeviceStatus(MqttDeviceStatusMessage message) {
        ActionHistory pendingAction = actionHistoryRepository
                .findFirstByDeviceIdAndStatusOrderByIdDesc(
                        message.deviceId(),
                        ActionStatus.LOADING
                )
                .orElse(null);

        if (pendingAction == null) {
            return;
        }

        Device device = pendingAction.getDevice();

        ActionStatus actionStatus = ActionStatus.valueOf(message.status().name());
        device.setStatus(message.status());
        pendingAction.setStatus(actionStatus);

        eventPublisher.publishEvent(new DeviceControlStatusChangedEvent(
                device.getId(),
                actionStatus
        ));
    }
}
