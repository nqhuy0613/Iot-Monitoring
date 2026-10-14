package iot.backend.iotmonitoring.service.device;

import iot.backend.iotmonitoring.entity.ActionHistory;
import iot.backend.iotmonitoring.enums.ActionStatus;
import iot.backend.iotmonitoring.event.DeviceControlStatusChangedEvent;
import iot.backend.iotmonitoring.repository.ActionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceControlTimeoutService {

    private final ActionHistoryRepository actionHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void markActionAsFailedIfPending(Long actionHistoryId) {
        ActionHistory pendingAction = actionHistoryRepository.findByIdForUpdate(actionHistoryId)
                .orElse(null);

        if (pendingAction == null || pendingAction.getStatus() != ActionStatus.LOADING) {
            return;
        }

        pendingAction.setStatus(ActionStatus.FAILED);
        eventPublisher.publishEvent(new DeviceControlStatusChangedEvent(
                pendingAction.getDevice().getId(),
                ActionStatus.FAILED
        ));
    }
}
