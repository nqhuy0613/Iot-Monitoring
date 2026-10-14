package iot.backend.iotmonitoring.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LatestSensorDataResponse {

    private Double temperature;

    private Double humidity;

    private Double light;

    private LocalDateTime createdAt;
}
