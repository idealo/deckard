package de.idealo.kafka.deckard.properties;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

public interface GlobalKafkaPropertiesSupplierConfigurer {

    String DEFAULT_BEAN_NAME = "globalKafkaPropertiesSupplierConfigurer";

    GlobalKafkaPropertiesSupplier configureGlobalKafkaPropertiesSupplier(KafkaProperties springManagedKafkaProperties);
}
