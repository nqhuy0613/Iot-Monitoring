package iot.backend.iotmonitoring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class DeviceControlTimeoutConfig {

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService deviceControlTimeoutScheduler() {
        return Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "device-control-timeout");
            thread.setDaemon(true);
            return thread;
        });
    }
}
