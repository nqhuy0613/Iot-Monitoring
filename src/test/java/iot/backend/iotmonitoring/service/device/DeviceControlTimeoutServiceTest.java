package iot.backend.iotmonitoring.service.device;

import iot.backend.iotmonitoring.entity.ActionHistory;
import iot.backend.iotmonitoring.entity.Device;
import iot.backend.iotmonitoring.enums.ActionStatus;
import iot.backend.iotmonitoring.enums.DeviceStatus;
import iot.backend.iotmonitoring.event.DeviceControlStatusChangedEvent;
import iot.backend.iotmonitoring.repository.ActionHistoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeviceControlTimeoutServiceTest {

    @Test
    void marksLoadingActionAsFailedWithoutChangingDeviceStatus() {
        ActionHistoryRepository repository = mock(ActionHistoryRepository.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
        DeviceControlTimeoutService service =
                new DeviceControlTimeoutService(repository, eventPublisher);
        Device device = new Device();
        device.setId(1L);
        device.setStatus(DeviceStatus.OFF);
        ActionHistory history = new ActionHistory();
        history.setId(10L);
        history.setDevice(device);
        history.setStatus(ActionStatus.LOADING);
        when(repository.findByIdForUpdate(10L)).thenReturn(Optional.of(history));

        service.markActionAsFailedIfPending(10L);

        assertEquals(ActionStatus.FAILED, history.getStatus());
        assertEquals(DeviceStatus.OFF, device.getStatus());
        verify(eventPublisher).publishEvent(new DeviceControlStatusChangedEvent(1L, ActionStatus.FAILED));
    }
}
