package de.idealo.deckard.configuration;

import java.util.Map;

import de.idealo.deckard.proxy.ProxyBeanFactory;
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
