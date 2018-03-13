package com.kloia.eventapis.api.emon.configuration;

import com.kloia.eventapis.kafka.JsonDeserializer;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.spring.configuration.EventApisConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.Properties;
import java.util.regex.Pattern;

@Configuration
@Import(EventApisConfiguration.class)
@Slf4j
public class TopicServiceConfiguration {

    @Autowired
    private EventApisConfiguration eventApisConfiguration;


    @Bean("eventTopicRegex")
    public Pattern eventTopicRegex(@Value(value = "${eventapis.eventBus.eventTopicRegex:.*Event}")
                                           String eventTopicRegexStr) {
        return Pattern.compile(eventTopicRegexStr);
    }


    @Bean(name = "kafkaAdminProperties")
    public Properties kafkaAdminProperties() {
        String bootstrapServers = String.join(",", eventApisConfiguration.getEventBus().getBootstrapServers());
        String zookeeperServers = String.join(",", eventApisConfiguration.getEventBus().getZookeeperServers());
        Properties properties = new Properties();
        properties.put("zookeeper", zookeeperServers);
        properties.put("bootstrap.servers", bootstrapServers);
        return properties;
    }

    @Bean("adminClient")
    public AdminClient adminClient(@Autowired @Qualifier("kafkaAdminProperties") Properties kafkaAdminProperties) {
        return AdminClient.create(kafkaAdminProperties);
    }

    @Bean("adminToolsClient")
    public kafka.admin.AdminClient adminToolsClient(@Autowired @Qualifier("kafkaAdminProperties") Properties kafkaAdminProperties) {
        return kafka.admin.AdminClient.create(kafkaAdminProperties);
    }

    @Bean
    public Consumer<String, Operation> kafkaConsumer() {
        return new DefaultKafkaConsumerFactory<>(
                eventApisConfiguration.getEventBus().buildConsumerProperties(),
                new StringDeserializer(),
                new JsonDeserializer<>(Operation.class)).createConsumer();
    }


}
