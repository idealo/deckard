package de.idealo.kafka.deckard.properties;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

import java.util.function.Supplier;

public interface GlobalKafkaPropertiesSupplier extends Supplier<KafkaProperties> {

    String DEFAULT_BEAN_NAME = "globalKafkaPropertiesSupplier";
}
