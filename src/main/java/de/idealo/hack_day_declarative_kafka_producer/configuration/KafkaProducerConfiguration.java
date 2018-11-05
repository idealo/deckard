package de.idealo.hack_day_declarative_kafka_producer.configuration;

import de.idealo.hack_day_declarative_kafka_producer.proxy.ProxyBeanFactory;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaProducerConfiguration {

    @Bean
    ProxyBeanFactory proxyBeanFactory(KafkaTemplate kafkaTemplate) {
        return new ProxyBeanFactory(kafkaTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    <K, V> KafkaTemplate<K, V> kafkaTemplate(KafkaProperties kafkaProperties) {
        Map<String, Object> producerProps = kafkaProperties.buildProducerProperties();
        DefaultKafkaProducerFactory<K, V> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
        return new KafkaTemplate<>(producerFactory);
    }
}
