package iot.backend.iotmonitoring;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "mqtt.enabled=false")
class IotMonitoringApplicationTests {

    @Test
    void contextLoads() {
    }

}
