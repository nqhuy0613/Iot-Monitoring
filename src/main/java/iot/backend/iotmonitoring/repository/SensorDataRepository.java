package iot.backend.iotmonitoring.repository;

import iot.backend.iotmonitoring.entity.SensorData;
import iot.backend.iotmonitoring.enums.SensorType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SensorDataRepository extends
        JpaRepository<SensorData, Long>,
        JpaSpecificationExecutor<SensorData> {

    @Override
    @EntityGraph(attributePaths = "sensor")
    Page<SensorData> findAll(Specification<SensorData> specification, Pageable pageable);

    @Query("""
            SELECT sd.value
            FROM SensorData sd
            WHERE sd.sensor.type = :sensorType
            ORDER BY sd.createdAt DESC, sd.id DESC
            LIMIT 1
            """)
    Optional<Double> findLatestValueBySensorType(@Param("sensorType") SensorType sensorType);

    @Query("""
            SELECT sd.createdAt
            FROM SensorData sd
            ORDER BY sd.createdAt DESC, sd.id DESC
            LIMIT 1
            """)
    Optional<LocalDateTime> findLatestCreatedAt();

    @Query("""
            SELECT sd
            FROM SensorData sd
            WHERE sd.sensor.type = :sensorType
            ORDER BY sd.createdAt DESC, sd.id DESC
            """)
    List<SensorData> findLatestBySensorType(
            @Param("sensorType") SensorType sensorType,
            Pageable pageable
    );
}

