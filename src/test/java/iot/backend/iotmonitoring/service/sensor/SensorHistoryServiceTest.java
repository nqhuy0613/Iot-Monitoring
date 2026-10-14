package iot.backend.iotmonitoring.service.sensor;

import iot.backend.iotmonitoring.dto.request.SensorHistoryFilterRequest;
import iot.backend.iotmonitoring.dto.response.SensorHistoryPageResponse;
import iot.backend.iotmonitoring.entity.Sensor;
import iot.backend.iotmonitoring.entity.SensorData;
import iot.backend.iotmonitoring.enums.SensorType;
import iot.backend.iotmonitoring.repository.SensorDataRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SensorHistoryServiceTest {

    private final SensorDataRepository sensorDataRepository = mock(SensorDataRepository.class);
    private final SensorDataService sensorDataService = new SensorDataService(sensorDataRepository);

    @Test
    void returnsPagedHistorySortedByTimeThenIdDescending() {
        SensorHistoryFilterRequest filter = new SensorHistoryFilterRequest();
        filter.setPage(1);
        filter.setSize(2);
        filter.setSortBy("time");
        filter.setSortDirection("desc");
        filter.setSearchBy("temperature");
        filter.setValue("28.5");

        LocalDateTime createdAt = LocalDateTime.of(2026, 10, 12, 9, 30);
        SensorData sensorData = sensorData(11L, SensorType.TEMPERATURE, 28.5, createdAt);
        PageRequest repositoryPageRequest = PageRequest.of(1, 2);

        when(sensorDataRepository.findAll(
                org.mockito.ArgumentMatchers.<Specification<SensorData>>any(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(sensorData), repositoryPageRequest, 5));

        SensorHistoryPageResponse response = sensorDataService.getSensorDataHistory(filter);

        assertEquals(1, response.page());
        assertEquals(2, response.size());
        assertEquals(5, response.totalElements());
        assertEquals(3, response.totalPages());
        assertEquals(1, response.numberOfElements());
        assertEquals(11L, response.content().get(0).id());
        assertEquals(SensorType.TEMPERATURE, response.content().get(0).sensorType());
        assertEquals(28.5, response.content().get(0).value());
        assertEquals(createdAt, response.content().get(0).createdAt());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(sensorDataRepository).findAll(
                org.mockito.ArgumentMatchers.<Specification<SensorData>>any(),
                pageableCaptor.capture()
        );

        Pageable pageable = pageableCaptor.getValue();
        assertEquals(1, pageable.getPageNumber());
        assertEquals(2, pageable.getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("createdAt").getDirection());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("id").getDirection());
    }

    @Test
    void acceptsIdAscendingSort() {
        SensorHistoryFilterRequest filter = new SensorHistoryFilterRequest();
        filter.setSortBy("id");
        filter.setSortDirection("asc");

        when(sensorDataRepository.findAll(
                org.mockito.ArgumentMatchers.<Specification<SensorData>>any(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of()));

        sensorDataService.getSensorDataHistory(filter);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(sensorDataRepository).findAll(
                org.mockito.ArgumentMatchers.<Specification<SensorData>>any(),
                pageableCaptor.capture()
        );
        assertEquals(
                Sort.Direction.ASC,
                pageableCaptor.getValue().getSort().getOrderFor("id").getDirection()
        );
    }

    @Test
    void rejectsUnsupportedSortField() {
        SensorHistoryFilterRequest filter = new SensorHistoryFilterRequest();
        filter.setSortBy("value");

        assertThrows(
                ResponseStatusException.class,
                () -> sensorDataService.getSensorDataHistory(filter)
        );
    }

    @Test
    void rejectsUnsupportedSortDirection() {
        SensorHistoryFilterRequest filter = new SensorHistoryFilterRequest();
        filter.setSortDirection("up");

        assertThrows(
                ResponseStatusException.class,
                () -> sensorDataService.getSensorDataHistory(filter)
        );
    }

    @Test
    void rejectsValueWithoutSearchBy() {
        SensorHistoryFilterRequest filter = new SensorHistoryFilterRequest();
        filter.setValue("28.5");

        assertThrows(
                ResponseStatusException.class,
                () -> sensorDataService.getSensorDataHistory(filter)
        );
    }

    @Test
    void acceptsSensorTypeWithoutValue() {
        SensorHistoryFilterRequest filter = new SensorHistoryFilterRequest();
        filter.setSearchBy("humidity");

        when(sensorDataRepository.findAll(
                org.mockito.ArgumentMatchers.<Specification<SensorData>>any(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of()));

        sensorDataService.getSensorDataHistory(filter);

        verify(sensorDataRepository).findAll(
                org.mockito.ArgumentMatchers.<Specification<SensorData>>any(),
                any(Pageable.class)
        );
    }

    @Test
    void acceptsTimeWithoutValueAndReturnsAllSensorTypes() {
        SensorHistoryFilterRequest filter = new SensorHistoryFilterRequest();
        filter.setSearchBy("time");

        when(sensorDataRepository.findAll(
                org.mockito.ArgumentMatchers.<Specification<SensorData>>any(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of()));

        sensorDataService.getSensorDataHistory(filter);

        verify(sensorDataRepository).findAll(
                org.mockito.ArgumentMatchers.<Specification<SensorData>>any(),
                any(Pageable.class)
        );
    }

    @Test
    void acceptsTimeValueWithMinutePrecision() {
        SensorHistoryFilterRequest filter = new SensorHistoryFilterRequest();
        filter.setSearchBy("time");
        filter.setValue("2026-08-15 11:20");

        when(sensorDataRepository.findAll(
                org.mockito.ArgumentMatchers.<Specification<SensorData>>any(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of()));

        sensorDataService.getSensorDataHistory(filter);

        verify(sensorDataRepository).findAll(
                org.mockito.ArgumentMatchers.<Specification<SensorData>>any(),
                any(Pageable.class)
        );
    }

    @Test
    void rejectsNonNumericValueForSensorType() {
        SensorHistoryFilterRequest filter = new SensorHistoryFilterRequest();
        filter.setSearchBy("light");
        filter.setValue("bright");

        assertThrows(
                ResponseStatusException.class,
                () -> sensorDataService.getSensorDataHistory(filter)
        );
    }

    @Test
    void rejectsUnsupportedSearchField() {
        SensorHistoryFilterRequest filter = new SensorHistoryFilterRequest();
        filter.setSearchBy("pressure");

        assertThrows(
                ResponseStatusException.class,
                () -> sensorDataService.getSensorDataHistory(filter)
        );
    }

    private SensorData sensorData(
            Long id,
            SensorType sensorType,
            Double value,
            LocalDateTime createdAt
    ) {
        Sensor sensor = new Sensor();
        sensor.setType(sensorType);

        SensorData sensorData = new SensorData();
        sensorData.setId(id);
        sensorData.setSensor(sensor);
        sensorData.setValue(value);
        sensorData.setCreatedAt(createdAt);
        return sensorData;
    }
}
