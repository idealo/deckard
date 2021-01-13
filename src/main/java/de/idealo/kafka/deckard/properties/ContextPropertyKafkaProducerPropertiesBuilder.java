package de.idealo.kafka.deckard.properties;

import de.idealo.kafka.deckard.stereotype.KafkaProducer;

import java.util.Map;

public interface ContextPropertyKafkaProducerPropertiesBuilder {

    String DEFAULT_BEAN_NAME = "contextPropertyKafkaProducerPropertiesBuilder";

    Map<String, Object> buildProducerProperties(KafkaProducer kafkaProducer);
}
