package iot.backend.iotmonitoring.dto.response;

import java.util.List;

public record SensorHistoryPageResponse(
        List<SensorHistoryItemResponse> content,
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
