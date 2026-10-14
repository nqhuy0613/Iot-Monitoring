package iot.backend.iotmonitoring.service.device;

import iot.backend.iotmonitoring.dto.request.DeviceActionHistoryFilterRequest;
import iot.backend.iotmonitoring.dto.response.DeviceActionHistoryItemResponse;
import iot.backend.iotmonitoring.dto.response.DeviceActionHistoryPageResponse;
import iot.backend.iotmonitoring.entity.ActionHistory;
import iot.backend.iotmonitoring.repository.ActionHistoryRepository;
import iot.backend.iotmonitoring.specification.ActionHistorySpecification;
import iot.backend.iotmonitoring.util.HistorySortFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceActionHistoryService {

    private final ActionHistoryRepository actionHistoryRepository;

    @Transactional(readOnly = true)
    public DeviceActionHistoryPageResponse getDeviceActionHistory(
            DeviceActionHistoryFilterRequest filter
    ) {
        Sort sort = HistorySortFactory.createSort(
                filter.getSortBy(),
                filter.getSortDirection()
        );
        PageRequest pageRequest = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Page<ActionHistory> actionHistoryPage = actionHistoryRepository.findAll(
                ActionHistorySpecification.matchesHistoryFilter(filter),
                pageRequest
        );

        List<DeviceActionHistoryItemResponse> content = actionHistoryPage.getContent().stream()
                .map(this::toHistoryItemResponse)
                .toList();

        return new DeviceActionHistoryPageResponse(
                content,
                actionHistoryPage.getNumber(),
                actionHistoryPage.getSize(),
                actionHistoryPage.getTotalElements(),
                actionHistoryPage.getTotalPages(),
                actionHistoryPage.isFirst(),
                actionHistoryPage.isLast(),
                actionHistoryPage.getNumberOfElements(),
                actionHistoryPage.isEmpty()
        );
    }

    private DeviceActionHistoryItemResponse toHistoryItemResponse(
            ActionHistory actionHistory
    ) {
        return new DeviceActionHistoryItemResponse(
                actionHistory.getId(),
                actionHistory.getUser().getFullName(),
                actionHistory.getDevice().getName(),
                actionHistory.getAction(),
                actionHistory.getStatus(),
                actionHistory.getCreatedAt()
        );
    }
}
