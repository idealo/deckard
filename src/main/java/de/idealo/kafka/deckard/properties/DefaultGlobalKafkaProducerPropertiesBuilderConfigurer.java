package de.idealo.kafka.deckard.properties;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

import java.util.Optional;

@Slf4j
public class DefaultGlobalKafkaProducerPropertiesBuilderConfigurer implements GlobalKafkaProducerPropertiesBuilderConfigurer{
    @Override
    public GlobalKafkaProducerPropertiesBuilder configureGlobalKafkaProducerPropertiesBuilder(KafkaProperties springManagedKafkaProperties) {
        return () -> Optional.ofNullable(springManagedKafkaProperties).orElseGet(() -> {
            log.warn("You didn't specify any Kafka properties in your configuration. Either this is a test scenario, or this was not your intention.");
            return new KafkaProperties();
        }).buildProducerProperties();
    }
}
