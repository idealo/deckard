package de.idealo.kafka.deckard.properties;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

public interface KafkaProducerPropertiesBuilderConfigurer {

    String DEFAULT_BEAN_NAME = "kafkaProducerPropertiesBuilderConfigurer";

    KafkaProducerPropertiesBuilder configureProducerPropertiesBuilder(KafkaProperties globalKafkaProperties, DeckardKafkaPropertiesSupplier deckardKafkaPropertiesSupplier);
}
