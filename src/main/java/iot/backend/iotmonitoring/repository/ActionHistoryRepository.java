package iot.backend.iotmonitoring.repository;

import iot.backend.iotmonitoring.entity.ActionHistory;
import iot.backend.iotmonitoring.enums.ActionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ActionHistoryRepository extends
        JpaRepository<ActionHistory, Long>,
        JpaSpecificationExecutor<ActionHistory> {

    @Override
    @EntityGraph(attributePaths = {"user", "device"})
    Page<ActionHistory> findAll(
            Specification<ActionHistory> specification,
            Pageable pageable
    );

    boolean existsByDeviceIdAndStatus(Long deviceId, ActionStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "device")
    Optional<ActionHistory> findFirstByDeviceIdAndStatusOrderByIdDesc(
            Long deviceId,
            ActionStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT ah
            FROM ActionHistory ah
            JOIN FETCH ah.device
            WHERE ah.id = :id
            """)
    Optional<ActionHistory> findByIdForUpdate(@Param("id") Long id);
}
