package iot.backend.iotmonitoring.service.device;

import iot.backend.iotmonitoring.dto.request.DeviceActionHistoryFilterRequest;
import iot.backend.iotmonitoring.dto.response.DeviceActionHistoryPageResponse;
import iot.backend.iotmonitoring.entity.ActionHistory;
import iot.backend.iotmonitoring.entity.Device;
import iot.backend.iotmonitoring.entity.User;
import iot.backend.iotmonitoring.enums.ActionStatus;
import iot.backend.iotmonitoring.enums.DeviceAction;
import iot.backend.iotmonitoring.repository.ActionHistoryRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeviceActionHistoryServiceTest {

    private final ActionHistoryRepository actionHistoryRepository =
            mock(ActionHistoryRepository.class);
    private final DeviceActionHistoryService deviceActionHistoryService =
            new DeviceActionHistoryService(actionHistoryRepository);

    @Test
    void returnsFilteredDeviceActionHistoryPage() {
        DeviceActionHistoryFilterRequest filter = new DeviceActionHistoryFilterRequest();
        filter.setDeviceId(2L);
        filter.setAction(DeviceAction.ON);
        filter.setStatus(ActionStatus.LOADING);
        filter.setPage(1);
        filter.setSize(2);
        filter.setSortBy("time");
        filter.setSortDirection("desc");

        LocalDateTime actionTime = LocalDateTime.of(2026, 10, 12, 10, 5);
        ActionHistory actionHistory = actionHistory(actionTime);
        PageRequest repositoryPageRequest = PageRequest.of(1, 2);

        when(actionHistoryRepository.findAll(
                org.mockito.ArgumentMatchers.<Specification<ActionHistory>>any(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(
                List.of(actionHistory),
                repositoryPageRequest,
                5
        ));

        DeviceActionHistoryPageResponse response =
                deviceActionHistoryService.getDeviceActionHistory(filter);

        assertEquals(1, response.page());
        assertEquals(2, response.size());
        assertEquals(5, response.totalElements());
        assertEquals(3, response.totalPages());
        assertEquals(1, response.numberOfElements());
        assertEquals(25L, response.content().get(0).id());
        assertEquals("Nguyen An", response.content().get(0).userName());
        assertEquals("Light 2", response.content().get(0).deviceName());
        assertEquals(DeviceAction.ON, response.content().get(0).action());
        assertEquals(ActionStatus.LOADING, response.content().get(0).status());
        assertEquals(actionTime, response.content().get(0).createdAt());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(actionHistoryRepository).findAll(
                org.mockito.ArgumentMatchers.<Specification<ActionHistory>>any(),
                pageableCaptor.capture()
        );

        Pageable pageable = pageableCaptor.getValue();
        assertEquals(1, pageable.getPageNumber());
        assertEquals(2, pageable.getPageSize());
        assertEquals(
                Sort.Direction.DESC,
                pageable.getSort().getOrderFor("createdAt").getDirection()
        );
        assertEquals(
                Sort.Direction.DESC,
                pageable.getSort().getOrderFor("id").getDirection()
        );
    }

    @Test
    void supportsIdAscendingSort() {
        DeviceActionHistoryFilterRequest filter = new DeviceActionHistoryFilterRequest();
        filter.setSortBy("id");
        filter.setSortDirection("asc");

        when(actionHistoryRepository.findAll(
                org.mockito.ArgumentMatchers.<Specification<ActionHistory>>any(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of()));

        deviceActionHistoryService.getDeviceActionHistory(filter);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(actionHistoryRepository).findAll(
                org.mockito.ArgumentMatchers.<Specification<ActionHistory>>any(),
                pageableCaptor.capture()
        );
        assertEquals(
                Sort.Direction.ASC,
                pageableCaptor.getValue().getSort().getOrderFor("id").getDirection()
        );
    }

    private ActionHistory actionHistory(LocalDateTime actionTime) {
        User user = new User();
        user.setFullName("Nguyen An");

        Device device = new Device();
        device.setName("Light 2");

        ActionHistory actionHistory = new ActionHistory();
        actionHistory.setId(25L);
        actionHistory.setUser(user);
        actionHistory.setDevice(device);
        actionHistory.setAction(DeviceAction.ON);
        actionHistory.setStatus(ActionStatus.LOADING);
        actionHistory.setCreatedAt(actionTime);
        return actionHistory;
    }
}
