package iot.backend.iotmonitoring.specification;

import iot.backend.iotmonitoring.dto.request.DeviceActionHistoryFilterRequest;
import iot.backend.iotmonitoring.entity.ActionHistory;
import iot.backend.iotmonitoring.util.PartialDateTimeRangeParser;
import iot.backend.iotmonitoring.util.PartialDateTimeRangeParser.DateTimeRange;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class ActionHistorySpecification {

    private ActionHistorySpecification() {
    }

    public static Specification<ActionHistory> matchesHistoryFilter(
            DeviceActionHistoryFilterRequest filter
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> filters = new ArrayList<>();

            if (filter.getDeviceId() != null) {
                filters.add(criteriaBuilder.equal(
                        root.get("device").get("id"),
                        filter.getDeviceId()
                ));
            }

            if (filter.getAction() != null) {
                filters.add(criteriaBuilder.equal(root.get("action"), filter.getAction()));
            }

            if (filter.getStatus() != null) {
                filters.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getTime() != null && !filter.getTime().isBlank()) {
                DateTimeRange timeRange = PartialDateTimeRangeParser.parse(filter.getTime());
                filters.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        timeRange.startInclusive()
                ));
                filters.add(criteriaBuilder.lessThan(
                        root.get("createdAt"),
                        timeRange.endExclusive()
                ));
            }

            return criteriaBuilder.and(filters.toArray(Predicate[]::new));
        };
    }
}
