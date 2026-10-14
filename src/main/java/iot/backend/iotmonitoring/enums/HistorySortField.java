package iot.backend.iotmonitoring.enums;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public enum HistorySortField {
    ID("id"),
    TIME("createdAt");

    private final String entityProperty;

    HistorySortField(String entityProperty) {
        this.entityProperty = entityProperty;
    }

    public String getEntityProperty() {
        return entityProperty;
    }

    public static HistorySortField fromQueryValue(String value) {
        for (HistorySortField field : values()) {
            if (field.name().equalsIgnoreCase(value)) {
                return field;
            }
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "sortBy must be either 'id' or 'time'"
        );
    }
}
