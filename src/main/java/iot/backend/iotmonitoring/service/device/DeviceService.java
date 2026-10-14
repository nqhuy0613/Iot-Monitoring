package iot.backend.iotmonitoring.service.device;

import iot.backend.iotmonitoring.dto.response.DeviceControlResponse;
import iot.backend.iotmonitoring.dto.response.DeviceStatusResponse;
import iot.backend.iotmonitoring.entity.ActionHistory;
import iot.backend.iotmonitoring.entity.Device;
import iot.backend.iotmonitoring.entity.User;
import iot.backend.iotmonitoring.enums.ActionStatus;
import iot.backend.iotmonitoring.enums.DeviceAction;
import iot.backend.iotmonitoring.event.DeviceControlRequestedEvent;
import iot.backend.iotmonitoring.repository.ActionHistoryRepository;
import iot.backend.iotmonitoring.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final ActionHistoryRepository actionHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<DeviceStatusResponse> getAllDeviceStatuses() {
        return deviceRepository.findAllByOrderByIdAsc().stream()
                .map(device -> new DeviceStatusResponse(device.getId(), device.getStatus()))
                .toList();
    }

    @Transactional
    public DeviceControlResponse requestDeviceControl(
            Long deviceId,
            DeviceAction action,
            User currentUser
    ) {
        Device device = deviceRepository.findByIdForUpdate(deviceId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Device not found: " + deviceId
                ));

        if (actionHistoryRepository.existsByDeviceIdAndStatus(deviceId, ActionStatus.LOADING)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Device already has a pending command"
            );
        }

        ActionHistory pendingAction = new ActionHistory();
        pendingAction.setUser(currentUser);
        pendingAction.setDevice(device);
        pendingAction.setAction(action);
        pendingAction.setStatus(ActionStatus.LOADING);
        pendingAction = actionHistoryRepository.saveAndFlush(pendingAction);

        eventPublisher.publishEvent(new DeviceControlRequestedEvent(
                pendingAction.getId(),
                device.getId(),
                action
        ));

        return new DeviceControlResponse(device.getId(), action, ActionStatus.LOADING);
    }
}
