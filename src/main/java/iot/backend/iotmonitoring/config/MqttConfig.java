package iot.backend.iotmonitoring.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.integration.annotation.ServiceActivator;

@Configuration
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true", matchIfMissing = true)
public class MqttConfig {

    @Bean
    public MqttPahoClientFactory mqttClientFactory(
            @Value("${mqtt.broker-url}") String brokerUrl,
            @Value("${mqtt.username:}") String username,
            @Value("${mqtt.password:}") String password
    ) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{brokerUrl});
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);

        if (!username.isBlank()) {
            options.setUserName(username);
        }
        if (!password.isBlank()) {
            options.setPassword(password.toCharArray());
        }

        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    public MessageChannel mqttSensorDataInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel mqttDeviceStatusInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel mqttDeviceCommandOutputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer mqttSensorDataInboundAdapter(
            MqttPahoClientFactory mqttClientFactory,
            @Qualifier("mqttSensorDataInputChannel") MessageChannel sensorDataInputChannel,
            @Value("${mqtt.client-id}") String clientId,
            @Value("${mqtt.sensor-topic}") String sensorTopic
    ) {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        clientId,
                        mqttClientFactory,
                        sensorTopic
                );

        adapter.setQos(1);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setOutputChannel(sensorDataInputChannel);
        return adapter;
    }

    @Bean
    public MessageProducer mqttDeviceStatusInboundAdapter(
            MqttPahoClientFactory mqttClientFactory,
            @Qualifier("mqttDeviceStatusInputChannel") MessageChannel deviceStatusInputChannel,
            @Value("${mqtt.client-id}") String clientId,
            @Value("${mqtt.device-status-topic}") String deviceStatusTopic
    ) {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        clientId + "-device-status",
                        mqttClientFactory,
                        deviceStatusTopic
                );

        adapter.setQos(1);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setOutputChannel(deviceStatusInputChannel);
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttDeviceCommandOutputChannel")
    public MessageHandler mqttDeviceCommandOutboundHandler(
            MqttPahoClientFactory mqttClientFactory,
            @Value("${mqtt.client-id}") String clientId
    ) {
        MqttPahoMessageHandler handler = new MqttPahoMessageHandler(
                clientId + "-outbound",
                mqttClientFactory
        );
        handler.setAsync(false);
        handler.setDefaultQos(1);
        return handler;
    }
}
