package de.idealo.kafka.deckard.properties;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

public interface GlobalKafkaProducerPropertiesBuilderConfigurer {

    String DEFAULT_BEAN_NAME = "globalKafkaPropertiesSupplierConfigurer";

    GlobalKafkaProducerPropertiesBuilder configureGlobalKafkaProducerPropertiesBuilder(KafkaProperties springManagedKafkaProperties);
}
