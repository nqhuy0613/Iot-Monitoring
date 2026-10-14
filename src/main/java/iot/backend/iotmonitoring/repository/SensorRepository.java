package iot.backend.iotmonitoring.repository;

import iot.backend.iotmonitoring.entity.Sensor;
import iot.backend.iotmonitoring.enums.SensorType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, Long> {

    Optional<Sensor> findByType(SensorType type);
}
