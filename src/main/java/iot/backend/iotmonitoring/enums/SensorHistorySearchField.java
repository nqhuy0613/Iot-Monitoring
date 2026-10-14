package iot.backend.iotmonitoring.enums;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

public enum SensorHistorySearchField {
    TEMPERATURE(SensorType.TEMPERATURE),
    HUMIDITY(SensorType.HUMIDITY),
    LIGHT(SensorType.LIGHT),
    TIME(null);

    private final SensorType sensorType;

    SensorHistorySearchField(SensorType sensorType) {
        this.sensorType = sensorType;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public boolean isTime() {
        return this == TIME;
    }

    public static SensorHistorySearchField fromQueryValue(String value) {
        try {
            return SensorHistorySearchField.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "searchBy must be one of: temperature, humidity, light, time"
            );
        }
    }
}
