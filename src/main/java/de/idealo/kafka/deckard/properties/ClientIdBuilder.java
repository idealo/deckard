package de.idealo.kafka.deckard.properties;

import de.idealo.kafka.deckard.stereotype.KafkaProducer;

import java.util.Map;

public interface ClientIdBuilder {

    String DEFAULT_BEAN_NAME = "clientIdBuilder";

    String buildClientId(KafkaProducer kafkaProducer, Map<String, Object> producerProperties);
}
