package iot.backend.iotmonitoring.service.device;

import iot.backend.iotmonitoring.dto.mqtt.MqttDeviceStatusMessage;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeviceStatusConfirmationServiceTest {

    private final ActionHistoryRepository actionHistoryRepository =
            mock(ActionHistoryRepository.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final DeviceStatusConfirmationService deviceStatusConfirmationService = new DeviceStatusConfirmationService(
            actionHistoryRepository,
            eventPublisher
    );

    @Test
    void updatesHistoryAndDeviceFromHardwareResponse() {
        Device device = new Device();
        device.setId(1L);
        device.setStatus(DeviceStatus.OFF);
        ActionHistory history = history(10L, device, ActionStatus.LOADING);
        when(actionHistoryRepository.findFirstByDeviceIdAndStatusOrderByIdDesc(
                1L,
                ActionStatus.LOADING
        )).thenReturn(Optional.of(history));

        deviceStatusConfirmationService.confirmDeviceStatus(new MqttDeviceStatusMessage(
                1L,
                DeviceStatus.ON
        ));

        assertEquals(DeviceStatus.ON, device.getStatus());
        assertEquals(ActionStatus.ON, history.getStatus());
        verify(eventPublisher).publishEvent(new DeviceControlStatusChangedEvent(1L, ActionStatus.ON));
    }

    @Test
    void ignoresLateHardwareResponseAfterTimeout() {
        Device device = new Device();
        device.setId(1L);
        device.setStatus(DeviceStatus.OFF);
        when(actionHistoryRepository.findFirstByDeviceIdAndStatusOrderByIdDesc(
                1L,
                ActionStatus.LOADING
        )).thenReturn(Optional.empty());

        deviceStatusConfirmationService.confirmDeviceStatus(new MqttDeviceStatusMessage(
                1L,
                DeviceStatus.ON
        ));

        assertEquals(DeviceStatus.OFF, device.getStatus());
        verify(eventPublisher, never()).publishEvent(
                new DeviceControlStatusChangedEvent(1L, ActionStatus.ON)
        );
    }

    private ActionHistory history(Long id, Device device, ActionStatus status) {
        ActionHistory history = new ActionHistory();
        history.setId(id);
        history.setDevice(device);
        history.setStatus(status);
        return history;
    }
}
