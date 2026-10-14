package iot.backend.iotmonitoring.dto.response;

import java.util.List;

public record DeviceActionHistoryPageResponse(
        List<DeviceActionHistoryItemResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        int numberOfElements,
        boolean empty
) {
}
