package iot.backend.iotmonitoring.service.device;

import iot.backend.iotmonitoring.dto.response.DeviceControlResponse;
import iot.backend.iotmonitoring.dto.response.DeviceStatusResponse;
import iot.backend.iotmonitoring.entity.ActionHistory;
import iot.backend.iotmonitoring.entity.Device;
import iot.backend.iotmonitoring.entity.User;
import iot.backend.iotmonitoring.enums.ActionStatus;
import iot.backend.iotmonitoring.enums.DeviceAction;
import iot.backend.iotmonitoring.enums.DeviceStatus;
import iot.backend.iotmonitoring.event.DeviceControlRequestedEvent;
import iot.backend.iotmonitoring.repository.ActionHistoryRepository;
import iot.backend.iotmonitoring.repository.DeviceRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeviceServiceTest {

    private final DeviceRepository deviceRepository = mock(DeviceRepository.class);
    private final ActionHistoryRepository actionHistoryRepository =
            mock(ActionHistoryRepository.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final DeviceService deviceService = new DeviceService(
            deviceRepository,
            actionHistoryRepository,
            eventPublisher
    );

    @Test
    void returnsCurrentStatusOfAllDevices() {
        when(deviceRepository.findAllByOrderByIdAsc()).thenReturn(List.of(
                device(1L, DeviceStatus.OFF),
                device(2L, DeviceStatus.ON)
        ));

        List<DeviceStatusResponse> response = deviceService.getAllDeviceStatuses();

        assertEquals(new DeviceStatusResponse(1L, DeviceStatus.OFF), response.get(0));
        assertEquals(new DeviceStatusResponse(2L, DeviceStatus.ON), response.get(1));
    }

    @Test
    void createsLoadingHistoryForCurrentUserAndPublishesCommandEvent() {
        Device device = device(1L, DeviceStatus.OFF);
        User user = new User();
        user.setId(7L);
        when(deviceRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(device));
        when(actionHistoryRepository.existsByDeviceIdAndStatus(1L, ActionStatus.LOADING))
                .thenReturn(false);
        when(actionHistoryRepository.saveAndFlush(any(ActionHistory.class)))
                .thenAnswer(invocation -> {
                    ActionHistory history = invocation.getArgument(0);
                    history.setId(99L);
                    return history;
                });

        DeviceControlResponse response =
                deviceService.requestDeviceControl(1L, DeviceAction.ON, user);

        assertEquals(new DeviceControlResponse(1L, DeviceAction.ON, ActionStatus.LOADING), response);

        ArgumentCaptor<ActionHistory> historyCaptor = ArgumentCaptor.forClass(ActionHistory.class);
        verify(actionHistoryRepository).saveAndFlush(historyCaptor.capture());
        assertEquals(user, historyCaptor.getValue().getUser());
        assertEquals(device, historyCaptor.getValue().getDevice());
        assertEquals(DeviceAction.ON, historyCaptor.getValue().getAction());
        assertEquals(ActionStatus.LOADING, historyCaptor.getValue().getStatus());

        verify(eventPublisher).publishEvent(
                new DeviceControlRequestedEvent(99L, 1L, DeviceAction.ON)
        );
    }

    @Test
    void rejectsControlWhenDeviceDoesNotExist() {
        User user = new User();
        when(deviceRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> deviceService.requestDeviceControl(99L, DeviceAction.ON, user)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void rejectsSecondCommandWhileDeviceIsLoading() {
        User user = new User();
        when(deviceRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(device(1L, DeviceStatus.OFF)));
        when(actionHistoryRepository.existsByDeviceIdAndStatus(1L, ActionStatus.LOADING))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> deviceService.requestDeviceControl(1L, DeviceAction.ON, user)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    private Device device(Long id, DeviceStatus status) {
        Device device = new Device();
        device.setId(id);
        device.setStatus(status);
        return device;
    }
}
