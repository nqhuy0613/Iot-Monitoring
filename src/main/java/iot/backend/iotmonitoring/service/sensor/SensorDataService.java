package iot.backend.iotmonitoring.service.sensor;

import iot.backend.iotmonitoring.dto.request.SensorHistoryFilterRequest;
import iot.backend.iotmonitoring.dto.response.LatestSensorDataResponse;
import iot.backend.iotmonitoring.dto.response.SensorChartPointResponse;
import iot.backend.iotmonitoring.dto.response.SensorChartResponse;
import iot.backend.iotmonitoring.dto.response.SensorHistoryItemResponse;
import iot.backend.iotmonitoring.dto.response.SensorHistoryPageResponse;
import iot.backend.iotmonitoring.entity.SensorData;
import iot.backend.iotmonitoring.enums.SensorType;
import iot.backend.iotmonitoring.exception.ApplicationException;
import iot.backend.iotmonitoring.repository.SensorDataRepository;
import iot.backend.iotmonitoring.specification.SensorDataSpecification;
import iot.backend.iotmonitoring.util.HistorySortFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SensorDataService {

    private static final int CHART_POINT_LIMIT = 15;

    private final SensorDataRepository sensorDataRepository;

    @Transactional(readOnly = true)
    public LatestSensorDataResponse getLatestSensorData() {
        Double latestTemperature = getLatestValue(SensorType.TEMPERATURE);
        Double latestHumidity = getLatestValue(SensorType.HUMIDITY);
        Double latestLight = getLatestValue(SensorType.LIGHT);
        LocalDateTime latestCreatedAt = sensorDataRepository.findLatestCreatedAt()
                .orElseThrow(() -> new ApplicationException("Sensor data not found"));

        return new LatestSensorDataResponse(
                latestTemperature,
                latestHumidity,
                latestLight,
                latestCreatedAt
        );
    }

    @Transactional(readOnly = true)
    public SensorChartResponse getChartData() {
        return new SensorChartResponse(
                getChartPoints(SensorType.TEMPERATURE),
                getChartPoints(SensorType.HUMIDITY),
                getChartPoints(SensorType.LIGHT)
        );
    }

    @Transactional(readOnly = true)
    public SensorHistoryPageResponse getSensorDataHistory(SensorHistoryFilterRequest filter) {
        Sort sort = HistorySortFactory.createSort(
                filter.getSortBy(),
                filter.getSortDirection()
        );
        PageRequest pageRequest = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Page<SensorData> sensorDataPage = sensorDataRepository.findAll(
                SensorDataSpecification.matchesHistoryFilter(filter),
                pageRequest
        );

        List<SensorHistoryItemResponse> content = sensorDataPage.getContent().stream()
                .map(this::toSensorHistoryItemResponse)
                .toList();

        return new SensorHistoryPageResponse(
                content,
                sensorDataPage.getNumber(),
                sensorDataPage.getSize(),
                sensorDataPage.getTotalElements(),
                sensorDataPage.getTotalPages(),
                sensorDataPage.isFirst(),
                sensorDataPage.isLast(),
                sensorDataPage.getNumberOfElements(),
                sensorDataPage.isEmpty()
        );
    }

    private SensorHistoryItemResponse toSensorHistoryItemResponse(SensorData sensorData) {
        return new SensorHistoryItemResponse(
                sensorData.getId(),
                sensorData.getSensor().getType(),
                sensorData.getValue(),
                sensorData.getCreatedAt()
        );
    }

    private List<SensorChartPointResponse> getChartPoints(SensorType sensorType) {
        List<SensorData> latestData = new ArrayList<>(
                sensorDataRepository.findLatestBySensorType(
                        sensorType,
                        PageRequest.of(0, CHART_POINT_LIMIT)
                )
        );

        Collections.reverse(latestData);

        return latestData.stream()
                .map(sensorData -> new SensorChartPointResponse(
                        sensorData.getCreatedAt(),
                        sensorData.getValue()
                ))
                .toList();
    }

    private Double getLatestValue(SensorType sensorType) {
        return sensorDataRepository.findLatestValueBySensorType(sensorType)
                .orElseThrow(()-> new ApplicationException("Sensor type " + sensorType + " not found") );
    }
}
