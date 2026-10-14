package iot.backend.iotmonitoring.util;

import iot.backend.iotmonitoring.enums.HistorySortField;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class HistorySortFactory {

    private HistorySortFactory() {
    }

    public static Sort createSort(String sortBy, String sortDirection) {
        HistorySortField sortField = HistorySortField.fromQueryValue(sortBy);
        Sort.Direction direction;

        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "sortDirection must be either 'asc' or 'desc'"
            );
        }

        Sort sort = Sort.by(direction, sortField.getEntityProperty());
        if (sortField == HistorySortField.TIME) {
            sort = sort.and(Sort.by(direction, "id"));
        }
        return sort;
    }
}
