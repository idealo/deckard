package de.idealo.deckard.configuration;

import de.idealo.deckard.proxy.ProxyBeanFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;
import java.util.Optional;

@Slf4j
public class KafkaProducerConfiguration {

    @Bean
    ProxyBeanFactory proxyBeanFactory(KafkaTemplate kafkaTemplate) {
        return new ProxyBeanFactory(kafkaTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    <K, V> KafkaTemplate<K, V> kafkaTemplate(@Autowired(required = false) KafkaProperties kafkaProperties) {
        KafkaProperties properties = Optional.ofNullable(kafkaProperties).orElseGet(() -> {
            log.warn("You didn't specify any Kafka properties in your configuration. Either this is a test scenario," +
                    "or this was not your intention.");
            return new KafkaProperties();
        });

        Map<String, Object> producerProps = properties.buildProducerProperties();
        DefaultKafkaProducerFactory<K, V> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);

        return new KafkaTemplate<>(producerFactory);
    }
}
