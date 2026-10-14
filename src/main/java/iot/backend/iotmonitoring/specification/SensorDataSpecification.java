package iot.backend.iotmonitoring.specification;

import iot.backend.iotmonitoring.dto.request.SensorHistoryFilterRequest;
import iot.backend.iotmonitoring.entity.Sensor;
import iot.backend.iotmonitoring.entity.SensorData;
import iot.backend.iotmonitoring.enums.SensorHistorySearchField;
import iot.backend.iotmonitoring.enums.SensorType;
import iot.backend.iotmonitoring.util.PartialDateTimeRangeParser;
import iot.backend.iotmonitoring.util.PartialDateTimeRangeParser.DateTimeRange;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class SensorDataSpecification {

    private SensorDataSpecification() {
    }

    public static Specification<SensorData> matchesHistoryFilter(
            SensorHistoryFilterRequest filter
    ) {
        String searchBy = trimToNull(filter.getSearchBy());
        String value = trimToNull(filter.getValue());

        if (searchBy == null) {
            if (value != null) {
                throw badRequest("searchBy is required when value is provided");
            }
            return allSensorData();
        }

        SensorHistorySearchField searchField =
                SensorHistorySearchField.fromQueryValue(searchBy);

        if (searchField.isTime()) {
            return value == null ? allSensorData() : createdAtMatches(value);
        }

        SensorType sensorType = searchField.getSensorType();
        if (value == null) {
            return sensorTypeMatches(sensorType);
        }

        return sensorTypeAndValueMatch(sensorType, parseSensorValue(value));
    }

    private static Specification<SensorData> allSensorData() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
    }

    private static Specification<SensorData> sensorTypeMatches(SensorType sensorType) {
        return (root, query, criteriaBuilder) -> {
            Join<SensorData, Sensor> sensor = root.join("sensor");
            return criteriaBuilder.equal(sensor.get("type"), sensorType);
        };
    }

    private static Specification<SensorData> sensorTypeAndValueMatch(
            SensorType sensorType,
            Double value
    ) {
        return (root, query, criteriaBuilder) -> {
            Join<SensorData, Sensor> sensor = root.join("sensor");
            return criteriaBuilder.and(
                    criteriaBuilder.equal(sensor.get("type"), sensorType),
                    criteriaBuilder.equal(root.get("value"), value)
            );
        };
    }

    private static Specification<SensorData> createdAtMatches(String value) {
        DateTimeRange timeRange = PartialDateTimeRangeParser.parse(value);
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        timeRange.startInclusive()
                ),
                criteriaBuilder.lessThan(
                        root.get("createdAt"),
                        timeRange.endExclusive()
                )
        );
    }

    private static Double parseSensorValue(String value) {
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException exception) {
            throw badRequest("value must be a number when searching by sensor type");
        }
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
