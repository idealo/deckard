package de.idealo.kafka.deckard.properties;

import de.idealo.kafka.deckard.stereotype.KafkaProducer;

import java.util.Map;

public interface KafkaProducerPropertiesBuilder {

    String DEFAULT_BEAN_NAME = "kafkaProducerPropertiesBuilder";

    Map<String, Object> buildProducerProperties(KafkaProducer kafkaProducer);
}
